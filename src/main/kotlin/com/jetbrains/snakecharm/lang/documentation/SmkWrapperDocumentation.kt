package com.jetbrains.snakecharm.lang.documentation

import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.lang.documentation.DocumentationMarkup
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.python.psi.PyStringLiteralExpression
import com.jetbrains.snakecharm.codeInsight.wrapper.WrapperStorage
import com.jetbrains.snakecharm.lang.SnakemakeLanguageDialect
import com.jetbrains.snakecharm.lang.SnakemakeNames
import com.jetbrains.snakecharm.lang.psi.SmkRuleOrCheckpointArgsSection

class SmkWrapperDocumentation : AbstractDocumentationProvider() {
    companion object {
        private const val URL_START = "https://bitbucket.org/snakemake/snakemake-wrappers/src/"
        private const val WRAPPER = "wrapper.py"
        private const val META = "meta.yaml"
        private const val ENVIRONMENT = "environment.yaml"
        private const val EXAMPLE_SNAKEFILE = "test/Snakefile"

        private const val HTML_NBSP = ":&nbsp;"
        private const val HTML_P = "<p>"
    }

    private val nextSectionRegex = Regex("[a-z]+:")


    override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
        if (!originalElement.isStringLiteralInWrapperSection()) {
            return null
        }

        val text = originalElement?.text?.replace("\"", "") ?: return null

        val wrapper = WrapperStorage.getInstance().getWrapperList().find { text.contains(it.pathToWrapperDirectory) }

        val result = mutableListOf<String>()
        result.add(DocumentationMarkup.DEFINITION_START)
        result.add("Snakemake Wrapper")
        result.add(DocumentationMarkup.DEFINITION_END)
        result.add(DocumentationMarkup.CONTENT_START)
        result.add(text)
        result.add(DocumentationMarkup.CONTENT_END)
        result.add(HTML_P)

        result.add(DocumentationMarkup.SECTIONS_START)
        result.addAll(buildSectionWithFileLink(text, WRAPPER))
        if (wrapper != null) {
            val descriptionAndFollowingText = wrapper.metaFileContent.substringAfter("description:")
            val textAfterDescriptionStart = nextSectionRegex.find(descriptionAndFollowingText)?.range?.first
            val description = if (textAfterDescriptionStart == null) {
                descriptionAndFollowingText
            } else {
                descriptionAndFollowingText.substring(0, textAfterDescriptionStart)
            }
            result.addAll(buildSectionWithTextAndLink(
                    "description",
                    description.trim(),
                    "$URL_START$text/$META",
                    META
            ))
        } else {
            result.addAll(buildSectionWithFileLink(text, META))
        }
        if (wrapper != null) {
            result.addAll(buildSectionWithTextAndLink(
                    "dependencies",
                    wrapper.environmentFileContent
                            .substringAfter("dependencies:")
                            .trim { it.isWhitespace() || it == '\n' },
                    "$URL_START$text/$ENVIRONMENT",
                    ENVIRONMENT
            ))
        } else {
            result.addAll(buildSectionWithFileLink(text, ENVIRONMENT))
        }
        if (wrapper != null) {
            result.addAll(buildSectionWithTextAndLink(
                    "example",
                    wrapper.testSnakefileContent,
                    "$URL_START$text/$EXAMPLE_SNAKEFILE",
                    EXAMPLE_SNAKEFILE
            ))
        } else {
            result.addAll(buildSectionWithFileLink(text, EXAMPLE_SNAKEFILE, "Example Snakefile"))
        }
        result.add(DocumentationMarkup.SECTIONS_END)

        val body = "<body>${result.joinToString("")}</body>"
        return "<html>$body</html>"
    }

    override fun getCustomDocumentationElement(editor: Editor, file: PsiFile, contextElement: PsiElement?) =
            if (contextElement.isStringLiteralInWrapperSection()) contextElement else null

    private fun buildSectionWithFileLink(
            partialPath: String,
            filename: String,
            sectionTitle: String = filename
    ): List<String> {
        val wrapperPath = if (partialPath.endsWith("/")) partialPath else "$partialPath/"
        val url = "$URL_START$wrapperPath$filename"
        return listOf(
                DocumentationMarkup.SECTION_HEADER_START,
                sectionTitle,
                DocumentationMarkup.SECTION_SEPARATOR,
                "$HTML_P<a href=\"$url\">$url</a>",
                DocumentationMarkup.SECTION_END
        )
    }

    private fun buildSectionWithTextAndLink(
            sectionTitle: String,
            sectionContent: String,
            link: String?,
            linkText: String?
    ): List<String> {
        return listOf(
                DocumentationMarkup.SECTION_HEADER_START,
                sectionTitle,
                DocumentationMarkup.SECTION_SEPARATOR,
                HTML_P,
                sectionContent.toHTML(),
                if (link != null && linkText != null) { "$HTML_P<a href=\"$link\">($linkText)</a>" } else "",
                DocumentationMarkup.SECTIONS_END
        )
    }

    private fun PsiElement?.isStringLiteralInWrapperSection() =
            SnakemakeLanguageDialect.isInsideSmkFile(this) &&
                    this.isInWrapperSection() &&
                    PsiTreeUtil.getParentOfType(this, PyStringLiteralExpression::class.java) != null

    private fun PsiElement?.isInWrapperSection() =
            PsiTreeUtil.getParentOfType(this, SmkRuleOrCheckpointArgsSection::class.java)?.name ==
                    SnakemakeNames.SECTION_WRAPPER

    private fun String.toHTML() = this.replace("\n", HTML_P).replace(" ", HTML_NBSP)
}