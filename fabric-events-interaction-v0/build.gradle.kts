val withClientSourceSet: () -> Unit by extra
val withTestMod: () -> Unit by extra
val withGameTest: (String) -> Unit by extra

withClientSourceSet()
withTestMod()
withGameTest("fabric_events_interaction_v0_testmod")

mixin {
    add(sourceSets.main.get(), "fabric-events-interaction-v0-refmap.json")
    config("fabric-events-interaction-v0.client.mixins.json")
    config("fabric-events-interaction-v0.mixins.json")
}

dependencies {
    referenceApi(group = "net.fabricmc.fabric-api", name = project.name, version = "0.5.1+76ba65ebf4")
    
    api(project(":fabric-api-base"))
}
