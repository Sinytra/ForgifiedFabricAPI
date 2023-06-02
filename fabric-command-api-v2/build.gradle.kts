val withClientSourceSet: () -> Unit by extra
val withTestMod: () -> Unit by extra
val withGameTest: (String) -> Unit by extra

withClientSourceSet()
withTestMod()
withGameTest("fabric_command_api_v2_testmod")

mixin {
    config("fabric-command-api-v2.mixins.json")
    config("fabric-command-api-v2.client.mixins.json")
}

dependencies {
    referenceApi(group = "net.fabricmc.fabric-api", name = project.name, version = "2.2.8+504944c8f4")

    api(project(":fabric-api-base"))
    "testModImplementation"(project(":fabric-lifecycle-events-v1"))
}

tasks.configureEach { 
    if (name == "runClientTest" || name == "runGameTestServer") {
        dependsOn(configurations["testModRuntimeClasspath"])
    }
}