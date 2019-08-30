package com.jetbrains.snakecharm.inspections

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.util.Ref
import com.jetbrains.snakecharm.SnakemakeBundle
import com.jetbrains.snakecharm.lang.psi.*

class SmkNotSameWildcardsSetInspection : SnakemakeInspection() {
    override fun buildVisitor(
            holder: ProblemsHolder,
            isOnTheFly: Boolean,
            session: LocalInspectionToolSession
    ) = object : SnakemakeInspectionVisitor(holder, session) {
        override fun visitSmkRule(rule: SmkRule) {
            processRuleOrCheckpointLike(rule)
        }

        override fun visitSmkCheckPoint(checkPoint: SmkCheckPoint) {
            processRuleOrCheckpointLike(checkPoint)
        }

        fun processRuleOrCheckpointLike(ruleOrCheckpoint: SmkRuleOrCheckpoint) {
            var wildcardsRef: Ref<List<String>?>? = null

            ruleOrCheckpoint.getSections().forEach { section ->
                if (section !is SmkRuleOrCheckpointArgsSection || !section.isWildcardsDefiningSection()) {
                    return@forEach
                }

                if (wildcardsRef == null) {
                    wildcardsRef = Ref.create(ruleOrCheckpoint.collectWildcards())
                }

                val wildcards = wildcardsRef!!.get()
                if (wildcards != null) {
                    // show warnings only if wildcards defined
                    processSection(section, wildcards)
                }
            }
        }

        private fun processSection(
                section: SmkRuleOrCheckpointArgsSection,
                wildcards: List<String>
        ) {
            val sectionArgs = section.argumentList?.arguments ?: return
            sectionArgs.forEach { arg ->
                // collect wildcards in section arguments:
                val collector = SmkWildcardsCollector(
                        visitDefiningSections = false,
                        visitSectionsAllowingUsage = false
                )
                arg.accept(collector)
                val argWildcards = collector.getWildcardsNames()

                if (argWildcards != null) {
                    // if wildcards defined for args section
                    val missingWildcards = wildcards.filter { it !in argWildcards }
                    if (missingWildcards.isNotEmpty()) {
                        registerProblem(
                                arg,
                                SnakemakeBundle.message(
                                        "INSP.NAME.not.same.wildcards.set",
                                        missingWildcards.sorted().joinToString() { "'$it'"}
                                )
                        )
                    }
                }
            }
        }
    }

    override fun getDisplayName(): String = SnakemakeBundle.message("INSP.NAME.not.same.wildcards.set", "")
}