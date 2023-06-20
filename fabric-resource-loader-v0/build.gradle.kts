val withClientSourceSet: () -> Unit by extra
val withTestMod: () -> Unit by extra
val shade: Configuration by configurations.creating

withClientSourceSet()
withTestMod()

configurations.minecraftLibrary {
    extendsFrom(shade)
}

mixin {
    add(sourceSets.main.get(), "fabric-resource-loader-v0-refmap.json")
    config("fabric-resource-loader-v0.mixins.json")
    config("fabric-resource-loader-v0.client.mixins.json")
}

dependencies {
    referenceApi(group = "net.fabricmc.fabric-api", name = project.name, version = "0.11.4+5ade3c38f4")

    shade(group = "net.fabricmc", name = "fabric-loader", version = "0.14.21") {
        isTransitive = false
    }
    "testModImplementation"(project(":fabric-lifecycle-events-v1"))
}

tasks.jar {
    // Hand pick classes that are used by the api for minimum overhead
    from(zipTree(provider { shade.singleFile })) {
        include("net/fabricmc/loader/api/metadata/ModMetadata.class")
        include("net/fabricmc/loader/api/ModContainer.class")
    }
}
