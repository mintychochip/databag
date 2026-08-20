package dev.mintychochip.databag;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import net.kyori.adventure.key.Key;
import org.junit.jupiter.api.Test;

class LivingEntityConditionTest {

  private static final Key ZOMBIE = Key.key("minecraft:zombie");
  private static final Key SPEED = Key.key("minecraft:speed");

  private static ConditionContext livingZombie() {
    return ConditionContext.builder()
        .present(false)
        .livingPresent(true)
        .entityType(ZOMBIE)
        .onFire(true)
        .onGround(true)
        .swimming(false)
        .baby(false)
        .health(12.0)
        .effects(Map.of(SPEED, new PotionEffectSnapshot(1, 80)))
        .build();
  }

  @Test
  void entityTypeMatchesLivingZombie() {
    assertTrue(Conditions.entityType(ZOMBIE).test(livingZombie()));
    assertFalse(Conditions.entityType(Key.key("minecraft:skeleton")).test(livingZombie()));
  }

  @Test
  void entityTypeFailsClosedWithoutLivingSubject() {
    ConditionContext blockOnly = ConditionContext.builder()
        .present(false)
        .livingPresent(false)
        .blockId(Key.key("minecraft:stone"))
        .build();
    assertFalse(Conditions.entityType(ZOMBIE).test(blockOnly));
    assertFalse(Conditions.entityType(ZOMBIE).test(ConditionContext.absent()));
  }

  @Test
  void livingFlagsAndHealthAndEffect() {
    ConditionContext ctx = livingZombie();
    assertTrue(Conditions.onFire(true).test(ctx));
    assertTrue(Conditions.onGround(true).test(ctx));
    assertTrue(Conditions.swimming(false).test(ctx));
    assertTrue(Conditions.baby(false).test(ctx));
    assertTrue(Conditions.playerResource(
        PlayerResourceType.HEALTH, RelationalOperator.GREATER_THAN, 10).test(ctx));
    assertTrue(Conditions.potionPresent(SPEED).test(ctx));
    assertFalse(Conditions.onFire(false).test(ctx));
  }

  @Test
  void livingFlagsFailClosedWhenLivingAbsent() {
    ConditionContext empty = ConditionContext.absent();
    assertFalse(Conditions.onFire(true).test(empty));
    assertFalse(Conditions.onGround(true).test(empty));
    assertFalse(Conditions.swimming(true).test(empty));
    assertFalse(Conditions.playerResource(
        PlayerResourceType.HEALTH, RelationalOperator.GREATER_THAN, 0).test(empty));
    assertFalse(Conditions.potionPresent(SPEED).test(empty));
  }

  @Test
  void sneakingAppliesToNonPlayerLivingEntity() {
    ConditionContext zombieSneaking = ConditionContext.builder()
        .present(false)
        .livingPresent(true)
        .entityType(ZOMBIE)
        .sneaking(true)
        .build();
    assertTrue(Conditions.sneaking(true).test(zombieSneaking));
  }
}
