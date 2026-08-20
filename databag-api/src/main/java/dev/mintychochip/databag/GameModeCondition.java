package dev.mintychochip.databag;

import java.util.Locale;
import java.util.Objects;

/**
 * Player-only game mode ({@code survival}, {@code creative}, {@code adventure},
 * {@code spectator}). Non-player living entities fail closed.
 */
public record GameModeCondition(String gameMode) implements Condition {

  public GameModeCondition {
    Objects.requireNonNull(gameMode, "gameMode");
    if (gameMode.isBlank()) {
      throw new IllegalArgumentException("gameMode must be non-blank");
    }
    gameMode = gameMode.toLowerCase(Locale.ROOT);
  }

  @Override
  public boolean test(ConditionContext context) {
    if (!context.present() || context.gameMode() == null) {
      return false;
    }
    return gameMode.equalsIgnoreCase(context.gameMode());
  }
}
