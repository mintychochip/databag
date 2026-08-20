package dev.mintychochip.databag;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.key.Key;

/**
 * Process-wide registry of {@link DataHandler}s. Keys must be unique.
 */
public final class DataHandlers {

  private static final ConcurrentHashMap<String, DataHandler<?>> BY_KEY = new ConcurrentHashMap<>();

  private DataHandlers() {}

  public static <T> void register(DataHandler<T> handler) {
    Objects.requireNonNull(handler, "handler");
    String id = handler.key().asString();
    DataHandler<?> previous = BY_KEY.putIfAbsent(id, handler);
    if (previous != null && previous != handler) {
      throw new IllegalStateException("DataHandler already registered for " + id);
    }
  }

  public static void unregister(Key key) {
    BY_KEY.remove(Objects.requireNonNull(key, "key").asString());
  }

  @SuppressWarnings("unchecked")
  public static <T> Optional<DataHandler<T>> get(Key key) {
    return Optional.ofNullable((DataHandler<T>) BY_KEY.get(
        Objects.requireNonNull(key, "key").asString()));
  }
}
