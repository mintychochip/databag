package dev.mintychochip.databag;

import net.kyori.adventure.key.Key;

/**
 * Matches a block's id (for example {@code minecraft:chest}).
 */
public record BlockIdCondition(Key blockId) implements Condition {

  @Override
  public boolean test(ConditionContext context) {
    if (context.blockId() == null) {
      return false;
    }
    return BiomeCondition.keysEqual(blockId, context.blockId());
  }
}
