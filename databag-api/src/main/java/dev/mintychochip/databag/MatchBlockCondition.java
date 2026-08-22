package dev.mintychochip.databag;

import java.util.Objects;

/**
 * Matches the block subject in the current condition context.
 */
public record MatchBlockCondition(Condition condition) implements Condition {

  public MatchBlockCondition {
    Objects.requireNonNull(condition, "condition");
  }

  @Override
  public boolean test(ConditionContext context) {
    return condition.test(context);
  }
}
