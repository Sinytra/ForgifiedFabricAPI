val withClientSourceSet: () -> Unit by extra
val withTestMod: () -> Unit by extra

withClientSourceSet()
withTestMod()

mixin {
    add(sourceSets.main.get(), "fabric-resource-loader-v0-refmap.json")
    config("fabric-resource-loader-v0.mixins.json")
    config("fabric-resource-loader-v0.client.mixins.json")
}

// TODO Get from Su5eD Maven
repositories {
    mavenLocal()
}

dependencies {
    referenceApi(group = "net.fabricmc.fabric-api", name = project.name, version = "0.11.4+5ade3c38f4")
    
    // TODO Bundle ModContainer class for API dep
    minecraftLibrary("dev.su5ed.sinytra:fabric-loader:1.0")
    "testModImplementation"(project(":fabric-lifecycle-events-v1"))
}
