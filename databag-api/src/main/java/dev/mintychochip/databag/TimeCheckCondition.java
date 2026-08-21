package dev.mintychochip.databag;

import org.jetbrains.annotations.Nullable;

/**
 * Matches world time against optional min/max bounds, optionally modulo {@code period}.
 */
public record TimeCheckCondition(
    @Nullable Long min, @Nullable Long max, @Nullable Long period) implements Condition {

  @Override
  public boolean test(ConditionContext context) {
    Long dayTime = context.dayTime();
    if (dayTime == null) {
      return false;
    }
    long t = (period == null || period <= 0) ? dayTime : Math.floorMod(dayTime, period);
    return (min == null || t >= min) && (max == null || t <= max);
  }
}
