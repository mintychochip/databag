package dev.mintychochip.databag;

import java.math.BigDecimal;

/**
 * Matches the entity's invulnerability frames (no-damage ticks).
 */
public record InvulnerableFramesCondition(RelationalOperator operator, int ticks)
    implements Condition {

  @Override
  public boolean test(ConditionContext context) {
    Integer actual = context.invulnerableTicks();
    if (actual == null) {
      return false;
    }
    return operator.test(BigDecimal.valueOf(actual), BigDecimal.valueOf(ticks));
  }
}
