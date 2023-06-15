val withClientSourceSet: () -> Unit by extra
val withTestMod: () -> Unit by extra

withClientSourceSet()
withTestMod()

mixin {
    add(sourceSets.main.get(), "fabric-rendering-v1-refmap.json")
    config("fabric-rendering-v1.mixins.json")
}

dependencies {
    referenceApi(group = "net.fabricmc.fabric-api", name = project.name, version = "2.1.3+504944c8f4")
    
    api(project(":fabric-api-base"))
}
