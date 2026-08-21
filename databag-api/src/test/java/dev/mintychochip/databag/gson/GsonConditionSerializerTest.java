package dev.mintychochip.databag.gson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import dev.mintychochip.databag.Condition;
import dev.mintychochip.databag.ConditionContext;
import dev.mintychochip.databag.ConditionSerializer;
import dev.mintychochip.databag.Conditions;
import dev.mintychochip.databag.PotionEffectSnapshot;
import dev.mintychochip.databag.RelationalOperator;
import dev.mintychochip.databag.SneakingCondition;
import net.kyori.adventure.key.Key;
import org.junit.jupiter.api.Test;

class GsonConditionSerializerTest {

  private final ConditionSerializer serializer = GsonConditionSerializer.gson();

  @Test
  void writesSneakingAsVanillaEntityProperties() {
    byte[] bytes = serializer.write(Conditions.sneaking(true));
    JsonObject json = JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8))
        .getAsJsonObject();
    assertEquals("minecraft:entity_properties", json.get("condition").getAsString());
    assertEquals("this", json.get("entity").getAsString());
    assertTrue(json.getAsJsonObject("predicate")
        .getAsJsonObject("flags")
        .get("is_sneaking")
        .getAsBoolean());
  }

  @Test
  void readsVanillaSneakingJson() {
    String json = """
        {
          "condition": "minecraft:entity_properties",
          "entity": "this",
          "predicate": { "flags": { "is_sneaking": true } }
        }
        """;
    Condition condition = serializer.read(json.getBytes(StandardCharsets.UTF_8));
    assertInstanceOf(SneakingCondition.class, condition);
    assertTrue(condition.test(
        ConditionContext.builder().present(true).sneaking(true).build()));
  }

  @Test
  void unknownConditionIdThrowsOnRead() {
    String json = """
        { "condition": "minecraft:random_chance", "chance": 0.5 }
        """;
    assertThrows(IllegalArgumentException.class,
        () -> serializer.read(json.getBytes(StandardCharsets.UTF_8)));
  }

  @Test
  void readsGamemodeListAsAnyOf() {
    String json = """
        {
          "condition": "minecraft:entity_properties",
          "entity": "this",
          "predicate": {
            "type_specific": {
              "type": "minecraft:player",
              "gamemode": ["survival", "adventure"]
            }
          }
        }
        """;
    Condition condition = serializer.read(json.getBytes(StandardCharsets.UTF_8));
    assertTrue(condition.test(
        ConditionContext.builder().present(true).gameMode("adventure").build()));
    assertFalse(condition.test(
        ConditionContext.builder().present(true).gameMode("creative").build()));
  }

  @Test
  void readsBiomeListAsAnyOf() {
    String json = """
        {
          "condition": "minecraft:entity_properties",
          "entity": "this",
          "predicate": {
            "location": { "biomes": ["minecraft:badlands", "minecraft:desert"] }
          }
        }
        """;
    Condition condition = serializer.read(json.getBytes(StandardCharsets.UTF_8));
    assertTrue(condition.test(ConditionContext.builder()
        .present(true).biome(Key.key("minecraft:desert")).build()));
    assertFalse(condition.test(ConditionContext.builder()
        .present(true).biome(Key.key("minecraft:plains")).build()));
  }

  @Test
  void readsFluidListAsAnyOf() {
    String json = """
        {
          "condition": "minecraft:location_check",
          "predicate": { "fluid": { "fluids": ["minecraft:water", "minecraft:lava"] } }
        }
        """;
    Condition condition = serializer.read(json.getBytes(StandardCharsets.UTF_8));
    assertTrue(condition.test(ConditionContext.builder()
        .present(true).fluid(Key.key("minecraft:lava")).build()));
    assertFalse(condition.test(ConditionContext.builder()
        .present(true).build()));
  }

  @Test
  void readsBlockListAsAnyOfAndedWithState() {
    String json = """
        {
          "condition": "minecraft:location_check",
          "predicate": {
            "block": {
              "blocks": ["minecraft:chest", "minecraft:barrel"],
              "state": { "facing": "north" }
            }
          }
        }
        """;
    Condition condition = serializer.read(json.getBytes(StandardCharsets.UTF_8));
    assertTrue(condition.test(ConditionContext.builder()
        .blockId(Key.key("minecraft:barrel"))
        .blockProperties(Map.of("facing", "north"))
        .build()));
    assertFalse(condition.test(ConditionContext.builder()
        .blockId(Key.key("minecraft:barrel"))
        .blockProperties(Map.of("facing", "south"))
        .build()));
    assertFalse(condition.test(ConditionContext.builder()
        .blockId(Key.key("minecraft:furnace"))
        .blockProperties(Map.of("facing", "north"))
        .build()));
  }

  @Test
  void readsAmplifierAndDurationAsAllOf() {
    String json = """
        {
          "condition": "minecraft:entity_properties",
          "entity": "this",
          "predicate": {
            "effects": {
              "minecraft:speed": { "amplifier": { "min": 1 }, "duration": { "min": 50 } }
            }
          }
        }
        """;
    Condition condition = serializer.read(json.getBytes(StandardCharsets.UTF_8));
    Key speed = Key.key("minecraft:speed");
    assertTrue(condition.test(ConditionContext.builder()
        .present(true).livingPresent(true)
        .effects(Map.of(speed, new PotionEffectSnapshot(1, 80))).build()));
    assertFalse(condition.test(ConditionContext.builder()
        .present(true).livingPresent(true)
        .effects(Map.of(speed, new PotionEffectSnapshot(1, 10))).build()));
    assertFalse(condition.test(ConditionContext.builder()
        .present(true).livingPresent(true)
        .effects(Map.of(speed, new PotionEffectSnapshot(0, 80))).build()));
  }

  @Test
  void roundTripsStrictPotionOperatorsLosslessly() {
    Key speed = Key.key("minecraft:speed");
    Condition greaterThan = serializer.read(serializer.write(
        Conditions.potionAmplifier(speed, RelationalOperator.GREATER_THAN, 1)));
    assertTrue(greaterThan.test(ConditionContext.builder()
        .present(true).livingPresent(true)
        .effects(Map.of(speed, new PotionEffectSnapshot(2, 100))).build()));
    assertFalse(greaterThan.test(ConditionContext.builder()
        .present(true).livingPresent(true)
        .effects(Map.of(speed, new PotionEffectSnapshot(1, 100))).build()));

    Condition notEqual = serializer.read(serializer.write(
        Conditions.potionDuration(speed, RelationalOperator.NOT_EQUAL, 100)));
    assertFalse(notEqual.test(ConditionContext.builder()
        .present(true).livingPresent(true)
        .effects(Map.of(speed, new PotionEffectSnapshot(0, 100))).build()));
    assertTrue(notEqual.test(ConditionContext.builder()
        .present(true).livingPresent(true)
        .effects(Map.of(speed, new PotionEffectSnapshot(0, 90))).build()));

    Condition lessThan = serializer.read(serializer.write(
        Conditions.potionAmplifier(speed, RelationalOperator.LESS_THAN, 2)));
    assertTrue(lessThan.test(ConditionContext.builder()
        .present(true).livingPresent(true)
        .effects(Map.of(speed, new PotionEffectSnapshot(1, 100))).build()));
    assertFalse(lessThan.test(ConditionContext.builder()
        .present(true).livingPresent(true)
        .effects(Map.of(speed, new PotionEffectSnapshot(2, 100))).build()));
  }

  @Test
  void liquidWithoutValueThrows() {
    String json = "{ \"condition\": \"liquid\" }";
    assertThrows(IllegalArgumentException.class,
        () -> serializer.read(json.getBytes(StandardCharsets.UTF_8)));
  }
}
