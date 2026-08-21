package org.sinytra.ffapi.task

import com.google.gson.JsonParser
import com.moandjiezana.toml.TomlWriter
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import java.io.File
import kotlin.io.path.*

abstract class GenerateModMetadataTask : DefaultTask() {
    @get:SkipWhenEmpty
    @get:InputFiles
    abstract val sourceRoots: ConfigurableFileCollection

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @get:Input
    @get:Optional
    abstract val forgeVersionString: Property<String>

    @get:Input
    @get:Optional
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
        val displayURL: String
    )

    data class Mixin(
        val config: String
    )

    @TaskAction
    fun run() {
        val output = outputFile.get().asFile.toPath()
        for (sourceRoot in sourceRoots) {
            if (!sourceRoot.isDirectory()) {
                continue
            }

            val root = sourceRoot.toPath()
            val fabricMetadata = root.resolve("fabric.mod.json")
            val isTestMod = root.parent.name.contains("test")

            if (fabricMetadata.notExists()) {
                continue
            }

            val json = fabricMetadata.bufferedReader().use(JsonParser::parseReader).asJsonObject

            val originalModid = json.get("id").asString
            val normalModid = normalizeModid(originalModid)
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
            val baseDependencies: MutableList<ModDependency> = mutableListOf()

            if (forgeVersionString.isPresent) {
                val parts = forgeVersionString.get().split(".")
                val neoMajorMC = parts[0]
                val neoMinorMC = parts[1]
                val neoPatchMC = parts[2]
                val neoBuild = parts[3]
                val nextMajor = neoMajorMC.toInt() + 1

                baseDependencies += ModDependency(
                    "neoforge",
                    "required",
                    "[$neoMajorMC.$neoMinorMC.$neoPatchMC.$neoBuild,$nextMajor)",
                    "NONE",
                    "BOTH"
                )
            }
            if (minecraftVersionString.isPresent) {
                val parts = minecraftVersionString.get().split(".")
                val mcMajor = parts[0]
                val mcMinor = parts[1]
                val nextMajor = mcMajor.toInt() + 1

                baseDependencies += ModDependency(
                    "minecraft",
                    "required",
                    "[$mcMajor.$mcMinor,$nextMajor)",
                    "NONE",
                    "BOTH"
                )
            }

            val allDependencies: List<ModDependency> = baseDependencies + modDependencies
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
            val modproperties = mutableMapOf<String, Any>();

            if (isTestMod) {
                modproperties["sinytra:use_default_fluid_type"] = true
            }

            if (normalModid != originalModid) {
                modproperties["fabric:provides"] = listOf(originalModid);
            }

            json.getAsJsonObject("entrypoints")
                ?.let {
                    val entrypoints = mutableMapOf<String, List<String>>()
                    allowedEntrypoints.forEach { key ->
                        it.get(key)?.let { entrypoints[key] = it.asJsonArray.map { it.asString } }
                    }
                    modproperties["fabric:entrypoints"] = entrypoints
                }

            val modsToml = ModsToml(
                modLoader = "javafml",
                loaderVersion = "*",
                license = json.get("license")?.asString ?: "All Rights Reserved",
                displayTest,
                issueTrackerURL = "https://github.com/Sinytra/ForgifiedFabricAPI/issues",

                mods,
                dependencies = mapOf(normalModid to allDependencies),
                mixins,
                modproperties.takeIf { it.isNotEmpty() }?.let { mapOf(normalModid to it) }
            )
            output.deleteIfExists()
            output.parent.createDirectories()
            TomlWriter().write(modsToml, output.toFile())
        }
    }
}
