package dev.mintychochip.databag;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Map;
import java.util.Set;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Nullable;

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

  public static Condition timeCheck(Long min, Long max, Long period) {
    return new TimeCheckCondition(min, max, period);
  }

  public static Condition entityScores(Map<String, EntityScoresCondition.Bound> scores) {
    return new EntityScoresCondition(Objects.requireNonNull(scores));
  }

  public static Condition killedByPlayer(boolean expected) {
    return new KilledByPlayerCondition(expected);
  }

  public static Condition targeted(EntityTarget target, Condition condition) {
    return new EntityTargetCondition(
        Objects.requireNonNull(target), Objects.requireNonNull(condition));
  }

  public static Condition position(
      Double minX, Double maxX, Double minY, Double maxY, Double minZ, Double maxZ) {
    return new PositionCondition(minX, maxX, minY, maxY, minZ, maxZ);
  }

  public static Condition dimension(Key dimensionKey) {
    return new DimensionCondition(Objects.requireNonNull(dimensionKey));
  }

  public static Condition light(
      Integer minLevel,
      Integer maxLevel,
      Integer minSky,
      Integer maxSky,
      Integer minBlock,
      Integer maxBlock) {
    return new LightCondition(minLevel, maxLevel, minSky, maxSky, minBlock, maxBlock);
  }

  public static Condition canSeeSky(boolean expected) {
    return new CanSeeSkyCondition(expected);
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

  public static Condition team(String teamName) {
    return new TeamCondition(Objects.requireNonNull(teamName));
  }

  public static Condition ping(RelationalOperator operator, int milliseconds) {
    return new PingCondition(operator, milliseconds);
  }

  public static Condition emptySlots(int minimum, boolean includeOffhand, boolean includeHotbar) {
    return new EmptySlotsCondition(minimum, includeOffhand, includeHotbar);
  }

  public static Condition sheep(@Nullable Boolean sheared, @Nullable Key color) {
    return new SheepCondition(sheared, color);
  }

  public static Condition frog(Key variant) {
    return new FrogCondition(Objects.requireNonNull(variant));
  }

  public static Condition cat(Key variant) {
    return new CatCondition(Objects.requireNonNull(variant));
  }

  public static Condition requiredItemCount(Key material, int minimum) {
    return new RequiredItemCountCondition(Objects.requireNonNull(material), minimum);
  }

  public static Condition playtime(RelationalOperator operator, long ticks) {
    return new PlaytimeCondition(operator, ticks);
  }

  public static Condition armorSet(Set<Key> items) {
    return new ArmorSetCondition(Objects.requireNonNull(items));
  }

  public static Condition armorSet(Key... items) {
    return new ArmorSetCondition(Set.of(items));
  }

  public static Condition invulnerableFrames(RelationalOperator operator, int ticks) {
    return new InvulnerableFramesCondition(operator, ticks);
  }

  public static Condition advancement(Key advancement) {
    return new AdvancementCondition(Objects.requireNonNull(advancement));
  }

  public static Condition vehicle(Condition condition) {
    return new VehicleCondition(Objects.requireNonNull(condition));
  }

  public static Condition passenger(Condition condition) {
    return new PassengerCondition(Objects.requireNonNull(condition));
  }

  public static Condition blockPropertyRange(String name, int min, int max) {
    return new BlockPropertyRangeCondition(Objects.requireNonNull(name), min, max);
  }

  public static Condition periodicTick(int period, int offset) {
    return new PeriodicTickCondition(period, offset);
  }

  public static Condition randomChance(double chance) {
    return new RandomChanceCondition(chance);
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

  public static Condition steppingOn(Condition condition) {
    return new SteppingOnCondition(Objects.requireNonNull(condition));
  }

  public static Condition matchBlock(Condition condition) {
    return new MatchBlockCondition(Objects.requireNonNull(condition));
  }

  public static Condition locationOffset(
      int offsetX, int offsetY, int offsetZ, Condition condition) {
    return new LocationOffsetCondition(offsetX, offsetY, offsetZ, Objects.requireNonNull(condition));
  }

  public static Condition offhandItem(Set<Key> materials, int minimumAmount) {
    return new OffhandItemCondition(materials, minimumAmount, null);
  }

  public static Condition offhandItem(
      Set<Key> materials, int minimumAmount, @Nullable Integer customModelData) {
    return new OffhandItemCondition(
        materials == null ? Set.of() : materials, minimumAmount, customModelData);
  }

  public static Condition itemTypeCount(Set<Key> items, Integer minimum, Integer maximum) {
    return new ItemTypeCountCondition(
        items == null ? Set.of() : items, minimum, maximum);
  }

  public static Condition equipmentItem(
      EquipmentSlotKey slot, Set<Key> items, Integer minimum, Integer maximum) {
    return new EquipmentItemCondition(
        Objects.requireNonNull(slot), items == null ? Set.of() : items, minimum, maximum);
  }

  public static Condition itemDurability(Integer minimum, Integer maximum) {
    return new ItemDurabilityCondition(minimum, maximum);
  }

  public static Condition itemEnchantments(Map<Key, ItemEnchantmentsCondition.LevelBound> bounds) {
    return new ItemEnchantmentsCondition(bounds == null ? Map.of() : bounds);
  }

  public static Condition itemTrim(@Nullable Key material, @Nullable Key pattern) {
    return new ItemTrimCondition(material, pattern);
  }

  public static Condition wolf(@Nullable Key collarColor, @Nullable Key variant) {
    return new WolfVariantCondition(collarColor, variant);
  }

  public static Condition tropicalFish(
      @Nullable Integer variant, @Nullable Key pattern, @Nullable Key baseColor,
      @Nullable Key patternColor) {
    return new TropicalFishCondition(variant, pattern, baseColor, patternColor);
  }

  public static Condition villager(
      @Nullable Key type, @Nullable Key profession, Integer minimumLevel, Integer maximumLevel) {
    return new VillagerCondition(type, profession, minimumLevel, maximumLevel);
  }

  public static Condition horse(@Nullable Key color, Set<Key> armorItems) {
    return new HorseCondition(color, armorItems == null ? Set.of() : armorItems);
  }

  public static Condition activeCooldown(
      Key key,
      @Nullable String source,
      @Nullable Boolean active,
      @Nullable Integer minimumRemainingTicks) {
    return new ActiveCooldownCondition(key, source, active, minimumRemainingTicks);
  }
}
