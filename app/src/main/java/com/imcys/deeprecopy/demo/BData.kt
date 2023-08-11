package com.imcys.deeprecopy.demo

import com.imcys.deeprecopy.an.DeepCopy

@DeepCopy
data class BData(
    val doc: String,
    val content: String,
)
