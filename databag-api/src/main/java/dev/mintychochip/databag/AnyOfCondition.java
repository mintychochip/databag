package dev.mintychochip.databag;

import java.util.List;

/**
 * Vanilla {@code minecraft:any_of}: at least one term holds. Empty terms is false.
 */
public record AnyOfCondition(List<Condition> terms) implements Condition {

  public AnyOfCondition {
    terms = List.copyOf(terms);
  }

  @Override
  public boolean test(ConditionContext context) {
    for (Condition term : terms) {
      if (term.test(context)) {
        return true;
      }
    }
    return false;
  }
}
