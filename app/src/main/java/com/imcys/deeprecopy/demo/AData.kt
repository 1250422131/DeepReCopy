package com.imcys.deeprecopy.demo

import com.imcys.deeprecopy.an.EnhancedData
import kotlinx.serialization.Serializable

@EnhancedData
@Serializable
data class AData(
    val name: Int,
    val title: String,
    @Serializable val bDatas: CData,
)
