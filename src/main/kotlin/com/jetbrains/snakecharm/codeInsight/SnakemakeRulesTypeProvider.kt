package com.jetbrains.snakecharm.codeInsight

import com.jetbrains.python.psi.PyReferenceExpression
import com.jetbrains.python.psi.types.PyType
import com.jetbrains.python.psi.types.PyTypeProviderBase
import com.jetbrains.python.psi.types.TypeEvalContext
import com.jetbrains.snakecharm.lang.SnakemakeLanguageDialect
import com.jetbrains.snakecharm.lang.psi.SnakemakeFile
import com.jetbrains.snakecharm.lang.psi.types.SnakemakeRulesType

class SnakemakeRulesTypeProvider : PyTypeProviderBase() {
    override fun getReferenceExpressionType(
            referenceExpression: PyReferenceExpression,
            context: TypeEvalContext
    ): PyType? {

        if (!SnakemakeLanguageDialect.isInsideSmkFile(referenceExpression)) {
            return null
        }

        val psiFile = referenceExpression.containingFile
        val name = referenceExpression.referencedName

        // XXX: at the moment affects all "rules" variables in a *.smk file, better to
        // affect only "rules" which is resolved to appropriate place
        return if (name == "rules") SnakemakeRulesType(psiFile as SnakemakeFile) else null
    }
}