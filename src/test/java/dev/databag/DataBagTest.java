package dev.databag;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.UUID;
import net.kyori.adventure.key.Key;
import org.junit.jupiter.api.Test;

class DataBagTest {

  private static final Key FLAG = Key.key("databag", "flag");
  private static final Key COUNT = Key.key("databag", "count");
  private static final Key TICKS = Key.key("databag", "ticks");
  private static final Key RATIO = Key.key("databag", "ratio");
  private static final Key AMOUNT = Key.key("databag", "amount");
  private static final Key NAME = Key.key("databag", "name");
  private static final Key BLOB = Key.key("databag", "blob");
  private static final Key SLOT = Key.key("databag", "slot");
  private static final Key META = Key.key("databag", "meta");
  private static final Key SLOTS = Key.key("databag", "slots");
  private static final Key TIMES = Key.key("databag", "times");
  private static final Key OWNER = Key.key("databag", "owner");
  private static final Key MISSING = Key.key("databag", "missing");

  @Test
  void roundTripsMixedPrimitivesThroughKryoBytes() {
    DataBag bag = DataBag.create()
        .setBoolean(FLAG, true)
        .setInt(COUNT, 42)
        .setLong(TICKS, 9_000_000_000L)
        .setFloat(RATIO, 1.5f)
        .setDouble(AMOUNT, 3.25d)
        .setString(NAME, "mining_helmet")
        .setBytes(BLOB, new byte[] {1, 2, 3, 4})
        .setByte(SLOT, (byte) 7)
        .setShort(META, (short) 300)
        .setInts(SLOTS, new int[] {0, 1, 39})
        .setLongs(TIMES, new long[] {10L, 20L})
        .setUuid(OWNER, UUID.fromString("11111111-2222-3333-4444-555555555555"));

    byte[] encoded = bag.toBytes();
    assertTrue(encoded.length > 0);

    DataBag restored = DataBag.fromBytes(encoded);
    assertEquals(Optional.of(true), restored.getBoolean(FLAG));
    assertEquals(OptionalInt.of(42), restored.getInt(COUNT));
    assertEquals(OptionalLong.of(9_000_000_000L), restored.getLong(TICKS));
    assertEquals(1.5f, restored.getFloat(RATIO).orElseThrow(), 0.0001f);
    assertEquals(OptionalDouble.of(3.25d), restored.getDouble(AMOUNT));
    assertEquals(Optional.of("mining_helmet"), restored.getString(NAME));
    assertArrayEquals(new byte[] {1, 2, 3, 4}, restored.getBytes(BLOB).orElseThrow());
    assertEquals(Optional.of((byte) 7), restored.getByte(SLOT));
    assertEquals(Optional.of((short) 300), restored.getShort(META));
    assertArrayEquals(new int[] {0, 1, 39}, restored.getInts(SLOTS).orElseThrow());
    assertArrayEquals(new long[] {10L, 20L}, restored.getLongs(TIMES).orElseThrow());
    assertEquals(
        Optional.of(UUID.fromString("11111111-2222-3333-4444-555555555555")),
        restored.getUuid(OWNER));
  }

  @Test
  void missingKeyIsEmptyNotThrown() {
    DataBag bag = DataBag.create().setInt(COUNT, 1);
    DataBag restored = DataBag.fromBytes(bag.toBytes());
    assertFalse(restored.has(MISSING));
    assertTrue(restored.getBoolean(MISSING).isEmpty());
    assertTrue(restored.getInt(MISSING).isEmpty());
    assertTrue(restored.getLong(MISSING).isEmpty());
    assertTrue(restored.getFloat(MISSING).isEmpty());
    assertTrue(restored.getDouble(MISSING).isEmpty());
    assertTrue(restored.getString(MISSING).isEmpty());
    assertTrue(restored.getBytes(MISSING).isEmpty());
    assertTrue(restored.getByte(MISSING).isEmpty());
    assertTrue(restored.getShort(MISSING).isEmpty());
    assertTrue(restored.getInts(MISSING).isEmpty());
    assertTrue(restored.getLongs(MISSING).isEmpty());
    assertTrue(restored.getUuid(MISSING).isEmpty());
  }

  @Test
  void wrongTypeIsEmptyNotThrown() {
    DataBag bag = DataBag.fromBytes(DataBag.create().setString(NAME, "x").toBytes());
    assertTrue(bag.getInt(NAME).isEmpty());
    assertTrue(bag.getBytes(NAME).isEmpty());
  }
}
