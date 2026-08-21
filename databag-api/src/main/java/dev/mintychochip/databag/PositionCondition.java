package dev.mintychochip.databag;

import org.jetbrains.annotations.Nullable;

/**
 * Matches snapshot coordinates against optional per-axis bounds.
 */
public record PositionCondition(
    @Nullable Double minX,
    @Nullable Double maxX,
    @Nullable Double minY,
    @Nullable Double maxY,
    @Nullable Double minZ,
    @Nullable Double maxZ)
    implements Condition {

  @Override
  public boolean test(ConditionContext context) {
    Double x = context.x();
    Double y = context.y();
    Double z = context.z();
    if (x == null || y == null || z == null) {
      return false;
    }
    return withinBounds(x, minX, maxX)
        && withinBounds(y, minY, maxY)
        && withinBounds(z, minZ, maxZ);
  }

  private static boolean withinBounds(double value, @Nullable Double min, @Nullable Double max) {
    if (min != null && value < min) {
      return false;
    }
    if (max != null && value > max) {
      return false;
    }
    return true;
  }
}
