package dev.mintychochip.databag.gson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import dev.mintychochip.databag.Condition;
import dev.mintychochip.databag.ConditionContext;
import dev.mintychochip.databag.ConditionSerializer;
import dev.mintychochip.databag.Conditions;
import dev.mintychochip.databag.PlayerResourceType;
import dev.mintychochip.databag.PotionEffectSnapshot;
import dev.mintychochip.databag.RelationalOperator;
import net.kyori.adventure.key.Key;
import org.junit.jupiter.api.Test;

class EntityBlockSerializerTest {

  private final ConditionSerializer json = GsonConditionSerializer.gson();

  private static final Key ZOMBIE = Key.key("minecraft:zombie");
  private static final Key SPEED = Key.key("minecraft:speed");
  private static final Key CHEST = Key.key("minecraft:chest");

  private static ConditionContext livingZombie() {
    return ConditionContext.builder()
        .present(false)
        .livingPresent(true)
        .entityType(ZOMBIE)
        .onFire(true)
        .onGround(true)
        .swimming(false)
        .health(12.0)
        .effects(Map.of(SPEED, new PotionEffectSnapshot(1, 80)))
        .build();
  }

  private static ConditionContext chestNorth() {
    return ConditionContext.builder()
        .present(false)
        .livingPresent(false)
        .blockId(CHEST)
        .blockProperties(Map.of("facing", "north"))
        .build();
  }

  private static ConditionContext survivalFlyer() {
    return ConditionContext.builder()
        .present(true)
        .flying(true)
        .gameMode("survival")
        .build();
  }

  @Test
  void entityTypeAndFlagsRoundTripVanillaShape() {
    Condition original = Conditions.allOf(
        Conditions.entityType(ZOMBIE),
        Conditions.onFire(true),
        Conditions.onGround(true));
    byte[] bytes = json.write(original);
    JsonObject written = JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8))
        .getAsJsonObject();
    assertEquals("minecraft:all_of", written.get("condition").getAsString());

    Condition restored = json.read(bytes);
    assertTrue(restored.test(livingZombie()));
    assertFalse(restored.test(ConditionContext.absent()));
    assertFalse(restored.test(chestNorth()));
  }

  @Test
  void readsVanillaEntityPropertiesTypeAndFlags() {
    String vanilla = """
        {
          "condition": "minecraft:entity_properties",
          "entity": "this",
          "predicate": {
            "type": "minecraft:zombie",
            "flags": { "is_on_fire": true, "is_on_ground": true }
          }
        }
        """;
    Condition condition = json.read(vanilla.getBytes(StandardCharsets.UTF_8));
    assertTrue(condition.test(livingZombie()));
    assertFalse(condition.test(ConditionContext.builder()
        .present(false)
        .livingPresent(true)
        .entityType(Key.key("minecraft:skeleton"))
        .onFire(true)
        .onGround(true)
        .build()));
  }

  @Test
  void healthAndEffectRoundTripOnLivingSnapshot() {
    Condition original = Conditions.allOf(
        Conditions.playerResource(PlayerResourceType.HEALTH, RelationalOperator.GREATER_THAN, 10),
        Conditions.potionPresent(SPEED));
    Condition restored = json.read(json.write(original));
    assertTrue(restored.test(livingZombie()));
    assertFalse(restored.test(ConditionContext.absent()));
  }

  @Test
  void blockIdAndPropertyRoundTripAsBlockStateProperty() {
    Condition original = Conditions.allOf(
        Conditions.blockId(CHEST),
        Conditions.blockProperty("facing", "north"));
    byte[] bytes = json.write(original);
    String text = new String(bytes, StandardCharsets.UTF_8);
    assertTrue(text.contains("minecraft:block_state_property"));

    Condition restored = json.read(bytes);
    assertTrue(restored.test(chestNorth()));
    assertFalse(restored.test(livingZombie()));
    assertFalse(restored.test(ConditionContext.absent()));
  }

  @Test
  void readsVanillaBlockStateProperty() {
    String vanilla = """
        {
          "condition": "minecraft:block_state_property",
          "block": "minecraft:chest",
          "properties": { "facing": "north" }
        }
        """;
    Condition condition = json.read(vanilla.getBytes(StandardCharsets.UTF_8));
    assertTrue(condition.test(chestNorth()));
    assertFalse(condition.test(ConditionContext.builder()
        .present(false)
        .blockId(CHEST)
        .blockProperties(Map.of("facing", "south"))
        .build()));
  }

  @Test
  void readsLocationCheckBlock() {
    String vanilla = """
        {
          "condition": "minecraft:location_check",
          "predicate": {
            "block": {
              "blocks": "minecraft:chest",
              "state": { "facing": "north" }
            }
          }
        }
        """;
    Condition condition = json.read(vanilla.getBytes(StandardCharsets.UTF_8));
    assertTrue(condition.test(chestNorth()));
  }

  @Test
  void gameModeRoundTripIsPlayerOnly() {
    Condition original = Conditions.gameMode("survival");
    byte[] bytes = json.write(original);
    String text = new String(bytes, StandardCharsets.UTF_8);
    assertTrue(text.contains("minecraft:entity_properties"));
    assertTrue(text.contains("gamemode"));

    Condition restored = json.read(bytes);
    assertTrue(restored.test(survivalFlyer()));
    assertFalse(restored.test(livingZombie()));
  }

  @Test
  void flyingFlagRoundTripIsPlayerOnly() {
    Condition restored = json.read(json.write(Conditions.flying(true)));
    assertTrue(restored.test(survivalFlyer()));
    assertFalse(restored.test(livingZombie()));
  }
}
