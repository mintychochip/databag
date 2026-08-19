package dev.databag;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.esotericsoftware.kryo.io.Output;
import java.util.OptionalInt;
import net.kyori.adventure.key.Key;
import org.junit.jupiter.api.Test;

class DataBagFormatTest {

  private static final Key COUNT = Key.key("databag", "count");
  private static final Key NAME = Key.key("databag", "name");
  private static final Key BLOB = Key.key("databag", "blob");
  private static final Key FUTURE = Key.key("databag", "future");

  @Test
  void writesCurrentEnvelopeWithMagic() {
    byte[] encoded = DataBag.create().setInt(COUNT, 1).toBytes();
    assertTrue(DataBag.isVersioned(encoded));
    assertEquals('D', encoded[0]);
    assertEquals('B', encoded[1]);
    assertEquals('A', encoded[2]);
    assertEquals('G', encoded[3]);
    assertEquals(DataBag.CURRENT_FORMAT, encoded[4]);
    assertEquals(DataBag.CURRENT_FORMAT, DataBag.fromBytes(encoded).sourceFormat());
  }

  @Test
  void readsUnversionedBodyAndUpgradesOnWrite() {
    DataBag original = DataBag.create().setInt(COUNT, 42).setString(NAME, "mining_helmet");
    byte[] v0 = Formats.writeUnversioned(original);
    assertFalse(DataBag.isVersioned(v0));

    DataBag restored = DataBag.fromBytes(v0);
    assertEquals(DataBag.UNVERSIONED_FORMAT, restored.sourceFormat());
    assertEquals(OptionalInt.of(42), restored.getInt(COUNT));
    assertEquals("mining_helmet", restored.getString(NAME).orElseThrow());

    byte[] upgraded = restored.toBytes();
    assertTrue(DataBag.isVersioned(upgraded));
    DataBag fromV1 = DataBag.fromBytes(upgraded);
    assertEquals(OptionalInt.of(42), fromV1.getInt(COUNT));
    assertEquals("mining_helmet", fromV1.getString(NAME).orElseThrow());
  }

  @Test
  void unknownEnvelopeVersionThrows() {
    byte[] bytes = {'D', 'B', 'A', 'G', 99};
    UnknownBagFormatException ex =
        assertThrows(UnknownBagFormatException.class, () -> DataBag.fromBytes(bytes));
    assertEquals(99, ex.version());
  }

  @Test
  void v1SkipsUnknownTagsSoNewPrimitivesDoNotBumpVersion() {
    Output intPayload = new Output(16, -1);
    PrimitiveIO.write(intPayload, Tags.INT, 7);
    byte[] intBytes = intPayload.toBytes();

    Output unknownPayload = new Output(16, -1);
    unknownPayload.writeString("ignored");
    byte[] unknownBytes = unknownPayload.toBytes();

    Output body = new Output(64, -1);
    body.writeVarInt(2, true);
    body.writeString(COUNT.asString());
    body.writeByte(Tags.INT);
    body.writeVarInt(intBytes.length, true);
    body.writeBytes(intBytes);
    body.writeString(FUTURE.asString());
    body.writeByte((byte) 99);
    body.writeVarInt(unknownBytes.length, true);
    body.writeBytes(unknownBytes);

    Output full = new Output(128, -1);
    full.writeBytes(Formats.MAGIC);
    full.writeByte(Formats.V1);
    full.writeBytes(body.toBytes());

    DataBag bag = DataBag.fromBytes(full.toBytes());
    assertEquals(OptionalInt.of(7), bag.getInt(COUNT));
    assertFalse(bag.has(FUTURE));
  }

  @Test
  void formattedBytesCarryFormatIdForPayloadMigrations() {
    byte[] json = "{\"v\":1}".getBytes();
    DataBag bag = DataBag.create().setBytes(BLOB, 1, json);
    DataBag restored = DataBag.fromBytes(bag.toBytes());

    FormattedBytes formatted = restored.getFormatted(BLOB).orElseThrow();
    assertEquals(1, formatted.format());
    assertArrayEquals(json, formatted.value());
    assertTrue(restored.getBytes(BLOB).isEmpty());
  }

  @Test
  void unversionedUnknownTagStillThrows() {
    Output output = new Output(32, -1);
    output.writeVarInt(1, true);
    output.writeString(NAME.asString());
    output.writeByte((byte) 99);
    output.writeByte((byte) 0);
    assertThrows(IllegalArgumentException.class, () -> DataBag.fromBytes(output.toBytes()));
  }
}
