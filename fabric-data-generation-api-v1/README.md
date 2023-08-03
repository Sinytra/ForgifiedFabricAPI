# Fabric Data Generation API (v1)

## Forgified Fabric API Notes

Fabric uses a custom `DataGeneratorEntrypoint` entrypoint to allow mods to hook into the datagen process. Since loader
entrypoints don't exist in Forge, FFAPI provides a `FabricDataGenerator#create` extension method to easily create
a `FabricDataGenerator` (which would normally be provided to you via the entrypoint) inside Forge's `GatherDataEvent`. If
you need to generate any dynamic registry entries, create a new `RegistryBuilder` yourself and pass it
into `FabricDataGenerator#create`.
