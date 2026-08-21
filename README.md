# databag

PDC-shaped primitive bag plus player/entity/block predicates. Java package
and Maven group: **`dev.mintychochip.databag`**.

| Artifact | Role |
|----------|------|
| `dev.mintychochip.databag:databag-common` | Namespaced Kryo `DataBag` |
| `dev.mintychochip.databag:databag-api` | Immutable `Condition` graph + loot-condition JSON |
| `dev.mintychochip.databag:databag-paper` | `PaperConditionContexts` + `PersistentBags` |

Namespaced keys, PDC-like primitives, no Bukkit in common/api. The whole bag
encodes to a Kryo `byte[]` that Paper writes as `PersistentDataType.BYTE_ARRAY`.
Missing or wrong-typed keys are empty, never thrown.

Scalars: boolean, byte, short, int, long, float, double, string, UUID.
Arrays: byte[], int[], long[]. Formatted: `byte[]` + format id.

```java
import dev.mintychochip.databag.DataBag;
import net.kyori.adventure.key.Key;

DataBag bag = DataBag.create()
    .setBoolean(Key.key("modularjobs", "enabled"), true)
    .setInt(Key.key("modularjobs", "priority"), 100)
    .setBytes(Key.key("modularjobs", "payload"), jsonBytes);

byte[] pdcPayload = bag.toBytes();
DataBag back = DataBag.fromBytes(pdcPayload);
```

## Formats and migrations

Writes always use envelope **v1**: magic `DBAG` + version byte + length-prefixed
entries. Reads still accept the original unversioned body (v0). Rewriting a
decoded bag upgrades it to v1.

Unknown envelope versions throw `UnknownBagFormatException`. Unknown value tags
in v1+ are skipped, so adding a primitive does not need a version bump — only
a breaking change to an existing tag or the header does.

Payload encodings that may change (JSON v1 → v2, binary, …) should use a
**format id** on the `byte[]` slot:

```java
bag.setBytes(Key.key("modularjobs", "boost_data"), 1, jsonBytes);

FormattedBytes payload = bag.getFormatted(Key.key("modularjobs", "boost_data")).orElseThrow();
switch (payload.format()) {
  case 1 -> decodeV1(payload.value());
  case 2 -> decodeV2(payload.value());
  default -> throw new IllegalArgumentException("boost_data format " + payload.format());
}
```

`getBytes(key)` stays empty for formatted slots so callers cannot ignore the id.

Plugins that should not live in ModularJobs (party size, region, …) register a
`DataHandler` and store the typed value themselves:

```java
DataHandlers.register(partySizeHandler);
bag.set(partySizeHandler, 4);
int size = bag.get(partySizeHandler).orElse(0);
```

## Conditions

```java
import dev.mintychochip.databag.Condition;
import dev.mintychochip.databag.ConditionContext;
import dev.mintychochip.databag.Conditions;

Condition netherSneak = Conditions.allOf(
    Conditions.world("world_nether"),
    Conditions.sneaking(true));

boolean matches = netherSneak.test(
    ConditionContext.builder()
        .present(true)
        .worldName("world_nether")
        .sneaking(true)
        .build());
```

Living entity (not a player) and block:

```java
Condition burningZombie = Conditions.allOf(
    Conditions.entityType(Key.key("minecraft:zombie")),
    Conditions.onFire(true));

Condition northChest = Conditions.allOf(
    Conditions.blockId(Key.key("minecraft:chest")),
    Conditions.blockProperty("facing", "north"));

// Player-only — a generic living snapshot fails closed
Condition survival = Conditions.gameMode("survival");
```

On Paper, build the snapshot from a live player:

```java
import dev.mintychochip.databag.paper.PaperConditionContexts;
import java.util.Set;

var ctx = PaperConditionContexts.from(player, Set.of("modularjobs:miner"));
boolean ok = netherSneak.test(ctx);

PaperConditionContexts.fromLiving(zombie);
PaperConditionContexts.fromBlock(block);
```

Death events snapshot nested attacker contexts, and a condition can target a
specific slot instead of `this`:

```java
import dev.mintychochip.databag.EntityTarget;

var ctx = PaperConditionContexts.fromDeath(
    victim, Set.of("modularjobs:miner"), attacker, directAttacker, attackingPlayer);

// evaluated against the attacker slot, not the victim
Condition attackerOnFire =
    Conditions.targeted(EntityTarget.ATTACKER, Conditions.onFire(true));
```

In vanilla JSON the target rides the `entity` field:

```json
{
  "condition": "minecraft:entity_properties",
  "entity": "attacker",
  "predicate": { "flags": { "is_on_fire": true } }
}
```

## JSON (vanilla loot-condition shape)

```java
import dev.mintychochip.databag.gson.GsonConditionSerializer;

var json = GsonConditionSerializer.gson();
byte[] bytes = json.write(netherSneak);
Condition back = json.read(bytes);
```

Sneaking:

```json
{
  "condition": "minecraft:entity_properties",
  "entity": "this",
  "predicate": { "flags": { "is_sneaking": true } }
}
```

Nether + sneaking:

```json
{
  "condition": "minecraft:all_of",
  "terms": [
    { "condition": "modularjobs:world", "world": "world_nether" },
    {
      "condition": "minecraft:entity_properties",
      "entity": "this",
      "predicate": { "flags": { "is_sneaking": true } }
    }
  ]
}
```

Condition graphs stay vanilla JSON bytes in a `byte[]` slot — they are not Kryo
condition classes.

```java
DataBag bag = DataBag.create()
    .setBoolean(Key.key("modularjobs", "enabled"), true)
    .setInt(Key.key("modularjobs", "priority"), 100)
    .setBytes(Key.key("modularjobs", "condition"), json.write(condition));
```

On an item: `PersistentBags.write(stack, namespacedKey, bag)`.

## Extension SPI

Vanilla / `modularjobs:*` kinds stay built-in. Anything ModularJobs should not
own (party, region, …) registers a `ConditionHandler`. JSON fields become a
`DataBag` (`condition:<field>`); evaluation extras live on
`ConditionContext.extras()`.

```java
ConditionHandlers.register(new ConditionHandler() {
  public Key id() { return Key.key("acme", "party_size"); }
  public Condition read(DataBag arguments) {
    int min = arguments.getInt(Key.key("condition", "min")).orElseThrow();
    return ctx -> ctx.extras().getInt(Key.key("acme", "party_size")).orElse(0) >= min;
  }
  public Optional<DataBag> write(Condition condition) { /* … */ }
});
```

```json
{ "condition": "acme:party_size", "min": 3 }
```

Paper fills extras without the conditions library knowing the plugin:

```java
DataBag extras = DataBag.create().setInt(Key.key("acme", "party_size"), partySize);
PaperConditionContexts.from(player, jobKeys, extras);
```

Unregistered ids still throw on read.

## Versioning

CalVer `YY.M.D.REVISION` (example `26.8.19.1`). Local: `0.0.0-SNAPSHOT`.
Release: `./gradlew publishAllPublicationsToLocalBuildRepoRepository -PreleaseVersion=26.8.19.1`.

## Build

```bash
./gradlew test
./gradlew publishAllPublicationsToLocalBuildRepoRepository
```
