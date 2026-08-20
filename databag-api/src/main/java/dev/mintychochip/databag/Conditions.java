package dev.mintychochip.databag;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import net.kyori.adventure.key.Key;

/**
 * Factories for the built-in {@link Condition} graph. No Bridge / Bukkit.
 */
public final class Conditions {

  private Conditions() {}

  public static Condition always() {
    return AlwaysCondition.INSTANCE;
  }

  public static Condition sneaking(boolean expected) {
    return new SneakingCondition(expected);
  }

  public static Condition sprinting(boolean expected) {
    return new SprintingCondition(expected);
  }

  public static Condition entityType(Key entityType) {
    return new EntityTypeCondition(Objects.requireNonNull(entityType));
  }

  public static Condition onFire(boolean expected) {
    return new OnFireCondition(expected);
  }

  public static Condition onGround(boolean expected) {
    return new OnGroundCondition(expected);
  }

  public static Condition swimming(boolean expected) {
    return new SwimmingCondition(expected);
  }

  public static Condition baby(boolean expected) {
    return new BabyCondition(expected);
  }

  public static Condition gliding(boolean expected) {
    return new GlidingCondition(expected);
  }

  public static Condition flying(boolean expected) {
    return new FlyingCondition(expected);
  }

  public static Condition gameMode(String gameMode) {
    return new GameModeCondition(gameMode);
  }

  public static Condition blockId(Key blockId) {
    return new BlockIdCondition(Objects.requireNonNull(blockId));
  }

  public static Condition blockProperty(String name, String value) {
    return new BlockPropertyCondition(name, value);
  }

  public static Condition biome(Key biomeKey) {
    return new BiomeCondition(Objects.requireNonNull(biomeKey));
  }

  public static Condition world(String worldName) {
    return new WorldCondition(worldName);
  }

  public static Condition weather(WeatherState state) {
    return new WeatherCondition(Objects.requireNonNull(state));
  }

  public static Condition fluid(Key fluidKey) {
    return new FluidCondition(Objects.requireNonNull(fluidKey));
  }

  public static Condition playerResource(
      PlayerResourceType type, RelationalOperator operator, double expected) {
    return new PlayerResourceCondition(type, operator, expected);
  }

  public static Condition potionPresent(Key effectKey) {
    return new PotionPresentCondition(Objects.requireNonNull(effectKey));
  }

  public static Condition potionAmplifier(
      Key effectKey, RelationalOperator operator, int expected) {
    return new PotionAmplifierCondition(Objects.requireNonNull(effectKey), operator, expected);
  }

  public static Condition potionDuration(
      Key effectKey, RelationalOperator operator, int expected) {
    return new PotionDurationCondition(Objects.requireNonNull(effectKey), operator, expected);
  }

  public static Condition job(String jobKey) {
    return new JobCondition(Set.of(jobKey));
  }

  public static Condition jobAny(String... jobKeys) {
    return new JobCondition(Set.of(jobKeys));
  }

  public static Condition allOf(Condition... terms) {
    return new AllOfCondition(List.of(terms));
  }

  public static Condition allOf(Condition a, Condition b) {
    List<Condition> terms = new ArrayList<>();
    flattenAll(a, terms);
    flattenAll(b, terms);
    return new AllOfCondition(List.copyOf(terms));
  }

  public static Condition anyOf(Condition... terms) {
    return new AnyOfCondition(List.of(terms));
  }

  public static Condition anyOf(Condition a, Condition b) {
    List<Condition> terms = new ArrayList<>();
    flattenAny(a, terms);
    flattenAny(b, terms);
    return new AnyOfCondition(List.copyOf(terms));
  }

  public static Condition inverted(Condition term) {
    return new InvertedCondition(Objects.requireNonNull(term));
  }

  private static void flattenAll(Condition condition, List<Condition> out) {
    if (condition instanceof AllOfCondition all) {
      out.addAll(all.terms());
    } else {
      out.add(condition);
    }
  }

  private static void flattenAny(Condition condition, List<Condition> out) {
    if (condition instanceof AnyOfCondition any) {
      out.addAll(any.terms());
    } else {
      out.add(condition);
    }
  }
}
