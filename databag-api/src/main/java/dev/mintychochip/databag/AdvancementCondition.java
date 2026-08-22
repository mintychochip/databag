package dev.mintychochip.databag;

import net.kyori.adventure.key.Key;

/**
 * Checks the player has completed the given advancement.
 */
public record AdvancementCondition(Key advancement) implements Condition {

  @Override
  public boolean test(ConditionContext context) {
    return context.advancements() != null && context.advancements().contains(advancement.asString());
  }
}
