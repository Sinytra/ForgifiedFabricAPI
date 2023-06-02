val withTestMod: () -> Unit by extra

withTestMod()

dependencies { 
    referenceApi(group = "net.fabricmc.fabric-api", name = project.name, version = "0.4.26+1e9487d2f4")
    
    "testModImplementation"(project(":fabric-lifecycle-events-v1"))
    "testModImplementation"(project(":fabric-command-api-v2"))
}