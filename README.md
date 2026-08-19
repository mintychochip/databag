# databag

PDC-shaped primitive bag. Java package and Maven group: **`dev.databag`**.

Namespaced keys, PDC-like primitives, no Bukkit. The whole bag encodes to a Kryo
`byte[]` that Paper writes as `PersistentDataType.BYTE_ARRAY`. Missing or
wrong-typed keys are empty, never thrown.

Scalars: boolean, byte, short, int, long, float, double, string, UUID.
Arrays: byte[], int[], long[].

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

## Versioning

CalVer `YY.M.D.REVISION` (example `26.8.19.1`). Local: `0.0.0-SNAPSHOT`.
Release: `./gradlew publishAllPublicationsToLocalBuildRepoRepository -PreleaseVersion=26.8.19.1`.

## Build

```bash
./gradlew test
./gradlew publishAllPublicationsToLocalBuildRepoRepository
```
