val withTestMod: () -> Unit by extra

withTestMod()

dependencies {
    referenceApi(group = "net.fabricmc.fabric-api", name = project.name, version = "1.5.1+fe8721bef4")
    
    "testModImplementation"(project(":fabric-lifecycle-events-v1"))
}
