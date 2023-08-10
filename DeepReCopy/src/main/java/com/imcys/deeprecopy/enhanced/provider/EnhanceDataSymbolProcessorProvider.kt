package com.imcys.deeprecopy.enhanced.provider

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.imcys.deeprecopy.enhanced.processor.EnhanceDataSymbolProcessor

class EnhanceDataSymbolProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        EnhanceDataSymbolProcessor(environment)
}