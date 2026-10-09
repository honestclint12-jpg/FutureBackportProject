# Future Backport

Brings blocks, items, mobs and biomes from newer Minecraft versions to 1.21.1:
the pale garden and creaking, resin, the spring drop flora and farm animal variants,
happy ghasts and harnesses, the copper age (copper golem, tools, armor, chests, shelves),
spears and nautiluses, and more.

## Versions and loaders

| Minecraft | Loaders | Branch |
|---|---|---|
| **1.21.1** | NeoForge, Fabric | `main` |
| **1.20.1** | Forge 47.1+, Fabric | `minecraft-1.20.1` |

On every loader, all content registers, worldgen, spawns, loot and data apply, and the client renders the same as
the others (checked with side-by-side client screenshots). All 50 game tests pass on each of the four loaders, and
CI runs them on every push and pull request.

Content changes are made on `main` (1.21.1) first and then ported to `minecraft-1.20.1`.

## Layout

```
common/    Shared code (most of the mod). Compiles against plain Minecraft and only talks to the loader through
           com.futurebackport.platform (Services.*). Also holds all assets and data, including the NeoForge-format
           biome modifiers / data maps / loot modifiers, which the Fabric build reads too. Client registration
           lives in common/.../client/ModClientSetup.java; each loader passes in its own registration sinks.
neoforge/  NeoForge entry point and event glue, platform service implementations, biome modifier type,
           Boat.Type enum extension, client event glue, dev showcase.
fabric/    Fabric entry points, platform service implementations, mixins that replace NeoForge events (including
           the pale oak Boat.Type), and NeoForgeDataOnFabric (applies the shared NeoForge-format data with Fabric
           APIs).
tools/     vanilla-assets.py (see "Minecraft textures and sounds" below).
```

Game tests live in `common/.../gametest` and use vanilla APIs only, so the same tests run on both loaders. Add new
test classes to `TEST_CLASSES` in `FutureBackportGameTests`.

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
./gradlew :neoforge:runGameTestServer    # all game tests on NeoForge; fails on any failure
./gradlew :fabric:runGameTestServer      # the same tests on Fabric
./gradlew :neoforge:runClient            # NeoForge dev client (also :fabric:runClient)
./gradlew :fabric:runServer              # Fabric dev server
```

GitHub Actions (`.github/workflows/build.yml`) builds and runs the game tests on every push and pull request, and
keeps the jars as a downloadable artifact of each run.

## Releasing

Set `version` in `gradle.properties`, then push a tag named `v<version>` (for example `v1.0.1`) on `main`. The
release workflow builds and tests both Minecraft versions and publishes a GitHub Release with all four jars
(NeoForge and Fabric for 1.21.1, Forge and Fabric for 1.20.1). It checks that the tag matches `version` on both
branches first, so bump `version` on `minecraft-1.20.1` too. A version with a suffix, such as `1.0.0-beta.1`, is
published as a pre-release.

To also upload the jars to Modrinth and CurseForge, add the projects' IDs as repository variables
(`MODRINTH_ID`, `CURSEFORGE_ID`) and API tokens as repository secrets (`MODRINTH_TOKEN`, `CURSEFORGE_TOKEN`) under
Settings → Secrets and variables → Actions. Without them, that step is skipped.

## License

MIT, see [LICENSE](LICENSE). This covers the mod's code and its own files. Minecraft's textures and sounds belong
to Mojang; the mod downloads them from Mojang instead of redistributing them.
