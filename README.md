# Future Backport

Brings blocks, items, mobs and biomes from newer Minecraft versions to 1.21.1:
the pale garden and creaking, resin, the spring drop flora and farm animal variants,
happy ghasts and harnesses, the copper age (copper golem, tools, armor, chests, shelves),
spears and nautiluses, and more.

**Current target:** NeoForge 1.21.1 (21.1.256+). Multi-loader support (Fabric 1.21.1,
Forge/Fabric 1.20.1) is the next milestone.

## Source history

This source was recovered by decompiling `futurebackport-1.0.0.jar`, because the original
project was not under version control. Names and structure survived; original comments and
formatting did not. The recovered build produces the same jar contents and passes all 48
game tests. If the original source turns up, it can replace this.

## Building and testing

Requires Java 21.

```sh
./gradlew build              # jar in build/libs/
./gradlew runGameTestServer  # runs all @GameTest tests headlessly; fails on any test failure
./gradlew runClient          # dev client
```
