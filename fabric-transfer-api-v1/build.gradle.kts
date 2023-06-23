val withClientSourceSet: () -> Unit by extra
val withTestMod: () -> Unit by extra
val withGameTest: (String) -> Unit by extra

withClientSourceSet()
withTestMod()
withGameTest("fabric_transfer_api_v1_testmod")

mixin {
    add(sourceSets.main.get(), "fabric-transfer-api-v1-refmap.json")
    config("fabric-transfer-api-v1.mixins.json")
}

dependencies {
    referenceApi(group = "net.fabricmc.fabric-api", name = project.name, version = "3.2.0+80d07a0af4")
    
    implementation(project(":fabric-api-base"))
    implementation(project(":fabric-api-lookup-api-v1"))
    implementation(project(":fabric-rendering-fluids-v1"))
    "testModImplementation"(project(":fabric-object-builder-api-v1"))
    "testModImplementation"(project(":fabric-rendering-v1"))
}
