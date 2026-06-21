package org.sinytra.ffapi.task

import com.google.gson.JsonParser
import com.moandjiezana.toml.TomlWriter
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction
import java.io.File
import kotlin.io.path.bufferedReader
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.notExists
import kotlin.text.split

abstract class GenerateModMetadataTask : DefaultTask() {
    @get:SkipWhenEmpty
    @get:InputFiles
    abstract val sourceRoots: ConfigurableFileCollection

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @get:Input
    abstract val loaderVersionString: Property<String>

    @get:Input
    abstract val forgeVersionString: Property<String>

    @get:Input
    abstract val minecraftVersionString: Property<String>

    private fun normalizeModid(modid: String): String {
        return modid.replace('-', '_')
    }

    data class ModsToml(
        val modLoader: String,
        val loaderVersion: String,
        val license: String,
        val displayTest: String?,
        val issueTrackerURL: String?,

        val mods: List<Mod>,
        val dependencies: Map<String, List<ModDependency>>,
        val mixins: List<Mixin>?,
        val modproperties: Map<String, Map<String, Any>>?
    )

    data class ModDependency(
        val modId: String,
        val type: String,
        val versionRange: String,
        val ordering: String,
        val side: String
    )

    data class Mod(
        val modId: String,
        val version: String,
        val displayName: String,
        val logoFile: String?,
        val authors: String?,
        val description: String?,
        val provides: List<String>?,
        val displayURL: String
    )

    data class Mixin(
        val config: String
    )

    @TaskAction
    fun run() {
        val output = outputFile.get().asFile.toPath()
        val containsCode = sourceRoots.any { File(it.parentFile, "java").exists() }
        for (sourceRoot in sourceRoots) {
            if (!sourceRoot.isDirectory()) {
                continue
            }

            val root = sourceRoot.toPath()
            val fabricMetadata = root.resolve("fabric.mod.json")

            if (fabricMetadata.notExists()) {
                continue
            }

            val json = fabricMetadata.bufferedReader().use(JsonParser::parseReader).asJsonObject

            val originalModid = json.get("id").asString
            val normalModid = normalizeModid(originalModid)
            val parts = minecraftVersionString.get().split(".")
            val currentMajor = parts[0]
            val nextMinor = (minecraftVersionString.get().split('.')[1].toInt()) + 1
            val excludedDeps = listOf("fabricloader", "java", "minecraft")
            val modDependencies =
                (json.getAsJsonObject("depends")?.entrySet() ?: emptySet()).filter { !excludedDeps.contains(it.key) }.map {
                    val normalDepModid = normalizeModid(it.key as String)
                    return@map ModDependency(
                        normalDepModid,
                        "required",
                        "*",
                        "NONE",
                        "BOTH"
                    )
                }
            val allDependencies: List<ModDependency> = listOf(
                ModDependency(
                    "neoforge",
                    "required",
                    "[${forgeVersionString.get()},)",
                    "NONE",
                    "BOTH"
                ),
                ModDependency(
                    "minecraft",
                    "required",
                    "[${minecraftVersionString.get()},$currentMajor.$nextMinor)",
                    "NONE",
                    "BOTH"
                )
            ) + modDependencies
            val displayTest = when (json.get("environment")?.asString) {
                "client" -> "IGNORE_ALL_VERSION"
                "server" -> "IGNORE_SERVER_VERSION"
                else -> null
            }
            val providedMods = buildList<String> {
                json.getAsJsonArray("provides")?.forEach { add(it.asString) }
                if (originalModid != normalModid) {
                    add(originalModid)
                }
            }
            val mods = listOf(
                Mod(
                    modId = normalModid,
                    version = "\${file.jarVersion}",
                    displayName = "Forgified " + json.get("name").asString,
                    logoFile = json.get("icon")?.asString,
                    authors = (listOf("Sinytra") + (json.getAsJsonArray("authors")?.map { it.asString } ?: emptyList())).joinToString(separator = ", "),
                    description = json.get("description")?.asString,
                    provides = providedMods,
                    displayURL = "https://github.com/Sinytra/ForgifiedFabricAPI"
                )
            )
            val mixins = json.getAsJsonArray("mixins")?.map {
                if (it.isJsonObject) {
                    Mixin(it.asJsonObject.get("config").asString)
                } else if (it.isJsonPrimitive) {
                    Mixin(it.asString)
                } else {
                    throw RuntimeException("Unknown mixin config type $it")
                }
            }
            val allowedEntrypoints = listOf("fabric-client-gametest", "fabric-gametest", "fabric-datagen")
            val modproperties = json.getAsJsonObject("entrypoints")
                ?.let { 
                    val entrypoints = mutableMapOf<String, List<String>>()
                    allowedEntrypoints.forEach { key ->
                        it.get(key)?.let { entrypoints[key] = it.asJsonArray.map { it.asString } }
                    }
                    mapOf<String, Map<String, Any>>(normalModid to mapOf("fabric:entrypoints" to entrypoints))
                }
                ?.takeIf { it.isNotEmpty() }

            val modsToml = ModsToml(
                modLoader = if (containsCode) "javafml" else "lowcodefml",
                loaderVersion = "[${loaderVersionString.get()},)",
                license = json.get("license")?.asString ?: "All Rights Reserved",
                displayTest,
                issueTrackerURL = "https://github.com/Sinytra/ForgifiedFabricAPI/issues",

                mods,
                dependencies = mapOf(normalModid to allDependencies),
                mixins,
                modproperties
            )
            output.deleteIfExists()
            output.parent.createDirectories()
            TomlWriter().write(modsToml, output.toFile())
        }
    }
}
