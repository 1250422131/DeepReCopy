package com.imcys.deeprecopy.demo

import com.imcys.deeprecopy.an.EnhancedData

@EnhancedData
data class AData(
    val name: Int,
    val title: String,
    val bDatas: CData,
)
