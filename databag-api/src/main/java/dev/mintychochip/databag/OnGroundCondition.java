package dev.mintychochip.databag;

/**
 * Matches {@link ConditionContext#onGround()} on a living entity.
 */
public record OnGroundCondition(boolean expected) implements Condition {

  @Override
  public boolean test(ConditionContext context) {
    if (!context.livingPresent()) {
      return false;
    }
    return context.onGround() == expected;
  }
}
