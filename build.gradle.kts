import dev.su5ed.gradle.SimpleRenameTask
import net.minecraftforge.gradle.common.tasks.CheckJarCompatibility
import net.minecraftforge.gradle.common.util.RunConfig
import net.minecraftforge.srgutils.IMappingFile
import net.minecraftforge.srgutils.IMappingFile.load
import net.minecraftforge.srgutils.INamedMappingFile
import java.time.LocalDateTime
import java.util.zip.ZipFile

plugins {
    `java-library`
    `maven-publish`
    id("net.minecraftforge.gradle") version "[6.0,6.2)"
    id("org.parchmentmc.librarian.forgegradle") version "1.+"
    id("org.spongepowered.mixin") version "0.7.+" apply false
}

version = "1.0"

val versionMc: String by project
val versionYarn: String by project

val yarnMappings: Configuration by configurations.creating

jarJar.enable()

dependencies {
    yarnMappings(group = "net.fabricmc", name = "yarn", version = versionYarn)
}

val createIntermediaryToSrg by tasks.registering(ConvertSRGTask::class) {
    inputYarnMappings.set { yarnMappings.singleFile }
    inputSrgMappings.set(tasks.extractSrg.flatMap { it.output })
    inputMcpMappings.set(tasks.createSrgToMcp.flatMap { it.output })
}

allprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")
    apply(plugin = "net.minecraftforge.gradle")
    apply(plugin = "org.parchmentmc.librarian.forgegradle")

    group = "dev.su5ed.sinytra.fabric-api"

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(17))
        withSourcesJar()
    }

    minecraft {
        mappings("parchment", "1.19.3-2023.03.12-$versionMc")

        val atFile = file("src/main/resources/META-INF/accesstransformer.cfg")
        if (atFile.exists()) accessTransformer(atFile)

        runs {
            val config = Action<RunConfig> {
                property("forge.logging.console.level", "debug")
                property("forge.logging.markers", "REGISTRIES,SCAN,FMLHANDSHAKE")
                property("mixin.debug", "true")
                workingDirectory = project.file("run").canonicalPath

                mods {
                    create("fabric_api") {
                        sources(sourceSets.main.get())
                    }
                }
            }

            create("client", config)
            create("server", config)
        }
    }

    repositories {
        maven {
            name = "Fabric"
            url = uri("https://maven.fabricmc.net")
        }
        exclusiveRepo("https://maven.su5ed.dev/releases", "dev.su5ed.sinytra")
    }

    dependencies {
        minecraft(group = "net.minecraftforge", name = "forge", version = "$versionMc-45.0.66")
    }

    // Clean without deleting fg_cache to preserve ATs
    val cachedClean by tasks.registering(Delete::class) {
        group = "build"
        delete(fileTree(project.buildDir) {
            exclude("fg_cache/**")
        })
    }

    tasks {
        setOf(jar, named<Jar>("sourcesJar")).forEach { provider ->
            provider.configure { 
                from(rootProject.file("LICENSE")) {
                    rename { "${it}-${project.base.archivesName.get()}" }
                }
            }
        }
        
        jar {
            finalizedBy("reobfJar")
        }

        withType<JavaCompile> {
            options.encoding = "UTF-8"
        }

        withType<GenerateModuleMetadata> {
            enabled = false
        }
        
        configureEach {
            if (name.startsWith("configureReobfTaskForReobf")) {
                val target = name.substringAfter("configureReobfTaskForReobf")
                val taskName = target[0].lowercase() + target.substring(1)
                mustRunAfter(taskName)
            }
        }
    }

    publishing {
        publications { 
            create<MavenPublication>("mavenJava") {
                from(components["java"])
                fg.component(this)
            }
        }
    }
}

