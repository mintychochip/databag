package dev.mintychochip.databag;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PlayerExpansionTest {

  private static ConditionContext survivalPlayer() {
    return ConditionContext.builder()
        .present(true)
        .livingPresent(true)
        .flying(true)
        .gliding(false)
        .gameMode("survival")
        .onGround(true)
        .build();
  }

  @Test
  void flyingAndGameModeMatchPlayer() {
    assertTrue(Conditions.flying(true).test(survivalPlayer()));
    assertTrue(Conditions.gameMode("survival").test(survivalPlayer()));
    assertFalse(Conditions.gameMode("creative").test(survivalPlayer()));
  }

  @Test
  void flyingAndGameModeArePlayerOnly() {
    ConditionContext zombie = ConditionContext.builder()
        .present(false)
        .livingPresent(true)
        .entityType(net.kyori.adventure.key.Key.key("minecraft:zombie"))
        .flying(true)
        .gameMode("survival")
        .build();
    assertFalse(Conditions.flying(true).test(zombie));
    assertFalse(Conditions.gameMode("survival").test(zombie));
  }

  @Test
  void glidingAppliesToLivingEntity() {
    ConditionContext phantom = ConditionContext.builder()
        .present(false)
        .livingPresent(true)
        .gliding(true)
        .build();
    assertTrue(Conditions.gliding(true).test(phantom));
    assertFalse(Conditions.gliding(true).test(ConditionContext.absent()));
  }
}
