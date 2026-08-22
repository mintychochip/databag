package dev.mintychochip.databag;

import java.util.List;

/**
 * Passes if the inner condition matches any passenger.
 */
public record PassengerCondition(Condition condition) implements Condition {

  @Override
  public boolean test(ConditionContext context) {
    List<ConditionContext> list = context.passengers();
    if (list == null || list.isEmpty()) {
      return false;
    }
    for (ConditionContext p : list) {
      if (condition.test(p)) {
        return true;
      }
    }
    return false;
  }
}
