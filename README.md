# Forgified Fabric API

Essential hooks for modding with Fabric ported to MinecraftForge.

Fabric API is the library for essential hooks and interoperability mechanisms for mods. Examples include:

- Exposing functionality that is useful but difficult to access for many mods such as particles, biomes and dimensions
- Adding events, hooks and APIs to improve interopability between mods.
- Essential features such as registry synchronization and adding information to crash reports.
- An advanced rendering API designed for compatibility with optimization mods and graphics overhaul mods.

For support and discussion for both developers and users,
visit [GitHub Discussions](https://github.com/Sinytra/ForgifiedFabricAPI/discussions).

## About

The Forgified Fabric API (FFAPI) is a direct port of [Fabric API](https://github.com/FabricMC/fabric) to Minecraft
Forge, regularly kept up to date with the upstream repository. It is designed to make cross platform mod development
easier by allowing developers to use Fabric API as a common library to interact with the game's code on both platforms.
However, it is not an abstraction layer, and loader-specific code still needs to be handled separately for each
platform.

### Compatibility

The Forgified Fabric API has checks in place to ensure full api compatibility with Fabric API. This usually
includes `net.fabricmc.*.api` packages and other non-internal code. However, we make no guarantees for implementation
code and internal APIs, as they are subject to change at any time. For the best results, avoid using internal classes
and look for native solutions offered by your platform.

Where possible, Fabric APIs such as `FabricItem`, `ItemStorage` and `FluidStorage` are bridged to Forge's counterparts.
More information on how to properly consume bridged APIs can be found in their module's README.

### Extension methods

In some places, Fabric API uses Fabric Loader classes (such as `ModContainer`) in API method headers. These are not
available on Forge, and will result in a crash when referenced by mods at runtime. To solve this problem, FFAPI
introduces new overload methods using their respective FML counterpart (`IModInfo`).

### Design Goals

Our goal is to port as much of Fabric API to use Forge's systems as possible and keep modifications to minecraft's code
at minimum, in order to increase mod compatibility and reduce maintenance costs. On the other hand, it's important that
using Forge API doesn't come at the expense of preserving intended behavior.

## Using Forgified Fabric API to play with mods

Make sure you have installed MinecraftForge first. More information about installing Forge can be
found [here](https://github.com/minecraftforge/minecraftforge/#installing-forge).

To use Forgified Fabric API, download it from [GitHub Releases](https://github.com/Sinytra/ForgifiedFabricAPI/releases).
Standalone CurseForge and Modrinth distributions will be available soon.

The downloaded jar file should be placed in your `mods` folder.

## Using Forgified Fabric API to develop mods

To set up a Forge development environment, check out
the [MinecraftForge docs](https://docs.minecraftforge.net/en/latest/gettingstarted) and follow the instructions there.

The Forgified Fabric API is published under the `dev.su5ed.sinytra.fabric-api` group. To include the full Forgified
Fabric API with all modules in the development environment, add the following to your `dependencies` block in the gradle
buildscript:

### Groovy DSL

```groovy
repositories {
    maven {
        url "https://maven.su5ed.dev/releases"
    }
}
dependencies {
    implementation fg.deobf("dev.su5ed.sinytra.fabric-api:fabric-api:FABRIC_API_VERSION")
}
```

### Kotlin DSL

```kotlin
repositories {
    maven("https://maven.su5ed.dev/releases")
}
dependencies {
    implementation fg.deobf("dev.sinytra.fabric-api:fabric-api:FABRIC_API_VERSION")
}
```

<!--Linked to gradle documentation on properties-->
Instead of hardcoding version constants all over the build script, Gradle properties may be used to replace these
constants. Properties are defined in the `gradle.properties` file at the root of a project. More information is
available [here](https://docs.gradle.org/current/userguide/organizing_gradle_projects.html#declare_properties_in_gradle_properties_file).

## Modules

Fabric API is designed to be modular for ease of updating. This also has the advantage of splitting up the codebase into
smaller chunks.

Each module contains its own `README.md`* explaining the module's purpose and additional info on using the module.

\* The README for each module is being worked on; not every module has a README at the moment

## Porting Status

| API                                  |          State           |  Lifecycle   |
|:-------------------------------------|:------------------------:|:------------:|
| fabric-api-base                      |         ✅ Tested         |    Stable    |
| fabric-api-lookup-api-v1             |         ✅ Tested         |    Stable    |
| fabric-biome-api-v1                  |         ✅ Tested         | Experimental |
| fabric-block-api-v1                  |         ✅ Tested         |    Stable    |
| fabric-blockrenderlayer-v1           |         ✅ Tested         |    Stable    |
| fabric-client-tags-api-v1            |         ✅ Tested         |    Stable    |
| fabric-command-api-v2                |         ✅ Tested         |    Stable    |
| fabric-content-registries-v0         |         ✅ Tested         |    Stable    |
| fabric-convention-tags-v1            |         ✅ Tested         |    Stable    |
| fabric-crash-report-info-v1          | 🚧 Not Planned [[1]](#1) |    Stable    |
| fabric-data-generation-api-v1        |         ✅ Tested         |    Stable    |
| fabric-dimensions-v1                 |         ✅ Tested         |    Stable    |
| fabric-entity-events-v1              |         ✅ Tested         |    Stable    |
| fabric-events-interaction-v0         |         ✅ Tested         |    Stable    |
| fabric-game-rule-api-v1              |         ✅ Tested         |    Stable    |
| fabric-gametest-api-v1               | 🚧 Not Planned [[2]](#2) |    Stable    |
| fabric-item-api-v1                   |         ✅ Tested         |    Stable    |
| fabric-item-group-api-v1             |         ✅ Tested         |    Stable    |
| fabric-key-binding-api-v1            |         ✅ Tested         |    Stable    |
| fabric-lifecycle-events-v1           |         ✅ Tested         |    Stable    |
| fabric-loot-api-v2                   |         ✅ Tested         |    Stable    |
| fabric-message-api-v1                |         ✅ Tested         | Experimental |
| fabric-mining-level-api-v1           |         ✅ Tested         |    Stable    |
| fabric-model-loading-api-v1          |         ✅ Tested         |    Stable    |
| fabric-networking-api-v1             |         ✅ Tested         |    Stable    |
| fabric-object-builder-api-v1         |         ✅ Tested         |    Stable    |
| fabric-particles-v1                  |         ✅ Tested         |    Stable    |
| fabric-recipe-api-v1                 |         ✅ Tested         |    Stable    |
| fabric-registry-sync-v0              |         ✅ Tested         |    Stable    |
| fabric-renderer-api-v1               |         ✅ Tested         |    Stable    |
| fabric-renderer-indigo               |         ✅ Tested         |    Stable    |
| fabric-rendering-data-attachment-v1  |         ✅ Tested         |    Stable    |
| fabric-rendering-fluids-v1           |         ✅ Tested         |    Stable    |
| fabric-rendering-v1                  |         ✅ Tested         |    Stable    |
| fabric-resource-conditions-api-v1    |         ✅ Tested         | Experimental |
| fabric-resource-loader-v0            |         ✅ Tested         |    Stable    |
| fabric-screen-api-v1                 |         ✅ Tested         |    Stable    |
| fabric-screen-handler-api-v1         |         ✅ Tested         |    Stable    |
| fabric-sound-api-v1                  |         ✅ Tested         |    Stable    |
| fabric-transfer-api-v1               |         ✅ Tested         | Experimental |
| fabric-transitive-access-wideners-v1 |         ✅ Tested         |    Stable    |
| fabric-command-api-v1                |        ⚠️ Missing        |  Deprecated  |
| fabric-commands-v0                   |        ⚠️ Missing        |  Deprecated  |
| fabric-containers-v0                 |        ⚠️ Missing        |  Deprecated  |
| fabric-events-lifecycle-v0           |        ⚠️ Missing        |  Deprecated  |
| fabric-keybindings-v0                |        ⚠️ Missing        |  Deprecated  |
| fabric-loot-tables-v1                |        ⚠️ Missing        |  Deprecated  |
| fabric-models-v0                     |         ✅ Tested         |    Stable    |
| fabric-networking-v0                 |        ⚠️ Missing        |  Deprecated  |
| fabric-renderer-registries-v1        |        ⚠️ Missing        |  Deprecated  |
| fabric-rendering-v0                  |        ⚠️ Missing        |  Deprecated  |
