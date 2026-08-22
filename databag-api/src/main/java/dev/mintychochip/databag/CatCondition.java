package dev.mintychochip.databag;

import net.kyori.adventure.key.Key;

/**
 * Matches a cat variant (e.g. minecraft:siamese).
 */
public record CatCondition(Key variant) implements Condition {

  @Override
  public boolean test(ConditionContext context) {
    return context.catVariant() != null && context.catVariant().equals(variant);
  }
}
