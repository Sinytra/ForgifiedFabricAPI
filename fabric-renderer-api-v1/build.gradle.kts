val withClientSourceSet: () -> Unit by extra
val withTestMod: () -> Unit by extra

withClientSourceSet()
//withTestMod()

mixin {
    add(sourceSets.main.get(), "fabric-renderer-api-v1-refmap.json")
    config("fabric-renderer-api-v1.mixins.json")
    config("fabric-renderer-api-v1.debughud.mixins.json")
}

dependencies {
    referenceApi(group = "net.fabricmc.fabric-api", name = project.name, version = "2.2.7+ebc93ff3f4")
    
    api(project(":fabric-api-base"))
}
