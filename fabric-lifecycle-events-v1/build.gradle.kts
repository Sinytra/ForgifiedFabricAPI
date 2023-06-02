val withClientSourceSet: () -> Unit by extra

withClientSourceSet()

mixin {
    config("fabric-lifecycle-events-v1.mixins.json")
    config("fabric-lifecycle-events-v1.client.mixins.json")
}

dependencies {
    referenceApi(group = "net.fabricmc.fabric-api", name = project.name, version = "2.2.17+1e9487d2f4")

    api(project(":fabric-api-base"))
}