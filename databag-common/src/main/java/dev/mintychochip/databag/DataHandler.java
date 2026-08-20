package dev.mintychochip.databag;

import net.kyori.adventure.key.Key;

/**
 * Codec for a typed value stored at a namespaced key. Plugins register
 * handlers so {@link DataBag} can carry domain objects without knowing those
 * types.
 */
public interface DataHandler<T> {

  /** Bag key this handler reads and writes. */
  Key key();

  /** Payload format id stored with the bytes (see {@link DataBag#setBytes(Key, int, byte[])}). */
  int format();

  byte[] encode(T value);

  T decode(byte[] bytes);
}
