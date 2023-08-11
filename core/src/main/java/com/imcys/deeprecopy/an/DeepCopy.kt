package com.imcys.deeprecopy.an


/**
 * 增强需要被深度拷贝的类
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class DeepCopy
