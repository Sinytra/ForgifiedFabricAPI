val withClientSourceSet: () -> Unit by extra
val withTestMod: () -> Unit by extra

withClientSourceSet()
withTestMod()

mixin {
    add(sourceSets.main.get(), "fabric-message-api-v1-refmap.json")
    config("fabric-message-api-v1.mixins.json")
    config("fabric-message-api-v1.client.mixins.json")
}

dependencies {
    referenceApi(group = "net.fabricmc.fabric-api", name = project.name, version = "5.1.3+504944c8f4")
    
    api(project(":fabric-api-base"))
    "testModImplementation"(project(":fabric-command-api-v2"))
}
