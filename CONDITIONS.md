# Condition coverage

Status of the `databag-api` condition graph vs. vanilla loot-condition JSON,
target **Minecraft JE 26.2** (Paper `26.2.build.65-beta`). Source of truth for
the JSON shapes: <https://minecraft.wiki/w/Predicate>.

## Vanilla loot-condition types (20 current in JE 26.2)

| Vanilla id | Status | Notes |
|---|---|---|
| `all_of` / `any_of` / `inverted` | full | combinators, flattening on `Conditions.allOf/anyOf` |
| `block_state_property` | partial | exact property values only; ranged `{min,max}` state values unsupported |
| `weather_check` | full | |
| `entity_properties` | partial | see field breakdown below |
| `location_check` | partial | see field breakdown below |
| `time_check` | full | `value` bounds + `period`; `clock` field ignored (overworld time only) |
| `entity_scores` | full | `entity=this`; scores from context |
| `killed_by_player` | full* | evaluated from `attackingPlayer` flag; Paper population is event-driven (callers set it via the builder) |
| `match_tool` | unsupported | requires tool item from loot execution context |
| `survives_explosion` | unsupported | requires explosion radius from loot execution context |
| `random_chance` | unsupported | requires loot execution RNG context |
| `random_chance_with_enchanted_bonus` | unsupported | requires attacker enchantment level from loot context |
| `table_bonus` | unsupported | requires tool enchantment level from loot context |
| `enchantment_active_check` | unsupported | requires `enchanted_location` loot context |
| `damage_source_properties` | unsupported | requires damage source and attacker entities (multi-entity snapshot) |
| `value_check` | unsupported | requires vanilla number providers |
| `reference` | unsupported | requires predicate file resolver |
| `environment_attribute_check` | unsupported | requires environment attribute values in snapshot |

Unsupported ids throw `IllegalArgumentException("Unsupported vanilla condition
<id>: <reason>")` on read — deliberate accounting, not an unknown-id accident.

### Upcoming JE 26.3

- `minecraft:match_block` replaces `block_state_property` (adds block-entity
  NBT/components matching). Read throws with an "upcoming JE 26.3" reason today.
- Root discriminator `condition` is renamed to `type`; read side already accepts
  both, write side must flip when Paper targets 26.3.
- `reference` and `block_state_property` are removed upstream.

## `entity_properties` predicate fields

Covered: `type`, all eight `flags`, `type_specific.player.gamemode` (list =
any-of), `location.biomes` (list = any-of), `effects` (amplifier AND duration).

Not covered: `equipment.*`, `nbt`/custom data, `components`, `predicates`,
`movement`, `periodic_ticks`, `vehicle`, `passenger`, `stepping_on`,
`target.targeted_entity`, `team`, `slots`; `type_specific` variants (`player`
level/advancements/stats/input, `fishing_hook`, `lightning_bolt`, mob variant
sub-predicates); entity targets other than `this` (`attacker`,
`direct_attacker`, `attacking_player`, `target_entity`, `interacting_entity`).
Multi-entity snapshots are an open design decision (nested contexts vs. flat
fields); those targets currently throw on read.

## `location_check` predicate fields

Covered: `biomes` (list = any-of), `fluid.fluids` (list = any-of),
`block.blocks` (list = any-of) + `block.state`, `position` x/y/z ranges,
`dimension`, `light` (combined bound only — JE 26.2 has no sky/block split;
`LightCondition` sky/block pairs throw on write), `can_see_sky`.

Not covered: `structures`, condition-level `offsetX/Y/Z`, ranged `{min,max}`
block-state values.

## Library-owned conditions

- `modularjobs:world` — world name or namespaced key.
- `modularjobs:player_resource` — HEALTH, HUNGER, EXPERIENCE (progress 0..1),
  LEVEL, ABSORPTION, AIR (remaining ticks).
- `modularjobs:job` / bare legacy read aliases (`sneaking`, `sprinting`,
  `biome`, `liquid`, `potion_effect`, `always`, `world`).
- Bare `liquid` without `value` throws (no silent water default).

## Snapshot data captured by `PaperConditionContexts`

Position (x/y/z), combined/sky/block light, can-see-sky, world day time, XP
level, absorption, remaining air, scoreboard scores (players), potion effects,
biome/world/weather/fluid, entity flags, health, block id + state properties.
`attackingPlayer` is event-driven: callers set it via the builder.
