package dev.mintychochip.databag;

/**
 * Matches the player's network ping against a relational threshold.
 * Fails closed when no player is present or ping is unavailable.
 */
public record PingCondition(RelationalOperator operator, int milliseconds) implements Condition {

  @Override
  public boolean test(ConditionContext context) {
    if (!context.present()) {
      return false;
    }
    Integer actual = context.ping();
    if (actual == null) {
      return false;
    }
    return operator.test(java.math.BigDecimal.valueOf(actual), java.math.BigDecimal.valueOf(milliseconds));
  }
}
