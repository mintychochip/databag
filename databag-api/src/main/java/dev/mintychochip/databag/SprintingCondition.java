package dev.mintychochip.databag;

/**
 * Matches {@link ConditionContext#sprinting()} against {@code expected}.
 */
public record SprintingCondition(boolean expected) implements Condition {

  @Override
  public boolean test(ConditionContext context) {
    if (!context.livingPresent()) {
      return false;
    }
    return context.sprinting() == expected;
  }
}
