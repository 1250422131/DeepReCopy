package com.imcys.deeprecopy.demo

import com.imcys.deeprecopy.an.EnhancedData
import java.util.Date

@EnhancedData
data class AData(
    val name: Int,
    val title: String,
    val bDatas: BData,
    val dates: MutableList<Date>,
    val onComplete: (Boolean) -> Unit,

    )
