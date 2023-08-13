package com.imcys.deeprecopy.demo

import com.imcys.deeprecopy.an.EnhancedData

@EnhancedData
data class AData(
    val name: String,
    val title: String,
    val bData: BData,
)
