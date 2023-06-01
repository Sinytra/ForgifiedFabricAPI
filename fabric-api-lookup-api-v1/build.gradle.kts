val withTestMod: () -> Unit by extra

withTestMod()

mixin {
    config("fabric-api-lookup-api-v1.mixins.json")
}