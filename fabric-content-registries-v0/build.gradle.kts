val withTestMod: () -> Unit by extra
val withGameTest: (String) -> Unit by extra

withTestMod()
withGameTest("fabric_content_registries_v0_testmod")

mixin {
    config("fabric-content-registries-v0.mixins.json")
}

dependencies {
    referenceApi(group = "net.fabricmc.fabric-api", name = project.name, version = "3.5.11+ae0966baf4")

    api(project(":fabric-api-base"))
}

tasks.devJar {
    from(zipTree(project(":fabric-api-base").tasks.named<Jar>("devJar").flatMap { it.archiveFile })) {
        include("net/fabricmc/fabric/api/util/**")
    }
}
