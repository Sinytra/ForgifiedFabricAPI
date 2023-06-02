val withClientSourceSet: () -> Unit by extra

withClientSourceSet()

mixin {
    config("fabric-object-builder-v1.mixins.json")
}

dependencies { 
    referenceApi(group = "net.fabricmc.fabric-api", name = project.name, version = "7.0.5+504944c8f4")

    implementation(project(":fabric-api-base"))
}