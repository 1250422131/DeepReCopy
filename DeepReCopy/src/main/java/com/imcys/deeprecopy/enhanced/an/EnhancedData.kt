package com.imcys.deeprecopy.enhanced.an

/**
 * 增强Kotlin的Data类注解
 * 此注解可以使得Data类扩展一个新的Copy函数，使其具备深拷贝功能
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class EnhancedData
