package dev.mintychochip.databag;

import java.util.Objects;

/**
 * Vanilla {@code minecraft:inverted}.
 */
public record InvertedCondition(Condition term) implements Condition {

  public InvertedCondition {
    Objects.requireNonNull(term, "term");
  }

  @Override
  public boolean test(ConditionContext context) {
    return !term.test(context);
  }
}
