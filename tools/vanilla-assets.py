#!/usr/bin/env python3
"""Moves vanilla Minecraft textures and sounds out of the mod and into the download manifest.

The mod does not ship Mojang's files. Each player's game downloads them from Mojang the first time it
starts (see com.futurebackport.client.assets.VanillaAssets). This script keeps the manifest that drives
that download in sync with the resources folder:

  1. Every .png, .ogg and .mcmeta under common/src/main/resources/assets that is not in the manifest yet
     is looked up by SHA-1 in the client jars and asset indexes of the given Minecraft versions.
  2. A match becomes a manifest entry and the file is deleted from the resources folder.
  3. Files that match nothing are the mod's own art. They stay in the mod and are listed at the end.

Run it after adding new assets copied from a newer Minecraft version:

  python3 tools/vanilla-assets.py [--versions 26.2,1.21.11]

Versions are tried in order, so put the newest first. An older version is only used for files the newer
ones no longer have (the 1.21.x sign textures, for example).

Entries can also rebuild a file the original author edited (the game applies them after downloading):
  "overlay": {"jar": ..., "path": ..., "sha1": ...}   draws a second vanilla image on top
  "patch": ["x,y,rrggbbaa", ...]                        sets single pixels (the mod's own edits)
"""
import argparse
import hashlib
import json
import os
import sys
import urllib.request
import zipfile

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'common', 'src', 'main', 'resources')
MANIFEST = os.path.join(ROOT, 'futurebackport_vanilla_assets.json')
VERSIONS_URL = 'https://piston-meta.mojang.com/mc/game/version_manifest_v2.json'
CACHE = os.path.expanduser('~/.cache/futurebackport-vanilla')


def fetch(url):
    with urllib.request.urlopen(url) as r:
        return r.read()


def load_version(version_id, versions):
    meta = next((v for v in versions if v['id'] == version_id), None)
    if meta is None:
        sys.exit(f'unknown Minecraft version {version_id}')
    vj = json.loads(fetch(meta['url']))
    client = vj['downloads']['client']
    os.makedirs(CACHE, exist_ok=True)
    jar_path = os.path.join(CACHE, f'{version_id}.jar')
    if not os.path.exists(jar_path) or hashlib.sha1(open(jar_path, 'rb').read()).hexdigest() != client['sha1']:
        print(f'downloading {version_id} client jar')
        with open(jar_path, 'wb') as f:
            f.write(fetch(client['url']))
    jar_files = {}
    with zipfile.ZipFile(jar_path) as z:
        for name in z.namelist():
            if name.startswith('assets/') and not name.endswith('/'):
                jar_files.setdefault(hashlib.sha1(z.read(name)).hexdigest(), name)
    objects = {o['hash'] for o in json.loads(fetch(vj['assetIndex']['url']))['objects'].values()}
    return {'url': client['url'], 'sha1': client['sha1']}, jar_files, objects


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--versions', default='26.2,1.21.11')
    args = parser.parse_args()

    manifest = json.load(open(MANIFEST)) if os.path.exists(MANIFEST) else {'jars': {}, 'files': {}}
    candidates = []
    for base, _, names in os.walk(os.path.join(ROOT, 'assets')):
        for name in names:
            if name.endswith(('.png', '.ogg', '.mcmeta')) and name != 'pack.mcmeta':
                rel = os.path.relpath(os.path.join(base, name), ROOT).replace(os.sep, '/')
                if rel not in manifest['files']:
                    candidates.append(rel)
    if not candidates:
        print('nothing new to move')
        return

    versions = json.loads(fetch(VERSIONS_URL))['versions']
    sources = [(v, *load_version(v, versions)) for v in args.versions.split(',')]
    unmatched = []
    for rel in sorted(candidates):
        sha1 = hashlib.sha1(open(os.path.join(ROOT, rel), 'rb').read()).hexdigest()
        entry = None
        for version, jar, jar_files, _ in sources:
            if sha1 in jar_files:
                manifest['jars'][version] = jar
                entry = {'jar': version, 'path': jar_files[sha1], 'sha1': sha1}
                break
        if entry is None and any(sha1 in objects for *_, objects in sources):
            entry = {'object': sha1}
        if entry is None:
            unmatched.append(rel)
            continue
        manifest['files'][rel] = entry
        os.remove(os.path.join(ROOT, rel))

    manifest['files'] = dict(sorted(manifest['files'].items()))
    with open(MANIFEST, 'w') as f:
        json.dump(manifest, f, indent=1)
        f.write('\n')
    for base, _, _ in os.walk(os.path.join(ROOT, 'assets'), topdown=False):
        if not os.listdir(base):
            os.rmdir(base)
    print(f'moved {len(candidates) - len(unmatched)} files into the manifest')
    if unmatched:
        print('kept (no vanilla match, so these are the mod\'s own files or edited copies):')
        for rel in unmatched:
            print('  ' + rel)


if __name__ == '__main__':
    main()
