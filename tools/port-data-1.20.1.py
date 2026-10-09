"""One-shot conversion of the mod's 1.21.1 data pack to the Minecraft 1.20.1 format.

Run from the repository root:  python3 tools/port-data-1.20.1.py
It edits common/src/main/resources/data in place and is safe to run twice (converted files are left alone).

What changes between 1.21.1 and 1.20.1:
- folder names are plural again (recipes, loot_tables, advancements, structures, tags/items, tags/blocks, ...)
- recipe results use "item" (crafting, smithing) or a plain id string (cooking, stonecutting)
- item predicates list item ids ("items": ["minecraft:vine"]) and name tags with "tag"
- loot functions and conditions from 1.20.5+ map back to their older forms (looting_enchant, copy_name, ...)
- int/float providers in worldgen are wrapped in "value"
- NeoForge biome modifiers and loot modifiers become their Forge 1.20.1 equivalents
- 1.21-only registries (data-driven enchantments, jukebox songs, painting variants) are registered in code instead
"""
import json
import os
import shutil
import sys

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'common', 'src', 'main', 'resources', 'data')

FOLDER_RENAMES = {
    'recipe': 'recipes',
    'loot_table': 'loot_tables',
    'advancement': 'advancements',
    'structure': 'structures',
}
TAG_RENAMES = {
    'item': 'items',
    'block': 'blocks',
    'entity_type': 'entity_types',
    'fluid': 'fluids',
    'game_event': 'game_events',
}
# Files and folders that have no 1.20.1 meaning (registered in code, or features 1.20.1 doesn't have).
DELETE = [
    'futurebackport/enchantment',          # Lunge is a code enchantment (ModEnchantments)
    'futurebackport/jukebox_song',         # music discs are RecordItems
    'futurebackport/painting_variant',     # ModPaintings
    'minecraft/tags/enchantment',          # 1.21 enchantment tags
    'minecraft/tags/items/enchantable',    # 1.21 enchantability tags (SpearEnchanting replaces them)
    'futurebackport/tags/items/enchantable',
    'minecraft/tags/worldgen/biome/has_structure/trial_chambers.json',  # no trial chambers in 1.20.1
]

INT_PROVIDERS_WRAPPED = {'minecraft:uniform', 'minecraft:biased_to_bottom', 'minecraft:clamped', 'minecraft:clamped_normal'}
FLOAT_PROVIDERS_WRAPPED = {'minecraft:uniform', 'minecraft:clamped_normal', 'minecraft:trapezoid'}


def load(path):
    with open(path) as f:
        return json.load(f)


def save(path, data):
    with open(path, 'w') as f:
        json.dump(data, f, indent=2)
        f.write('\n')


def walk_json(top):
    for dirpath, _, files in os.walk(top):
        for name in sorted(files):
            if name.endswith('.json'):
                yield os.path.join(dirpath, name)


def rename_folders():
    for namespace in os.listdir(ROOT):
        ns = os.path.join(ROOT, namespace)
        for old, new in FOLDER_RENAMES.items():
            if os.path.isdir(os.path.join(ns, old)):
                os.rename(os.path.join(ns, old), os.path.join(ns, new))
        tags = os.path.join(ns, 'tags')
        for old, new in TAG_RENAMES.items():
            if os.path.isdir(os.path.join(tags, old)):
                os.rename(os.path.join(tags, old), os.path.join(tags, new))


def delete_unused():
    for rel in DELETE:
        path = os.path.join(ROOT, rel)
        if os.path.isdir(path):
            shutil.rmtree(path)
        elif os.path.exists(path):
            os.remove(path)


# ---- recipes ----

def convert_recipe(recipe):
    kind = recipe.get('type', '')
    result = recipe.get('result')
    if isinstance(result, dict) and 'id' in result:
        if kind in ('minecraft:smelting', 'minecraft:blasting', 'minecraft:smoking', 'minecraft:campfire_cooking'):
            recipe['result'] = result['id']
        elif kind == 'minecraft:stonecutting':
            recipe['result'] = result['id']
            recipe['count'] = result.get('count', 1)
        else:
            converted = {'item': result['id']}
            if result.get('count', 1) != 1:
                converted['count'] = result['count']
            recipe['result'] = converted
    recipe.pop('show_notification', None)
    return recipe


