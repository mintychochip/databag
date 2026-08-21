package dev.mintychochip.databag;

import net.kyori.adventure.key.Key;

/**
 * Matches the snapshot dimension key.
 */
public record DimensionCondition(Key dimensionKey) implements Condition {

  @Override
  public boolean test(ConditionContext context) {
    return context.worldKey() != null
        && BiomeCondition.keysEqual(dimensionKey, context.worldKey());
  }
}
