package dev.mintychochip.databag;
import net.kyori.adventure.key.Key;

import java.util.Set;

/**
 * Matches the subject item stack against a set of item IDs and an inclusive
 * count range. Requires an {@link ItemSubject}; absent subjects fail closed.
 */
public record ItemTypeCountCondition(Set<Key> items, Integer minimum, Integer maximum)
    implements Condition {

  public ItemTypeCountCondition {
    items = Set.copyOf(items == null ? Set.of() : items);
    if (minimum != null && minimum < 0) {
      throw new IllegalArgumentException("minimum must be >= 0");
    }
    if (maximum != null && maximum < 0) {
      throw new IllegalArgumentException("maximum must be >= 0");
    }
    if (items.isEmpty()) {
      throw new IllegalArgumentException("items must not be empty");
    }
    if (minimum != null && maximum != null && minimum > maximum) {
      throw new IllegalArgumentException("minimum exceeds maximum");
    }
  }

  @Override
  public boolean test(ConditionContext context) {
    ItemSubject subject = context.itemSubject();
    if (subject == null || subject.material() == null || subject.amount() <= 0) {
      return false;
    }
    for (Key item : items) {
      if (!BiomeCondition.keysEqual(item, subject.material())) {
        continue;
      }
      int amount = subject.amount();
      if (minimum != null && amount < minimum.intValue()) {
        return false;
      }
      if (maximum != null && amount > maximum.intValue()) {
        return false;
      }
      return true;
    }
    return false;
  }
}
