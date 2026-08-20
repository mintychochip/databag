package dev.mintychochip.databag;

/**
 * Matches {@link ConditionContext#onFire()} on a living entity.
 */
public record OnFireCondition(boolean expected) implements Condition {

  @Override
  public boolean test(ConditionContext context) {
    if (!context.livingPresent()) {
      return false;
    }
    return context.onFire() == expected;
  }
}
