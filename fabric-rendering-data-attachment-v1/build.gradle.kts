mixin {
    add(sourceSets.main.get(), "fabric-rendering-data-attachment-v1-refmap.json")
    config("fabric-rendering-data-attachment-v1.mixins.json")
}

dependencies {
    referenceApi(group = "net.fabricmc.fabric-api", name = project.name, version = "0.3.30+afca2f3ef4")
}
