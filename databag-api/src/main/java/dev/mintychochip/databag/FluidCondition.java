package dev.mintychochip.databag;

import net.kyori.adventure.key.Key;

/**
 * Matches the fluid at the player's feet (water/lava).
 */
public record FluidCondition(Key fluidKey) implements Condition {

  @Override
  public boolean test(ConditionContext context) {
    if (!context.present() || context.fluid() == null) {
      return false;
    }
    return BiomeCondition.keysEqual(fluidKey, context.fluid());
  }
}
