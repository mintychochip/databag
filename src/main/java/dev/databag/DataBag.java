package dev.databag;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import java.util.UUID;
import net.kyori.adventure.key.Key;

/**
 * Light namespaced primitive bag, PDC-shaped but Paper-free. The whole bag
 * encodes to a Kryo {@code byte[]} (boolean, byte, short, int, long, float,
 * double, string, UUID, byte[], int[], long[], formatted bytes). Missing or
 * wrong-typed keys are empty, never thrown.
 *
 * <p>Writes always use the current envelope ({@link #CURRENT_FORMAT}). Reads
 * accept unversioned bags and every known envelope. Unknown envelope versions
 * throw {@link UnknownBagFormatException}. Unknown value tags in v1+ are
 * skipped so new primitives do not need a version bump.
 */
public final class DataBag {

  /** Unversioned map body written before the {@code DBAG} header existed. */
  public static final int UNVERSIONED_FORMAT = Formats.UNVERSIONED;

  /** Current write envelope: magic + version + length-prefixed entries. */
  public static final int CURRENT_FORMAT = Formats.CURRENT;

  private final Map<String, Entry> values = new LinkedHashMap<>();
  private int sourceFormat = CURRENT_FORMAT;

  private DataBag() {}

  public static DataBag create() {
    return new DataBag();
  }

  /**
   * {@code true} when {@code bytes} start with the versioned DataBag magic.
   * Unversioned (v0) bags return {@code false} — they are still readable.
   */
  public static boolean isVersioned(byte[] bytes) {
    return Formats.hasMagic(bytes);
  }

  /** Envelope this bag was decoded from; {@link #CURRENT_FORMAT} if created fresh. */
  public int sourceFormat() {
    return sourceFormat;
  }

  public boolean has(Key key) {
    return values.containsKey(id(key));
  }

  public DataBag setBoolean(Key key, boolean value) {
    values.put(id(key), new Entry(Tags.BOOL, value));
    return this;
  }

  public DataBag setInt(Key key, int value) {
    values.put(id(key), new Entry(Tags.INT, value));
    return this;
  }

  public DataBag setLong(Key key, long value) {
    values.put(id(key), new Entry(Tags.LONG, value));
    return this;
  }

  public DataBag setFloat(Key key, float value) {
    values.put(id(key), new Entry(Tags.FLOAT, value));
    return this;
  }

  public DataBag setDouble(Key key, double value) {
    values.put(id(key), new Entry(Tags.DOUBLE, value));
    return this;
  }

  public DataBag setString(Key key, String value) {
    values.put(id(key), new Entry(Tags.STRING, Objects.requireNonNull(value)));
    return this;
  }

  public DataBag setBytes(Key key, byte[] value) {
    values.put(id(key), new Entry(Tags.BYTES, value.clone()));
    return this;
  }

  /**
   * Stores {@code value} with a caller-defined format id. Use this when the
   * bytes may change encoding later (JSON v1 → JSON v2, binary, …).
   */
  public DataBag setBytes(Key key, int format, byte[] value) {
    values.put(id(key), new Entry(Tags.FORMATTED, new FormattedBytes(format, value)));
    return this;
  }

  /**
   * Stores {@code value} using a registered (or caller-held) {@link DataHandler}.
   */
  public <T> DataBag set(DataHandler<T> handler, T value) {
    Objects.requireNonNull(handler, "handler");
    return setBytes(handler.key(), handler.format(), handler.encode(value));
  }

  public DataBag setByte(Key key, byte value) {
    values.put(id(key), new Entry(Tags.BYTE, value));
    return this;
  }

  public DataBag setShort(Key key, short value) {
    values.put(id(key), new Entry(Tags.SHORT, value));
    return this;
  }

  public DataBag setInts(Key key, int[] value) {
    values.put(id(key), new Entry(Tags.INTS, value.clone()));
    return this;
  }

