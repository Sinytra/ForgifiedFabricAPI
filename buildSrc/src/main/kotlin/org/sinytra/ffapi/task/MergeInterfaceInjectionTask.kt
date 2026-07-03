package org.sinytra.ffapi.task

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

abstract class MergeInterfaceInjectionTask : DefaultTask() {
    @get:InputFiles
    abstract val inputFiles: ConfigurableFileCollection

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun run() {
        val result = JsonObject()

        inputFiles.forEach { f ->
            val content = f.reader().use(JsonParser::parseReader).asJsonObject
            for ((key, values) in content.entrySet()) {
                val combined = result.getAsJsonArray(key) ?: JsonArray().also { result.add(key, it) }

                combined.addAll(values.asJsonArray)
            }
        }

        val gson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()
        outputFile.asFile.get().writeText(gson.toJson(result))
    }
}
