package com.rubensousa.dependencyguard.plugin.internal

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

internal class JsonFileWriter {

    private val json = Json {
        prettyPrint = true
        prettyPrintIndent = "  "
    }

    fun writeToFile(content: Any, file: File) {
        file.mkdirs()
        file.writeText(json.encodeToString(content))
    }

}
