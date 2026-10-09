# Future Backport

Brings blocks, items, mobs and biomes from newer Minecraft versions to 1.21.1:
the pale garden and creaking, resin, the spring drop flora and farm animal variants,
happy ghasts and harnesses, the copper age (copper golem, tools, armor, chests, shelves),
spears and nautiluses, and more.

## Loader support

| Loader (1.21.1) | Status |
|---|---|
| **NeoForge** | Complete; all 48 game tests pass |
| **Fabric** | Playable: all content registers, worldgen/spawns/loot/data maps apply, and client rendering (entities, block entities, particles, screens, colors, item renderers) matches NeoForge in side-by-side client screenshots. See gaps below. |
| Forge / Fabric 1.20.1 | Not started |

### Fabric gaps

- **Pale oak boats**: `Boat.Type` is an enum in 1.21.1. NeoForge extends it via `enumextensions.json`; Fabric
  currently falls back to oak boats (logged as a warning).
- **Game tests** run on NeoForge only.

## Layout

```
common/    Shared code (most of the mod). Compiles against plain Minecraft and only talks to the loader through
           com.futurebackport.platform (Services.*). Also holds all assets and data, including the NeoForge-format
           biome modifiers / data maps / loot modifiers, which the Fabric build reads too. Client registration
           lives in common/.../client/ModClientSetup.java; each loader passes in its own registration sinks.
neoforge/  NeoForge entry point and event glue, platform service implementations, biome modifier type,
           Boat.Type enum extension, client event glue, dev showcase, game tests.
fabric/    Fabric entry points, platform service implementations, mixins that replace NeoForge events, and
           NeoForgeDataOnFabric (applies the shared NeoForge-format data with Fabric APIs).
```

Loader-specific code is reached through `Services` (registration, platform helpers, data attachments,
networking). Shared event handlers are plain static methods; each loader calls them from its own events.

Shared code may use vanilla members that NeoForge's own access transformer opens. Those are listed in
`common/src/main/resources/META-INF/accesstransformer.cfg` and mirrored in `futurebackport.accesswidener`
for Fabric: keep the two in sync.

## Minecraft textures and sounds

The mod does not ship Mojang's textures and sounds. The first time the game starts, it downloads the ones the mod
uses (about 1,300 files) from Mojang's own servers into `<game folder>/futurebackport/vanilla-assets` and loads
them as a built-in resource pack. This takes about 10 seconds and only happens once; after that the game works
offline. Each file is checked against its SHA-1 before it is used.

- The list of files is `common/src/main/resources/futurebackport_vanilla_assets.json`. It names the Minecraft
  version and path each file comes from. Only the needed files are fetched out of the client jar (HTTP range
  requests), not the whole jar.
- To add new textures or sounds from a newer Minecraft version, put them in `common/src/main/resources/assets` as
  usual and run `python3 tools/vanilla-assets.py`. It moves every file that matches a vanilla file into the list.
  Files that match nothing are the mod's own art and stay in the jar.
- A few textures the original author edited are rebuilt from the vanilla files after downloading (see the
  `overlay` and `patch` entries in the list).
- If the first start has no internet connection, the mod's blocks and mobs show missing textures until a later
  start can download them. Dedicated servers download nothing.

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

## License

MIT, see [LICENSE](LICENSE). This covers the mod's code and its own files. Minecraft's textures and sounds belong
to Mojang; the mod downloads them from Mojang instead of redistributing them.
