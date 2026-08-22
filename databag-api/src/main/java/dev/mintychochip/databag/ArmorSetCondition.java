package dev.mintychochip.databag;

import java.util.Set;
import net.kyori.adventure.key.Key;

/**
 * Checks the entity is wearing every item in {@code items}.
 * The armor set is the set of material keys of currently equipped armor.
 */
public record ArmorSetCondition(Set<Key> items) implements Condition {

  @Override
  public boolean test(ConditionContext context) {
    return context.armorSet() != null && context.armorSet().containsAll(items);
  }
}
