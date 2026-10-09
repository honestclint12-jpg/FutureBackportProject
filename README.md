# Future Backport (Minecraft 1.20.1 branch)

> This is the **1.20.1** branch (Forge and Fabric). The 1.21.1 version (NeoForge and Fabric) lives on `main`.
> Content changes are made on 1.21.1 first and then ported here.

Brings blocks, items, mobs and biomes from newer Minecraft versions to 1.20.1:
the pale garden and creaking, resin, the spring drop flora and farm animal variants,
happy ghasts and harnesses, the copper age (copper golem, tools, armor, chests, shelves),
spears and nautiluses, and more.

## Loader support

| Loader (1.20.1) | Status |
|---|---|
| **Forge** 47.1+ | All 50 game tests pass; boots on server and client, rendering checked in client screenshots. Built against NeoForged's 1.20.1 build of Forge. |
| **Fabric** (Loader 0.16.10+, Fabric API) | All 50 game tests pass; same server and client checks as Forge, with identical results (same pale garden location for the same seed). |

### Gaps

- 1.20.1 has no BODY equipment slot, attribute-based step height or scale, data-driven enchantments or data maps;
  the mod backports what it needs (see `ModDataMaps`, `MixinAttachmentService`, `LungeEnchantment`, `SpearEnchanting`).
- Armadillos don't exist in 1.20.1, so the baby armadillo model is not included.

## Layout

```
common/    Shared code (most of the mod). Compiles against plain 1.20.1 and only talks to the loader through
           com.futurebackport.platform (Services.*). Also holds all assets and data, including the Forge-format
           biome and loot modifiers, which the Fabric build reads too. Client registration lives in
           common/.../client/ModClientSetup.java; each loader passes in its own registration sinks.
forge/     Forge entry point and event glue, platform services, biome modifier and add_table loot modifier types,
           client event glue.
fabric/    Fabric entry points, platform services, mixins that stand in for Forge events, and ForgeDataOnFabric
           (applies the shared Forge-format data with Fabric APIs).
tools/     at2srg.py / regen-at.sh (access transformer, see below) and port-data-1.20.1.py (the one-shot converter
           that turned the 1.21.1 data pack into the 1.20.1 format).
```

Game tests live in `common/.../gametest` and use vanilla APIs only (plus `TestPlayers`, which fills in test helpers
1.20.1 lacks). Add new test classes to `TEST_CLASSES` in `FutureBackportGameTests`, and only look at entities inside
your own arena: tests run side by side.

Loader-specific code is reached through `Services` (registration, platform helpers, networking). Entity data
attachments use one mixin-based implementation in common on both loaders. Shared event handlers are plain static
methods; each loader calls them from its own events.

### Access transformer

Forge 1.20.1 applies access transformers by SRG name. Edit the Mojang-named `tools/accesstransformer.named.cfg`,
then run `tools/regen-at.sh` to regenerate `common/src/main/resources/META-INF/accesstransformer.cfg`. Mirror every
entry in `futurebackport.accesswidener` for Fabric.

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

Gradle runs on Java 21; the mod itself targets Java 17.

```sh
./gradlew build                 # jars in forge/build/libs (SRG-remapped) and fabric/build/libs
./gradlew :forge:runGameTestServer    # all game tests, headless; fails on any failure
./gradlew :fabric:runGameTestServer   # the same tests on Fabric
./gradlew :forge:runClient      # Forge dev client
./gradlew :forge:runServer      # Forge dev server
./gradlew :fabric:runClient     # Fabric dev client
./gradlew :fabric:runServer     # Fabric dev server
```

GitHub Actions (`.github/workflows/build.yml`) builds and runs the game tests on Forge and Fabric on every push and
pull request. Releases are published from `main`, which builds this branch too (see the README there).

## License

MIT, see [LICENSE](LICENSE). This covers the mod's code and its own files. Minecraft's textures and sounds belong
to Mojang; the mod downloads them from Mojang instead of redistributing them.
