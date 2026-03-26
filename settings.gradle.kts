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
