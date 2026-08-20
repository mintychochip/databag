package dev.mintychochip.databag;

import java.util.Objects;

/**
 * Matches one block-state property (for example {@code facing=north}).
 */
public record BlockPropertyCondition(String name, String value) implements Condition {

  public BlockPropertyCondition {
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(value, "value");
    if (name.isBlank()) {
      throw new IllegalArgumentException("block property name must be non-blank");
    }
  }

  @Override
  public boolean test(ConditionContext context) {
    if (context.blockId() == null) {
      return false;
    }
    String actual = context.blockProperties().get(name);
    if (actual == null) {
      return false;
    }
    return actual.equalsIgnoreCase(value);
  }
}
