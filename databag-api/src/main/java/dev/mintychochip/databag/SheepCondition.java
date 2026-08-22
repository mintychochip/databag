package dev.mintychochip.databag;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Nullable;

/**
 * Matches a sheep's sheared state and wool color.
 * Both checks are optional; missing criteria are skipped.
 */
public record SheepCondition(@Nullable Boolean sheared, @Nullable Key color) implements Condition {

  @Override
  public boolean test(ConditionContext context) {
    if (context.sheared() == null) {
      return false;
    }
    if (sheared != null && !sheared.equals(context.sheared())) {
      return false;
    }
    if (color != null && !color.equals(context.woolColor())) {
      return false;
    }
    return true;
  }
}
