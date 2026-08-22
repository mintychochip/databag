package dev.mintychochip.databag.gson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.Random;
import dev.mintychochip.databag.Condition;
import dev.mintychochip.databag.ItemSubject;
import dev.mintychochip.databag.EquipmentItemCondition;
import dev.mintychochip.databag.CooldownSnapshot;
import dev.mintychochip.databag.EquipmentSlotKey;
import dev.mintychochip.databag.ItemDurabilityCondition;
import dev.mintychochip.databag.ItemEnchantmentsCondition;
import dev.mintychochip.databag.ItemTypeCountCondition;
import dev.mintychochip.databag.ItemTrimCondition;
import dev.mintychochip.databag.TrimSnapshot;
import dev.mintychochip.databag.MatchBlockCondition;
import dev.mintychochip.databag.ItemSnapshot;
import dev.mintychochip.databag.OffhandItemCondition;
import dev.mintychochip.databag.LocationOffsetCondition;
import dev.mintychochip.databag.BlockPropertyRangeCondition;
import dev.mintychochip.databag.ConditionContext;
import dev.mintychochip.databag.ConditionSerializer;
import dev.mintychochip.databag.Conditions;
import dev.mintychochip.databag.EntityTarget;
import dev.mintychochip.databag.EntityTargetCondition;
import dev.mintychochip.databag.PeriodicTickCondition;
import dev.mintychochip.databag.PotionEffectSnapshot;
import dev.mintychochip.databag.RandomChanceCondition;
import dev.mintychochip.databag.SteppingOnCondition;
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
        { "condition": "minecraft:match_block" }
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
        Map.entry("random_chance_with_enchanted_bonus", "requires attacker enchantment level from loot context"),
        Map.entry("table_bonus", "requires tool enchantment level from loot context"),
        Map.entry("enchantment_active_check", "requires enchanted_location loot context"),
        Map.entry("damage_source_properties",
            "requires damage source capture in snapshot; attacker slots exist but damage type/tags do not"),
        Map.entry("value_check", "requires vanilla number providers"),
        Map.entry("reference", "requires predicate file resolver"),
        Map.entry("environment_attribute_check", "requires environment attribute values in snapshot")
        );
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

  @Test
  void periodicTickRejectsNonPositivePeriod() {
    assertThrows(IllegalArgumentException.class, () -> Conditions.periodicTick(0, 0));
    assertThrows(IllegalArgumentException.class, () -> Conditions.periodicTick(-5, 0));
  }

  @Test
  void periodicTickRejectsNonPositivePeriodFromJson() {
    String json = """
        { "condition": "modularjobs:periodic_tick", "period": 0, "offset": 2 }
        """;
    assertThrows(IllegalArgumentException.class,
        () -> serializer.read(json.getBytes(StandardCharsets.UTF_8)));
  }

  @Test
  void periodicTickRoundTripAndTest() {
    byte[] bytes = serializer.write(Conditions.periodicTick(5, 2));
    PeriodicTickCondition condition = assertInstanceOf(
        PeriodicTickCondition.class, serializer.read(bytes));
    assertEquals(5, condition.period());
    assertEquals(2, condition.offset());
    ConditionContext match = ConditionContext.builder().ticksLived(7).build();
    ConditionContext miss = ConditionContext.builder().ticksLived(8).build();
    assertTrue(condition.test(match));
    assertFalse(condition.test(miss));
  }

  @Test
  void blockPropertyRangeBoundsAndInvalidNumeric() {
    Condition range = Conditions.blockPropertyRange("age", 1, 3);
    assertTrue(range.test(ConditionContext.builder()
        .blockProperties(Map.of("age", "1")).build()));
    assertTrue(range.test(ConditionContext.builder()
        .blockProperties(Map.of("age", "3")).build()));
    assertFalse(range.test(ConditionContext.builder()
        .blockProperties(Map.of("age", "0")).build()));
    assertFalse(range.test(ConditionContext.builder()
        .blockProperties(Map.of("age", "4")).build()));
    assertFalse(range.test(ConditionContext.builder()
        .blockProperties(Map.of("age", "seven")).build()));
    assertFalse(range.test(ConditionContext.builder()
        .blockProperties(Map.of()).build()));
  }

  @Test
  void blockStatePropertyJsonWithRange() {
    String json = """
        {
          "condition": "minecraft:block_state_property",
          "block": "minecraft:wheat",
          "properties": { "age": { "min": 1, "max": 3 } }
        }
        """;
    Condition condition = serializer.read(json.getBytes(StandardCharsets.UTF_8));
    assertTrue(condition.test(ConditionContext.builder()
        .blockId(Key.key("minecraft:wheat"))
        .blockProperties(Map.of("age", "2")).build()));
    assertFalse(condition.test(ConditionContext.builder()
        .blockId(Key.key("minecraft:wheat"))
        .blockProperties(Map.of("age", "0")).build()));
  }

  @Test
  void randomChanceRejectsOutOfBoundsAndNaN() {
    assertThrows(IllegalArgumentException.class, () -> Conditions.randomChance(-0.1));
    assertThrows(IllegalArgumentException.class, () -> Conditions.randomChance(1.1));
    assertThrows(IllegalArgumentException.class, () -> Conditions.randomChance(Double.NaN));
  }

  @Test
  void randomChanceRoundTripAndBoundaries() {
    byte[] bytes = serializer.write(Conditions.randomChance(0.0));
    RandomChanceCondition alwaysFalse = assertInstanceOf(
        RandomChanceCondition.class, serializer.read(bytes));
    assertEquals(0.0, alwaysFalse.chance(), 0.0001);
    assertFalse(alwaysFalse.test(ConditionContext.builder()
        .random(new Random(1)).build()));

    byte[] oneBytes = serializer.write(Conditions.randomChance(1.0));
    RandomChanceCondition alwaysTrue = assertInstanceOf(
        RandomChanceCondition.class, serializer.read(oneBytes));
    assertTrue(alwaysTrue.test(ConditionContext.builder()
        .random(new Random(1)).build()));
  }

  @Test
  void randomChanceJsonRoundTrip() {
    String json = """
        { "condition": "minecraft:random_chance", "chance": 0.5 }
        """;
    RandomChanceCondition condition = assertInstanceOf(
        RandomChanceCondition.class,
        serializer.read(json.getBytes(StandardCharsets.UTF_8)));
    assertEquals(0.5, condition.chance(), 0.0001);
  }

  @Test
  void entityPropertiesSteppingOnRoundTrip() {
    String json = """
        {
          "condition": "minecraft:entity_properties",
          "entity": "this",
          "predicate": {
            "stepping_on": {
              "block": {
                "blocks": ["minecraft:farmland"],
                "state": { "moisture": { "min": 5 } }
              }
            }
          }
        }
        """;
    Condition condition = serializer.read(json.getBytes(StandardCharsets.UTF_8));
    JsonObject written = JsonParser.parseString(
        new String(serializer.write(condition), StandardCharsets.UTF_8)).getAsJsonObject();
    assertEquals("minecraft:entity_properties", written.get("condition").getAsString());
    assertTrue(written.has("predicate"));
    JsonObject steppingOn = written.getAsJsonObject("predicate").getAsJsonObject("stepping_on");
    assertTrue(steppingOn.has("block"));

    ConditionContext ground = ConditionContext.builder()
        .blockId(Key.key("minecraft:farmland"))
        .blockProperties(Map.of("moisture", "7"))
        .build();
    ConditionContext context = ConditionContext.builder().standingOn(ground).build();
    assertTrue(condition.test(context));

    ConditionContext dirt = ConditionContext.builder()
        .blockId(Key.key("minecraft:dirt"))
        .blockProperties(Map.of("moisture", "7"))
        .build();
    assertFalse(condition.test(ConditionContext.builder().standingOn(dirt).build()));
  }

  @Test
  void steppingOnCombinedBlockPredicateRoundTrip() {
    Condition inner = Conditions.allOf(
        Conditions.blockId(Key.key("minecraft:wheat")),
        Conditions.blockProperty("age", "2"));
    byte[] bytes = serializer.write(Conditions.steppingOn(inner));
    Condition condition = serializer.read(bytes);
    assertInstanceOf(SteppingOnCondition.class, condition);
    JsonObject written = JsonParser.parseString(
        new String(bytes, StandardCharsets.UTF_8)).getAsJsonObject();
    JsonObject block = written.getAsJsonObject("predicate")
        .getAsJsonObject("stepping_on").getAsJsonObject("block");
    assertTrue(block.has("blocks"));
    assertTrue(block.has("state"));

    ConditionContext match = ConditionContext.builder()
        .standingOn(ConditionContext.builder()
            .blockId(Key.key("minecraft:wheat"))
            .blockProperties(Map.of("age", "2"))
            .build())
        .build();
    assertTrue(condition.test(match));
  }

  @Test
  void steppingOnWriteRejectsNonBlockInner() {
    assertThrows(IllegalArgumentException.class,
        () -> serializer.write(Conditions.steppingOn(Conditions.sneaking(true))));
  }

  @Test
  void matchBlockPlainPredicateRoundTrip() {
    String json = """
        {
          "condition": "minecraft:match_block",
          "predicate": { "blocks": ["minecraft:stone"] }
        }
        """;
    Condition condition = serializer.read(json.getBytes(StandardCharsets.UTF_8));
    assertInstanceOf(MatchBlockCondition.class, condition);

    JsonObject written = JsonParser.parseString(
        new String(serializer.write(condition), StandardCharsets.UTF_8)).getAsJsonObject();
    assertEquals("minecraft:match_block", written.get("condition").getAsString());
    assertTrue(written.getAsJsonObject("predicate").has("blocks"));

    assertTrue(condition.test(ConditionContext.builder()
        .blockId(Key.key("minecraft:stone")).build()));
    assertFalse(condition.test(ConditionContext.builder()
        .blockId(Key.key("minecraft:dirt")).build()));
  }

  @Test
  void matchBlockCombinedPredicateRoundTrip() {
    Condition condition = Conditions.matchBlock(Conditions.allOf(
        Conditions.blockId(Key.key("minecraft:wheat")),
        Conditions.blockProperty("age", "2")));
    byte[] bytes = serializer.write(condition);
    Condition roundTrip = serializer.read(bytes);

    JsonObject written = JsonParser.parseString(
        new String(bytes, StandardCharsets.UTF_8)).getAsJsonObject();
    JsonObject predicate = written.getAsJsonObject("predicate");
    assertTrue(predicate.has("blocks"));
    assertTrue(predicate.has("state"));
    assertTrue(roundTrip.test(ConditionContext.builder()
        .blockId(Key.key("minecraft:wheat"))
        .blockProperties(Map.of("age", "2"))
        .build()));
  }

  @Test
  void matchBlockWriteRejectsNonBlockInner() {
    assertThrows(IllegalArgumentException.class,
        () -> serializer.write(Conditions.matchBlock(Conditions.sneaking(true))));
  }

  @Test
  void locationOffsetBlockPredicateRoundTrip() {
    String json = """
        {
          "condition": "minecraft:location_check",
          "offsetX": 0,
          "offsetY": -1,
          "offsetZ": 0,
          "predicate": {
            "block": { "blocks": ["minecraft:stone"] }
          }
        }
        """;
    Condition condition = serializer.read(json.getBytes(StandardCharsets.UTF_8));
    assertInstanceOf(LocationOffsetCondition.class, condition);

    JsonObject written = JsonParser.parseString(
        new String(serializer.write(condition), StandardCharsets.UTF_8)).getAsJsonObject();
    assertEquals("minecraft:location_check", written.get("condition").getAsString());
    assertEquals(-1, written.get("offsetY").getAsInt());
    assertTrue(written.getAsJsonObject("predicate").has("block"));

    ConditionContext target = ConditionContext.builder()
        .blockId(Key.key("minecraft:stone"))
        .build();
    ConditionContext base = ConditionContext.builder()
        .offsetResolver((x, y, z) -> x == 0 && y == -1 && z == 0 ? target : null)
        .build();
    assertTrue(condition.test(base));
  }

  @Test
  void locationOffsetCombinedBlockPredicateRoundTrip() {
    Condition condition = Conditions.locationOffset(
        1,
        2,
        3,
        Conditions.allOf(
            Conditions.blockId(Key.key("minecraft:wheat")),
            Conditions.blockProperty("age", "2")));
    byte[] bytes = serializer.write(condition);
    Condition roundTrip = serializer.read(bytes);
    JsonObject written = JsonParser.parseString(
        new String(bytes, StandardCharsets.UTF_8)).getAsJsonObject();
    JsonObject block = written.getAsJsonObject("predicate").getAsJsonObject("block");
    assertTrue(block.has("blocks"));
    assertTrue(block.has("state"));
    assertTrue(roundTrip.test(ConditionContext.builder()
        .offsetResolver((x, y, z) -> x == 1 && y == 2 && z == 3
            ? ConditionContext.builder()
                .blockId(Key.key("minecraft:wheat"))
                .blockProperties(Map.of("age", "2"))
                .build()
            : null)
        .build()));
  }

  @Test
  void locationOffsetWithoutResolverFailsClosed() {
    Condition condition = Conditions.locationOffset(
        1,
        0,
        0,
        Conditions.blockId(Key.key("minecraft:stone")));
    assertFalse(condition.test(ConditionContext.builder().build()));
  }

  @Test
  void offhandItemJsonRoundTrip() {
    String json = """
        {
          "condition": "modularjobs:inventory_offhand_item",
          "material": ["minecraft:shield", "minecraft:totem_of_undying"],
          "minimum_amount": 1,
          "custom_model_data": 42
        }
        """;
    Condition condition = serializer.read(json.getBytes(StandardCharsets.UTF_8));
    OffhandItemCondition offhand = assertInstanceOf(OffhandItemCondition.class, condition);
    assertEquals(Set.of(Key.key("minecraft:shield"), Key.key("minecraft:totem_of_undying")),
        offhand.materials());
    assertEquals(1, offhand.minimumAmount());
    assertEquals(42, offhand.customModelData());

    byte[] bytes = serializer.write(condition);
    JsonObject written = JsonParser.parseString(
        new String(bytes, StandardCharsets.UTF_8)).getAsJsonObject();
    assertEquals("modularjobs:inventory_offhand_item", written.get("condition").getAsString());

    assertTrue(condition.test(ConditionContext.builder()
        .offhandItem(new ItemSnapshot(
            Key.key("minecraft:shield"), 3, 42, "Shield", List.of()))
        .build()));
    assertFalse(condition.test(ConditionContext.builder()
        .offhandItem(new ItemSnapshot(
            Key.key("minecraft:dirt"), 3, 42, null, List.of()))
        .build()));
  }

  @Test
  void offhandItemFailsClosedWithoutSnapshotOrAmount() {
    Condition condition = Conditions.offhandItem(
        Set.of(Key.key("minecraft:shield")), 2);
    assertFalse(condition.test(ConditionContext.builder().build()));
    assertFalse(condition.test(ConditionContext.builder()
        .offhandItem(new ItemSnapshot(Key.key("minecraft:shield"), 1, null, null, List.of()))
        .build()));
    assertTrue(condition.test(ConditionContext.builder()
        .offhandItem(new ItemSnapshot(Key.key("minecraft:shield"), 2, null, null, List.of()))
        .build()));
  }

  @Test
  void itemTypeCountJsonRoundTrip() {
    String json = """
        {
          "condition": "modularjobs:item_type_count",
          "items": ["minecraft:emerald", "minecraft:diamond"],
          "count": { "min": 8, "max": 64 }
        }
        """;
    Condition condition = serializer.read(json.getBytes(StandardCharsets.UTF_8));
    ItemTypeCountCondition count = assertInstanceOf(ItemTypeCountCondition.class, condition);
    assertEquals(Set.of(Key.key("minecraft:emerald"), Key.key("minecraft:diamond")),
        count.items());
    assertEquals(8, count.minimum());
    assertEquals(64, count.maximum());

    byte[] bytes = serializer.write(condition);
    JsonObject written = JsonParser.parseString(
        new String(bytes, StandardCharsets.UTF_8)).getAsJsonObject();
    assertEquals("modularjobs:item_type_count", written.get("condition").getAsString());
    assertTrue(written.getAsJsonObject("count").has("min"));
    assertTrue(written.getAsJsonObject("count").has("max"));

    assertTrue(condition.test(ConditionContext.builder()
        .itemSubject(ItemSubject.of(Key.key("minecraft:emerald"), 8))
        .build()));
    assertFalse(condition.test(ConditionContext.builder()
        .itemSubject(ItemSubject.of(Key.key("minecraft:emerald"), 7))
        .build()));
    assertFalse(condition.test(ConditionContext.builder()
        .itemSubject(ItemSubject.of(Key.key("minecraft:diamond"), 65))
        .build()));
    assertFalse(condition.test(ConditionContext.builder()
        .itemSubject(ItemSubject.of(Key.key("minecraft:dirt"), 10))
        .build()));
    assertFalse(condition.test(ConditionContext.builder().build()));
  }

  @Test
  void itemTypeCountUnboundedRange() {
    Condition minimumOnly = Conditions.itemTypeCount(
        Set.of(Key.key("minecraft:gold_ingot")), 5, null);
    assertTrue(minimumOnly.test(ConditionContext.builder()
        .itemSubject(ItemSubject.of(Key.key("minecraft:gold_ingot"), 100))
        .build()));
    assertFalse(minimumOnly.test(ConditionContext.builder()
        .itemSubject(ItemSubject.of(Key.key("minecraft:gold_ingot"), 4))
        .build()));

    Condition maximumOnly = Conditions.itemTypeCount(
        Set.of(Key.key("minecraft:iron_ingot")), null, 3);
    assertTrue(maximumOnly.test(ConditionContext.builder()
        .itemSubject(ItemSubject.of(Key.key("minecraft:iron_ingot"), 1))
        .build()));
    assertFalse(maximumOnly.test(ConditionContext.builder()
        .itemSubject(ItemSubject.of(Key.key("minecraft:iron_ingot"), 4))
        .build()));
  }

  @Test
  void equipmentSlotJsonRoundTrip() {
    String json = """
        {
          "condition": "minecraft:entity_properties",
          "entity": "this",
          "predicate": {
            "equipment": {
              "mainhand": {
                "items": ["minecraft:diamond_sword"],
                "count": { "min": 1, "max": 1 }
              }
            }
          }
        }
        """;
    Condition condition = serializer.read(json.getBytes(StandardCharsets.UTF_8));
    EquipmentItemCondition equipment = assertInstanceOf(EquipmentItemCondition.class, condition);
    assertEquals(EquipmentSlotKey.MAINHAND, equipment.slot());
    assertEquals(Set.of(Key.key("minecraft:diamond_sword")), equipment.items());

    JsonObject written = JsonParser.parseString(
        new String(serializer.write(condition), StandardCharsets.UTF_8)).getAsJsonObject();
    assertEquals("minecraft:entity_properties", written.get("condition").getAsString());
    assertTrue(written.getAsJsonObject("predicate")
        .getAsJsonObject("equipment").has("mainhand"));

    ConditionContext match = ConditionContext.builder()
        .equipmentSlot(
            EquipmentSlotKey.MAINHAND,
            ItemSubject.of(Key.key("minecraft:diamond_sword"), 1))
        .build();
    assertTrue(condition.test(match));
  }

  @Test
  void equipmentAbsentAndWrongSlotFailClosed() {
    Condition condition = Conditions.equipmentItem(
        EquipmentSlotKey.HEAD,
        Set.of(Key.key("minecraft:golden_helmet")),
        1,
        null);
    assertFalse(condition.test(ConditionContext.builder().build()));
    assertFalse(condition.test(ConditionContext.builder()
        .equipmentSlot(EquipmentSlotKey.MAINHAND,
            ItemSubject.of(Key.key("minecraft:golden_helmet"), 1))
        .build()));
    assertFalse(condition.test(ConditionContext.builder()
        .equipmentSlot(EquipmentSlotKey.HEAD,
            ItemSubject.of(Key.key("minecraft:leather_helmet"), 1))
        .build()));
    assertTrue(condition.test(ConditionContext.builder()
        .equipmentSlot(EquipmentSlotKey.HEAD,
            ItemSubject.of(Key.key("minecraft:golden_helmet"), 1))
        .build()));
  }

  @Test
  void equipmentMultiSlotAllOfRoundTrip() {
    String json = """
        {
          "condition": "minecraft:entity_properties",
          "entity": "this",
          "predicate": {
            "equipment": {
              "mainhand": { "items": ["minecraft:diamond_sword"] },
              "head": { "items": ["minecraft:netherite_helmet"] }
            }
          }
        }
        """;
    Condition condition = serializer.read(json.getBytes(StandardCharsets.UTF_8));

    byte[] bytes = serializer.write(condition);
    JsonObject written = JsonParser.parseString(
        new String(bytes, StandardCharsets.UTF_8)).getAsJsonObject();
    JsonObject slots = written.getAsJsonObject("predicate").getAsJsonObject("equipment");
    assertTrue(slots.has("mainhand"));
    assertTrue(slots.has("head"));

    Condition roundTrip = serializer.read(bytes);
    ConditionContext match = ConditionContext.builder()
        .equipmentSlot(EquipmentSlotKey.MAINHAND,
            ItemSubject.of(Key.key("minecraft:diamond_sword"), 1))
        .equipmentSlot(EquipmentSlotKey.HEAD,
            ItemSubject.of(Key.key("minecraft:netherite_helmet"), 1))
        .build();
    assertTrue(roundTrip.test(match));
    assertFalse(roundTrip.test(ConditionContext.builder()
        .equipmentSlot(EquipmentSlotKey.MAINHAND,
            ItemSubject.of(Key.key("minecraft:diamond_sword"), 1))
        .build()));
  }

  @Test
  void equipmentSlotRejectsUnsupportedComponents() {
    String json = """
        {
          "condition": "minecraft:entity_properties",
          "entity": "this",
          "predicate": {
            "equipment": {
              "mainhand": {
                "items": ["minecraft:diamond_sword"],
                "components": { "minecraft:trim": {} }
              }
            }
          }
        }
        """;
    IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
        () -> serializer.read(json.getBytes(StandardCharsets.UTF_8)));
    assertTrue(error.getMessage().contains("components are not supported"));

  }
  @Test
  void itemDurabilityJsonRoundTripAndBoundaries() {
    String json = """
        {
          "condition": "modularjobs:item_durability",
          "durability": { "min": 100, "max": 1561 }
        }
        """;
    Condition condition = serializer.read(json.getBytes(StandardCharsets.UTF_8));
    ItemDurabilityCondition durability = assertInstanceOf(ItemDurabilityCondition.class, condition);
    assertEquals(100, durability.minimum());
    assertEquals(1561, durability.maximum());

    byte[] bytes = serializer.write(condition);
    JsonObject written = JsonParser.parseString(
        new String(bytes, StandardCharsets.UTF_8)).getAsJsonObject();
    assertEquals("modularjobs:item_durability", written.get("condition").getAsString());

    ConditionContext damaged = ConditionContext.builder()
        .itemSubject(ItemSubject.of(
            Key.key("minecraft:diamond_pickaxe"), 1, 1561, 100))
        .build();
    assertTrue(condition.test(damaged));
    assertFalse(condition.test(ConditionContext.builder()
        .itemSubject(ItemSubject.of(Key.key("minecraft:diamond_pickaxe"), 1, 1561, 1462))
        .build()));
    assertFalse(condition.test(ConditionContext.builder().build()));
    assertFalse(condition.test(ConditionContext.builder()
        .itemSubject(ItemSubject.of(Key.key("minecraft:stick"), 1))
        .build()));

  }
  @Test
  void itemEnchantmentsJsonRoundTripAndMatching() {
    String json = """
        {
          "condition": "modularjobs:item_enchantments",
          "enchantments": [
            { "enchantment": "minecraft:fortune", "levels": { "min": 2, "max": 3 } },
            { "enchantment": "minecraft:unbreaking", "levels": { "min": 3 } }
          ]
        }
        """;
    Condition condition = serializer.read(json.getBytes(StandardCharsets.UTF_8));
    ItemEnchantmentsCondition enchantments =
        assertInstanceOf(ItemEnchantmentsCondition.class, condition);
    assertEquals(2, enchantments.enchantments().size());

    byte[] bytes = serializer.write(condition);
    JsonObject written = JsonParser.parseString(
        new String(bytes, StandardCharsets.UTF_8)).getAsJsonObject();
    assertEquals("modularjobs:item_enchantments", written.get("condition").getAsString());
    assertEquals(2, written.getAsJsonArray("enchantments").size());

    Map<Key, Integer> actual = Map.of(
        Key.key("minecraft:fortune"), 2,
        Key.key("minecraft:unbreaking"), 3);
    assertTrue(condition.test(ConditionContext.builder()
        .itemSubject(new ItemSubject(
            Key.key("minecraft:diamond_pickaxe"), 1, 1561, 0, actual, null))
        .build()));
    assertFalse(condition.test(ConditionContext.builder()
        .itemSubject(new ItemSubject(
            Key.key("minecraft:diamond_pickaxe"), 1, 1561, 0,
            Map.of(Key.key("minecraft:fortune"), 1), null))
        .build()));
    assertFalse(condition.test(ConditionContext.builder().build()));
  }

  @Test
  void itemTrimJsonRoundTripAndMatching() {
    String json = """
        {
          "condition": "modularjobs:item_trim",
          "components": {
            "minecraft:trim": {
              "material": "minecraft:amethyst",
              "pattern": "minecraft:sentry"
            }
          }
        }
        """;
    Condition condition = serializer.read(json.getBytes(StandardCharsets.UTF_8));
    ItemTrimCondition trim = assertInstanceOf(ItemTrimCondition.class, condition);
    assertEquals(Key.key("minecraft:amethyst"), trim.material());
    assertEquals(Key.key("minecraft:sentry"), trim.pattern());

    byte[] bytes = serializer.write(condition);
    JsonObject written = JsonParser.parseString(
        new String(bytes, StandardCharsets.UTF_8)).getAsJsonObject();
    assertEquals("modularjobs:item_trim", written.get("condition").getAsString());
    assertTrue(written.getAsJsonObject("components")
        .getAsJsonObject("minecraft:trim").has("material"));

    ConditionContext match = ConditionContext.builder()
        .itemSubject(new ItemSubject(
            Key.key("minecraft:diamond_helmet"), 1, null, null,
            Map.of(),
            new TrimSnapshot(
                Key.key("minecraft:amethyst"),
                Key.key("minecraft:sentry"))))
        .build();
    assertTrue(condition.test(match));
  }

  @Test
  void itemTrimFailsClosedWithoutTrimOrWrongMaterial() {
    Condition condition = Conditions.itemTrim(
        Key.key("minecraft:gold"), Key.key("minecraft:coast"));
    assertFalse(condition.test(ConditionContext.builder().build()));
    assertFalse(condition.test(ConditionContext.builder()
        .itemSubject(ItemSubject.of(Key.key("minecraft:diamond_helmet"), 1))
        .build()));
    assertFalse(condition.test(ConditionContext.builder()
        .itemSubject(new ItemSubject(
            Key.key("minecraft:diamond_helmet"), 1, null, null,
            Map.of(),
            new TrimSnapshot(
                Key.key("minecraft:amethyst"),
                Key.key("minecraft:coast"))))
        .build()));
  }

  @Test
  void itemTrimRejectsUnknownComponentKeysAndFields() {
    String unknownComponent = """
        {
          "condition": "modularjobs:item_trim",
          "components": {
            "minecraft:trim": { "material": "minecraft:gold" },
            "minecraft:other": {}
          }
        }
        """;
    IllegalArgumentException componentError = assertThrows(
        IllegalArgumentException.class,
        () -> serializer.read(unknownComponent.getBytes(StandardCharsets.UTF_8)));
    assertTrue(componentError.getMessage().contains("does not support component"));

    String unknownField = """
        {
          "condition": "modularjobs:item_trim",
          "components": {
            "minecraft:trim": {
              "material": "minecraft:gold",
              "unknown": true
            }
          }
        }
        """;
    IllegalArgumentException fieldError = assertThrows(
        IllegalArgumentException.class,
        () -> serializer.read(unknownField.getBytes(StandardCharsets.UTF_8)));
    assertTrue(fieldError.getMessage().contains("does not support field"));
  }

  @Test
  void typeSpecificVariantPredicatesMatch() {
    Condition wolf = serializer.read("""
        {"condition":"minecraft:entity_properties","entity":"this","predicate":{
          "type_specific":{"wolf":{"collar_color":"minecraft:red","variant":"minecraft:ashen"}}}}
        """.getBytes(StandardCharsets.UTF_8));
    assertTrue(wolf.test(ConditionContext.builder()
        .entityType(Key.key("minecraft:wolf"))
        .woolColor(Key.key("minecraft:red"))
        .wolfVariant(Key.key("minecraft:ashen"))
        .build()));
    assertFalse(wolf.test(ConditionContext.builder()
        .entityType(Key.key("minecraft:wolf"))
        .wolfVariant(Key.key("minecraft:ashen"))
        .build()));

    Condition villager = serializer.read("""
        {"condition":"minecraft:entity_properties","entity":"this","predicate":{
          "type_specific":{"villager":{"type":"minecraft:taiga",
            "profession":"minecraft:toolsmith","level":{"min":3}}}}}
        """.getBytes(StandardCharsets.UTF_8));
    assertTrue(villager.test(ConditionContext.builder()
        .entityType(Key.key("minecraft:villager"))
        .villagerType(Key.key("minecraft:taiga"))
        .villagerProfession(Key.key("minecraft:toolsmith"))
        .villagerLevel(4)
        .build()));
    assertFalse(villager.test(ConditionContext.builder()
        .entityType(Key.key("minecraft:villager"))
        .villagerType(Key.key("minecraft:taiga"))
        .villagerProfession(Key.key("minecraft:toolsmith"))
        .villagerLevel(2)
        .build()));

    Condition fish = Conditions.tropicalFish(
        null,
        Key.key("minecraft:stripey"),
        Key.key("minecraft:orange"),
        Key.key("minecraft:blue"));
    assertTrue(fish.test(ConditionContext.builder()
        .entityType(Key.key("minecraft:tropical_fish"))
        .tropicalFishPattern(Key.key("minecraft:stripey"))
        .tropicalFishBaseColor(Key.key("minecraft:orange"))
        .tropicalFishPatternColor(Key.key("minecraft:blue"))
        .build()));
    assertFalse(fish.test(ConditionContext.builder()
        .entityType(Key.key("minecraft:tropical_fish"))
        .tropicalFishPattern(Key.key("minecraft:stripey"))
        .tropicalFishBaseColor(Key.key("minecraft:white"))
        .tropicalFishPatternColor(Key.key("minecraft:blue"))
        .build()));

    Condition horse = serializer.read("""
        {"condition":"minecraft:entity_properties","entity":"this","predicate":{
          "type_specific":{"horse":{"variant":"minecraft:dark_brown",
            "armor":{"items":["minecraft:diamond_horse_armor"]}}}}}
        """.getBytes(StandardCharsets.UTF_8));
    assertTrue(horse.test(ConditionContext.builder()
        .entityType(Key.key("minecraft:horse"))
        .horseColor(Key.key("minecraft:dark_brown"))
        .horseArmor(ItemSubject.of(Key.key("minecraft:diamond_horse_armor"), 1))
        .build()));
    assertFalse(horse.test(ConditionContext.builder()
        .entityType(Key.key("minecraft:horse"))
        .horseColor(Key.key("minecraft:dark_brown"))
        .build()));
    assertRoundTrip(wolf);
    assertRoundTrip(villager);
    assertRoundTrip(fish);
    assertRoundTrip(horse);
  }

  private void assertRoundTrip(Condition condition) {
    byte[] bytes = serializer.write(condition);
    Condition roundTrip = serializer.read(bytes);
    assertEquals(JsonParser.parseString(new String(serializer.write(roundTrip),
        StandardCharsets.UTF_8)), JsonParser.parseString(new String(bytes,
        StandardCharsets.UTF_8)));
  }

  @Test
  void activeCooldownJsonRoundTripAndMatching() {
    String json = """
        {
          "condition": "modularjobs:active_cooldown",
          "key": "minecraft:ender_pearl",
          "source": "paper",
          "active": true,
          "minimum_remaining_ticks": 5
        }
        """;
    Condition condition = serializer.read(json.getBytes(StandardCharsets.UTF_8));

    byte[] bytes = serializer.write(condition);
    JsonObject written = JsonParser.parseString(
        new String(bytes, StandardCharsets.UTF_8)).getAsJsonObject();
    assertEquals("modularjobs:active_cooldown", written.get("condition").getAsString());
    assertEquals("minecraft:ender_pearl", written.get("key").getAsString());
    assertEquals("paper", written.get("source").getAsString());

    ConditionContext match = ConditionContext.builder()
        .activeCooldown(new CooldownSnapshot(
            Key.key("minecraft:ender_pearl"), "paper", 10))
        .build();
    assertTrue(condition.test(match));

    assertFalse(condition.test(ConditionContext.builder()
        .activeCooldown(new CooldownSnapshot(
            Key.key("minecraft:ender_pearl"), "custom", 10))
        .build()));
    assertFalse(condition.test(ConditionContext.builder()
        .activeCooldown(new CooldownSnapshot(
            Key.key("minecraft:ender_pearl"), "paper", 3))
        .build()));
    assertFalse(condition.test(ConditionContext.builder().build()));
  }

  @Test
  void activeCooldownSourceAgnosticAndInactive() {
    Condition sourceAgnostic = Conditions.activeCooldown(
        Key.key("minecraft:ender_pearl"), null, null, null);
    // Same key, two sources: source-agnostic query must match either.
    assertTrue(sourceAgnostic.test(ConditionContext.builder()
        .activeCooldown(new CooldownSnapshot(
            Key.key("minecraft:ender_pearl"), "custom", 10))
        .build()));
    assertFalse(sourceAgnostic.test(ConditionContext.builder()
        .activeCooldown(new CooldownSnapshot(
            Key.key("minecraft:golden_apple"), "paper", 10))
        .build()));

    Condition inactiveRequired = Conditions.activeCooldown(
        Key.key("minecraft:ender_pearl"), null, false, null);
    assertTrue(inactiveRequired.test(ConditionContext.builder().build()));
    assertFalse(inactiveRequired.test(ConditionContext.builder()
        .activeCooldown(new CooldownSnapshot(
            Key.key("minecraft:ender_pearl"), "paper", 1))
        .build()));
  }
}