subprojects {
    apply(plugin = "org.spongepowered.mixin")
    apply(plugin = "package-info-convention")

    version = "1.0" // TODO

    val referenceApi by configurations.creating {
        isTransitive = false
    }
    val devJar by tasks.registering(Jar::class) {
        from(sourceSets.main.map { it.output })
        archiveClassifier.set("dev")
    }
    val renameReferenceApi by tasks.registering(SimpleRenameTask::class) {
        inputFile.set(referenceApi::getSingleFile)
        mappingFile.set(createIntermediaryToSrg.flatMap(ConvertSRGTask::outputFile))
    }

    val checkReferenceCompatibility by tasks.registering(CheckJarCompatibility::class) {
        dependsOn(renameReferenceApi)
        group = "verification"

        tool.set("dev.su5ed.sinytra:JarCompatibilityChecker:0.1.+:all")
        binaryMode.set(false)
        annotationCheckMode.set("warn_added")
        baseJar.set(renameReferenceApi.flatMap { it.outputFile })
        inputJar.set(devJar.flatMap { it.archiveFile })
        commonLibraries.from(configurations.minecraft.map { c -> c.files.filter { it.name.endsWith(".jar") } })
        args.addAll("--internal-ann-mode", "skip")
        outputs.upToDateWhen { true }
    }

    tasks {
        check {
            dependsOn(checkReferenceCompatibility)
        }

        configureEach {
            if (name.startsWith("run") && name.contains("Test")) {
                dependsOn(configurations["testModRuntimeClasspath"])
            }
        }
    }

    sourceSets.main {
        // Include resources generated by data generators.
        resources {
            srcDir("src/generated/resources")
        }
    }

    //@formatter:off
    @Suppress("UNUSED_VARIABLE")
    val withClientSourceSet: () -> Unit by extra { {
        sourceSets.main {
            java {
                srcDir("src/client/java")
            }
            resources {
                srcDir("src/client/resources")
            }
        }
    } }
    @Suppress("UNUSED_VARIABLE")
    val withTestMod: () -> Unit by extra { {
        val testMod: SourceSet by sourceSets.creating {
            // Include resources generated by data generators.
            resources {
                srcDir("src/testmod/generated")
            }
        }
        configurations.named(testMod.implementationConfigurationName) {
            extendsFrom(configurations.implementation.get())
        }
        dependencies.add(testMod.implementationConfigurationName, sourceSets.main.get().output)
        minecraft.runs.create("clientTest") {
            parent(minecraft.runs["client"])
            workingDirectory = project.file("run_test").canonicalPath
            mods {
                create("${project.name}-test") {
                    sources(testMod)
                }
            }
        }
        minecraft.runs.create("serverTest") {
            parent(minecraft.runs["server"])
            workingDirectory = project.file("run_test").canonicalPath
            mods {
                create("${project.name}-test") {
                    sources(testMod)
                }
            }
        }
    } }
    @Suppress("UNUSED_VARIABLE")
    val withGameTest: (String) -> Unit by extra { { modid ->
        minecraft.runs.create("gameTestServer") {
            parent(minecraft.runs["server"])
            workingDirectory = project.file("run_test").canonicalPath
            property("forge.enabledGameTestNamespaces", modid)
            mods {
                create("${project.name}_test") {
                    sources(sourceSets["testMod"])
                }
            }
        }
    } }
    //@formatter:on

    dependencies {
        annotationProcessor(group = "org.spongepowered", name = "mixin", version = "0.8.5", classifier = "processor")
    }
}

tasks {
    jar {
        archiveClassifier.set("slim")
        
        manifest {
            attributes(
                "Specification-Title" to "fabric-api",
                "Specification-Vendor" to "FabricMC",
                "Specification-Version" to "1",
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version,
                "Implementation-Vendor" to "Sinytra",
                "Implementation-Timestamp" to LocalDateTime.now(),
                "FMLModType" to "LIBRARY"
            )
        }
    }
    
    this.jarJar {
        project.subprojects.forEach { subproject ->
            dependsOn(subproject.tasks.jarJar, subproject.tasks.jar)
        }
        archiveClassifier.set("")
    }
}

dependencies {
    project.subprojects.forEach { subproject ->
        include(api(project(":${subproject.name}")) as ModuleDependency)
    }
}

publishing {
    publications { 
        named<MavenPublication>("mavenJava") {
            jarJar.component(this)
        }
    }
}

open class ConvertSRGTask : DefaultTask() {
    @get:InputFile
    val inputYarnMappings: RegularFileProperty = project.objects.fileProperty()

    @get:InputFile
    val inputSrgMappings: RegularFileProperty = project.objects.fileProperty()

    @get:Optional
    @get:InputFile
    val inputMcpMappings: RegularFileProperty = project.objects.fileProperty()

    @get:OutputFile
    val outputFile: RegularFileProperty = project.objects.fileProperty().convention(project.layout.buildDirectory.file("$name/output.tsrg"))

    @TaskAction
    fun execute() {
        val yarnMappings = ZipFile(inputYarnMappings.asFile.get()).use { zip ->
            val inputStream = zip.getInputStream(zip.getEntry("mappings/mappings.tiny"))
            INamedMappingFile.load(inputStream)
        }
        val obfToIntermediary = yarnMappings.getMap("official", "intermediary")
        val obfToSrg = load(inputSrgMappings.asFile.get())

        val intermediaryToSrg = obfToIntermediary.reverse().chain(obfToSrg).let { map ->
            if (inputMcpMappings.isPresent) {
                val srgToMcp = load(inputMcpMappings.asFile.get())
                map.chain(srgToMcp)
            } else map
        }

        intermediaryToSrg.write(outputFile.get().asFile.toPath(), IMappingFile.Format.TSRG2, false)
    }
}

fun DependencyHandler.include(dependency: ModuleDependency) {
    dependency.isTransitive = false
    val nextMajor = dependency.version!!.substringBefore('.').toInt() + 1
    jarJar(dependency) {
        jarJar.ranged(dependency, "[${dependency.version}, $nextMajor.0)")
    }
}

// Adapted from https://gist.github.com/pupnewfster/6c21401789ca6d74f9892be8c1c505c9
fun RepositoryHandler.exclusiveRepo(location: String, vararg groups: String) {
    exclusiveRepo(location) {
        for (group in groups) {
            includeGroup(group)
        }
    }
}

fun RepositoryHandler.exclusiveRepo(location: String, config: Action<InclusiveRepositoryContentDescriptor>) {
    exclusiveContent {
        forRepositories(maven { url = uri(location) }, fg.repository)
        filter(config)
    }
}
