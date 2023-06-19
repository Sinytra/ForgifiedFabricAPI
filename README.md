# Forgified Fabric API

## API Support Status

Missing -> Added -> Tested

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
| fabric-data-generation-api-v1        |        ⚠️ Missing        |    Stable    |
| fabric-dimensions-v1                 |         ✅ Tested         |    Stable    |
| fabric-entity-events-v1              |         ✅ Tested         |    Stable    |
| fabric-events-interaction-v0         |         ✅ Tested         |    Stable    |
| fabric-game-rule-api-v1              |         ✅ Tested         |    Stable    |
| fabric-gametest-api-v1               |        ⚠️ Missing        |    Stable    |
| fabric-item-api-v1                   |         ✅ Tested         |    Stable    |
| fabric-item-group-api-v1             |         ✅ Tested         |    Stable    |
| fabric-key-binding-api-v1            |         ✅ Tested         |    Stable    |
| fabric-lifecycle-events-v1           |         ✅ Tested         |    Stable    |
| fabric-loot-api-v2                   |         ✅ Tested         |    Stable    |
| fabric-message-api-v1                |         ✅ Tested         | Experimental |
| fabric-mining-level-api-v1           |         ✅ Tested         |    Stable    |
| fabric-models-v0                     |         ❓ Added          |    Stable    |
| fabric-networking-api-v1             |         ✅ Tested         |    Stable    |
| fabric-object-builder-api-v1         |         ✅ Tested         |    Stable    |
| fabric-particles-v1                  |         ✅ Tested         |    Stable    |
| fabric-recipe-api-v1                 |         ✅ Tested         |    Stable    |
| fabric-registry-sync-v0              |        ⚠️ Missing        |    Stable    |
| fabric-renderer-api-v1               |         ❓ Added          |    Stable    |
| fabric-renderer-indigo               |         ❓ Added          |    Stable    |
| fabric-rendering-data-attachment-v1  |         ❓ Added          |    Stable    |
| fabric-rendering-fluids-v1           |        ⚠️ Missing        |              |
| fabric-rendering-v1                  |         ✅ Tested         |    Stable    |
| fabric-resource-conditions-api-v1    |         ✅ Tested         | Experimental |
| fabric-resource-loader-v0            |        ⚠️ Missing        |              |
| fabric-screen-api-v1                 |         ✅ Tested         |    Stable    |
| fabric-screen-handler-api-v1         |         ✅ Tested         |    Stable    |
| fabric-sound-api-v1                  |         ✅ Tested         |    Stable    |
| fabric-transfer-api-v1               |        ⚠️ Missing        |              |
| fabric-transitive-access-wideners-v1 |        ⚠️ Missing        |              |
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