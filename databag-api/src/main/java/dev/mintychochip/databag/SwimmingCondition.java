package dev.mintychochip.databag;

/**
 * Matches {@link ConditionContext#swimming()} on a living entity.
 */
public record SwimmingCondition(boolean expected) implements Condition {

  @Override
  public boolean test(ConditionContext context) {
    if (!context.livingPresent()) {
      return false;
    }
    return context.swimming() == expected;
  }
}
