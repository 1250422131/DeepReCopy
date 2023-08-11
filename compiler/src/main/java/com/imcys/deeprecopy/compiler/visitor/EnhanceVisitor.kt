package com.imcys.deeprecopy.compiler.visitor

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.symbol.Nullability

class EnhanceVisitor(
    private val environment: SymbolProcessorEnvironment,
    private val deepCopySymbols: Sequence<KSAnnotated>,
) : KSVisitorVoid() {

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        super.visitClassDeclaration(classDeclaration, data)

        val primaryConstructor = classDeclaration.primaryConstructor!!
        val params = primaryConstructor.parameters
        // 获取类名和包名
        val className = classDeclaration.simpleName.asString()
        val packageName = classDeclaration.packageName.asString()

        val file = environment.codeGenerator.createNewFile(
            Dependencies(true, classDeclaration.containingFile!!),
            packageName,
            "${className}Enhance",
        )

        // 生成扩展函数的代码
        val extensionFunctionCode = generateCode(packageName, className, params)

        file.write(extensionFunctionCode.toByteArray())
        file.close()
    }

    private fun generateCode(
        packageName: String,
        className: String,
        params: List<KSValueParameter>,
    ): String {
        val complexClassName = "_${className}CopyFun"
        val extensionFunctionCode = buildString {
            appendLine("package $packageName\n\n")

            appendLine("data class $complexClassName(")
            appendParams(params)
            appendLine(")\n\n")

            appendLine("fun $className.deepCopy(")
            appendParamsWithDefaultValues(params)
            appendLine("): $className {")
            appendLine("    return $className(${getReturn(params)})")
            appendLine("}\n\n")

            appendLine("fun $className.deepCopy(")
            appendLine("    copyFunction:$complexClassName.()->Unit): $className{")
            appendLine("    val copyData = $complexClassName(${getReturn(params)})")
            appendLine("    copyData.copyFunction()")
            appendLine("    return this.deepCopy(${getReturn(params, "copyData.")})")
            appendLine("}")
        }
        return extensionFunctionCode
    }

    private fun StringBuilder.appendParams(params: List<KSValueParameter>) {
        params.forEach {
            val paramName = it.name?.getShortName() ?: "Erro"
            val typeName = generateParamsType(it.type)
            appendLine(
                "    var $paramName : $typeName,",
            )
        }
    }

    private fun StringBuilder.appendParamsWithDefaultValues(params: List<KSValueParameter>) {
        params.forEach {
            val paramName = it.name?.getShortName() ?: "Erro"
            val typeName = generateParamsType(it.type)
            appendLine(
                "    $paramName : $typeName = ${getDefaultValue(paramName, typeName)},",
            )
            // 将参数的名称和类型名称追加到字符串构建器中
        }
    }

    private fun getDefaultValue(
        paramName: String,
        typeName: String,
    ): String {
        return if (typeName.startsWith("kotlin.")) {
            // 类型为标准类型
            if (typeName in "?") "this?.$paramName" else "this.$paramName"
        } else {
            val newParamCode = getNewParamCode(paramName, typeName)
            if (newParamCode.isNullOrBlank()) {
                if (typeName in "?") "this?.$paramName" else "this.$paramName"
            } else {
                val tmpTypeName = typeName.replace("?", "")
                    .replace("<", "")
                    .replace(">", "")

                "$tmpTypeName($newParamCode)" // new xxx()
            }
        }
    }

    /**
     * 组装一个参数的new XXXX()
     */
    private fun getNewParamCode(fParamName: String, typeName: String): String {
        deepCopySymbols.forEach {
            val classDeclaration = it as? KSClassDeclaration
            val classQualifiedName = classDeclaration?.qualifiedName?.asString() ?: ""

            if (classQualifiedName == typeName) {
                val params = classDeclaration?.primaryConstructor?.parameters

                val extensionFunctionCode = buildString {
                    params?.forEachIndexed { index, ksValueParameter ->
                        val paramName = ksValueParameter.name?.getShortName() ?: "Erro"
                        val typeName = generateParamsType(ksValueParameter.type)
                        val mParamName = "$fParamName.$paramName"

                        if (index != params.size - 1) {
                            append(
                                "$paramName = ${getDefaultValue(mParamName, typeName)}, ",
                            )
                        } else {
                            append(
                                "$paramName = ${getDefaultValue(mParamName, typeName)}",
                            )
                        }
                    } ?: apply {
                        return ""
                    }
                }

                return "$extensionFunctionCode"
            }
        }

        return ""
    }

    private fun generateParamsType(type: KSTypeReference): String {
        val typeName = StringBuilder(
            type.resolve().declaration.qualifiedName?.asString() ?: "<ERROR>",
            // 获取参数的类型名称，如果获取失败，则使用 "<ERROR>"
        )

        val typeArgs = type.element!!.typeArguments
        // 获取参数的类型参数列表

        // 可能存在泛型的情况
        if (type.element!!.typeArguments.isNotEmpty()) {
            // 检查类型参数列表是否非空
            typeName.append("<")
            typeName.append(
                typeArgs.map { typeArgument ->
                    val type = typeArgument.type?.resolve()
                    // 获取类型参数的类型，并尝试解析其声明
                    "${typeArgument.variance.label} ${type?.declaration?.qualifiedName?.asString() ?: "ERROR"}" +
                            if (type?.nullability == Nullability.NULLABLE) "?" else ""
                    // 构建类型参数的字符串表示形式，包括协变/逆变标记和类型参数的完全限定名称
                },
            )
            typeName.append(">")
        }

        val mType = type.resolve()
        typeName.append(if (mType.nullability == Nullability.NULLABLE) "?" else "")

        return typeName.toString().replace("[", "").replace("]", "")
    }

    private fun getReturn(params: List<KSValueParameter>, prefix: String = ""): String {
        return params.joinToString(", ") { param ->
            val paramName = param.name?.getShortName() ?: "Error"
            "$prefix$paramName"
        }
    }

    private fun generateComplexClassName(): String {
        val letters = "abcdefghijklmnopqrstuvwxyz"
        val numbers = "0123456789"
        val random = java.util.Random()

        val className = buildString {
            append("_DeepReCopy_")
            repeat(10) {
                val randomLetter = letters[random.nextInt(letters.length)]
                val randomDigit = numbers[random.nextInt(numbers.length)]
                append(randomLetter)
                append(randomDigit)
            }
        }

        return className
    }
}
