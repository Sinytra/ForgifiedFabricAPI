import net.minecraftforge.gradle.common.util.RunConfig
import java.time.LocalDateTime

plugins {
    `java-library`
    `maven-publish`
    id("net.minecraftforge.gradle") version "[6.0,6.2)"
    id("org.parchmentmc.librarian.forgegradle") version "1.+"
    id("org.spongepowered.mixin") version "0.7.+" apply false
    id("dev.su5ed.yarndeobf") version "0.1.+" apply false
}

group = "dev.su5ed.sinytra.fabric-api"
version = "1.0"

val versionMc: String by project

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    withSourcesJar()
}

println("Java: ${System.getProperty("java.version")}, JVM: ${System.getProperty("java.vm.version")} (${System.getProperty("java.vendor")}), Arch: ${System.getProperty("os.arch")}")
minecraft {
    mappings("parchment", "1.19.3-2023.03.12-$versionMc")

//     accessTransformer(file("src/mod/resources/META-INF/accesstransformer.cfg"))

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

        create("data") {
            config(this)
            args(
                "--mod", "fabric_api",
                "--all",
                "--output", file("src/generated/resources/"),
                "--existing", file("src/main/resources/")
            )
        }
    }
}

sourceSets.main {
    resources {
        srcDir("src/generated/resources")
    }
}

repositories {
    maven {
        name = "Fabric"
        url = uri("https://maven.fabricmc.net")
    }
}

dependencies {
    minecraft(group = "net.minecraftforge", name = "forge", version = "$versionMc-45.0.66")
}

tasks {
    jar {
        finalizedBy("reobfJar")

        manifest {
            attributes(
                "Specification-Title" to "fabric-api",
                "Specification-Vendor" to "FabricMC",
                "Specification-Version" to "1",
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version,
                "Implementation-Vendor" to "Sinytra",
                "Implementation-Timestamp" to LocalDateTime.now()
            )
        }
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "net.minecraftforge.gradle")
    apply(plugin = "org.parchmentmc.librarian.forgegradle")
    apply(plugin = "org.spongepowered.mixin")

    group = "dev.su5ed.sinytra.fabric-api"
    version = "1.0"

    fun applyClientSourceSet() {
        sourceSets.main {
            java {
                srcDir("src/client/java")
            }
            resources {
                srcDir("src/client/resources")
            }
        }
    }

    fun applyTestMod() {
        val testMod: SourceSet by sourceSets.creating
        configurations.named(testMod.implementationConfigurationName) {
            extendsFrom(configurations.implementation.get())
        }
        dependencies.add(testMod.implementationConfigurationName, sourceSets.main.get().output)
        minecraft.runs.create("clientTest") {
            parent(minecraft.runs["client"])
            workingDirectory = project.file("run_test").canonicalPath
            mods {
                create("${project.name}_test") {
                    sources(testMod)
                }
            }
        }
    }

    @Suppress("UNUSED_VARIABLE") val withClientSourceSet: () -> Unit by extra { ::applyClientSourceSet }
    @Suppress("UNUSED_VARIABLE") val withTestMod: () -> Unit by extra { ::applyTestMod }

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(17))
        withSourcesJar()
    }

    minecraft {
        mappings("parchment", "1.19.3-2023.03.12-$versionMc")

        val atFile = file("src/main/resources/META-INF/accesstransformer.cfg")
        if (atFile.exists()) accessTransformer(atFile)

        runs {
            val runConfigurator = Action<RunConfig> {
                property("forge.logging.console.level", "debug")
                property("forge.logging.markers", "REGISTRIES,SCAN,FMLHANDSHAKE")
                property("mixin.debug", "true")
                workingDirectory = project.file("run").canonicalPath

                mods {
                    create(project.name) {
                        sources(sourceSets.main.get())
                    }
                }
            }

            create("client", runConfigurator)
            create("server", runConfigurator)
        }
    }

    repositories {
        maven {
            name = "Fabric"
            url = uri("https://maven.fabricmc.net")
        }
    }

    dependencies {
        minecraft(group = "net.minecraftforge", name = "forge", version = "$versionMc-45.0.66")

        implementation(group = "net.fabricmc", name = "fabric-loader", version = "0.14.19")
        annotationProcessor(group = "org.spongepowered", name = "mixin", version = "0.8.5", classifier = "processor")
    }
}
