val withClientSourceSet: () -> Unit by extra
val withTestMod: () -> Unit by extra

withClientSourceSet()
withTestMod()

mixin {
    add(sourceSets.main.get(), "fabric-screen-handler-api-v1-refmap.json")
    config("fabric-screen-handler-api-v1.mixins.json")
}

dependencies {
    referenceApi(group = "net.fabricmc.fabric-api", name = project.name, version = "1.3.22+504944c8f4")
    
    api(project(":fabric-api-base"))
    "testModImplementation"(project(":fabric-object-builder-api-v1"))
}
