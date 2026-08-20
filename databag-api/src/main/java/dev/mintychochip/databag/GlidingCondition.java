package dev.mintychochip.databag;

/**
 * Matches {@link ConditionContext#gliding()} on a living entity (elytra).
 */
public record GlidingCondition(boolean expected) implements Condition {

  @Override
  public boolean test(ConditionContext context) {
    if (!context.livingPresent()) {
      return false;
    }
    return context.gliding() == expected;
  }
}
