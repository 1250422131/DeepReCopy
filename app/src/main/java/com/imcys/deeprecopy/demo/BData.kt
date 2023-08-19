package com.imcys.deeprecopy.demo

import java.io.Serializable

class BData(
    val doc: String,
    val cc: String,
) : Serializable {
    constructor() : this("", "")
}
