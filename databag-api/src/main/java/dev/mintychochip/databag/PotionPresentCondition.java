package dev.mintychochip.databag;

import net.kyori.adventure.key.Key;

/**
 * True when the snapshot has the named potion effect.
 */
public record PotionPresentCondition(Key effectKey) implements Condition {

  @Override
  public boolean test(ConditionContext context) {
    if (!context.livingPresent()) {
      return false;
    }
    for (Key key : context.effects().keySet()) {
      if (BiomeCondition.keysEqual(effectKey, key)) {
        return true;
      }
    }
    return false;
  }
}
