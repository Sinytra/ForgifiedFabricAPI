package dev.su5ed.gradle

import net.minecraftforge.fart.api.Renamer
import net.minecraftforge.fart.api.Transformer
import net.minecraftforge.srgutils.IMappingFile
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.commons.ClassRemapper
import org.objectweb.asm.commons.Remapper

// TODO Find a reliable way to remap INTERMEDIARY to MOJMAP in dev
// The issue here is we can't rely on inheritance given our dev mc artifact is mapped to mojmap.
// For now we'll use simple key:value mapping, but I noticed some names in intermediary are duplicate so it's not an ideal solution.
// The only alternative I can think of is setting up a loom subproject that can produce mojmap artifacts.

abstract class SimpleRenameTask : DefaultTask() {
    @get:InputFile
    val inputFile: RegularFileProperty = project.objects.fileProperty()

    @get:InputFile
    val mappingFile: RegularFileProperty = project.objects.fileProperty()

    @get:OutputFile
    val outputFile: RegularFileProperty = project.objects.fileProperty()
        .convention(project.layout.buildDirectory.file("$name/output.jar"))

    @TaskAction
    fun execute() {
        val mappings = IMappingFile.load(mappingFile.asFile.get())
        val flatMapping = mappings.flatten()
        val renamer = Renamer.builder()
            .add(SimpleRenamingTransformer(flatMapping))
            .logger(project.logger::debug)
            .debug(project.logger::debug)
            .build()
        renamer.run(inputFile.asFile.get(), outputFile.asFile.get())
    }
}

fun IMappingFile.flatten(): Map<String, String> = mutableMapOf<String, String>().also { map ->
    fun addToMap(original: String, mapped: String) {
        if (original != mapped) {
            map[original]?.takeIf { mapped != it }?.let { existing -> println("Detected duplicate mapping for $original (previously $existing, now $mapped)") }
            map[original] = mapped
        }
    }
    classes.forEach { cls ->
        addToMap(cls.original, cls.mapped)
        cls.fields.forEach { field -> addToMap(field.original, field.mapped) }
        cls.methods.forEach { method -> addToMap(method.original, method.mapped) }
    }
}

class SimpleRenamingTransformer(mappings: Map<String, String>) : Transformer {
    private val remapper: Remapper = DeadSimpleRemapper(mappings)

    override fun process(entry: Transformer.ClassEntry): Transformer.ClassEntry {
        val reader = ClassReader(entry.data)
        val writer = ClassWriter(0)
        val remapper = ClassRemapper(writer, remapper)

        reader.accept(remapper, 0)

        val data: ByteArray = writer.toByteArray()
        return if (entry.isMultiRelease) Transformer.ClassEntry.create(entry.name, entry.time, data, entry.version)
        else Transformer.ClassEntry.create(entry.name, entry.time, data)
    }

    class DeadSimpleRemapper(private val mapping: Map<String, String>) : Remapper() {
        override fun mapMethodName(owner: String, name: String, descriptor: String): String =
            map(name) ?: name

        override fun mapInvokeDynamicMethodName(name: String, descriptor: String): String = map(name) ?: name

        override fun mapAnnotationAttributeName(descriptor: String, name: String): String = map(name) ?: name

        override fun mapFieldName(owner: String, name: String, descriptor: String): String = map(name) ?: name

        override fun map(key: String): String? =
            mapping[key]
    }
}