val withClientSourceSet: () -> Unit by extra

withClientSourceSet()

mixin {
    config("fabric-blockrenderlayer-v1.mixins.json")
}

dependencies {
    referenceApi(group = "net.fabricmc.fabric-api", name = project.name, version = "1.1.36+c2e6f674f4")
}
