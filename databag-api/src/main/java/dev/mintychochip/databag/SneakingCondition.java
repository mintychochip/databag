package dev.mintychochip.databag;

/**
 * Matches {@link ConditionContext#sneaking()} against {@code expected}.
 */
public record SneakingCondition(boolean expected) implements Condition {

  @Override
  public boolean test(ConditionContext context) {
    if (!context.livingPresent()) {
      return false;
    }
    return context.sneaking() == expected;
  }
}
