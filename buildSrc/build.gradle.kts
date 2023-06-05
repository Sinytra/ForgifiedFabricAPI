plugins {
    `kotlin-dsl`
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