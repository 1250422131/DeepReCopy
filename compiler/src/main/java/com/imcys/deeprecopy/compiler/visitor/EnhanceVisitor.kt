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
import com.imcys.deeprecopy.an.EnhancedData
import com.imcys.deeprecopy.compiler.extend.existCloneFunctions
import com.imcys.deeprecopy.compiler.extend.existEmptyConstructor
import com.imcys.deeprecopy.compiler.extend.fullyQualifiedNotIncludedGenericsTypeName
import com.imcys.deeprecopy.compiler.extend.fullyQualifiedTypeName
import com.imcys.deeprecopy.compiler.extend.isArrayType
import com.imcys.deeprecopy.compiler.extend.isBasicDataType
import com.imcys.deeprecopy.compiler.extend.isImplementSerializable
import com.imcys.deeprecopy.compiler.extend.isListButNotMutableListType
import com.imcys.deeprecopy.compiler.extend.isMutableListType

class EnhanceVisitor(
    private val environment: SymbolProcessorEnvironment,
) : KSVisitorVoid() {

    private val tag = "DeepReCopy->${this::class.simpleName}:"
    private val logger = environment.logger

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
    @OptIn(KspExperimental::class)
    private fun generateCode(
        packageName: String,
        className: String,
        params: List<KSValueParameter>,
    ): String {
        // 生成临时类
        val complexClassName = "_${className}CopyFun"

        // 临时类参数构建
        val classParamsString = params.joinToString(separator = "\n" + "\t".repeat(3)) {
            val paramName = it.name?.getShortName() ?: "Error"
            val typeName = it.type.fullyQualifiedTypeName()
            "var $paramName: $typeName, "
        }

        // 深拷贝函数参数构建
        val funParamsString = params.joinToString(separator = "\n" + "\t".repeat(3)) {
            val paramName = it.name?.getShortName() ?: "Error"
            val typeName = it.type.fullyQualifiedTypeName()
            "$paramName: $typeName = this.$paramName, "
        }
        // 深拷贝函数返回参数构建
        val deepCopyParamsString = params.joinToString(", ") { param ->
            val paramName = param.name?.getShortName() ?: "Error"
            val mType = param.type

            val typeName = mType.fullyQualifiedTypeName()

            // 有待改进写法
            if (mType.isListButNotMutableListType()) {
                "new${paramName}MutableList.toList()"
            } else if (mType.isMutableListType()) {
                "new${paramName}MutableList"
            } else if (mType.isArrayType()) {
                "new${paramName}MutableList.toTypedArray()"
            } else {
                getNotEnhancedDataClassCopyCode(typeName, mType, paramName)
            }
        }

        // DSL写法函数参数构建
        val deepCopyDSLParamsString = params.joinToString(", ") {
            it.name?.getShortName() ?: "Error"
        }
        // DSL写法函数输出参数构建
        val copyDataParamsString = params.joinToString(", ") {
            "copyData.${it.name?.getShortName() ?: "Error"}"
        }

        return """
        // 添加包声明
        package $packageName
        import com.imcys.deeprecopy.utils.SerializableUtils
        
        // 新增为DSL写法支持的Data类
        data class $complexClassName(
            $classParamsString
        )
        
        // 新增深拷贝扩展函数代码
        fun $className.deepCopy(
            $funParamsString
        ): $className {
            ${getMutableListDeepCopyCode(params)}
            return $className($deepCopyParamsString)
        }
        
        // 新增DSL写法的深拷贝扩展函数代码
        fun $className.deepCopy(
            copyFunction:$complexClassName.()->Unit
            ): $className{
            val copyData = $complexClassName($deepCopyDSLParamsString)
            copyData.copyFunction()
            return this.deepCopy($copyDataParamsString)
        }
        """.trimIndent()
    }

    /**
     * 获取未增强数据类复制代码
     * @param typeName 待生成的类型名称
     * @param mType 待生成的类型
     * @param paramName 属性名称
     */
    @OptIn(KspExperimental::class)
    private fun getNotEnhancedDataClassCopyCode(
        typeName: String,
        mType: KSTypeReference,
        paramName: String,
    ): String {
        // 是否为可空类型
        val isNullableType = typeName.contains("?")
        val existEmptyConstructor = mType.existEmptyConstructor()
        val isEnhancedData =
            mType.resolve().declaration.isAnnotationPresent(EnhancedData::class)

        // 序列化的条件是继承了Serializable，并且有空构造函数
        val isSerializable =
            mType.isImplementSerializable() && existEmptyConstructor && !isNullableType
        // 是基本数据类型
        val isBasicDataType = mType.isBasicDataType()
        // 存在克隆函数
        val existCloneFunctions = mType.existCloneFunctions()
        // 生成连接符
        val hyphen = if (isNullableType) "?." else "."
        return if (existCloneFunctions) {
            "${paramName}${hyphen}clone() as $typeName"
        } else if (isEnhancedData) {
            "${paramName}${hyphen}deepCopy()"
        } else if (!isBasicDataType && isSerializable) {
            "SerializableUtils.deepCopy($paramName.javaClass.kotlin)"
        } else {
            // 无法处理的
            paramName
        }
    }

    /*private fun deepCopyParamsString(params: List<KSValueParameter>):String{
        params.joinToString(separator = "\n\t") {
            val paramName = it.name?.getShortName() ?: "Error"
            val typeName = generateParamsType(it.type)
            "$paramName: $typeName = this.$paramName, "
        }
    }*/

    /**
     * 生成MutableList深拷贝：如果有MutableList类型参数的话
     */
    private fun getMutableListDeepCopyCode(params: List<KSValueParameter>): String {
        val code = StringBuilder("")

        params.forEach { param ->
            val paramName = param.name?.getShortName() ?: "Error"
            // 每个属性的类型
            val mType = param.type

            val isMutableListClass = mType.isMutableListType()
            val isList = mType.isListButNotMutableListType()
            val isArray = mType.isArrayType()

            if (isMutableListClass || isList || isArray) {
                val genericsArgs = mType.element!!.typeArguments
                val genericsArgsType = genericsArgs[0].type!!
                // list类型
                // val mTypeName = mType.fullyQualifiedTypeName()
                val genericsArgsTypeName = genericsArgsType.fullyQualifiedTypeName()
                val listTypeName = mType.fullyQualifiedNotIncludedGenericsTypeName()

                val oldMutableListCode = if (isMutableListClass) {
                    paramName
                } else {
                    if (listTypeName.contains("?")) {
                        "$paramName?.toMutableList()"
                    } else {
                        "$paramName.toMutableList()"
                    }
                }
                val oldMutableListTypeCode = if (listTypeName.contains("?")) {
                    ":MutableList<$genericsArgsTypeName>?"
                } else {
                    ":MutableList<$genericsArgsTypeName>"
                }

                code.appendLine(
                    """
                    val old${paramName}MutableList  = $oldMutableListCode
                    var new${paramName}MutableList $oldMutableListTypeCode = mutableListOf()
                        ${getMutableListForeachDeepCopyCode(genericsArgsType, paramName, mType)}
                    """.trimIndent(),
                )
            }
        }

        return code.toString()
    }

    /**
     * 生成MutableList深拷贝的核心内容
     * 这个函数参数有争议，下面详细阐述一下
     * @param type MutableList中泛型类型
     * @param paramName MutableList属性名称
     * @param listType MutableList类型
     */
    @OptIn(KspExperimental::class)
    private fun getMutableListForeachDeepCopyCode(
        type: KSTypeReference,
        paramName: String,
        listType: KSTypeReference,
    ): String {
        val isEnhancedData = type.resolve().declaration.isAnnotationPresent(EnhancedData::class)
        val newMutableListName = "new${paramName}MutableList"
        val oldMutableListName = "old${paramName}MutableList"
        val typeName = type.fullyQualifiedTypeName()
        // 这里说的是MutableList<E> 中 MutableList<E>是不是可空
        val listTypeName = listType.fullyQualifiedNotIncludedGenericsTypeName()
        val addCopyCode = getNotEnhancedDataClassCopyCode(typeName, type, "it")

        return if (listTypeName.contains("?")) {
            // 这里说的是MutableList<E> 中 E是不是可空
            // MutableList可空
            if (addCopyCode == "it") {
                "    $newMutableListName  =  $oldMutableListName?.toMutableList()"
            } else {
                "    $oldMutableListName?.forEach{$newMutableListName?.add($addCopyCode)}"
            }
        } else {
            // MutableList不可空
            if (addCopyCode == "it") {
                "    $newMutableListName =  $oldMutableListName.toMutableList()"
            } else {
                "    $oldMutableListName.forEach{$newMutableListName.add($addCopyCode)}"
            }
        }
    }
}
