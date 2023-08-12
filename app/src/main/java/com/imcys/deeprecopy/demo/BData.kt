package com.imcys.deeprecopy.demo

import com.imcys.deeprecopy.an.EnhancedData

@EnhancedData
open class BData(
    val doc: String,
    val cc: String
) {
    constructor( doc: String) : this(doc, "") {
        val a = ""
        print(a)
    }
}
