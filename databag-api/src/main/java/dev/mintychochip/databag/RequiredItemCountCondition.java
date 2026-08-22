package dev.mintychochip.databag;

import net.kyori.adventure.key.Key;

/**
 * Checks the player has at least {@code minimum} of {@code material} in inventory.
 */
public record RequiredItemCountCondition(Key material, int minimum) implements Condition {

  @Override
  public boolean test(ConditionContext context) {
    if (context.itemCounts() == null) {
      return false;
    }
    Integer count = context.itemCounts().get(material);
    return count != null && count >= minimum;
  }
}
