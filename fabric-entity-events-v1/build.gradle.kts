val withClientSourceSet: () -> Unit by extra
val withTestMod: () -> Unit by extra

withClientSourceSet()
withTestMod()

mixin {
    add(sourceSets.main.get(), "fabric-entity-events-v1-refmap.json")
    config("fabric-entity-events-v1.client.mixins.json")
    config("fabric-entity-events-v1.mixins.json")
}

dependencies {
    referenceApi(group = "net.fabricmc.fabric-api", name = project.name, version = "1.5.15+504944c8f4")
    
    api(project(":fabric-api-base"))
    "testModImplementation"(project(":fabric-command-api-v2"))
}
