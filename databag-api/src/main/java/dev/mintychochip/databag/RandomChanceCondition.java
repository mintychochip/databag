package dev.mintychochip.databag;

import java.util.Random;

/**
 * Passes with the given probability, using the context's Random instance.
 */
public record RandomChanceCondition(double chance) implements Condition {

  public RandomChanceCondition {
    if (Double.isNaN(chance) || chance < 0.0 || chance > 1.0) {
      throw new IllegalArgumentException("chance must be in [0, 1], got " + chance);
    }
  }

  @Override
  public boolean test(ConditionContext context) {
    Random r = context.random();
    if (r == null) {
      return false;
    }
    return r.nextDouble() < chance;
  }
}
