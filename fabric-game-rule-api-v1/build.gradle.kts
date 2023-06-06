val withClientSourceSet: () -> Unit by extra
val withTestMod: () -> Unit by extra

withClientSourceSet()
withTestMod()

mixin {
    config("fabric-game-rule-api-v1.client.mixins.json")
    config("fabric-game-rule-api-v1.mixins.json")
}

dependencies {
    referenceApi(group = "net.fabricmc.fabric-api", name = project.name, version = "1.0.34+a1ccd7bff4")

    "testModImplementation"(project(":fabric-lifecycle-events-v1"))
}

// Disabled due to ongoing issue https://github.com/SpongePowered/Mixin/issues/560
configurations.annotationProcessor {
    dependencies.clear()
}
