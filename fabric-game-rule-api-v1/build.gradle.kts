val withClientSourceSet: () -> Unit by extra
val withTestMod: () -> Unit by extra

withClientSourceSet()
withTestMod()

mixin {
    add(sourceSets.main.get(), "fabric-game-rule-api-v1-refmap.json")
    config("fabric-game-rule-api-v1.client.mixins.json")
    config("fabric-game-rule-api-v1.mixins.json")
}

dependencies {
    referenceApi(group = "net.fabricmc.fabric-api", name = project.name, version = "1.0.34+a1ccd7bff4")

    "testModImplementation"(project(":fabric-lifecycle-events-v1"))
}
