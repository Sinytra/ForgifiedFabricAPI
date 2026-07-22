package org.sinytra.ffapi.task

import com.google.gson.GsonBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.sinytra.ffapi.InterfaceInjection
import kotlin.io.path.bufferedReader
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

abstract class GenerateInjectedInterfacesTask : DefaultTask() {
    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @get:InputFile
    @get:Optional
    abstract val classTweaker: RegularFileProperty

    @TaskAction
    fun run() {
        val output = outputFile.asFile.get().toPath()
        
        if (classTweaker.isPresent) {
            val ctPath = classTweaker.get().asFile.toPath()

            // Process injected interfaces
            val interfaces = ctPath.bufferedReader().use(InterfaceInjection::toInjectedInterfaces)
            if (!interfaces.isEmpty()) {
                val gson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()
                val text = gson.toJson(interfaces)

                output.parent.createDirectories()
                output.writeText(text)
            }
        }
    }
}
