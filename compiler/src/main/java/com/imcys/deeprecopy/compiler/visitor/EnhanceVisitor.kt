package com.imcys.deeprecopy.compiler.visitor

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.symbol.Nullability
import com.imcys.deeprecopy.an.EnhancedData

class EnhanceVisitor(
    private val environment: SymbolProcessorEnvironment,
) : KSVisitorVoid() {

    private val tag = "DeepReCopy->${this::class.simpleName}:"
    val logger = environment.logger

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        super.visitClassDeclaration(classDeclaration, data)

        // 检查是否能找到主构造函数，找不到就报错
        val primaryConstructor = classDeclaration.primaryConstructor
            ?: throw Exception("error no find primaryConstructor")

        // 获取类名，当前包名
        val params = primaryConstructor.parameters
        val className = classDeclaration.simpleName.asString()
        val packageName = classDeclaration.packageName.asString()

        logger.info("$tag ClassProcessed->$className")

        // 创建KSP生成的文件
        val file = environment.codeGenerator.createNewFile(
            Dependencies(false, classDeclaration.containingFile!!),
            packageName,
            "${className}Enhance",
        )

        // 生成扩展函数的代码
        val extensionFunctionCode = generateCode(packageName, className, params)

        // 写入生成的代码
        file.write(extensionFunctionCode.toByteArray())

        // 释放内存
        file.close()

        logger.info("$tag RunEnd")
    }

    override fun visitFile(file: KSFile, data: Unit) {
        file.declarations.forEach { it.accept(this, Unit) }
    }

    /**
     * 生成开发代码
     */
    private fun generateCode(
        packageName: String,
        className: String,
        params: List<KSValueParameter>,
    ): String {
        // 生成临时类
        val complexClassName = "_${className}CopyFun"
        val extensionFunctionCode = buildString {
            // 添加包声明
            appendLine("package $packageName\n\n")

            // 新增为DSL写法支持的Data类
            appendCopyFunDataClassCode(complexClassName, params)

            // 新增深拷贝扩展函数代码
            appendDeepCopyFunCode(className, params)

            // 新增DSL写法的深拷贝扩展函数代码
            appendDSLDeepCodyFunCode(className, complexClassName, params)
        }
        return extensionFunctionCode
    }

    /**
     * 生成DSL写法的深拷贝扩展函数
     */
    private fun StringBuilder.appendDSLDeepCodyFunCode(
        className: String,
        complexClassName: String,
        params: List<KSValueParameter>,
    ) {
        appendLine("fun $className.deepCopy(")
        appendLine("    copyFunction:$complexClassName.()->Unit): $className{")
        appendLine("    val copyData = $complexClassName(${getReturn(params, "")})")
        appendLine("    copyData.copyFunction()")
        appendLine("    return this.deepCopy(${getReturn(params, "copyData.")})")
        appendLine("}")
    }

    /**
     * 生成真正的深拷贝方法代码
     */
    private fun StringBuilder.appendDeepCopyFunCode(
        className: String,
        params: List<KSValueParameter>,
    ) {
        appendLine("fun $className.deepCopy(")
        appendParamsWithDefaultValues(params)
        appendLine("): $className {")
        //生成MutableList深拷贝：如果有
        appendLine(getMutableListDeepCopyCode(params))
        appendLine("    return $className(${getReturn(params)})")
        appendLine("}\n\n")
    }

    /**
     * 生成需要被深拷贝类的临时数据类代码
     * 用来支持DSL写法
     */
    private fun StringBuilder.appendCopyFunDataClassCode(
        complexClassName: String,
        params: List<KSValueParameter>,
    ) {
        appendLine("data class $complexClassName(")
        appendParams(params)
        appendLine(")\n\n")
    }

    private fun StringBuilder.appendParams(params: List<KSValueParameter>) {
        params.forEach {
            val paramName = it.name?.getShortName() ?: "Erro"
            val typeName = generateParamsType(it.type)
            appendLine("    var $paramName : $typeName,")
        }
    }

    private fun StringBuilder.appendParamsWithDefaultValues(params: List<KSValueParameter>) {
        params.forEach {
            val paramName = it.name?.getShortName() ?: "Erro"
            val typeName = generateParamsType(it.type)
            appendLine(
                "    $paramName : $typeName = this.$paramName,",
            )
        }
    }

    private fun generateParamsType(type: KSTypeReference): String {
        val typeName = StringBuilder(
            type.resolve().declaration.qualifiedName?.asString() ?: "<ERROR>",
            // 获取参数的类型名称，如果获取失败，则使用 "<ERROR>"
        )

        val typeArgs = type.element!!.typeArguments
        // 获取参数的类型参数列表

        // 可能存在泛型的情况,需要解析泛型
        if (type.element!!.typeArguments.isNotEmpty()) {
            // 检查类型参数列表是否非空
            typeName.append("<")
            typeArgs.forEach { typeArgument ->
                val mType = typeArgument.type?.resolve()
                // 获取类型参数的类型，并尝试解析其声明
                typeName.append(
                    "${typeArgument.variance.label} ${mType?.declaration?.qualifiedName?.asString() ?: "ERROR"}" +
                            // 这里是因为有可能是可空的
                            if (mType?.nullability == Nullability.NULLABLE) "?" else "",
                )
            }
            typeName.append(">")
        }

        // 如果类型可空，那么就在后面加上?
        typeName.append(if (type.resolve().nullability == Nullability.NULLABLE) "?" else "")

        return typeName.toString()
    }

    private fun getReturn(params: List<KSValueParameter>, prefix: String = ""): String {
        return params.joinToString(", ") { param ->
            val paramName = param.name?.getShortName() ?: "Error"
            "$prefix$paramName"
        }
    }

    @OptIn(KspExperimental::class)
    private fun getReturn(params: List<KSValueParameter>): String {
        return params.joinToString(", ") { param ->
            val paramName = param.name?.getShortName() ?: "Error"
            val typeName = generateParamsType(param.type)
            val mType = param.type
            val isEnhancedData =
                mType.resolve().declaration.isAnnotationPresent(EnhancedData::class)

            var isMutableListClass = false

            //检查是不是Array，因为它不可变
            (mType.resolve().declaration as? KSClassDeclaration)?.apply {
                for (superType in superTypes) {
                    val superClassType = generateParamsType(superType)
                    if (superClassType.contains("kotlin.collections.MutableList")) {
                        isMutableListClass = true
                        break
                    }
                }
            }

            if (isMutableListClass) {
                "new${paramName}MutableList"
            } else if (typeName.startsWith("kotlin.") || !isEnhancedData) {
                paramName
            } else {
                if (typeName.contains("?")) "$paramName?.deepCopy()" else "$paramName.deepCopy()"
            }
        }
    }

    /**
     * 生成MutableList深拷贝：如果有MutableList类型参数的话
     */
    private fun getMutableListDeepCopyCode(params: List<KSValueParameter>): String {
        val code = StringBuilder("")

        params.forEach { param ->
            val paramName = param.name?.getShortName() ?: "Error"
            val typeName = generateParamsType(param.type)
            val mType = param.type

            var isMutableListClass = false


            //检查是不是Array，因为它不可变
            (mType.resolve().declaration as? KSClassDeclaration)?.apply {
                for (superType in superTypes) {
                    val superClassType = generateParamsType(superType)
                    if (superClassType.contains("kotlin.collections.MutableList")) {
                        isMutableListClass = true
                        break
                    }
                }
            }

            if (isMutableListClass) {
                val typeArgs = mType.element!!.typeArguments
                val typeArgsType = typeArgs[0].type!!
                val mTypeName = generateParamsType(typeArgsType)

                val newListIsNullTypeCode = if (typeName.contains("?")) {
                    ": MutableList<$mTypeName>?"
                } else ""
                code.appendLine(
                    """
                            val old${paramName}MutableList = $paramName
                            var new${paramName}MutableList $newListIsNullTypeCode = mutableListOf<${mTypeName}>()
                        ${getMutableListForeachDeepCopyCode(typeArgsType, paramName, typeName)}
                    """.trimIndent()
                )
            }

        }

        return code.toString()
    }

    /**
     * 生成MutableList深拷贝的核心内容
     */
    @OptIn(KspExperimental::class)
    private fun getMutableListForeachDeepCopyCode(
        type: KSTypeReference,
        paramName: String,
        listTypeName: String,

        ): String {

        val isEnhancedData =
            type.resolve().declaration.isAnnotationPresent(EnhancedData::class)
        val newMutableListName = "new${paramName}MutableList"
        val oldMutableListName = "old${paramName}MutableList"

        val typeName = generateParamsType(type)
        //这里说的是MutableList<E> 中 MutableList<E>是不是可空
        val addCopyCode = if (typeName.contains("?")) "it?.deepCopy()" else "it.deepCopy()"
        return if (listTypeName.contains("?")) {
            //这里说的是MutableList<E> 中 E是不是可空
            //MutableList可空
            if (isEnhancedData) "    $newMutableListName  =  $oldMutableListName?.toMutableList()" else {
                "    $oldMutableListName?.forEach{$newMutableListName.add($addCopyCode)}"
            }
        } else {
            //MutableList不可空
            if (isEnhancedData) "    $newMutableListName =  $oldMutableListName.toMutableList()" else {
                "    $oldMutableListName.forEach{$newMutableListName.add($addCopyCode)}"
            }
        }

    }
}
