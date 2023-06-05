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
include("fabric-client-tags-api-v1")
include("fabric-content-registries-v0")
include("fabric-convention-tags-v1")
include("fabric-command-api-v2")
include("fabric-dimensions-v1")
include("fabric-entity-events-v1")
include("fabric-events-interaction-v0")
include("fabric-lifecycle-events-v1")
include("fabric-object-builder-api-v1")