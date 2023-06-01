val withClientSourceSet: () -> Unit by extra

withClientSourceSet()

mixin {
    config("fabric-object-builder-v1.mixins.json")
}

dependencies { 
    implementation(project(":fabric-api-base"))
}