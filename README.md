# \[Forgified] Fabric API

Essential hooks for modding with Fabric.

Fabric API is the library for essential hooks and interoperability mechanisms for Fabric mods. Examples include:

- Exposing functionality that is useful but difficult to access for many mods such as particles, biomes and dimensions
- Adding events, hooks and APIs to improve interopability between mods.
- Essential features such as registry synchronization and adding information to crash reports.
- An advanced rendering API designed for compatibility with optimization mods and graphics overhaul mods.

## Porting Status

Missing -> Added -> Tested

| API                                  |          State           |  Lifecycle   |
|:-------------------------------------|:------------------------:|:------------:|
| fabric-api-base                      |         ✅ Tested         |    Stable    |
| fabric-api-lookup-api-v1             |         ✅ Tested         |    Stable    |
| fabric-biome-api-v1                  |        ⚠️ Missing        | Experimental |
| fabric-block-api-v1                  |        ⚠️ Missing        |    Stable    |
| fabric-blockrenderlayer-v1           |        ⚠️ Missing        |    Stable    |
| fabric-client-tags-api-v1            |        ⚠️ Missing        |    Stable    |
| fabric-command-api-v2                |         ✅ Tested         |    Stable    |
| fabric-content-registries-v0         |        ⚠️ Missing        |    Stable    |
| fabric-convention-tags-v1            |        ⚠️ Missing        |    Stable    |
| fabric-crash-report-info-v1          | 🚧 Not Planned [[1]](#1) |    Stable    |
| fabric-data-generation-api-v1        |        ⚠️ Missing        |    Stable    |
| fabric-dimensions-v1                 |         ✅ Tested         |    Stable    |
| fabric-entity-events-v1              |        ⚠️ Missing        |    Stable    |
| fabric-events-interaction-v0         |        ⚠️ Missing        |    Stable    |
| fabric-game-rule-api-v1              |         ✅ Tested         |    Stable    |
| fabric-gametest-api-v1               |        ⚠️ Missing        |    Stable    |
| fabric-item-api-v1                   |        ⚠️ Missing        |    Stable    |
| fabric-item-group-api-v1             |        ⚠️ Missing        |    Stable    |
| fabric-key-binding-api-v1            |         ✅ Tested         |    Stable    |
| fabric-lifecycle-events-v1           |         ✅ Tested         |    Stable    |
| fabric-loot-api-v2                   |        ⚠️ Missing        |    Stable    |
| fabric-message-api-v1                |        ⚠️ Missing        | Experimental |
| fabric-mining-level-api-v1           |        ⚠️ Missing        |    Stable    |
| fabric-models-v0                     |        ⚠️ Missing        |    Stable    |
| fabric-networking-api-v1             |         ✅ Tested         |    Stable    |
| fabric-object-builder-api-v1         |         ✅ Tested         |    Stable    |
| fabric-particles-v1                  |        ⚠️ Missing        |    Stable    |
| fabric-recipe-api-v1                 |        ⚠️ Missing        |    Stable    |
| fabric-registry-sync-v0              |        ⚠️ Missing        |    Stable    |
| fabric-renderer-api-v1               |        ⚠️ Missing        |    Stable    |
| fabric-renderer-indigo               |        ⚠️ Missing        |    Stable    |
| fabric-rendering-data-attachment-v1  |        ⚠️ Missing        |    Stable    |
| fabric-rendering-fluids-v1           |        ⚠️ Missing        |    Stable    |
| fabric-rendering-v1                  |         ✅ Tested         |    Stable    |
| fabric-resource-conditions-api-v1    |        ⚠️ Missing        | Experimental |
| fabric-resource-loader-v0            |        ⚠️ Missing        |    Stable    |
| fabric-screen-api-v1                 |        ⚠️ Missing        |    Stable    |
| fabric-screen-handler-api-v1         |        ⚠️ Missing        |    Stable    |
| fabric-sound-api-v1                  |        ⚠️ Missing        |    Stable    |
| fabric-transfer-api-v1               |        ⚠️ Missing        | Experimental |
| fabric-transitive-access-wideners-v1 |        ⚠️ Missing        |    Stable    |
| fabric-command-api-v1                |        ⚠️ Missing        |  Deprecated  |
| fabric-commands-v0                   |        ⚠️ Missing        |  Deprecated  |
| fabric-containers-v0                 |        ⚠️ Missing        |  Deprecated  |
| fabric-events-lifecycle-v0           |        ⚠️ Missing        |  Deprecated  |
| fabric-keybindings-v0                |        ⚠️ Missing        |  Deprecated  |
| fabric-loot-tables-v1                |        ⚠️ Missing        |  Deprecated  |
| fabric-networking-v0                 |        ⚠️ Missing        |  Deprecated  |
| fabric-renderer-registries-v1        |        ⚠️ Missing        |  Deprecated  |
| fabric-rendering-v0                  |        ⚠️ Missing        |  Deprecated  |

<a id="1">[1]</a> Does not provide an API, features already implemented by FML.

## Modules

Fabric API is designed to be modular for ease of updating. This also has the advantage of splitting up the codebase into
smaller chunks.

Each module contains its own `README.md`* explaining the module's purpose and additional info on using the module.

\* The README for each module is being worked on; not every module has a README at the moment
