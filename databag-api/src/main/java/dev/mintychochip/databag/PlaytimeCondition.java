package dev.mintychochip.databag;

import java.math.BigDecimal;

/**
 * Matches the player's total playtime in ticks against a relational threshold.
 */
public record PlaytimeCondition(RelationalOperator operator, long ticks) implements Condition {

  @Override
  public boolean test(ConditionContext context) {
    if (context.present() == false) {
      return false;
    }
    Long actual = context.playtime();
    if (actual == null) {
      return false;
    }
    return operator.test(BigDecimal.valueOf(actual), BigDecimal.valueOf(ticks));
  }
}
