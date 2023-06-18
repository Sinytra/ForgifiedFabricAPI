val withTestMod: () -> Unit by extra
val withGameTest: (String) -> Unit by extra

withTestMod()
withGameTest("fabric_resource_conditions_api_v1_testmod")

mixin {
    add(sourceSets.main.get(), "fabric-resource-conditions-api-v1-refmap.json")
    config("fabric-resource-conditions-api-v1.mixins.json")
}

dependencies {
    referenceApi(group = "net.fabricmc.fabric-api", name = project.name, version = "2.3.2+e6c7d4eef4")
}
