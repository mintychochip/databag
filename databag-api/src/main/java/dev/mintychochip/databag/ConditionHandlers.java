package dev.mintychochip.databag;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.key.Key;

/**
 * Process-wide registry of {@link ConditionHandler}s. Built-in vanilla /
 * {@code modularjobs:*} kinds stay in the gson serializer; this is for
 * third-party ids so ModularJobs does not own them.
 */
public final class ConditionHandlers {

  private static final ConcurrentHashMap<String, ConditionHandler> BY_ID = new ConcurrentHashMap<>();

  private ConditionHandlers() {}

  public static void register(ConditionHandler handler) {
    Objects.requireNonNull(handler, "handler");
    String id = handler.id().asString();
    ConditionHandler previous = BY_ID.putIfAbsent(id, handler);
    if (previous != null && previous != handler) {
      throw new IllegalStateException("ConditionHandler already registered for " + id);
    }
  }

  public static void unregister(Key id) {
    BY_ID.remove(Objects.requireNonNull(id, "id").asString());
  }

  public static Optional<ConditionHandler> get(Key id) {
    return Optional.ofNullable(BY_ID.get(Objects.requireNonNull(id, "id").asString()));
  }

  public static Optional<ConditionHandler> get(String rawId) {
    if (rawId == null || rawId.isBlank()) {
      return Optional.empty();
    }
    ConditionHandler exact = BY_ID.get(rawId);
    if (exact != null) {
      return Optional.of(exact);
    }
    int colon = rawId.indexOf(':');
    if (colon <= 0) {
      return Optional.empty();
    }
    try {
      return get(Key.key(rawId));
    } catch (RuntimeException ignored) {
      return Optional.empty();
    }
  }

  public static Optional<ConditionHandler> findWriter(Condition condition) {
    Objects.requireNonNull(condition, "condition");
    for (ConditionHandler handler : BY_ID.values()) {
      if (handler.write(condition).isPresent()) {
        return Optional.of(handler);
      }
    }
    return Optional.empty();
  }
}
