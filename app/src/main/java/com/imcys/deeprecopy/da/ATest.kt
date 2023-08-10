package com.imcys.deeprecopy.da

import com.imcys.deeprecopy.enhanced.an.DeepCopy

@DeepCopy
class ATest<T>(
    var demoName: String = "",
    var aaa: String = "",
    var mTest: MTest,
    var dss: T,

)
