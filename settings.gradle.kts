pluginManagement {
    repositories {
        gradlePluginPortal()
        maven {
            name = "MinecraftForge"
            url = uri("https://maven.minecraftforge.net/")
        }
        maven {
            name = "ParchmentMC"
            url = uri("https://maven.parchmentmc.org")
        }
        maven {
            name = "Sponge Snapshots"
            url = uri("https://repo.spongepowered.org/repository/maven-public/")
        }
        maven {
            name = "Su5eD"
            url = uri("https://maven.su5ed.dev/releases")
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

rootProject.name = "ForgifiedFabricAPI"

include("fabric-api-base")
include("fabric-api-lookup-api-v1")
include("fabric-biome-api-v1")
include("fabric-block-api-v1")
include("fabric-blockrenderlayer-v1")
include("fabric-command-api-v2")
include("fabric-lifecycle-events-v1")
include("fabric-object-builder-api-v1")