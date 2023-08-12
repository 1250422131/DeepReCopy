package com.imcys.deeprecopy.compiler.processor

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import com.imcys.deeprecopy.an.EnhancedData
import com.imcys.deeprecopy.compiler.visitor.EnhanceVisitor

class EnhanceDataSymbolProcessor(private val environment: SymbolProcessorEnvironment) :
    SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        // 获取由EnhancedData注解类
        val enhancedDataSymbols =
            resolver.getSymbolsWithAnnotation(
                EnhancedData::class.qualifiedName
                    ?: "",
            )

        // 干掉无法处理的类
        val ret = mutableListOf<KSAnnotated>()
        ret.addAll(enhancedDataSymbols.filter { !it.validate() })

        generateDeepCopyClass(enhancedDataSymbols)

        return ret
    }

    private fun generateDeepCopyClass(
        symbols: Sequence<KSAnnotated>,
    ) {
        symbols
            .filter { it is KSClassDeclaration && it.validate() } // 检查是否为Data类
            .forEach {
                it.accept(EnhanceVisitor(environment), Unit)
            }
    }
}
