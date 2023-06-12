val withTestMod: () -> Unit by extra

withTestMod()

mixin {
    add(sourceSets.main.get(), "fabric-item-group-api-v1-refmap.json")
    config("fabric-item-group-api-v1.mixins.json")
}

dependencies {
    referenceApi(group = "net.fabricmc.fabric-api", name = project.name, version = "3.0.7+043f9acff4")
    
    api(project(":fabric-api-base"))
}
