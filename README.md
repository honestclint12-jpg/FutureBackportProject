# Future Backport

Brings blocks, items, mobs and biomes from newer Minecraft versions to 1.21.1:
the pale garden and creaking, resin, the spring drop flora and farm animal variants,
happy ghasts and harnesses, the copper age (copper golem, tools, armor, chests, shelves),
spears and nautiluses, and more.

## Loader support

| Loader (1.21.1) | Status |
|---|---|
| **NeoForge** | Complete; all 48 game tests pass |
| **Fabric** | Server side works (all content registers, worldgen/spawns/loot/data maps applied). **Client rendering is not wired up yet**, so it is not playable on a Fabric client. |
| Forge / Fabric 1.20.1 | Not started |

### Fabric gaps

- **Client registration**: entity, block entity and particle renderers, model layers, menu screens, color
  handlers, custom item renderers and the dry foliage reload listener are still registered only by the NeoForge
  client class (`neoforge/.../FutureBackportClient.java`). Next step: a client registration service in `common`.
- **Pale oak boats**: `Boat.Type` is an enum in 1.21.1. NeoForge extends it via `enumextensions.json`; Fabric
  currently falls back to oak boats (logged as a warning).
- **Game tests** run on NeoForge only.

## Layout

```
common/    Shared code (most of the mod). Compiles against plain Minecraft and only talks to the loader through
           com.futurebackport.platform (Services.*). Also holds all assets and data, including the NeoForge-format
           biome modifiers / data maps / loot modifiers, which the Fabric build reads too.
neoforge/  NeoForge entry point and event glue, platform service implementations, biome modifier type,
           Boat.Type enum extension, client registration, dev showcase, game tests.
fabric/    Fabric entry points, platform service implementations, mixins that replace NeoForge events, and
           NeoForgeDataOnFabric (applies the shared NeoForge-format data with Fabric APIs).
```

Loader-specific code is reached through `Services` (registration, platform helpers, data attachments,
networking). Shared event handlers are plain static methods; each loader calls them from its own events.

Shared code may use vanilla members that NeoForge's own access transformer opens. Those are listed in
`common/src/main/resources/META-INF/accesstransformer.cfg` and mirrored in `futurebackport.accesswidener`
for Fabric: keep the two in sync.

## Source history

This source was recovered by decompiling `futurebackport-1.0.0.jar`, because the original project was not under
version control. Names and structure survived; original comments and formatting did not.

## Building and testing

Requires Java 21.

```sh
./gradlew build                          # jars in neoforge/build/libs and fabric/build/libs
./gradlew :neoforge:runGameTestServer    # runs all 48 game tests headlessly; fails on any failure
./gradlew :neoforge:runClient            # NeoForge dev client
./gradlew :fabric:runServer              # Fabric dev server
```
