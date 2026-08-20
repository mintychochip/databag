package dev.mintychochip.databag;

/**
 * Player-only: matches {@link ConditionContext#flying()}. A generic living
 * entity snapshot without a player fails closed.
 */
public record FlyingCondition(boolean expected) implements Condition {

  @Override
  public boolean test(ConditionContext context) {
    if (!context.present()) {
      return false;
    }
    return context.flying() == expected;
  }
}
