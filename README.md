<p align="center">
  <img src="https://raw.githubusercontent.com/Sinytra/.github/main/art/ffapi_banner_small.png">
</p>
<p align="center">
 <a href="https://github.com/Sinytra/ForgifiedFabricAPI/actions/workflows/build.yml"><img src="https://github.com/Sinytra/ForgifiedFabricAPI/actions/workflows/build.yml/badge.svg"></a>
  <a href="https://github.com/Sinytra/ForgifiedFabricAPI/releases/latest"><img src="https://img.shields.io/github/v/release/Sinytra/ForgifiedFabricAPI?style=flat&label=Release&include_prereleases&sort=semver"></a>
  <a href="https://www.curseforge.com/minecraft/mc-mods/forgified-fabric-api"><img src="https://cf.way2muchnoise.eu/forgified-fabric-api.svg"></a>
  <a href="https://modrinth.com/mod/forgified-fabric-api"><img src="https://img.shields.io/modrinth/dt/Aqlf1Shp?logo=modrinth&label=Modrinth&color=00AF5C"></a>
  <a href="https://discord.gg/mamk7z3TKZ"><img src="https://discordapp.com/api/guilds/1141048834177388746/widget.png?style=shield"></a>
</p>

## 📖 About

Essential hooks for modding with Fabric, now available on NeoForge!

Fabric API is the library for essential hooks and interoperability mechanisms for mods. Examples include:

- Exposing functionality that is useful but difficult to access for many mods such as particles, biomes and dimensions
- Adding events, hooks and APIs to improve interopability between mods.
- Essential features such as registry synchronization and adding information to crash reports.
- An advanced rendering API designed for compatibility with optimization mods and graphics overhaul mods.

**📘 The official documentation is available at [sinytra.org](https://sinytra.org/docs).**

The Forgified Fabric API (FFAPI) is a direct port of [Fabric API](https://github.com/FabricMC/fabric) for the NeoForge
modloader, regularly kept up to date with the upstream repository. It is designed to make cross platform mod development
easier by allowing developers to use Fabric API as a common library to interact with the game's code on both platforms.
However, it is not an abstraction layer, and loader-specific code still needs to be handled separately for each
platform.

### 💬 Join the Community

We have an official [Discord community](https://discord.gg/mamk7z3TKZ) for our projects. By joining, you can:

- Get help and technical support from our team and community members
- Keep in touch with the latest development updates and community events
- Engage in the project's development and collaborate with our team
- ... and just hang out with the rest of our community.

### Compatibility

The Forgified Fabric API has checks in place to ensure full api compatibility with Fabric API. This usually
includes `net.fabricmc.*.api` packages and other non-internal code. However, we make no guarantees for implementation
code and internal APIs, as they are subject to change at any time. For the best results, avoid using internal classes
and look for native solutions offered by your platform.

### Design Goals

Our goal is to port as much of Fabric API to use NeoForge's systems as possible and keep modifications to minecraft's
code at minimum, in order to increase mod compatibility and reduce maintenance costs. On the other hand, it's important
that using NeoForge's API doesn't come at the expense of preserving intended behavior.

## 📋 Using Forgified Fabric API to play with mods

Make sure you have installed NeoForge first. More information about installing NeoForge can be
found [here](https://neoforged.net/).

The Forgified Fabric API is available for download on the following platforms:

- [GitHub Releases](https://github.com/Sinytra/ForgifiedFabricAPI/releases)
- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/forgified-fabric-api)
- [Modrinth](https://modrinth.com/mod/forgified-fabric-api)

The downloaded jar file should be placed in your `mods` folder.

## 🛠️ Using Forgified Fabric API to develop mods

To set up a NeoForge development environment, please read the [NeoForge docs](https://docs.neoforged.net/) and follow the instructions there.

The Forgified Fabric API is published under the `org.sinytra.forgified-fabric-api` group. To include the full Forgified
Fabric API with all modules in the development environment, add the following to your `dependencies` block in the gradle
buildscript:

```groovy
repositories {
    maven {
        url "https://maven.sinytra.org"
    }
}
dependencies {
    implementation "org.sinytra.forgified-fabric-api:forgified-fabric-api:FABRIC_API_VERSION"
}
```

<!--Linked to gradle documentation on properties-->
Instead of hardcoding version constants all over the build script, Gradle properties may be used to replace these
constants. Properties are defined in the `gradle.properties` file at the root of a project. More information is
available [here](https://docs.gradle.org/current/userguide/organizing_gradle_projects.html#declare_properties_in_gradle_properties_file).

### Mod metadata properties

FFAPI's data generation and gametest frameworks rely on discovering consumers using Fabric mod entrypoint information,
which is not available on NeoForge. Instead, we provide a special mod metadata property that will be used in its place.

The supported entrypoints are `fabric-datagen`, `fabric-gametest` and `fabric-client-gametest`. They can be defined
in your `neoforged.mods.toml` file as follows (replacing `examplemod` with your own mod id):

```toml
[modproperties.examplemod."fabric:entrypoints"]
fabric-datagen = ["com.example.examplemod.data.DataGenEntrypoint"]
fabric-gametest = ["com.example.examplemod.test.ExampleGameTest"]
fabric-client-gametest = ["com.example.examplemod.client.test.ExampleClientTest"]
```

## Modules

Fabric API is designed to be modular for ease of updating. This also has the advantage of splitting up the codebase into
smaller chunks.

Each module contains its own `README.md`* explaining the module's purpose and additional info on using the module.

\* The README for each module is being worked on; not every module has a README at the moment

