package com.imcys.deeprecopy.compiler.extend

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.Nullability

/**
 * 检查是否为MutableList类型
 */
fun KSTypeReference.isMutableListType(): Boolean {
    var isMutableListClass = false

    if (this.fullyQualifiedTypeName().contains("kotlin.collections.MutableList")) return true

    // 检查是不是Array，因为它不可变
    (this.resolve().declaration as? KSClassDeclaration)?.apply {
        for (superType in superTypes) {
            val superClassType = superType.fullyQualifiedTypeName()
            if (superClassType.contains("kotlin.collections.MutableList")) {
                isMutableListClass = true
                break
            }
        }
    }

    return isMutableListClass
}

/**
 * 检查是否为Array类型
 */
fun KSTypeReference.isArrayType(): Boolean {
    var isArrayClass = false

    // 检查是不是Array，因为它不可变
    if (this.fullyQualifiedTypeName().contains("kotlin.Array")) return true

    (this.resolve().declaration as? KSClassDeclaration)?.apply {
        for (superType in superTypes) {
            val superClassType = superType.fullyQualifiedTypeName()
            if (superClassType.contains("kotlin.Array")) {
                isArrayClass = true
                break
            }
        }
    }

    return isArrayClass
}

/**
 * 检查是否为Array类型
 */
fun KSTypeReference.isListButNotMutableListType(): Boolean {
    var isListClass = false
    var isMutableListClass = false

    if (this.fullyQualifiedTypeName().contains("kotlin.collections.List")) return true

    // 检查是不是Array，因为它不可变
    (this.resolve().declaration as? KSClassDeclaration)?.apply {
        for (superType in superTypes) {
            val superClassType = superType.fullyQualifiedTypeName()
            if (superClassType.contains("kotlin.collections.MutableList")) {
                isMutableListClass = true
                break
            } else if (superClassType.contains("kotlin.collections.List")) {
                isListClass = true
                break
            }
        }
    }

    return !isMutableListClass && isListClass
}

/**
 * 完整类型名称：包含泛型
 */
fun KSTypeReference.fullyQualifiedTypeName(): String {
    val typeName = StringBuilder(
        resolve().declaration.qualifiedName?.asString() ?: "ERROR",
        // 获取参数的类型名称，如果获取失败，则使用 "<ERROR>"
    )

    val typeArgs = element?.typeArguments
    // 获取参数的类型参数列表

    // 可能存在泛型的情况,需要解析泛型
    if (!typeArgs.isNullOrEmpty()) {
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
    typeName.append(if (resolve().nullability == Nullability.NULLABLE) "?" else "")

    return typeName.toString()
}

/**
 * 完整类型名称：不含泛型
 */
fun KSTypeReference.fullyQualifiedNonInclusiveGenericsTypeName(): String {
    val typeName = StringBuilder(
        resolve().declaration.qualifiedName?.asString() ?: "ERROR",
        // 获取参数的类型名称，如果获取失败，则使用 "<ERROR>"
    )

    // 如果类型可空，那么就在后面加上?
    typeName.append(if (resolve().nullability == Nullability.NULLABLE) "?" else "")

    return typeName.toString()
}
