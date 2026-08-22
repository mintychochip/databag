package dev.mintychochip.databag;
import net.kyori.adventure.key.Key;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.jetbrains.annotations.Nullable;

/**
 * Matches an equipment slot's item subject against item IDs and an inclusive
 * count range. Absent slots evaluate as empty stacks and fail closed.
 */
public record EquipmentItemCondition(
    EquipmentSlotKey slot,
    Set<Key> items,
    Integer minimum,
    Integer maximum) implements Condition {

  public EquipmentItemCondition {
    Objects.requireNonNull(slot, "slot");
    items = Set.copyOf(items == null ? Set.of() : items);
    if (items.isEmpty()) {
      throw new IllegalArgumentException("items must not be empty");
    }
    if (minimum != null && minimum < 0) {
      throw new IllegalArgumentException("minimum must be >= 0");
    }
    if (maximum != null && maximum < 0) {
      throw new IllegalArgumentException("maximum must be >= 0");
    }
    if (minimum != null && maximum != null && minimum > maximum) {
      throw new IllegalArgumentException("minimum exceeds maximum");
    }
  }

  public boolean test(Map<EquipmentSlotKey, ItemSubject> equipment) {
    if (equipment == null) {
      return false;
    }
    ItemSubject subject = equipment.get(slot);
    return testSubject(subject);
  }

  @Override
  public boolean test(ConditionContext context) {
    return test(context.equipment());
  }

  private boolean testSubject(@Nullable ItemSubject subject) {
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
