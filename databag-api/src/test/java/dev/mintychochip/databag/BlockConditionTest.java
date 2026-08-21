package dev.mintychochip.databag;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import net.kyori.adventure.key.Key;
import org.junit.jupiter.api.Test;

class BlockConditionTest {

  private static final Key CHEST = Key.key("minecraft:chest");

  private static ConditionContext chestFacingNorth() {
    return ConditionContext.builder()
        .present(false)
        .livingPresent(false)
        .blockId(CHEST)
        .blockProperties(Map.of("facing", "north", "type", "single"))
        .build();
  }

  @Test
  void blockIdMatches() {
    assertTrue(Conditions.blockId(CHEST).test(chestFacingNorth()));
    assertFalse(Conditions.blockId(Key.key("minecraft:stone")).test(chestFacingNorth()));
  }

  @Test
  void blockPropertyMatches() {
    assertTrue(Conditions.blockProperty("facing", "north").test(chestFacingNorth()));
    assertFalse(Conditions.blockProperty("facing", "south").test(chestFacingNorth()));
  }

  @Test
  void blockConditionsFailClosedWithoutBlock() {
    ConditionContext livingOnly = ConditionContext.builder()
        .present(false)
        .livingPresent(true)
        .entityType(Key.key("minecraft:zombie"))
        .build();
    assertFalse(Conditions.blockId(CHEST).test(livingOnly));
    assertFalse(Conditions.blockProperty("facing", "north").test(livingOnly));
    assertFalse(Conditions.blockId(CHEST).test(ConditionContext.absent()));
  }

  @Test
  void missingPropertyFailsClosed() {
    assertFalse(Conditions.blockProperty("waterlogged", "true").test(chestFacingNorth()));
  }

  @Test
  void locationConditionsMatchBlockSnapshot() {
    ConditionContext blockInWorld = ConditionContext.builder()
        .present(false)
        .livingPresent(false)
        .blockId(CHEST)
        .biome(Key.key("minecraft:plains"))
        .worldName("world")
        .weather(WeatherState.CLEAR)
        .build();
    assertTrue(Conditions.biome(Key.key("minecraft:plains")).test(blockInWorld));
    assertTrue(Conditions.world("world").test(blockInWorld));
    assertTrue(Conditions.weather(WeatherState.CLEAR).test(blockInWorld));
    // Field absent still fails closed.
    assertFalse(Conditions.biome(Key.key("minecraft:plains")).test(chestFacingNorth()));
  }
}
