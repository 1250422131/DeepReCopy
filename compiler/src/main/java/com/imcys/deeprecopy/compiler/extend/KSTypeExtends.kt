package com.imcys.deeprecopy.compiler.extend

import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.Nullability

private val javaPrimitiveTypes =
    setOf("boolean", "byte", "short", "int", "long", "float", "double", "char")
private val javaWrapperTypes =
    setOf("Boolean", "Byte", "Short", "Integer", "Long", "Float", "Double", "Character", "String")
private val kotlinWrapperTypes =
    setOf("Boolean", "Byte", "Short", "Int", "Long", "Float", "Double", "Char", "String")

private fun isJavaPrimitiveType(typeName: String): Boolean {
    return javaPrimitiveTypes.any { typeName.contains(it) }
}

private fun isJavaWrapperType(typeName: String): Boolean {
    return javaWrapperTypes.any { typeName.contains(it) }
}

private fun isKotlinWrapperType(typeName: String): Boolean {
    return kotlinWrapperTypes.any { typeName.contains(it) }
}

/**
 * 检查是不是kotlin或者Java的基本数据类型（含包装类）
 */
fun KSTypeReference.isBasicDataType(): Boolean {
    val typeName = fullyQualifiedNotIncludedGenericsTypeName()
    return isKotlinWrapperType(typeName) || isJavaPrimitiveType(typeName) || isJavaWrapperType(
        typeName,
    )
}

/**
 * 检查是否存在Clone方法
 */
fun KSTypeReference.existCloneFunctions(): Boolean {
    var existCloneFunctions = false
    (this.resolve().declaration as? KSClassDeclaration)?.apply {
        val kSClassDeclaration = getDeclaredFunctions().find { it.simpleName.asString() == "clone" }
        if (kSClassDeclaration != null) existCloneFunctions = true
    }
    return existCloneFunctions
}

/**
 * 检查是否为MutableList类型
 */
fun KSTypeReference.isMutableListType(): Boolean {
    return checkTypeHierarchy("kotlin.collections.MutableList")
}

/**
 * 检查是否为Array类型
 */
fun KSTypeReference.isArrayType(): Boolean {
    return checkTypeHierarchy("kotlin.Array")
}

/**
 * 检查是否为List但不是MutableList类型
 */
fun KSTypeReference.isListButNotMutableListType(): Boolean {
    return !isMutableListType() && checkTypeHierarchy("kotlin.collections.List")
}

/**
 * 检查是否实现了Serializable接口
 */
fun KSTypeReference.isImplementSerializable(): Boolean {
    return checkTypeHierarchy("java.io.Serializable")
}

/**
 * 检查是否为Set类型
 */
fun KSTypeReference.isSetType(): Boolean {
    return checkTypeHierarchy("kotlin.collections.Set")
}

fun KSTypeReference.isMutableSetType(): Boolean {
    return checkTypeHierarchy("kotlin.collections.MutableSet")
}

/**
 * 检查是否为可变集合->通用策略
 * @receiver KSTypeReference
 * @return Boolean
 */
fun KSTypeReference.isMutableCollectionType(): Boolean {
    return checkTypeHierarchy("kotlin.collections.MutableCollection")
}

fun KSTypeReference.existEmptyConstructor(): Boolean {
    var existEmptyConstructor = false
    (this.resolve().declaration as? KSClassDeclaration)?.apply {
        for (constructors in getConstructors()) {
            if (constructors.parameters.isEmpty()) existEmptyConstructor = true
        }
    }
    return existEmptyConstructor
}

/**
 * 完整类型名称：包含泛型
 */
fun KSTypeReference.fullyQualifiedTypeName(): String {
    val typeName = StringBuilder(resolve().declaration.qualifiedName?.asString() ?: "ERROR")

    val typeArgs = element?.typeArguments
    if (!typeArgs.isNullOrEmpty()) {
        typeName.append("<")
        typeArgs.forEachIndexed { index, typeArgument ->
            val mType = typeArgument.type?.resolve()
            typeName.append(
                "${typeArgument.variance.label} ${mType?.declaration?.qualifiedName?.asString() ?: "ERROR"}" +
                        if (mType?.nullability == Nullability.NULLABLE) "?" else "",
            )

            if (index != typeArgs.size) {
                typeName.append(",")
            }
        }
        typeName.append(">")
    }

    typeName.append(if (resolve().nullability == Nullability.NULLABLE) "?" else "")

    return typeName.toString()
}

/**
 * 完整类型名称：不含泛型
 */
fun KSTypeReference.fullyQualifiedNotIncludedGenericsTypeName(): String {
    val typeName = StringBuilder(resolve().declaration.qualifiedName?.asString() ?: "ERROR")
    typeName.append(if (resolve().nullability == Nullability.NULLABLE) "?" else "")

    return typeName.toString()
}

private fun KSTypeReference.checkTypeHierarchy(typeName: String): Boolean {
    var isType = false

    // 假设当前就是就不用判断了
    if (this.fullyQualifiedTypeName() == typeName) return true

    (this.resolve().declaration as? KSClassDeclaration)?.apply {
        for (superType in superTypes) {
            val superClassType = superType.fullyQualifiedTypeName()
            if (superClassType.contains(typeName)) {
                isType = true
                break
            }
        }
    }

    return isType
}
