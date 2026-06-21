package org.sinytra.ffapi.task

import dev.architectury.at.AccessTransformSet
import dev.architectury.at.io.AccessTransformFormats
import dev.architectury.loom.util.LfWriter
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.sinytra.ffapi.Aw2At
import kotlin.io.path.bufferedReader
import kotlin.io.path.bufferedWriter
import kotlin.io.path.createDirectories

abstract class GenerateAccessTransformerTask : DefaultTask() {
    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @get:InputFile
    @get:Optional
    abstract val classTweaker: RegularFileProperty

    @TaskAction
    fun run() {
        val atPath = outputFile.get().asFile.toPath()

        if (classTweaker.isPresent) {
            val ctPath = classTweaker.get().asFile.toPath()

            val tweaker = AccessTransformSet.create()
            ctPath.bufferedReader().use { tweaker.merge(Aw2At.toAccessTransformSet(it)) }

            atPath.parent.createDirectories()
            LfWriter(atPath.bufferedWriter()).use { AccessTransformFormats.FML.write(it, tweaker) }
        }
    }
}
