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
import dev.mintychochip.databag.EntityTarget;
import dev.mintychochip.databag.EntityTargetCondition;
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
  @Test
  void roundTripsNewConditions() {
    Condition time = serializer.read(serializer.write(Conditions.timeCheck(10L, 20L, 30L)));
    assertTrue(time.test(ConditionContext.builder().present(true).dayTime(40L).build()));
    assertTrue(time.test(ConditionContext.builder().present(true).dayTime(41L).build()));

    Condition scores = serializer.read(serializer.write(Conditions.entityScores(Map.of(
        "points", new dev.mintychochip.databag.EntityScoresCondition.Bound(1, 5),
        "wins", new dev.mintychochip.databag.EntityScoresCondition.Bound(3, 3)))));
    assertTrue(scores.test(ConditionContext.builder().present(true)
        .scores(Map.of("points", 4, "wins", 3)).build()));
    assertFalse(scores.test(ConditionContext.builder().present(true)
        .scores(Map.of("points", 6, "wins", 3)).build()));

    for (boolean expected : new boolean[] {true, false}) {
      Condition killed = serializer.read(serializer.write(Conditions.killedByPlayer(expected)));
      ConditionContext withAttackingPlayer = ConditionContext.builder().present(true)
          .attackingPlayer(ConditionContext.builder().present(true).build())
          .build();
      ConditionContext withoutAttackingPlayer = ConditionContext.builder().present(true).build();
      if (expected) {
        assertTrue(killed.test(withAttackingPlayer));
        assertFalse(killed.test(withoutAttackingPlayer));
      } else {
        assertTrue(killed.test(withoutAttackingPlayer));
        assertFalse(killed.test(withAttackingPlayer));
      }
    }

    Condition position = serializer.read(serializer.write(
        Conditions.position(1.0, 3.0, 4.0, 6.0, 7.0, 9.0)));
    assertTrue(position.test(ConditionContext.builder().present(true)
        .x(2.0).y(5.0).z(8.0).build()));
    assertFalse(position.test(ConditionContext.builder().present(true)
        .x(0.0).y(5.0).z(8.0).build()));

    Condition dimension = serializer.read(serializer.write(
        Conditions.dimension(Key.key("minecraft:the_nether"))));
    assertTrue(dimension.test(ConditionContext.builder()
        .worldKey(Key.key("minecraft:the_nether")).build()));
    assertFalse(dimension.test(ConditionContext.builder()
        .worldKey(Key.key("minecraft:overworld")).build()));

    Condition light = serializer.read(serializer.write(Conditions.light(5, 10, null, null, null, null)));
    assertTrue(light.test(ConditionContext.builder().lightLevel(7).build()));
    assertFalse(light.test(ConditionContext.builder().lightLevel(11).build()));

    Condition sky = serializer.read(serializer.write(Conditions.canSeeSky(true)));
    assertTrue(sky.test(ConditionContext.builder().canSeeSky(true).build()));
    assertFalse(sky.test(ConditionContext.builder().canSeeSky(false).build()));
  }

  @Test
  void readsVanillaNewConditionJson() {
    Condition time = serializer.read("""
        {"condition":"minecraft:time_check","value":{"min":10,"max":20},"period":30}
        """.getBytes(StandardCharsets.UTF_8));
    assertTrue(time.test(ConditionContext.builder().dayTime(40L).build()));

    Condition scores = serializer.read("""
        {"condition":"minecraft:entity_scores","entity":"this",
         "scores":{"points":4,"wins":{"min":2,"max":5}}}
        """.getBytes(StandardCharsets.UTF_8));
    assertTrue(scores.test(ConditionContext.builder().scores(Map.of("points", 4, "wins", 3)).build()));
    assertFalse(scores.test(ConditionContext.builder().scores(Map.of("points", 5, "wins", 3)).build()));

    Condition killed = serializer.read("""
        {"condition":"minecraft:killed_by_player","value":true}
        """.getBytes(StandardCharsets.UTF_8));
    assertTrue(killed.test(ConditionContext.builder()
        .attackingPlayer(ConditionContext.builder().present(true).build())
        .build()));

    Condition location = serializer.read("""
        {"condition":"minecraft:location_check","predicate":{
          "position":{"x":{"min":1,"max":3},"y":5,"z":{"min":7}},
          "dimension":"minecraft:the_nether",
          "light":{"light":{"min":5,"max":10}},
          "can_see_sky":true}}
        """.getBytes(StandardCharsets.UTF_8));
    assertTrue(location.test(ConditionContext.builder().x(2.0).y(5.0).z(8.0)
        .worldKey(Key.key("minecraft:the_nether")).lightLevel(7).canSeeSky(true).build()));
    assertFalse(location.test(ConditionContext.builder().x(2.0).y(5.0).z(8.0)
        .worldKey(Key.key("minecraft:the_nether")).lightLevel(7).canSeeSky(false).build()));
  }

  @Test
  void unsupportedVanillaConditionsExplainWhy() {
    Map<String, String> unsupported = Map.ofEntries(
        Map.entry("match_tool", "requires tool item from loot execution context"),
        Map.entry("survives_explosion", "requires explosion radius from loot execution context"),
        Map.entry("random_chance", "requires loot execution RNG context"),
        Map.entry("random_chance_with_enchanted_bonus", "requires attacker enchantment level from loot context"),
        Map.entry("table_bonus", "requires tool enchantment level from loot context"),
        Map.entry("enchantment_active_check", "requires enchanted_location loot context"),
        Map.entry("damage_source_properties",
            "requires damage source capture in snapshot; attacker slots exist but damage type/tags do not"),
        Map.entry("value_check", "requires vanilla number providers"),
        Map.entry("reference", "requires predicate file resolver"),
        Map.entry("environment_attribute_check", "requires environment attribute values in snapshot"),
        Map.entry("match_block", "upcoming JE 26.3 (replaces block_state_property)"));
    for (var entry : unsupported.entrySet()) {
      String json = "{\"condition\":\"minecraft:" + entry.getKey() + "\"}";
      IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
          () -> serializer.read(json.getBytes(StandardCharsets.UTF_8)));
      assertTrue(error.getMessage().contains("Unsupported vanilla condition"));
      assertTrue(error.getMessage().contains(entry.getValue()));
    }
  }

  @Test
  void lightSkyBoundsCannotBeWrittenToVanillaJson() {
    IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
        () -> serializer.write(Conditions.light(null, null, 5, 10, null, null)));
    assertTrue(error.getMessage().contains("sky/block bounds cannot be expressed in vanilla JE 26.2"));
  }

  @Test
  void readsModularJobsLevelResourceAlias() {
    String json = """
        {"condition":"modularjobs:player_resource","resource":"level",
         "operator":"equal","value":5}
        """;
    Condition condition = serializer.read(json.getBytes(StandardCharsets.UTF_8));
    assertTrue(condition.test(ConditionContext.builder().xpLevel(5.0).build()));
    assertFalse(condition.test(ConditionContext.builder().xpLevel(4.0).build()));
  }

  @Test
  void readsVanillaLocationPredicateVariantsIndividually() {
    String[] jsons = {
        "{\"condition\":\"minecraft:location_check\",\"predicate\":{\"position\":{\"x\":2}}}",
        "{\"condition\":\"minecraft:location_check\",\"predicate\":{\"dimension\":\"minecraft:overworld\"}}",
        "{\"condition\":\"minecraft:location_check\",\"predicate\":{\"light\":{\"light\":7}}}",
        "{\"condition\":\"minecraft:location_check\",\"predicate\":{\"can_see_sky\":true}}"
    };
    Condition[] conditions = new Condition[jsons.length];
    for (int i = 0; i < jsons.length; i++) {
      conditions[i] = serializer.read(jsons[i].getBytes(StandardCharsets.UTF_8));
    }
    assertTrue(conditions[0].test(ConditionContext.builder().x(2.0).y(0.0).z(0.0).build()));
    assertTrue(conditions[1].test(ConditionContext.builder()
        .worldKey(Key.key("minecraft:overworld")).build()));
    assertTrue(conditions[2].test(ConditionContext.builder().lightLevel(7).build()));
    assertTrue(conditions[3].test(ConditionContext.builder().canSeeSky(true).build()));
  }

  @Test
  void targetedConditionEvaluatesNestedSlot() {
    Condition attackerOnFire = Conditions.targeted(
        EntityTarget.ATTACKER, Conditions.onFire(true));
    ConditionContext withAttacker = ConditionContext.builder().present(true)
        .attacker(ConditionContext.builder().present(true).onFire(true).build())
        .build();
    ConditionContext attackerCalm = ConditionContext.builder().present(true)
        .attacker(ConditionContext.builder().present(true).onFire(false).build())
        .build();
    assertTrue(attackerOnFire.test(withAttacker));
    assertFalse(attackerOnFire.test(attackerCalm));
    assertFalse(attackerOnFire.test(ConditionContext.builder().present(true).build()));
  }

  @Test
  void readsEntityPropertiesWithNonThisTarget() {
    String json = """
        {
          "condition": "minecraft:entity_properties",
          "entity": "attacking_player",
          "predicate": { "flags": { "is_sneaking": true } }
        }
        """;
    EntityTargetCondition condition = assertInstanceOf(
        EntityTargetCondition.class, serializer.read(json.getBytes(StandardCharsets.UTF_8)));
    assertEquals(EntityTarget.ATTACKING_PLAYER, condition.target());
    assertTrue(condition.test(ConditionContext.builder().present(true)
        .attackingPlayer(ConditionContext.builder().present(true).sneaking(true).build())
        .build()));
    assertFalse(condition.test(ConditionContext.builder().present(true).build()));
  }

  @Test
  void writesTargetedConditionAsVanillaEntityField() {
    byte[] bytes = serializer.write(Conditions.targeted(
        EntityTarget.ATTACKER, Conditions.sneaking(true)));
    JsonObject json = JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8))
        .getAsJsonObject();
    assertEquals("minecraft:entity_properties", json.get("condition").getAsString());
    assertEquals("attacker", json.get("entity").getAsString());
    EntityTargetCondition back = assertInstanceOf(
        EntityTargetCondition.class, serializer.read(bytes));
    assertEquals(EntityTarget.ATTACKER, back.target());
  }

  @Test
  void unknownEntityTargetThrowsOnRead() {
    String json = """
        { "condition": "minecraft:entity_properties", "entity": "vehicle",
          "predicate": { "flags": { "is_sneaking": true } } }
        """;
    IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
        () -> serializer.read(json.getBytes(StandardCharsets.UTF_8)));
    assertTrue(error.getMessage().contains("Unknown entity target"));
  }
}
