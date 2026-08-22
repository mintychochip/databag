package dev.mintychochip.databag;

/**
 * Applies an inner condition to the entity's vehicle.
 */
public record VehicleCondition(Condition condition) implements Condition {

  @Override
  public boolean test(ConditionContext context) {
    ConditionContext v = context.vehicle();
    return v != null && condition.test(v);
  }
}