# ---- predicates (advancements and loot) ----

def item_predicate(pred):
    """1.21 item predicate -> 1.20.1 item predicate."""
    if not isinstance(pred, dict):
        return pred
    out = dict(pred)
    items = out.pop('items', None)
    if isinstance(items, str):
        if items.startswith('#'):
            out['tag'] = items[1:]
        else:
            out['items'] = [items]
    elif isinstance(items, list):
        out['items'] = items
    predicates = out.pop('predicates', None)
    if predicates:
        enchantments = predicates.get('minecraft:enchantments')
        if enchantments:
            out['enchantments'] = [
                {k if k != 'enchantments' else 'enchantment': v for k, v in e.items()} for e in enchantments
            ]
    return out


def entity_predicate(pred):
    """1.21.5 style entity predicate (minecraft:-prefixed keys) -> 1.20.1 entity predicate."""
    if not isinstance(pred, dict):
        return pred
    out = {}
    for key, value in pred.items():
        bare = key.split(':', 1)[1] if key.startswith('minecraft:') else key
        if bare == 'entity_type':
            bare = 'type'
        if bare in ('vehicle', 'passenger', 'targeted_entity', 'direct_entity', 'source_entity'):
            value = entity_predicate(value)
        out[bare] = value
    return out


def convert_advancement(adv):
    for criterion in adv.get('criteria', {}).values():
        conditions = criterion.get('conditions', {})
        if criterion.get('trigger') == 'minecraft:inventory_changed' and 'items' in conditions:
            conditions['items'] = [item_predicate(p) for p in conditions['items']]
    return adv


# ---- loot tables ----

def convert_loot_node(node):
    if isinstance(node, list):
        return [convert_loot_node(n) for n in node]
    if not isinstance(node, dict):
        return node
    node = {k: convert_loot_node(v) for k, v in node.items()}
    function = node.get('function')
    condition = node.get('condition')
    if function == 'minecraft:copy_components':
        if node.get('include') == ['minecraft:custom_name'] and node.get('source') == 'block_entity':
            node = {'function': 'minecraft:copy_name', 'source': 'block_entity', **({'conditions': node['conditions']} if 'conditions' in node else {})}
        else:
            sys.exit('copy_components with unsupported components: %s' % node)
    elif function == 'minecraft:enchanted_count_increase':
        node.pop('enchantment', None)
        node['function'] = 'minecraft:looting_enchant'
    elif function == 'minecraft:enchant_randomly':
        node.pop('options', None)
    elif function == 'minecraft:enchant_with_levels':
        if node.pop('options', None) is not None:
            node['treasure'] = True
    if condition == 'minecraft:match_tool':
        node['predicate'] = item_predicate(node.get('predicate', {}))
    elif condition == 'minecraft:random_chance_with_enchanted_bonus':
        chance = node.get('unenchanted_chance', 0.0)
        bonus = node.get('enchanted_chance', {})
        per_level = bonus.get('per_level_above_first', 0.0) if isinstance(bonus, dict) else 0.0
        node = {'condition': 'minecraft:random_chance_with_looting', 'chance': chance, 'looting_multiplier': per_level}
    elif condition == 'minecraft:entity_properties':
        node['predicate'] = entity_predicate(node.get('predicate', {}))
    elif condition == 'minecraft:damage_source_properties':
        pred = dict(node.get('predicate', {}))
        for key in ('direct_entity', 'source_entity'):
            if key in pred:
                pred[key] = entity_predicate(pred[key])
        node['predicate'] = pred
    return node


# ---- worldgen ----

