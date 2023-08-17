package com.imcys.deeprecopy.an

/**
 * 用于增强Kotlin的Data类
 * 此注解可以使得Data类扩展一个新的Copy函数，使其具备深拷贝功能
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class EnhancedData
