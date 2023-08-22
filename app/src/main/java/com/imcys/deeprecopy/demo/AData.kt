package com.imcys.deeprecopy.demo

import com.imcys.deeprecopy.an.EnhancedData
import java.util.Date

@EnhancedData
data class AData(
    val name: Int,
    val title: String,
    val bDatas: BData,
    val onComplete: (Boolean) -> Unit,
    val Set1: Set<BData>,
    val Set2: Set<BData>?,
    val Set3: Set<BData?>,
    val Set4: Set<BData?>?,
    val MutableSet1: MutableSet<BData>,
    val MutableSet2: MutableSet<BData>?,
    val MutableSet3: MutableSet<BData?>,
    val MutableSet4: MutableSet<BData?>?,
    val HashSet1: HashSet<BData>,
    val HashSet2: HashSet<BData>?,
    val HashSet3: HashSet<BData?>,
    val HashSet4: HashSet<BData?>?,
    val Map1: Map<String, BData>,
    val Map2: Map<String, BData>?,
    val Map3: Map<String, BData?>,
    val Map4: Map<String, BData?>?,
    val MutableMap1: MutableMap<String, BData>,
    val MutableMap2: MutableMap<String, BData>?,
    val MutableMap3: MutableMap<String, BData?>,
    val MutableMap4: MutableMap<String, BData?>?,
    val MutableList1: MutableList<Date>,
    val MutableList2: MutableList<BData>?,
    val MutableList3: MutableList<BData?>,
    val MutableList4: MutableList<BData?>?,
    val List1: List<Date>,
    val List2: List<BData>?,
    val List3: List<BData?>,
    val List4: List<BData?>?,
    val Array1: Array<Date>,
    val Array2: Array<BData>?,
    val Array3: Array<BData?>,
    val Array4: Array<BData?>?,
)
