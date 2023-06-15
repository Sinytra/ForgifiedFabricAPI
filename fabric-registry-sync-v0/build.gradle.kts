val withClientSourceSet: () -> Unit by extra
val withTestMod: () -> Unit by extra

withClientSourceSet()
//withTestMod()

mixin {
    add(sourceSets.main.get(), "fabric-registry-sync-v0-refmap.json")
    config("fabric-registry-sync-v0.mixins.json")
    config("fabric-registry-sync-v0.client.mixins.json")
}

dependencies {
    referenceApi(group = "net.fabricmc.fabric-api", name = project.name, version = "2.2.2+504944c8f4")
    
    api(project(":fabric-api-base"))
    implementation(project(":fabric-networking-api-v1"))
}
