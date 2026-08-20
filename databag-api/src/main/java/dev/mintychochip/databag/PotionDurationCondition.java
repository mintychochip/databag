package dev.mintychochip.databag;

import java.math.BigDecimal;
import net.kyori.adventure.key.Key;

/**
 * Compares a potion effect remaining duration (ticks) to {@code expected}.
 */
public record PotionDurationCondition(
    Key effectKey, RelationalOperator operator, int expected) implements Condition {

  @Override
  public boolean test(ConditionContext context) {
    if (!context.livingPresent()) {
      return false;
    }
    for (var entry : context.effects().entrySet()) {
      if (BiomeCondition.keysEqual(effectKey, entry.getKey())) {
        return operator.test(
            BigDecimal.valueOf(entry.getValue().duration()),
            BigDecimal.valueOf(expected));
      }
    }
    return false;
  }
}
