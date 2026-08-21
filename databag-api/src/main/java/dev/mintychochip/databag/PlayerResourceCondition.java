package dev.mintychochip.databag;

import java.math.BigDecimal;

/**
 * Compares a player resource snapshot value to {@code expected}.
 */
public record PlayerResourceCondition(
    PlayerResourceType type, RelationalOperator operator, double expected) implements Condition {

  @Override
  public boolean test(ConditionContext context) {
    boolean allowed = switch (type) {
      case HEALTH -> context.livingPresent();
      case HUNGER, EXPERIENCE, LEVEL, ABSORPTION, AIR -> context.present();
    };
    if (!allowed) {
      return false;
    }
    Double actual = switch (type) {
      case HEALTH -> context.health();
      case HUNGER -> context.hunger();
      case EXPERIENCE -> context.experience();
      case LEVEL -> context.xpLevel();
      case ABSORPTION -> context.absorption();
      case AIR -> context.airRemaining();
    };
    if (actual == null) {
      return false;
    }
    return operator.test(BigDecimal.valueOf(actual), BigDecimal.valueOf(expected));
  }
}
