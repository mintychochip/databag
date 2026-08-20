package dev.mintychochip.databag;

import net.kyori.adventure.key.Key;

/**
 * Matches the snapshot biome against {@code biomeKey}.
 */
public record BiomeCondition(Key biomeKey) implements Condition {

  @Override
  public boolean test(ConditionContext context) {
    if (!context.present() || context.biome() == null) {
      return false;
    }
    return keysEqual(biomeKey, context.biome());
  }

  static boolean keysEqual(Key expected, Key actual) {
    return expected.equals(actual)
        || expected.value().equalsIgnoreCase(actual.value())
        || expected.asString().equalsIgnoreCase(actual.asString());
  }
}
