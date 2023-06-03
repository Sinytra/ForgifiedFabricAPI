val withClientSourceSet: () -> Unit by extra
val withTestMod: () -> Unit by extra

withClientSourceSet()
withTestMod()

dependencies {
    referenceApi(group = "net.fabricmc.fabric-api", name = project.name, version = "1.0.17+504944c8f4")
    
    "testModImplementation"(project(":fabric-lifecycle-events-v1"))
    "testModImplementation"(project(":fabric-convention-tags-v1"))
}
