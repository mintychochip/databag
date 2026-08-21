package dev.mintychochip.databag;

import java.util.Map;
import java.util.Objects;
import org.jetbrains.annotations.Nullable;

/**
 * Matches scoreboard objective values against optional min/max bounds per objective.
 */
public record EntityScoresCondition(Map<String, Bound> scores) implements Condition {

  public EntityScoresCondition {
    scores = Map.copyOf(Objects.requireNonNull(scores, "scores"));
  }

  /**
   * Optional inclusive bounds for one scoreboard objective.
   */
  public record Bound(@Nullable Integer min, @Nullable Integer max) {

    public Bound {
      if (min == null && max == null) {
        throw new IllegalArgumentException("at least one bound must be non-null");
      }
    }
  }

  @Override
  public boolean test(ConditionContext context) {
    for (Map.Entry<String, Bound> entry : scores.entrySet()) {
      Integer actual = context.scores().get(entry.getKey());
      if (actual == null) {
        return false;
      }
      Bound bound = entry.getValue();
      if (bound.min() != null && actual < bound.min()) {
        return false;
      }
      if (bound.max() != null && actual > bound.max()) {
        return false;
      }
    }
    return true;
  }
}
