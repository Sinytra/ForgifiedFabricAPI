package org.sinytra.ffapi.task

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

abstract class MergeAccessTransformersTask : DefaultTask() {
    @get:InputFiles
    abstract val inputFiles: ConfigurableFileCollection

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun run() {
        val builder = StringBuilder()

        inputFiles.forEach { f ->
            val text = f.readText()

            builder.append(text).append("\n")
        }

        outputFile.asFile.get().writeText(builder.toString())
    }
}
