val withTestMod: () -> Unit by extra
val withGameTest: (String) -> Unit by extra

withTestMod()
withGameTest("fabric_block_api_v1_testmod")

mixin {
    add(sourceSets.main.get(), "fabric-block-api-v1-refmap.json")
    config("fabric-block-api-v1.mixins.json")
}

dependencies {
    referenceApi(group = "net.fabricmc.fabric-api", name = project.name, version = "1.0.7+e022e5d1f4")
}
