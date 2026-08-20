package dev.mintychochip.databag;

/**
 * Pure predicate over a {@link ConditionContext}. Implementations must not look
 * up Bukkit state. The snapshot may describe a player, a living entity, a
 * block, or several of those at once.
 */
@FunctionalInterface
public interface Condition {

  /**
   * Returns {@code true} when this predicate holds for {@code context}.
   */
  boolean test(ConditionContext context);

  default Condition and(Condition other) {
    return Conditions.allOf(this, other);
  }

  default Condition or(Condition other) {
    return Conditions.anyOf(this, other);
  }

  default Condition negate() {
    return Conditions.inverted(this);
  }
}
