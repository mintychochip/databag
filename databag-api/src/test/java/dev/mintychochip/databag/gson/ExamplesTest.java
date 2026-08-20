package dev.mintychochip.databag.gson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import dev.mintychochip.databag.Condition;
import dev.mintychochip.databag.ConditionContext;
import dev.mintychochip.databag.ConditionSerializer;
import dev.mintychochip.databag.Conditions;
import dev.mintychochip.databag.PlayerResourceType;
import dev.mintychochip.databag.PotionEffectSnapshot;
import dev.mintychochip.databag.RelationalOperator;
import dev.mintychochip.databag.WeatherState;
import net.kyori.adventure.key.Key;
import org.junit.jupiter.api.Test;

/**
 * Runnable examples for the public API. These are the snippets in README.md.
 */
class ExamplesTest {

  private final ConditionSerializer json = GsonConditionSerializer.gson();

  @Test
  void javaGraphNetherSneak() {
    Condition condition = Conditions.allOf(
        Conditions.world("world_nether"),
        Conditions.sneaking(true));

    ConditionContext matching = ConditionContext.builder()
        .present(true)
        .worldName("world_nether")
        .sneaking(true)
        .build();
    ConditionContext standing = ConditionContext.builder()
        .present(true)
        .worldName("world_nether")
        .sneaking(false)
        .build();

    assertTrue(condition.test(matching));
    assertFalse(condition.test(standing));
  }

  @Test
  void vanillaJsonSneakingRoundTrip() {
    String sneaking = """
        {
          "condition": "minecraft:entity_properties",
          "entity": "this",
          "predicate": { "flags": { "is_sneaking": true } }
        }
        """;
    Condition condition = json.read(sneaking.getBytes(StandardCharsets.UTF_8));
    assertTrue(condition.test(ConditionContext.builder().present(true).sneaking(true).build()));
    assertFalse(condition.test(ConditionContext.builder().present(true).sneaking(false).build()));
  }

  @Test
  void vanillaJsonAllOfWorldAndSneak() {
    String allOf = """
        {
          "condition": "minecraft:all_of",
          "terms": [
            { "condition": "modularjobs:world", "world": "world_nether" },
            {
              "condition": "minecraft:entity_properties",
              "entity": "this",
              "predicate": { "flags": { "is_sneaking": true } }
            }
          ]
        }
        """;
    Condition condition = json.read(allOf.getBytes(StandardCharsets.UTF_8));
    assertTrue(condition.test(ConditionContext.builder()
        .present(true)
        .worldName("world_nether")
        .sneaking(true)
        .build()));
  }

  @Test
  void weatherLowHealthAndJob() {
    Condition condition = Conditions.allOf(
        Conditions.weather(WeatherState.RAINING),
        Conditions.playerResource(PlayerResourceType.HEALTH, RelationalOperator.LESS_THAN_OR_EQUAL, 6.0),
        Conditions.potionPresent(Key.key("minecraft:strength")),
        Conditions.job("miner"));

    byte[] bytes = json.write(condition);
    Condition roundTrip = json.read(bytes);
    assertEquals(
        true,
        roundTrip.test(ConditionContext.builder()
            .present(true)
            .weather(WeatherState.RAINING)
            .health(4.0)
            .effects(Map.of(Key.key("minecraft:strength"), new PotionEffectSnapshot(0, 200)))
            .jobKeys(Set.of("modularjobs:miner"))
            .build()));
  }

  @Test
  void boostRuleStoresConditionAsBytes() {
    Condition sneak = Conditions.sneaking(true);
    byte[] conditionBytes = json.write(sneak);
    // Boost JSON embeds those bytes (Gson encodes byte[] as base64):
    // { "priority": 100, "conditions": "<base64>", "boost": { "type": "multiplicative", "amount": 1.25 } }
    assertTrue(conditionBytes.length > 0);
    assertTrue(new String(conditionBytes, StandardCharsets.UTF_8).contains("is_sneaking"));
  }
}
