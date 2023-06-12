val withClientSourceSet: () -> Unit by extra
val withTestMod: () -> Unit by extra
val withGameTest: (String) -> Unit by extra

withClientSourceSet()
withTestMod()
withGameTest("fabric_item_api_v1_testmod")

mixin {
    add(sourceSets.main.get(), "fabric-item-api-v1-refmap.json")
    config("fabric-item-api-v1.mixins.json")
}

dependencies {
    referenceApi(group = "net.fabricmc.fabric-api", name = project.name, version = "2.1.19+504944c8f4")
    
    api(project(":fabric-api-base"))
    "testModImplementation"(project(":fabric-content-registries-v0"))
}
