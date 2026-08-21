package dev.mintychochip.databag;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import net.kyori.adventure.key.Key;
import org.junit.jupiter.api.Test;

class VanillaParityTest {

  private static final Key OVERWORLD = Key.key("minecraft:overworld");

  @Test
  void timeCheckBoundsPeriodAndAbsentTime() {
    ConditionContext ctx = ConditionContext.builder().dayTime(1_000L).build();
    assertTrue(Conditions.timeCheck(1_000L, 1_000L, 24_000L).test(ctx));
    assertTrue(Conditions.timeCheck(null, 1_000L, null).test(ctx));
    assertFalse(Conditions.timeCheck(1_001L, null, null).test(ctx));
    assertTrue(Conditions.timeCheck(1_000L, 1_000L, 24_000L)
        .test(ConditionContext.builder().dayTime(25_000L).build()));
    assertFalse(Conditions.timeCheck(null, null, null).test(ConditionContext.absent()));
  }

  @Test
  void entityScoresRequireEveryObjectiveWithinBounds() {
    EntityScoresCondition.Bound bound = new EntityScoresCondition.Bound(10, 20);
    ConditionContext ctx = ConditionContext.builder().scores(Map.of("kills", 15)).build();
    assertTrue(Conditions.entityScores(Map.of("kills", bound)).test(ctx));
    assertFalse(Conditions.entityScores(Map.of("missing", bound)).test(ctx));
    assertFalse(Conditions.entityScores(Map.of(
        "kills", bound, "deaths", new EntityScoresCondition.Bound(null, 2))).test(ctx));
    assertFalse(Conditions.entityScores(Map.of("kills", new EntityScoresCondition.Bound(16, null)))
        .test(ctx));
  }

  @Test
  void killedByPlayerMatchesExpectedAndFailsClosed() {
    ConditionContext killed = ConditionContext.builder().attackingPlayer(true).build();
    ConditionContext notKilled = ConditionContext.builder().attackingPlayer(false).build();
    assertTrue(Conditions.killedByPlayer(true).test(killed));
    assertFalse(Conditions.killedByPlayer(false).test(killed));
    assertTrue(Conditions.killedByPlayer(false).test(notKilled));
    assertFalse(Conditions.killedByPlayer(true).test(ConditionContext.absent()));
  }

  @Test
  void positionRequiresAllCoordinatesWithinBounds() {
    ConditionContext ctx = ConditionContext.builder().x(1.0).y(2.0).z(3.0).build();
    assertTrue(Conditions.position(0.0, 1.0, 2.0, 2.0, 3.0, 4.0).test(ctx));
    assertFalse(Conditions.position(1.1, null, null, null, null, null).test(ctx));
    assertFalse(Conditions.position(null, null, null, null, null, null)
        .test(ConditionContext.builder().x(1.0).y(2.0).build()));
  }

  @Test
  void dimensionMatchesKeyAndFailsClosedWithoutWorldKey() {
    ConditionContext ctx = ConditionContext.builder().worldKey(OVERWORLD).build();
    assertTrue(Conditions.dimension(OVERWORLD).test(ctx));
    assertFalse(Conditions.dimension(Key.key("minecraft:the_nether")).test(ctx));
    assertFalse(Conditions.dimension(OVERWORLD).test(ConditionContext.absent()));
  }

  @Test
  void lightChecksCombinedAndSkyBlockBounds() {
    ConditionContext ctx = ConditionContext.builder()
        .lightLevel(10).skyLight(7).blockLight(3).build();
    assertTrue(Conditions.light(10, 12, 7, 8, 2, 3).test(ctx));
    assertFalse(Conditions.light(11, null, null, null, null, null).test(ctx));
    assertFalse(Conditions.light(null, null, 8, null, null, null).test(ctx));
    assertFalse(Conditions.light(null, null, null, null, 4, null).test(ctx));
    assertFalse(Conditions.light(0, 15, null, null, null, null)
        .test(ConditionContext.builder().skyLight(7).blockLight(3).build()));
  }

  @Test
  void canSeeSkyMatchesExpectedAndFailsClosed() {
    ConditionContext visible = ConditionContext.builder().canSeeSky(true).build();
    assertTrue(Conditions.canSeeSky(true).test(visible));
    assertFalse(Conditions.canSeeSky(false).test(visible));
    assertFalse(Conditions.canSeeSky(true).test(ConditionContext.absent()));
  }

  @Test
  void playerResourcesLevelAbsorptionAndAirSupportComparisons() {
    ConditionContext ctx = ConditionContext.builder()
        .xpLevel(30.0).absorption(4.0).airRemaining(200.0).build();
    assertTrue(Conditions.playerResource(
        PlayerResourceType.LEVEL, RelationalOperator.GREATER_THAN_OR_EQUAL, 30).test(ctx));
    assertTrue(Conditions.playerResource(
        PlayerResourceType.ABSORPTION, RelationalOperator.LESS_THAN, 5).test(ctx));
    assertTrue(Conditions.playerResource(
        PlayerResourceType.AIR, RelationalOperator.EQUAL, 200).test(ctx));
    assertFalse(Conditions.playerResource(
        PlayerResourceType.LEVEL, RelationalOperator.LESS_THAN, 30).test(ctx));
    assertFalse(Conditions.playerResource(
        PlayerResourceType.ABSORPTION, RelationalOperator.GREATER_THAN, 4).test(ctx));
    assertFalse(Conditions.playerResource(
        PlayerResourceType.AIR, RelationalOperator.GREATER_THAN, 200).test(ctx));
    assertFalse(Conditions.playerResource(
        PlayerResourceType.LEVEL, RelationalOperator.GREATER_THAN, 0)
        .test(ConditionContext.absent()));
    assertFalse(Conditions.playerResource(
        PlayerResourceType.ABSORPTION, RelationalOperator.GREATER_THAN, 0)
        .test(ConditionContext.absent()));
    assertFalse(Conditions.playerResource(
        PlayerResourceType.AIR, RelationalOperator.GREATER_THAN, 0)
        .test(ConditionContext.absent()));
  }
}
