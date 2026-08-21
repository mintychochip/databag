package dev.mintychochip.databag;

/**
 * Matches whether the attacking-player slot was present when the snapshot was captured.
 */
public record KilledByPlayerCondition(boolean expected) implements Condition {

  @Override
  public boolean test(ConditionContext context) {
    return expected == (context.attackingPlayer() != null);
  }
}
