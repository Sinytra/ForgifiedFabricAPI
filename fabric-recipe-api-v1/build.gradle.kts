val withClientSourceSet: () -> Unit by extra
val withTestMod: () -> Unit by extra
val withGameTest: (String) -> Unit by extra

withClientSourceSet()
withTestMod()
withGameTest("fabric_recipe_api_v1_testmod")

mixin {
    config("fabric-recipe-api-v1.mixins.json")
}

dependencies {
    referenceApi(group = "net.fabricmc.fabric-api", name = project.name, version = "1.0.10+a1ccd7bff4")
    
    implementation(project(":fabric-networking-api-v1"))
}
