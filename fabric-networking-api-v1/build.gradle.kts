val withClientSourceSet: () -> Unit by extra
val withTestMod: () -> Unit by extra

withClientSourceSet()
withTestMod()

mixin {
    add(sourceSets.main.get(), "fabric-networking-api-v1-refmap.json")
    config("fabric-networking-api-v1.mixins.json")
    config("fabric-networking-api-v1.client.mixins.json")
}

//minecraft.runs {
//    "clientTest" {
//        property("fabric-networking-api-v1.loginDelayTest", "true")
//    }
//}

dependencies {
    referenceApi(group = "net.fabricmc.fabric-api", name = project.name, version = "1.3.3+504944c8f4")
    
    api(project(":fabric-api-base"))
    "testModImplementation"(project(":fabric-command-api-v2"))
    "testModImplementation"(project(":fabric-key-binding-api-v1"))
    "testModImplementation"(project(":fabric-lifecycle-events-v1"))
}
