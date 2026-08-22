package dev.mintychochip.databag;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Nullable;

/**
 * Snapshot of a single active cooldown. {@code source} distinguishes Paper
 * material cooldowns from custom registered cooldowns; remaining ticks is
 * the authoritative countdown.
 */
public record CooldownSnapshot(Key key, String source, int remainingTicks) {

  public CooldownSnapshot {
    java.util.Objects.requireNonNull(key, "key");
    java.util.Objects.requireNonNull(source, "source");
    if (remainingTicks < 0) {
      throw new IllegalArgumentException("remainingTicks must be >= 0");
    }
  }

  public boolean isActive() {
    return remainingTicks > 0;
  }
}
