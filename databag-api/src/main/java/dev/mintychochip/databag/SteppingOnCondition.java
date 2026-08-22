package dev.mintychochip.databag;

import java.util.Objects;

/**
 * Passes when the inner condition matches the block the entity is standing on.
 */
public record SteppingOnCondition(Condition condition) implements Condition {

  public SteppingOnCondition {
    Objects.requireNonNull(condition, "condition");
  }

  @Override
  public boolean test(ConditionContext context) {
    ConditionContext standing = context.standingOn();
    if (standing == null) {
      return false;
    }
    return condition.test(standing);
  }
}
