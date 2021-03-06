package com.jetbrains.snakecharm.lang.psi.impl.refs

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.PlatformIcons
import com.jetbrains.python.psi.PyStringLiteralExpression
import com.jetbrains.snakecharm.lang.SnakemakeNames
import com.jetbrains.snakecharm.lang.psi.SmkRuleOrCheckpoint


class SmkParamsReference(
        element: PyStringLiteralExpression,
        textRange: TextRange
) : PsiReferenceBase<PyStringLiteralExpression>(element, textRange) {
    private val key: String = element.text.substring(textRange.startOffset, textRange.endOffset)

    override fun resolve() =
            getKeywordArguments().firstOrNull { it.name == key }

    override fun getVariants() =
            getKeywordArguments()
                    .mapNotNull { arg -> arg.keyword }
                    .map { keyword ->
                        LookupElementBuilder.create(keyword).withIcon(PlatformIcons.PARAMETER_ICON)
                    }
                    .toTypedArray()

    private fun getParamsSection() =
            PsiTreeUtil.getParentOfType(element, SmkRuleOrCheckpoint::class.java)
            ?.getSectionByName(SnakemakeNames.SECTION_PARAMS)

    private fun getKeywordArguments() =
            getParamsSection()?.keywordArguments ?: emptyList()
}