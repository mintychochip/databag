package dev.mintychochip.databag;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Nullable;

/**
 * Parsed armor-trim component from an item subject. Both keys are nullable
 * because vanilla trims always define both, but plugin-created metadata may
 * omit either side.
 */
public record TrimSnapshot(@Nullable Key material, @Nullable Key pattern) {

  public boolean matches(@Nullable Key material, @Nullable Key pattern) {
    if (this.material != null
        && (material == null || !BiomeCondition.keysEqual(this.material, material))) {
      return false;
    }
    if (this.pattern != null
        && (pattern == null || !BiomeCondition.keysEqual(this.pattern, pattern))) {
      return false;
    }
    return true;
  }

  public boolean isPresent() {
    return material != null || pattern != null;
  }
}
