package com.imcys.deeprecopy.enhanced.processor

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import com.imcys.deeprecopy.enhanced.an.DeepCopy
import com.imcys.deeprecopy.enhanced.an.EnhancedData
import com.imcys.deeprecopy.enhanced.visitor.EnhanceVisitor

class EnhanceDataSymbolProcessor(private val environment: SymbolProcessorEnvironment) :
    SymbolProcessor {
    private val logger = environment.logger

    override fun process(resolver: Resolver): List<KSAnnotated> {
        // 获取由EnhancedData注解类
        val enhancedDataSymbols =
            resolver.getSymbolsWithAnnotation(
                EnhancedData::class.qualifiedName
                    ?: "",
            )

        val deepCopySymbols =
            resolver.getSymbolsWithAnnotation(
                DeepCopy::class.qualifiedName
                    ?: "",
            )

        // 干掉无法处理的类
        val ret = mutableListOf<KSAnnotated>()
        ret.addAll(enhancedDataSymbols.filter { !it.validate() }.toList())

        // 执行生成
        logger.info("执行开始")
        generateDeepCopyClass(enhancedDataSymbols, deepCopySymbols)
        logger.info("执行完成")

        return ret
    }
    private fun generateDeepCopyClass(
        symbols: Sequence<KSAnnotated>,
        deepCopySymbols: Sequence<KSAnnotated>,
    ) {
        symbols
            .filter { it is KSClassDeclaration && it.validate() } // 检查是否为Data类
            .forEach {
                it.accept(EnhanceVisitor(environment, deepCopySymbols), Unit)
            }
    }
}
