val withTestMod: () -> Unit by extra

withTestMod()

mixin {
    add(sourceSets.main.get(), "fabric-loot-api-v2-refmap.json")
    config("fabric-loot-api-v2.mixins.json")
}

dependencies {
    referenceApi(group = "net.fabricmc.fabric-api", name = project.name, version = "1.1.29+75e98211f4")
    
    api(project(":fabric-api-base"))
}