package dev.mintychochip.databag;

/**
 * Checks a block-state property value falls within [min, max].
 */
public record BlockPropertyRangeCondition(String name, int min, int max) implements Condition {

  @Override
  public boolean test(ConditionContext context) {
    String raw = context.blockProperties().get(name);
    if (raw == null) {
      return false;
    }
    int value;
    try {
      value = Integer.parseInt(raw);
    } catch (NumberFormatException e) {
      return false;
    }
    return value >= min && value <= max;
  }
}
