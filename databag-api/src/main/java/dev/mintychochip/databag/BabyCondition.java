package dev.mintychochip.databag;

/**
 * Matches {@link ConditionContext#baby()} on a living entity.
 */
public record BabyCondition(boolean expected) implements Condition {

  @Override
  public boolean test(ConditionContext context) {
    if (!context.livingPresent()) {
      return false;
    }
    return context.baby() == expected;
  }
}
