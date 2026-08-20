package dev.mintychochip.databag;

/**
 * Reader/writer for {@link Condition} graphs. Bytes are vanilla loot-condition
 * JSON (UTF-8). This is the conditions-API replacement for Kryo codecs.
 */
public interface ConditionSerializer {

  /**
   * Serializes {@code condition} to vanilla-shaped JSON bytes.
   */
  byte[] write(Condition condition);

  /**
   * Parses vanilla-shaped JSON bytes into a {@link Condition}.
   *
   * @throws IllegalArgumentException when the payload is not an object or the
   *     condition id is unknown / malformed
   */
  Condition read(byte[] bytes);
}