  public DataBag setLongs(Key key, long[] value) {
    values.put(id(key), new Entry(Tags.LONGS, value.clone()));
    return this;
  }

  public DataBag setUuid(Key key, UUID value) {
    values.put(id(key), new Entry(Tags.UUID, Objects.requireNonNull(value)));
    return this;
  }

  public Optional<Boolean> getBoolean(Key key) {
    return typed(key, Tags.BOOL, Boolean.class);
  }

  public OptionalInt getInt(Key key) {
    return typed(key, Tags.INT, Integer.class).map(OptionalInt::of).orElseGet(OptionalInt::empty);
  }

  public OptionalLong getLong(Key key) {
    return typed(key, Tags.LONG, Long.class).map(OptionalLong::of).orElseGet(OptionalLong::empty);
  }

  public Optional<Float> getFloat(Key key) {
    return typed(key, Tags.FLOAT, Float.class);
  }

  public OptionalDouble getDouble(Key key) {
    return typed(key, Tags.DOUBLE, Double.class).map(OptionalDouble::of).orElseGet(OptionalDouble::empty);
  }

  public Optional<String> getString(Key key) {
    return typed(key, Tags.STRING, String.class);
  }

  public Optional<byte[]> getBytes(Key key) {
    return typed(key, Tags.BYTES, byte[].class).map(byte[]::clone);
  }

  public Optional<FormattedBytes> getFormatted(Key key) {
    return typed(key, Tags.FORMATTED, FormattedBytes.class);
  }

  /**
   * Reads a typed value with {@code handler}. Empty when the key is missing,
   * the format id does not match, or the slot is not formatted bytes.
   */
  public <T> Optional<T> get(DataHandler<T> handler) {
    Objects.requireNonNull(handler, "handler");
    Optional<FormattedBytes> formatted = getFormatted(handler.key());
    if (formatted.isEmpty() || formatted.get().format() != handler.format()) {
      return Optional.empty();
    }
    return Optional.of(handler.decode(formatted.get().value()));
  }

  /** Namespaced keys present in this bag, in insertion order. */
  public Set<Key> keys() {
    Set<Key> keys = new LinkedHashSet<>();
    for (String raw : values.keySet()) {
      keys.add(Key.key(raw));
    }
    return Set.copyOf(keys);
  }

  public Optional<Byte> getByte(Key key) {
    return typed(key, Tags.BYTE, Byte.class);
  }

  public Optional<Short> getShort(Key key) {
    return typed(key, Tags.SHORT, Short.class);
  }

  public Optional<int[]> getInts(Key key) {
    return typed(key, Tags.INTS, int[].class).map(int[]::clone);
  }

  public Optional<long[]> getLongs(Key key) {
    return typed(key, Tags.LONGS, long[].class).map(long[]::clone);
  }

  public Optional<UUID> getUuid(Key key) {
    return typed(key, Tags.UUID, UUID.class);
  }

  /**
   * Kryo-framed byte array of this bag in {@link #CURRENT_FORMAT}. This is the
   * payload written to a Paper PDC as {@code PersistentDataType.BYTE_ARRAY}.
   */
  public byte[] toBytes() {
    return Formats.write(this);
  }

  public static DataBag fromBytes(byte[] bytes) {
    return Formats.read(bytes);
  }

  int size() {
    return values.size();
  }

  Iterable<Map.Entry<String, Entry>> entries() {
    return values.entrySet();
  }

  void putRaw(String key, byte tag, Object value) {
    values.put(key, new Entry(tag, value));
  }

  void sourceFormat(int version) {
    this.sourceFormat = version;
  }

  private <T> Optional<T> typed(Key key, byte tag, Class<T> type) {
    Entry entry = values.get(id(key));
    if (entry == null || entry.tag != tag) {
      return Optional.empty();
    }
    return Optional.of(type.cast(entry.value));
  }

  private static String id(Key key) {
    return Objects.requireNonNull(key, "key").asString();
  }

  record Entry(byte tag, Object value) {}
}
