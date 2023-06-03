val withTestMod: () -> Unit by extra

withTestMod()

mixin {
    add(sourceSets.main.get(), "fabric-api-lookup-api-v1-refmap.json")
    config("fabric-api-lookup-api-v1.mixins.json")
}

dependencies {
    referenceApi(group = "net.fabricmc.fabric-api", name = project.name, version = "1.6.27+504944c8f4")

    implementation(project(":fabric-lifecycle-events-v1"))
    "testModImplementation"(project(":fabric-object-builder-api-v1"))
}
