package dev.mintychochip.databag;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import java.util.Map;

/**
 * Bag envelope versions. {@code 0} is the unversioned map body shipped first.
 * {@code 1} prefixes {@link #MAGIC} plus a version byte and length-prefixes
 * each value so unknown tags can be skipped.
 */
final class Formats {

  static final byte[] MAGIC = {'D', 'B', 'A', 'G'};
  static final int UNVERSIONED = 0;
  static final int V1 = 1;
  static final int CURRENT = V1;

  private Formats() {}

  static boolean hasMagic(byte[] bytes) {
    if (bytes == null || bytes.length < MAGIC.length + 1) {
      return false;
    }
    for (int i = 0; i < MAGIC.length; i++) {
      if (bytes[i] != MAGIC[i]) {
        return false;
      }
    }
    return true;
  }

  static byte[] write(DataBag bag) {
    Output output = new Output(256, -1);
    output.writeBytes(MAGIC);
    output.writeByte(CURRENT);
    writeV1Body(output, bag);
    return output.toBytes();
  }

  static DataBag read(byte[] bytes) {
    if (bytes == null || bytes.length == 0) {
      return DataBag.create();
    }
    if (hasMagic(bytes)) {
      int version = Byte.toUnsignedInt(bytes[MAGIC.length]);
      if (version != V1) {
        throw new UnknownBagFormatException(version);
      }
      Input input = new Input(bytes, MAGIC.length + 1, bytes.length - MAGIC.length - 1);
      DataBag bag = readV1Body(input);
      bag.sourceFormat(V1);
      return bag;
    }
    DataBag bag = readV0Body(new Input(bytes));
    bag.sourceFormat(UNVERSIONED);
    return bag;
  }

  static byte[] writeUnversioned(DataBag bag) {
    Output output = new Output(256, -1);
    writeV0Body(output, bag);
    return output.toBytes();
  }

  private static void writeV0Body(Output output, DataBag bag) {
    output.writeVarInt(bag.size(), true);
    for (Map.Entry<String, DataBag.Entry> e : bag.entries()) {
      output.writeString(e.getKey());
      DataBag.Entry entry = e.getValue();
      output.writeByte(entry.tag());
      PrimitiveIO.write(output, entry.tag(), entry.value());
    }
  }

  private static DataBag readV0Body(Input input) {
    DataBag bag = DataBag.create();
    int count = input.readVarInt(true);
    for (int i = 0; i < count; i++) {
      String key = input.readString();
      byte tag = input.readByte();
      bag.putRaw(key, tag, PrimitiveIO.read(input, tag));
    }
    return bag;
  }

  private static void writeV1Body(Output output, DataBag bag) {
    output.writeVarInt(bag.size(), true);
    for (Map.Entry<String, DataBag.Entry> e : bag.entries()) {
      output.writeString(e.getKey());
      DataBag.Entry entry = e.getValue();
      output.writeByte(entry.tag());
      Output payload = new Output(32, -1);
      PrimitiveIO.write(payload, entry.tag(), entry.value());
      byte[] blob = payload.toBytes();
      output.writeVarInt(blob.length, true);
      output.writeBytes(blob);
    }
  }

  private static DataBag readV1Body(Input input) {
    DataBag bag = DataBag.create();
    int count = input.readVarInt(true);
    for (int i = 0; i < count; i++) {
      String key = input.readString();
      byte tag = input.readByte();
      int length = input.readVarInt(true);
      byte[] payload = input.readBytes(length);
      if (!Tags.known(tag)) {
        continue;
      }
      bag.putRaw(key, tag, PrimitiveIO.read(new Input(payload), tag));
    }
    return bag;
  }
}
