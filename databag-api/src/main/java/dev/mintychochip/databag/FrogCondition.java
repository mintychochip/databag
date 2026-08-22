package dev.mintychochip.databag;

import net.kyori.adventure.key.Key;

/**
 * Matches a frog variant (minecraft:cold, minecraft:temperate, minecraft:warm).
 */
public record FrogCondition(Key variant) implements Condition {

  @Override
  public boolean test(ConditionContext context) {
    return context.frogVariant() != null && context.frogVariant().equals(variant);
  }
}
