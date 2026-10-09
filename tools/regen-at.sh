#!/bin/sh
# Regenerates the SRG-named access transformer from tools/accesstransformer.named.cfg.
# Needs common/build/moddev/artifacts/namedToIntermediate.tsrg (run ./gradlew :common:createMinecraftArtifacts once).
cd "$(dirname "$0")/.." || exit 1
{
  echo "# GENERATED from tools/accesstransformer.named.cfg by tools/regen-at.sh (SRG names; Forge 1.20.1 needs them). Do not edit by hand."
  python3 tools/at2srg.py tools/accesstransformer.named.cfg common/build/moddev/artifacts/namedToIntermediate.tsrg | grep -v '^# Mojang-named\|^# Forge 1.20.1\|^#   \|^#     '
} > common/src/main/resources/META-INF/accesstransformer.cfg
