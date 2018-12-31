package com.jetbrains.snakemake

import com.jetbrains.python.PythonFileType
import com.jetbrains.snakemake.lang.SnakemakeLanguageDialect

/**
 * @author Roman.Chernyatchik
 * @date 2018-12-30
 */
object SnakemakeFileType: PythonFileType(SnakemakeLanguageDialect) {
    override fun getIcon() = SnakemakeIcons.FILE
    override fun getName() = "Snakemake"
    override fun getDefaultExtension() = "smk"
    override fun getDescription() = "Snakemake pipeline file"
}