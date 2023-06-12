val withClientSourceSet: () -> Unit by extra
val withTestMod: () -> Unit by extra

withClientSourceSet()
withTestMod()

mixin {
    add(sourceSets.main.get(), "fabric-lifecycle-events-v1-refmap.json")
    config("fabric-lifecycle-events-v1.mixins.json")
    config("fabric-lifecycle-events-v1.client.mixins.json")
}

minecraft.runs {
    "clientTest" {
        property("fabric-lifecycle-events-testmod.printClientBlockEntityMessages", "true")
        property("fabric-lifecycle-events-testmod.printClientEntityMessages", "true")
        property("fabric-lifecycle-events-testmod.printServerBlockEntityMessages", "true")
        property("fabric-lifecycle-events-testmod.printServerEntityMessages", "true")
    }
}

dependencies {
    referenceApi(group = "net.fabricmc.fabric-api", name = project.name, version = "2.2.17+1e9487d2f4")

    api(project(":fabric-api-base"))
}