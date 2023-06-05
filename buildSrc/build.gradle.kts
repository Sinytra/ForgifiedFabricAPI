plugins {
    `kotlin-dsl`
}

configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "net.minecraftforge" && requested.name == "srgutils" && requested.version == "0.5.1") {
            useVersion("0.5.3")
            because("Fixes https://github.com/MinecraftForge/ForgeGradle/issues/919")
        }
    }
}

repositories {
    mavenCentral()
    maven {
        name = "MinecraftForge"
        url = uri("https://maven.minecraftforge.net")
    }
}

dependencies { 
    implementation("net.minecraftforge:ForgeAutoRenamingTool:1.0.+")
    implementation("org.ow2.asm:asm:9.5")
    implementation("org.ow2.asm:asm-commons:9.5")
}