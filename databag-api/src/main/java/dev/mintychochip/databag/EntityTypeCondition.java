package dev.mintychochip.databag;

import net.kyori.adventure.key.Key;

/**
 * Matches a living entity's type key (for example {@code minecraft:zombie}).
 */
public record EntityTypeCondition(Key entityType) implements Condition {

  @Override
  public boolean test(ConditionContext context) {
    if (!context.livingPresent() || context.entityType() == null) {
      return false;
    }
    return BiomeCondition.keysEqual(entityType, context.entityType());
  }
}
