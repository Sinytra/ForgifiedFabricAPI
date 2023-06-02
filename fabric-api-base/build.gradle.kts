val withTestMod: () -> Unit by extra
val withGameTest: (String) -> Unit by extra

withTestMod()
withGameTest("fabric_api_base_testmod")

minecraft.runs {
    setOf("client", "server").forEach { side ->
        create("${side}AutoTest") {
            parent(minecraft.runs[side])
            property("fabric.autoTest", "true")
            workingDirectory = project.file("run_test").canonicalPath
            mods {
                create("${project.name}_test") {
                    sources(sourceSets["testMod"])
                }
            }
        }
    }
}

dependencies {
    referenceApi(group = "net.fabricmc.fabric-api", name = project.name, version = "0.4.26+1e9487d2f4")

    "testModImplementation"(project(":fabric-lifecycle-events-v1"))
    "testModImplementation"(project(":fabric-command-api-v2"))
}

tasks.configureEach {
    if (name.startsWith("run") && name.contains("Test")) {
        dependsOn(configurations["testModRuntimeClasspath"])
    }
}