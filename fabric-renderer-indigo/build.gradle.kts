val withClientSourceSet: () -> Unit by extra
val withTestMod: () -> Unit by extra

withClientSourceSet()
withTestMod()

mixin {
    add(sourceSets.main.get(), "fabric-renderer-indigo-refmap.json")
    config("fabric-renderer-indigo.mixins.json")
}

dependencies {
    referenceApi(group = "net.fabricmc.fabric-api", name = project.name, version = "1.2.0+ebc93ff3f4")

    implementation(project(":fabric-renderer-api-v1"))
}
