val withClientSourceSet: () -> Unit by extra
val withTestMod: () -> Unit by extra

withClientSourceSet()
withTestMod()

mixin {
    add(sourceSets.main.get(), "fabric-models-v0-refmap.json")
    config("fabric-models-v0.mixins.json")
}

dependencies {
    referenceApi(group = "net.fabricmc.fabric-api", name = project.name, version = "0.3.32+504944c8f4")
    
    "testModImplementation"(project(":fabric-resource-loader-v0"))
    "testModImplementation"(project(":fabric-rendering-v1"))
}
