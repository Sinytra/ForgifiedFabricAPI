val withClientSourceSet: () -> Unit by extra
val withTestMod: () -> Unit by extra

withClientSourceSet()
withTestMod()

mixin {
    config("fabric-blockrenderlayer-v1.mixins.json")
}

dependencies {
    referenceApi(group = "net.fabricmc.fabric-api", name = project.name, version = "1.1.36+c2e6f674f4")

    "testModImplementation"(project(":fabric-lifecycle-events-v1"))
}