def convert_worldgen(node, parent_key=None):
    if isinstance(node, list):
        return [convert_worldgen(n) for n in node]
    if not isinstance(node, dict):
        return node
    node = {k: convert_worldgen(v, k) for k, v in node.items()}
    kind = node.get('type')
    if 'value' not in node and kind in INT_PROVIDERS_WRAPPED | FLOAT_PROVIDERS_WRAPPED:
        if any(k in node for k in ('min_inclusive', 'max_inclusive', 'max_exclusive', 'source', 'mean', 'deviation', 'plateau', 'min', 'max')):
            return {'type': kind, 'value': {k: v for k, v in node.items() if k != 'type'}}
    return node


# ---- loader data ----

def convert_biome_modifiers():
    src = os.path.join(ROOT, 'futurebackport', 'neoforge', 'biome_modifier')
    if not os.path.isdir(src):
        return
    dst = os.path.join(ROOT, 'futurebackport', 'forge', 'biome_modifier')
    os.makedirs(dst, exist_ok=True)
    for path in walk_json(src):
        data = load(path)
        if data['type'].startswith('neoforge:'):
            data['type'] = 'forge:' + data['type'].split(':', 1)[1]
        save(os.path.join(dst, os.path.relpath(path, src)), data)
    shutil.rmtree(os.path.join(ROOT, 'futurebackport', 'neoforge'))


def convert_loot_modifiers():
    """NeoForge add_table modifiers -> this mod's own futurebackport:add_table (Forge 1.20.1 has no add_table)."""
    neo_global = os.path.join(ROOT, 'neoforge', 'loot_modifiers', 'global_loot_modifiers.json')
    if not os.path.exists(neo_global):
        return
    forge_dir = os.path.join(ROOT, 'forge', 'loot_modifiers')
    os.makedirs(forge_dir, exist_ok=True)
    shutil.move(neo_global, os.path.join(forge_dir, 'global_loot_modifiers.json'))
    for path in walk_json(os.path.join(ROOT, 'futurebackport', 'loot_modifiers')):
        data = load(path)
        if data.get('type') == 'neoforge:add_table':
            data['type'] = 'futurebackport:add_table'
        for cond in data.get('conditions', []):
            if cond.get('condition') == 'neoforge:loot_table_id':
                cond['condition'] = 'forge:loot_table_id'
        save(path, data)
    os.rmdir(os.path.join(ROOT, 'neoforge', 'loot_modifiers'))


def move_data_maps():
    """NeoForge data maps don't exist in 1.20.1; the files move to the mod's namespace and are applied in code."""
    src = os.path.join(ROOT, 'neoforge', 'data_maps')
    if not os.path.isdir(src):
        return
    dst = os.path.join(ROOT, 'futurebackport', 'data_maps')
    shutil.move(src, dst)
    if not os.listdir(os.path.join(ROOT, 'neoforge')):
        os.rmdir(os.path.join(ROOT, 'neoforge'))


def convert_trim_materials():
    """1.20.1 only accepts vanilla armor material names as override keys."""
    for path in walk_json(os.path.join(ROOT, 'minecraft', 'trim_material')):
        data = load(path)
        overrides = data.get('override_armor_materials', {})
        kept = {k: v for k, v in overrides.items() if ':' not in k}
        if kept != overrides:
            if kept:
                data['override_armor_materials'] = kept
            else:
                data.pop('override_armor_materials', None)
            save(path, data)


def main():
    rename_folders()
    delete_unused()
    for namespace in os.listdir(ROOT):
        ns = os.path.join(ROOT, namespace)
        for path in walk_json(os.path.join(ns, 'recipes')):
            save(path, convert_recipe(load(path)))
        for path in walk_json(os.path.join(ns, 'advancements')):
            save(path, convert_advancement(load(path)))
        for path in walk_json(os.path.join(ns, 'loot_tables')):
            save(path, convert_loot_node(load(path)))
        for path in walk_json(os.path.join(ns, 'worldgen')):
            save(path, convert_worldgen(load(path)))
    convert_biome_modifiers()
    convert_loot_modifiers()
    move_data_maps()
    convert_trim_materials()


if __name__ == '__main__':
    main()
