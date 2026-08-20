package dev.mintychochip.databag;

import java.util.List;

/**
 * Vanilla {@code minecraft:all_of}: every term holds. Empty terms is true.
 */
public record AllOfCondition(List<Condition> terms) implements Condition {

  public AllOfCondition {
    terms = List.copyOf(terms);
  }

  @Override
  public boolean test(ConditionContext context) {
    for (Condition term : terms) {
      if (!term.test(context)) {
        return false;
      }
    }
    return true;
  }
}
