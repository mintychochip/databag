package dev.mintychochip.databag;

import org.jetbrains.annotations.Nullable;

/**
 * Matches the subject item's remaining durability ({@code maxDurability -
 * damage}) against an inclusive range. Non-damageable or absent subjects fail
 * closed.
 */
public record ItemDurabilityCondition(Integer minimum, Integer maximum)
    implements Condition {

  public ItemDurabilityCondition {
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

  @Override
  public boolean test(ConditionContext context) {
    ItemSubject subject = context.itemSubject();
    if (subject == null
        || subject.material() == null
        || subject.maxDurability() == null) {
      return false;
    }
    int remaining = subject.maxDurability().intValue()
        - (subject.damage() == null ? 0 : subject.damage().intValue());
    if (minimum != null && remaining < minimum.intValue()) {
      return false;
    }
    if (maximum != null && remaining > maximum.intValue()) {
      return false;
    }
    return true;
  }
}
