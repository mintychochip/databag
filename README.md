# databag

PDC-shaped primitive bag. Java package and Maven group: **`dev.databag`**.

Namespaced keys, PDC-like primitives, no Bukkit. The whole bag encodes to a Kryo
`byte[]` that Paper writes as `PersistentDataType.BYTE_ARRAY`. Missing or
wrong-typed keys are empty, never thrown.

Scalars: boolean, byte, short, int, long, float, double, string, UUID.
Arrays: byte[], int[], long[]. Formatted: `byte[]` + format id.

```java
import dev.databag.DataBag;
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

## Versioning

CalVer `YY.M.D.REVISION` (example `26.8.19.1`). Local: `0.0.0-SNAPSHOT`.
Release: `./gradlew publishAllPublicationsToLocalBuildRepoRepository -PreleaseVersion=26.8.19.1`.

## Build

```bash
./gradlew test
./gradlew publishAllPublicationsToLocalBuildRepoRepository
```
