val withTestMod: () -> Unit by extra

withTestMod()

mixin {
    config("fabric-biome-api-v1.mixins.json")
}

dependencies {
    referenceApi(group = "net.fabricmc.fabric-api", name = project.name, version = "13.0.8+348a9c64f4")
}
