package dev.mintychochip.databag;

/**
 * Matches whether a player attacker was present when the snapshot was captured.
 */
public record KilledByPlayerCondition(boolean expected) implements Condition {

  @Override
  public boolean test(ConditionContext context) {
    Boolean attackingPlayer = context.attackingPlayer();
    return attackingPlayer != null && attackingPlayer == expected;
  }
}
