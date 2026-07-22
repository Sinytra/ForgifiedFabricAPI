plugins {
    java
    `java-base`
    `java-library`
    `kotlin-dsl`
}

repositories {
    // The org.jetbrains.kotlin.jvm plugin requires a repository
    // where to download the Kotlin compiler dependencies from.
    mavenCentral()
    maven {
        name = "FabricMC"
        url = uri("https://maven.fabricmc.net")
    }
    maven {
        name = "Mojank"
        url = uri("https://libraries.minecraft.net")
    }
    maven {
        name = "NeoForged"
        url = uri("https://maven.neoforged.net/releases")
    }
    maven {
        name = "Architectury"
        url = uri("https://maven.architectury.dev")
    }
    gradlePluginPortal()
}

dependencies {
    implementation("net.neoforged:moddev-gradle:2.0.142")

    implementation("net.fabricmc:fabric-loader:0.15.10")
    implementation("net.fabricmc:class-tweaker:0.3.0-beta.2")

    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.moandjiezana.toml:toml4j:0.7.2")
    implementation("dev.architectury:at:1.0.1")

    implementation("commons-codec:commons-codec:1.17.0")
    
    implementation("org.eclipse.jgit:org.eclipse.jgit:6.9.0.202403050737-r")
}
