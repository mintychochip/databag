package dev.mintychochip.databag;

import java.util.Objects;

/**
 * Evaluates an inner location predicate against a context-relative offset.
 */
public record LocationOffsetCondition(
    int offsetX,
    int offsetY,
    int offsetZ,
    Condition condition) implements Condition {

  public LocationOffsetCondition {
    Objects.requireNonNull(condition, "condition");
  }

  @Override
  public boolean test(ConditionContext context) {
    OffsetContextResolver resolver = context.offsetResolver();
    if (resolver == null) {
      return false;
    }
    ConditionContext offset = resolver.resolve(offsetX, offsetY, offsetZ);
    if (offset == null) {
      return false;
    }
    return condition.test(offset);
  }
}
