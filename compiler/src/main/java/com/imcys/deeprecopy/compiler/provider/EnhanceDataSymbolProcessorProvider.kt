package com.imcys.deeprecopy.compiler.provider

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.imcys.deeprecopy.compiler.processor.EnhanceDataSymbolProcessor

class EnhanceDataSymbolProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        EnhanceDataSymbolProcessor(environment)
}