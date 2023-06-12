val withTestMod: () -> Unit by extra

withTestMod()

mixin {
    add(sourceSets.main.get(), "fabric-mining-level-api-v1-refmap.json")
    config("fabric-mining-level-api-v1.mixins.json")
}

dependencies {
    referenceApi(group = "net.fabricmc.fabric-api", name = project.name, version = "2.1.41+49abcf7ef4")
    
    api(project(":fabric-api-base"))
    implementation(project(":fabric-lifecycle-events-v1"))
}
