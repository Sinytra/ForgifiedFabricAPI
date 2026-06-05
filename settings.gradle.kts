pluginManagement {
    repositories {
        gradlePluginPortal()
        maven {
            name = "Architectury"
            url = uri("https://maven.architectury.dev/")
        }
        maven {
            name = "Fabric"
            url = uri("https://maven.fabricmc.net")
        }
        maven {
            name = "NeoForged"
            url = uri("https://maven.neoforged.net/releases")
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

rootProject.name = "forgified-fabric-api"

gradle.beforeProject {
    val localPropertiesFile = rootDir.resolve("ffapi.gradle.properties")
    if (localPropertiesFile.exists()) {
        val localProperties = java.util.Properties()
        localProperties.load(localPropertiesFile.inputStream())
        localProperties.forEach { (k, v) -> if (k is String) project.extra.set(k, v) }
    }
}

include("fabric-api-bom")
include("fabric-api-catalog")

include("fabric-api-base")

//include 'fabric-api-lookup-api-v1'
//include 'fabric-biome-api-v1'
//include 'fabric-block-api-v1'
//include 'fabric-block-getter-api-v2'
//include 'fabric-client-gametest-api-v1'
include("fabric-command-api-v2")
//include 'fabric-content-registries-v0'
//include 'fabric-convention-tags-v2'
//include 'fabric-creative-tab-api-v1'
//include 'fabric-data-attachment-api-v1'
//include 'fabric-data-generation-api-v1'
//include 'fabric-debug-api-v1'
//include 'fabric-dimensions-v1'
//include 'fabric-entity-events-v1'
//include 'fabric-events-interaction-v0'
//include 'fabric-game-rule-api-v1'
include("fabric-gametest-api-v1")
//include 'fabric-item-api-v1'
//include 'fabric-key-mapping-api-v1'
include("fabric-lifecycle-events-v1")
//include 'fabric-loot-api-v3'
//include 'fabric-menu-api-v1'
//include 'fabric-message-api-v1'
// //include 'fabric-model-loading-api-v1'
//include 'fabric-networking-api-v1'
//include 'fabric-object-builder-api-v1'
//include 'fabric-particles-v1'
//include 'fabric-recipe-api-v1'
//include 'fabric-registry-sync-v0'
// //include 'fabric-renderer-api-v1'
// //include 'fabric-renderer-indigo'
//include 'fabric-rendering-fluids-v1'
//include 'fabric-rendering-v1'
include("fabric-resource-conditions-api-v1")
include("fabric-resource-loader-v1")
//include 'fabric-screen-api-v1'
//include 'fabric-serialization-api-v1'
include("fabric-sound-api-v1")
//include 'fabric-tag-api-v1'
//include 'fabric-transfer-api-v1'
//include 'fabric-transitive-access-wideners-v1'
//include 'deprecated'
//include 'deprecated:fabric-resource-loader-v0'
