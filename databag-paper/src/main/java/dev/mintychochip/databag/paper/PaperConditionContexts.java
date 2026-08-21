package dev.mintychochip.databag.paper;

import dev.mintychochip.databag.ConditionContext;
import dev.mintychochip.databag.PotionEffectSnapshot;
import dev.mintychochip.databag.WeatherState;
import dev.mintychochip.databag.DataBag;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.kyori.adventure.key.Key;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.Nullable;

/**
 * Builds a {@link ConditionContext} from live Paper objects.
 */
public final class PaperConditionContexts {

  private PaperConditionContexts() {}

  /** Snapshot of {@code player} with no job keys. */
  public static ConditionContext from(@Nullable Player player) {
    return from(player, Set.of());
  }

  /**
   * Snapshot of {@code player} plus optional job keys (namespaced or bare).
   */
  public static ConditionContext from(
      @Nullable Player player, @Nullable Set<String> jobKeys) {
    return from(player, jobKeys, DataBag.create());
  }

  /**
   * Snapshot of {@code player} plus job keys and extension {@link DataBag}
   * extras for registered {@link dev.mintychochip.databag.ConditionHandler}s.
   */
  public static ConditionContext from(
      @Nullable Player player, @Nullable Set<String> jobKeys, @Nullable DataBag extras) {
    if (player == null || !player.isOnline()) {
      return ConditionContext.absent();
    }
    return livingBuilder(player)
        .present(true)
        .entityType(Key.key("minecraft:player"))
        .flying(player.isFlying())
        .gameMode(player.getGameMode().name().toLowerCase(Locale.ROOT))
        .hunger((double) player.getFoodLevel())
        .experience((double) player.getExp())
        .xpLevel((double) player.getLevel())
        .absorption(player.getAbsorptionAmount())
        .airRemaining((double) player.getRemainingAir())
        .scores(playerScores(player))
        .jobKeys(jobKeys == null ? Set.of() : jobKeys)
        .extras(extras == null ? DataBag.create() : extras)
        .build();
  }

  /**
   * Snapshot of a living entity that is not required to be a player.
   */
  public static ConditionContext fromLiving(@Nullable LivingEntity entity) {
    if (entity == null || entity.isDead()) {
      return ConditionContext.absent();
    }
    if (entity instanceof Player player) {
      return from(player);
    }
    return livingBuilder(entity)
        .present(false)
        .flying(false)
        .gameMode(null)
        .build();
  }

  /**
   * Snapshot of a block (id + block-state properties) and its location weather.
   */
  public static ConditionContext fromBlock(@Nullable Block block) {
    if (block == null) {
      return ConditionContext.absent();
    }
    World world = block.getWorld();
    Location loc = block.getLocation();
    return ConditionContext.builder()
        .present(false)
        .livingPresent(false)
        .blockId(block.getType().getKey())
        .blockProperties(BlockDataStrings.properties(block.getBlockData().getAsString()))
        .worldName(world.getName())
        .worldKey(world.getKey())
        .biome(world.getBiome(loc).getKey())
        .weather(weatherOf(world))
        .x(loc.getX())
        .y(loc.getY())
        .z(loc.getZ())
        .lightLevel((int) block.getLightLevel())
        .skyLight((int) block.getLightFromSky())
        .blockLight((int) block.getLightFromBlocks())
        .canSeeSky(block.getLightFromSky() >= 15)
        .dayTime(world.getFullTime())
        .build();
  }

  private static ConditionContext.Builder livingBuilder(LivingEntity entity) {
    World world = entity.getWorld();
    Location loc = entity.getLocation();
    Block block = loc.getBlock();
    Key fluid = entity.isInWater()
        ? Key.key("minecraft:water")
        : entity.isInLava() ? Key.key("minecraft:lava") : null;
    Map<Key, PotionEffectSnapshot> effects = new HashMap<>();
    for (PotionEffect effect : entity.getActivePotionEffects()) {
      effects.put(
          effect.getType().getKey(),
          new PotionEffectSnapshot(effect.getAmplifier(), effect.getDuration()));
    }
    boolean baby = entity instanceof Ageable ageable && !ageable.isAdult();
    return ConditionContext.builder()
        .livingPresent(true)
        .entityType(entity.getType().getKey())
        .sneaking(entity.isSneaking())
        .sprinting(entity instanceof Player player && player.isSprinting())
        .onFire(entity.getFireTicks() > 0)
        .onGround(entity.isOnGround())
        .swimming(entity.isSwimming())
        .baby(baby)
        .gliding(entity.isGliding())
        .biome(world.getBiome(loc).getKey())
        .worldName(world.getName())
        .worldKey(world.getKey())
        .weather(weatherOf(world))
        .fluid(fluid)
        .health(entity.getHealth())
        .effects(effects)
        .x(loc.getX())
        .y(loc.getY())
        .z(loc.getZ())
        .lightLevel((int) block.getLightLevel())
        .skyLight((int) block.getLightFromSky())
        .blockLight((int) block.getLightFromBlocks())
        .canSeeSky(block.getLightFromSky() >= 15)
        .dayTime(world.getFullTime());
  }

  private static Map<String, Integer> playerScores(Player player) {
    try {
      Scoreboard scoreboard = player.getScoreboard();
      Map<String, Integer> scores = new HashMap<>();
      for (Objective objective : scoreboard.getObjectives()) {
        Score score = objective.getScore(player.getName());
        if (score.isScoreSet()) {
          scores.put(objective.getName(), score.getScore());
        }
      }
      return scores;
    } catch (RuntimeException ignored) {
      return Map.of();
    }
  }

  private static WeatherState weatherOf(World world) {
    return world.isThundering()
        ? WeatherState.THUNDERING
        : world.hasStorm() ? WeatherState.RAINING : WeatherState.CLEAR;
  }

}
