val withTestMod: () -> Unit by extra

withTestMod()

dependencies {
    referenceApi(group = "net.fabricmc.fabric-api", name = project.name, version = "2.1.47+7f87f8faf4")
    
    "testModImplementation"(project(":fabric-lifecycle-events-v1"))
    "testModImplementation"(project(":fabric-command-api-v2"))
}
