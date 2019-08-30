package com.jetbrains.snakecharm.stringLanguage

import com.intellij.openapi.fileTypes.FileTypeConsumer
import com.intellij.openapi.fileTypes.FileTypeFactory

class SmkSLFileTypeFactory : FileTypeFactory() {
    override fun createFileTypes(consumer: FileTypeConsumer) =
        consumer.consume(SmkSLFileType)
}