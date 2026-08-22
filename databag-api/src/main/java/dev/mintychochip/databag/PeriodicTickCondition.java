package dev.mintychochip.databag;

/**
 * Matches when (ticksLived - offset) is divisible by period.
 */
public record PeriodicTickCondition(int period, int offset) implements Condition {

  public PeriodicTickCondition {
    if (period <= 0) {
      throw new IllegalArgumentException("period must be > 0, got " + period);
    }
  }

  @Override
  public boolean test(ConditionContext context) {
    Integer lived = context.ticksLived();
    if (lived == null) {
      return false;
    }
    return (lived - offset) % period == 0;
  }
}
