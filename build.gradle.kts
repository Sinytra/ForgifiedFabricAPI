import java.time.LocalDateTime
import net.minecraftforge.gradle.common.util.MavenArtifactDownloader
import net.minecraftforge.gradle.common.util.RunConfig
import net.minecraftforge.gradle.mcp.tasks.GenerateSRG

plugins {
    java
    `maven-publish`
    id("net.minecraftforge.gradle") version "5.1.+"
    id("com.github.johnrengelman.shadow") version "7.1.2" apply false
    id("org.spongepowered.mixin") version "0.7.+"
    id("dev.su5ed.yarndeobf") version "0.1.+"
}

version = "1.0"
group = "dev.su5ed.sinytra.fabric-api"

val versionMc: String by project

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    withSourcesJar()
}

println("Java: ${System.getProperty("java.version")}, JVM: ${System.getProperty("java.vm.version")} (${System.getProperty("java.vendor")}), Arch: ${System.getProperty("os.arch")}")
minecraft {
    mappings("official", versionMc)

//     accessTransformer(file("src/mod/resources/META-INF/accesstransformer.cfg"))

    runs {
        val config = Action<RunConfig> {
            property("forge.logging.console.level", "debug")
            property("forge.logging.markers", "REGISTRIES,SCAN,FMLHANDSHAKE")
            property("mixin.debug", "true")
            workingDirectory = project.file("run").canonicalPath
            // Don't exit the daemon when the game closes
            forceExit = false

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
    
}

dependencies {
    minecraft(group = "net.minecraftforge", name = "forge", version = "$versionMc-45.0.66")
    yarnMappings(group = "net.fabricmc", name = "yarn", version = "1.19.4+build.2")
}

tasks {
    jar {
        finalizedBy("reobfJar")

        manifest {
            attributes(
                "Specification-Title" to "examplemod",
                "Specification-Vendor" to "examplemodsareus",
                "Specification-Version" to "1", // We are version 1 of ourselves
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version,
                "Implementation-Vendor" to "examplemodsareus",
                "Implementation-Timestamp" to LocalDateTime.now()
            )
        }
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
}
