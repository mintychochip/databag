package dev.mintychochip.databag;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SneakingConditionTest {

  @Test
  void sneakingTrueMatchesSneakingPlayer() {
    Condition condition = Conditions.sneaking(true);
    ConditionContext context = ConditionContext.builder().present(true).sneaking(true).build();
    assertTrue(condition.test(context));
  }

  @Test
  void sneakingTrueRejectsStandingPlayer() {
    Condition condition = Conditions.sneaking(true);
    ConditionContext context = ConditionContext.builder().present(true).sneaking(false).build();
    assertFalse(condition.test(context));
  }

  @Test
  void missingPlayerFailsClosed() {
    Condition condition = Conditions.sneaking(true);
    assertFalse(condition.test(ConditionContext.absent()));
  }
}
