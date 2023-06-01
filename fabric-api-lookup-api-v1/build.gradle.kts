val withTestMod: () -> Unit by extra

withTestMod()

mixin {
    add(sourceSets.main.get(), "fabric-api-lookup-api-v1-refmap.json")
    config("fabric-api-lookup-api-v1.mixins.json")
}

dependencies {
    implementation(project(":fabric-lifecycle-events-v1"))
    // #BlameFG: testModImplementation doesn't create proper dependency on project output,
    // doesn't build project before running the game
    implementation(project(":fabric-object-builder-api-v1"))
}
