package dev.mintychochip.databag;

import java.util.Objects;

/**
 * Evaluates a nested condition against an entity slot on {@link ConditionContext}.
 */
public record EntityTargetCondition(EntityTarget target, Condition condition)
    implements Condition {

  public EntityTargetCondition {
    Objects.requireNonNull(target);
    Objects.requireNonNull(condition);
  }

  @Override
  public boolean test(ConditionContext context) {
    if (target == EntityTarget.THIS) {
      return condition.test(context);
    }
    ConditionContext slot =
        switch (target) {
          case ATTACKER -> context.attacker();
          case DIRECT_ATTACKER -> context.directAttacker();
          case ATTACKING_PLAYER -> context.attackingPlayer();
          case TARGET_ENTITY -> context.targetEntity();
          case INTERACTING_ENTITY -> context.interactingEntity();
          default -> null;
        };
    if (slot == null) {
      return false;
    }
    return condition.test(slot);
  }
}
