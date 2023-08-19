package com.imcys.deeprecopy.demo

import kotlinx.serialization.Serializable

@Serializable
class CData(val a: String) {
    constructor() : this("aaa")
}
