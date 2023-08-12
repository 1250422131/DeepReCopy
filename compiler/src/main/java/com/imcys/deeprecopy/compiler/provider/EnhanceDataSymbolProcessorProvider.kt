package com.imcys.deeprecopy.compiler.provider

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.imcys.deeprecopy.compiler.processor.EnhanceDataSymbolProcessor

class EnhanceDataSymbolProcessorProvider : SymbolProcessorProvider {

    private val tag = "DeepReCopy->${this::class.simpleName}:"
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        // 执行生成
        val logger = environment.logger
        logger.info("${tag}RunStart")
        return EnhanceDataSymbolProcessor(environment)
    }
}
