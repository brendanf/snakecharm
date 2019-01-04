package com.jetbrains.snakemake.lang.parser

import com.intellij.lang.PsiBuilder
import com.jetbrains.python.PyBundle
import com.jetbrains.python.PyTokenTypes
import com.jetbrains.python.parsing.StatementParsing
import com.jetbrains.snakemake.lang.psi.elementTypes.SnakemakeElementTypes

/**
 * @author Roman.Chernyatchik
 * @date 2018-12-31
 */
class SnakemakeStatementParsing(
        context: SnakemakeParserContext,
        futureFlag: FUTURE?
) : StatementParsing(context, futureFlag) {

    override fun getParsingContext() = myContext as SnakemakeParserContext

    // TODO cleanup
//    override fun getReferenceType(): IElementType {
//            return CythonElementTypes.REFERENCE_EXPRESSION
//        }

    override fun parseStatement() {
        // super.parseStatement()

        // TODO cleanup:
//        val context = parsingContext
//        val scope = context.scope as SnakemakeParsingScope
//        var isRule = scope.isRule
        //builder.setDebugMode(true)

        var marker: PsiBuilder.Marker? = null
//
        if (atToken(SnakemakeTokenTypes.RULE_KEYWORD)) run {
//            isRule = true
            marker = myBuilder.mark()
            nextToken()

            // rule name
            if (myBuilder.tokenType == PyTokenTypes.IDENTIFIER) {
                nextToken()
            }
            checkMatches(PyTokenTypes.COLON, "Identifier or ':' expected") // bundle
            checkEndOfStatement()
            checkMatches(PyTokenTypes.INDENT, "Indent expected...") // bundle
            while (!myBuilder.eof() && myBuilder.tokenType !== PyTokenTypes.DEDENT) {
                if (!parseRuleParameter(myBuilder)) {
                    break
                }
            }
            marker!!.done(SnakemakeElementTypes.RULE_DECLARATION)
             nextToken()

        } else {
            super.parseStatement()
        }

    }

    private fun parseRuleParameter(builder: PsiBuilder): Boolean {
        val keyword = builder.tokenText
        val ruleParam = builder.mark()

        if (!checkMatches(PyTokenTypes.IDENTIFIER, IDENTIFIER_EXPECTED)) {
            ruleParam.drop()
            return false
        }

        checkMatches(PyTokenTypes.COLON, PyBundle.message("PARSE.expected.colon"))

        var result = false
        if (keyword == "input" || keyword == "output") {
            result = parsingContext.expressionParser.parseRuleParamArgumentList()
            ruleParam.done(SnakemakeElementTypes.RULE_PARAMETER_LIST_STATEMENT)
        } else {
            // error
            myBuilder.error("Unexpected keyword $keyword in rule definition") // bundle

            //TODO advance until eof or STATEMENT_END?
            // checkEndOfStatement()
            ruleParam.drop()
        }
        return result
    }

    // TODO: cleanup
//    override fun getFunctionParser(): FunctionParsing {
//        return super.getFunctionParser()
//    }
}