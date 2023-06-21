val withClientSourceSet: () -> Unit by extra
val withTestMod: () -> Unit by extra

withClientSourceSet()
withTestMod()

mixin {
    add(sourceSets.main.get(), "fabric-rendering-fluids-v1-refmap.json")
    config("fabric-rendering-fluids-v1.mixins.json")
}

dependencies {
    referenceApi(group = "net.fabricmc.fabric-api", name = project.name, version = "3.0.23+504944c8f4")
}
