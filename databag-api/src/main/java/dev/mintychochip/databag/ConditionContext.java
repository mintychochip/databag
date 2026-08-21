package dev.mintychochip.databag;

import dev.mintychochip.databag.DataBag;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Nullable;

/**
 * Immutable evaluation snapshot. May describe a player, a non-player living
 * entity, a block, or a combination (for example a player standing on a block).
 * Conditions fail closed when the fields they need are absent.
 *
 * <p>{@code present} means a <em>player</em> is in the snapshot. Living-entity
 * conditions use {@code livingPresent}; block conditions use {@code blockId}.
 *
 * @param present {@code true} when a player is in the snapshot
 */
public record ConditionContext(
    boolean present,
    boolean livingPresent,
    boolean sneaking,
    boolean sprinting,
    boolean onFire,
    boolean onGround,
    boolean swimming,
    boolean baby,
    boolean flying,
    boolean gliding,
    @Nullable Key entityType,
    @Nullable String gameMode,
    @Nullable Key biome,
    @Nullable String worldName,
    @Nullable Key worldKey,
    @Nullable WeatherState weather,
    @Nullable Key fluid,
    @Nullable Double health,
    @Nullable Double hunger,
    @Nullable Double experience,
    Map<Key, PotionEffectSnapshot> effects,
    Set<String> jobKeys,
    @Nullable Key blockId,
    Map<String, String> blockProperties,
    @Nullable Long dayTime,
    @Nullable Double xpLevel,
    @Nullable Double absorption,
    @Nullable Double airRemaining,
    @Nullable Boolean attackingPlayer,
    @Nullable Double x,
    @Nullable Double y,
    @Nullable Double z,
    @Nullable Integer lightLevel,
    @Nullable Integer skyLight,
    @Nullable Integer blockLight,
    @Nullable Boolean canSeeSky,
    Map<String, Integer> scores,
    DataBag extras
) {

  public ConditionContext {
    effects = Map.copyOf(effects == null ? Map.of() : effects);
    jobKeys = Set.copyOf(jobKeys == null ? Set.of() : jobKeys);
    blockProperties = Map.copyOf(blockProperties == null ? Map.of() : blockProperties);
    scores = Map.copyOf(scores == null ? Map.of() : scores);
    extras = extras == null ? DataBag.create() : DataBag.fromBytes(extras.toBytes());
  }

  /** Extension snapshot data for registered {@link ConditionHandler}s. */
  public DataBag extras() {
    return DataBag.fromBytes(extras.toBytes());
  }

  public static ConditionContext absent() {
    return builder().present(false).livingPresent(false).build();
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {

    private boolean present = true;
    private boolean livingPresent = true;
    private boolean sneaking;
    private boolean sprinting;
    private boolean onFire;
    private boolean onGround;
    private boolean swimming;
    private boolean baby;
    private boolean flying;
    private boolean gliding;
    private @Nullable Key entityType;
    private @Nullable String gameMode;
    private @Nullable Key biome;
    private @Nullable String worldName;
    private @Nullable Key worldKey;
    private @Nullable WeatherState weather;
    private @Nullable Key fluid;
    private @Nullable Double health;
    private @Nullable Double hunger;
    private @Nullable Double experience;
    private Map<Key, PotionEffectSnapshot> effects = Map.of();
    private Set<String> jobKeys = Set.of();
    private @Nullable Key blockId;
    private Map<String, String> blockProperties = Map.of();
    private @Nullable Long dayTime;
    private @Nullable Double xpLevel;
    private @Nullable Double absorption;
    private @Nullable Double airRemaining;
    private @Nullable Boolean attackingPlayer;
    private @Nullable Double x;
    private @Nullable Double y;
    private @Nullable Double z;
    private @Nullable Integer lightLevel;
    private @Nullable Integer skyLight;
    private @Nullable Integer blockLight;
    private @Nullable Boolean canSeeSky;
    private Map<String, Integer> scores = Map.of();
    private DataBag extras = DataBag.create();
    private boolean livingPresentOverridden;

    private Builder() {}

    /**
     * Marks a player as present. Also marks a living entity present unless
     * {@link #livingPresent(boolean)} was set explicitly.
     */
    public Builder present(boolean present) {
      this.present = present;
      if (!livingPresentOverridden && present) {
        this.livingPresent = true;
      }
      if (!present && !livingPresentOverridden) {
        this.livingPresent = false;
      }
      return this;
    }

    public Builder livingPresent(boolean livingPresent) {
      this.livingPresent = livingPresent;
      this.livingPresentOverridden = true;
      return this;
    }

    public Builder sneaking(boolean sneaking) {
      this.sneaking = sneaking;
      return this;
    }

    public Builder sprinting(boolean sprinting) {
      this.sprinting = sprinting;
      return this;
    }

    public Builder onFire(boolean onFire) {
      this.onFire = onFire;
      return this;
    }

    public Builder onGround(boolean onGround) {
      this.onGround = onGround;
      return this;
    }

    public Builder swimming(boolean swimming) {
      this.swimming = swimming;
      return this;
    }

    public Builder baby(boolean baby) {
      this.baby = baby;
      return this;
    }

    public Builder flying(boolean flying) {
      this.flying = flying;
      return this;
    }

    public Builder gliding(boolean gliding) {
      this.gliding = gliding;
      return this;
    }

    public Builder entityType(@Nullable Key entityType) {
      this.entityType = entityType;
      return this;
    }

    public Builder gameMode(@Nullable String gameMode) {
      this.gameMode = gameMode;
      return this;
    }

    public Builder biome(@Nullable Key biome) {
      this.biome = biome;
      return this;
    }

    public Builder worldName(@Nullable String worldName) {
      this.worldName = worldName;
      return this;
    }

    public Builder worldKey(@Nullable Key worldKey) {
      this.worldKey = worldKey;
      return this;
    }

    public Builder weather(@Nullable WeatherState weather) {
      this.weather = weather;
      return this;
    }

    public Builder fluid(@Nullable Key fluid) {
      this.fluid = fluid;
      return this;
    }

    public Builder health(@Nullable Double health) {
      this.health = health;
      return this;
    }

    public Builder hunger(@Nullable Double hunger) {
      this.hunger = hunger;
      return this;
    }

    public Builder dayTime(@Nullable Long dayTime) {
      this.dayTime = dayTime;
      return this;
    }

    public Builder xpLevel(@Nullable Double xpLevel) {
      this.xpLevel = xpLevel;
      return this;
    }

    public Builder absorption(@Nullable Double absorption) {
      this.absorption = absorption;
      return this;
    }

    public Builder airRemaining(@Nullable Double airRemaining) {
      this.airRemaining = airRemaining;
      return this;
    }

    public Builder attackingPlayer(@Nullable Boolean attackingPlayer) {
      this.attackingPlayer = attackingPlayer;
      return this;
    }

    public Builder x(@Nullable Double x) {
      this.x = x;
      return this;
    }

    public Builder y(@Nullable Double y) {
      this.y = y;
      return this;
    }

    public Builder z(@Nullable Double z) {
      this.z = z;
      return this;
    }

    public Builder lightLevel(@Nullable Integer lightLevel) {
      this.lightLevel = lightLevel;
      return this;
    }

    public Builder skyLight(@Nullable Integer skyLight) {
      this.skyLight = skyLight;
      return this;
    }

    public Builder blockLight(@Nullable Integer blockLight) {
      this.blockLight = blockLight;
      return this;
    }

    public Builder canSeeSky(@Nullable Boolean canSeeSky) {
      this.canSeeSky = canSeeSky;
      return this;
    }

    public Builder scores(Map<String, Integer> scores) {
      this.scores = Objects.requireNonNull(scores);
      return this;
    }
    public Builder experience(@Nullable Double experience) {
      this.experience = experience;
      return this;
    }

    public Builder effects(Map<Key, PotionEffectSnapshot> effects) {
      this.effects = Objects.requireNonNull(effects);
      return this;
    }

    public Builder jobKeys(Set<String> jobKeys) {
      this.jobKeys = Objects.requireNonNull(jobKeys);
      return this;
    }

    public Builder blockId(@Nullable Key blockId) {
      this.blockId = blockId;
      return this;
    }

    public Builder blockProperties(Map<String, String> blockProperties) {
      this.blockProperties = Objects.requireNonNull(blockProperties);
      return this;
    }

    public Builder extras(DataBag extras) {
      this.extras = extras == null ? DataBag.create() : extras;
      return this;
    }

    public ConditionContext build() {
      return new ConditionContext(
          present,
          livingPresent,
          sneaking,
          sprinting,
          onFire,
          onGround,
          swimming,
          baby,
          flying,
          gliding,
          entityType,
          gameMode,
          biome,
          worldName,
          worldKey,
          weather,
          fluid,
          health,
          hunger,
          experience,
          effects,
          jobKeys,
          blockId,
          blockProperties,
          dayTime,
          xpLevel,
          absorption,
          airRemaining,
          attackingPlayer,
          x,
          y,
          z,
          lightLevel,
          skyLight,
          blockLight,
          canSeeSky,
          scores,
          extras);
    }
  }
}
