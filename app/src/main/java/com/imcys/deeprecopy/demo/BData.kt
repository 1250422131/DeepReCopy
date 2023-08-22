package com.imcys.deeprecopy.demo

import com.imcys.deeprecopy.an.EnhancedData
import java.io.Serializable
@EnhancedData
class BData(
    val doc: String,
    val cc: String,
) : Serializable {
    constructor() : this("", "")
}
