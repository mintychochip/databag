package dev.mintychochip.databag;

/**
 * Checks the player has at least {@code minimum} empty inventory slots.
 * Empty slots are counted from the main storage, hotbar, and offhand
 * according to the inclusion flags.
 */
public record EmptySlotsCondition(int minimum, boolean includeOffhand, boolean includeHotbar)
    implements Condition {

  @Override
  public boolean test(ConditionContext context) {
    if (!context.present()) {
      return false;
    }
    int main = context.emptyMain() == null ? 0 : context.emptyMain();
    int hotbar = context.emptyHotbar() == null ? 0 : context.emptyHotbar();
    int offhand = context.emptyOffhand() == null ? 0 : context.emptyOffhand();
    int total = main + (includeHotbar ? hotbar : 0) + (includeOffhand ? offhand : 0);
    return total >= minimum;
  }
}
