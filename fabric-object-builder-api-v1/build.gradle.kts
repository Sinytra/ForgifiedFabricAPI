val withClientSourceSet: () -> Unit by extra
val withTestMod: () -> Unit by extra
val withGameTest: (String) -> Unit by extra

withClientSourceSet()
withTestMod()
withGameTest("fabric_object_builder_api_v1_testmod")

mixin {
    config("fabric-object-builder-v1.mixins.json")
}

dependencies { 
    referenceApi(group = "net.fabricmc.fabric-api", name = project.name, version = "7.0.5+504944c8f4")

    implementation(project(":fabric-api-base"))
    "testModImplementation"(project(":fabric-command-api-v2"))
}