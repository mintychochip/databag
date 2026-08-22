package dev.mintychochip.databag;

import dev.mintychochip.databag.DataBag;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
    @Nullable ConditionContext attacker,
    @Nullable ConditionContext directAttacker,
    @Nullable ConditionContext attackingPlayer,
    @Nullable ConditionContext targetEntity,
    @Nullable ConditionContext interactingEntity,
    @Nullable Double x,
    @Nullable Double y,
    @Nullable Double z,
    @Nullable Integer lightLevel,
    @Nullable Integer skyLight,
    @Nullable Integer blockLight,
    @Nullable Boolean canSeeSky,
    @Nullable String team,
    @Nullable Integer ping,
    @Nullable Integer emptyMain,
    @Nullable Integer emptyHotbar,
    @Nullable Integer emptyOffhand,
    @Nullable Boolean sheared,
    @Nullable Key woolColor,
    @Nullable Key frogVariant,
    @Nullable Key catVariant,
    @Nullable Key wolfVariant,
    @Nullable Integer tropicalFishVariant,
    @Nullable Key tropicalFishPattern,
    @Nullable Key tropicalFishBaseColor,
    @Nullable Key tropicalFishPatternColor,
    @Nullable Key villagerType,
    @Nullable Key villagerProfession,
    @Nullable Integer villagerLevel,
    @Nullable Key horseColor,
    @Nullable ItemSubject horseArmor,
    Map<Key, Integer> itemCounts,
    @Nullable Long playtime,
    Set<Key> armorSet,
    @Nullable ItemSnapshot offhandItem,
    @Nullable ItemSubject itemSubject,
    Map<EquipmentSlotKey, ItemSubject> equipment,
    Set<CooldownSnapshot> activeCooldowns,
    @Nullable Integer invulnerableTicks,
    Set<String> advancements,
    @Nullable ConditionContext vehicle,
    List<ConditionContext> passengers,
    @Nullable Integer ticksLived,
    @Nullable Random random,
    @Nullable ConditionContext standingOn,
    @Nullable OffsetContextResolver offsetResolver,
    Map<String, Integer> scores,
    DataBag extras
) {

  public ConditionContext {
    effects = Map.copyOf(effects == null ? Map.of() : effects);
    jobKeys = Set.copyOf(jobKeys == null ? Set.of() : jobKeys);
    blockProperties = Map.copyOf(blockProperties == null ? Map.of() : blockProperties);
    itemCounts = Map.copyOf(itemCounts == null ? Map.of() : itemCounts);
    armorSet = Set.copyOf(armorSet == null ? Set.of() : armorSet);
    offhandItem = offhandItem;
    itemSubject = itemSubject;
    equipment = Map.copyOf(equipment == null ? Map.of() : equipment);
    activeCooldowns = Set.copyOf(activeCooldowns == null ? Set.of() : activeCooldowns);
    invulnerableTicks = invulnerableTicks;
    advancements = Set.copyOf(advancements == null ? Set.of() : advancements);
    vehicle = vehicle;
    passengers = List.copyOf(passengers == null ? List.of() : passengers);
    scores = Map.copyOf(scores == null ? Map.of() : scores);
    standingOn = standingOn;
    offsetResolver = offsetResolver;
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
    private @Nullable ConditionContext attacker;
    private @Nullable ConditionContext directAttacker;
    private @Nullable ConditionContext attackingPlayer;
    private @Nullable ConditionContext targetEntity;
    private @Nullable ConditionContext interactingEntity;
    private @Nullable Double x;
    private @Nullable Double y;
    private @Nullable Double z;
    private @Nullable Integer lightLevel;
    private @Nullable Integer skyLight;
    private @Nullable Integer blockLight;
    private @Nullable Boolean canSeeSky;
    private @Nullable String team;
    private @Nullable Integer ping;
    private @Nullable Integer emptyMain;
    private @Nullable Integer emptyHotbar;
    private @Nullable Integer emptyOffhand;
    private @Nullable Boolean sheared;
    private @Nullable Key woolColor;
    private @Nullable Key frogVariant;
    private @Nullable Key catVariant;
    private @Nullable Key wolfVariant;
    private @Nullable Integer tropicalFishVariant;
    private @Nullable Key tropicalFishPattern;
    private @Nullable Key tropicalFishBaseColor;
    private @Nullable Key tropicalFishPatternColor;
    private @Nullable Key villagerType;
    private @Nullable Key villagerProfession;
    private @Nullable Integer villagerLevel;
    private @Nullable Key horseColor;
    private @Nullable ItemSubject horseArmor;
    private Map<Key, Integer> itemCounts = Map.of();
    private @Nullable Long playtime;
    private Set<Key> armorSet = Set.of();
    private @Nullable ItemSnapshot offhandItem;
    private @Nullable ItemSubject itemSubject;
    private Map<EquipmentSlotKey, ItemSubject> equipment = Map.of();
    private Set<CooldownSnapshot> activeCooldowns = Set.of();
    private @Nullable Integer invulnerableTicks;
    private Set<String> advancements = Set.of();
    private @Nullable ConditionContext vehicle;
    private List<ConditionContext> passengers = List.of();
    private @Nullable Integer ticksLived;
    private @Nullable Random random;
    private @Nullable ConditionContext standingOn;
    private @Nullable OffsetContextResolver offsetResolver;
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

    public Builder attacker(@Nullable ConditionContext attacker) {
      this.attacker = attacker;
      return this;
    }

    public Builder directAttacker(@Nullable ConditionContext directAttacker) {
      this.directAttacker = directAttacker;
      return this;
    }

    public Builder attackingPlayer(@Nullable ConditionContext attackingPlayer) {
      this.attackingPlayer = attackingPlayer;
      return this;
    }

    public Builder targetEntity(@Nullable ConditionContext targetEntity) {
      this.targetEntity = targetEntity;
      return this;
    }

    public Builder interactingEntity(@Nullable ConditionContext interactingEntity) {
      this.interactingEntity = interactingEntity;
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

    public Builder team(@Nullable String team) {
      this.team = team;
      return this;
    }

    public Builder ping(@Nullable Integer ping) {
      this.ping = ping;
      return this;
    }

    public Builder emptyMain(@Nullable Integer emptyMain) {
      this.emptyMain = emptyMain;
      return this;
    }

    public Builder emptyHotbar(@Nullable Integer emptyHotbar) {
      this.emptyHotbar = emptyHotbar;
      return this;
    }

    public Builder emptyOffhand(@Nullable Integer emptyOffhand) {
      this.emptyOffhand = emptyOffhand;
      return this;
    }

    public Builder sheared(@Nullable Boolean sheared) {
      this.sheared = sheared;
      return this;
    }

    public Builder woolColor(@Nullable Key woolColor) {
      this.woolColor = woolColor;
      return this;
    }

    public Builder frogVariant(@Nullable Key frogVariant) {
      this.frogVariant = frogVariant;
      return this;
    }

    public Builder catVariant(@Nullable Key catVariant) {
      this.catVariant = catVariant;
      return this;
    }
    public Builder wolfVariant(@Nullable Key wolfVariant) {
      this.wolfVariant = wolfVariant;
      return this;
    }
    public Builder tropicalFishVariant(@Nullable Integer tropicalFishVariant) {
      this.tropicalFishVariant = tropicalFishVariant;
      return this;
    }
    public Builder tropicalFishPattern(@Nullable Key tropicalFishPattern) {
      this.tropicalFishPattern = tropicalFishPattern;
      return this;
    }
    public Builder tropicalFishBaseColor(@Nullable Key tropicalFishBaseColor) {
      this.tropicalFishBaseColor = tropicalFishBaseColor;
      return this;
    }
    public Builder tropicalFishPatternColor(@Nullable Key tropicalFishPatternColor) {
      this.tropicalFishPatternColor = tropicalFishPatternColor;
      return this;
    }
    public Builder villagerType(@Nullable Key villagerType) {
      this.villagerType = villagerType;
      return this;
    }
    public Builder villagerProfession(@Nullable Key villagerProfession) {
      this.villagerProfession = villagerProfession;
      return this;
    }
    public Builder villagerLevel(@Nullable Integer villagerLevel) {
      this.villagerLevel = villagerLevel;
      return this;
    }
    public Builder horseColor(@Nullable Key horseColor) {
      this.horseColor = horseColor;
      return this;
    }
    public Builder horseArmor(@Nullable ItemSubject horseArmor) {
      this.horseArmor = horseArmor;
      return this;
    }

    public Builder itemCounts(Map<Key, Integer> itemCounts) {
      this.itemCounts = Objects.requireNonNull(itemCounts);
      return this;
    }

    public Builder playtime(@Nullable Long playtime) {
      this.playtime = playtime;
      return this;
    }

    public Builder armorSet(Set<Key> armorSet) {
      this.armorSet = Objects.requireNonNull(armorSet);
      return this;
    }
    public Builder offhandItem(@Nullable ItemSnapshot offhandItem) {
      this.offhandItem = offhandItem;
      return this;
    }
    public Builder itemSubject(@Nullable ItemSubject itemSubject) {
      this.itemSubject = itemSubject;
      return this;
    }
    public Builder equipment(Map<EquipmentSlotKey, ItemSubject> equipment) {
      this.equipment = new java.util.HashMap<>(Objects.requireNonNull(equipment));
      return this;
    }
    public Builder equipmentSlot(EquipmentSlotKey slot, @Nullable ItemSubject subject) {
      if (this.equipment == null || this.equipment.isEmpty()) {
        this.equipment = new java.util.HashMap<>();
      }
      this.equipment.put(Objects.requireNonNull(slot), subject == null ? ItemSubject.empty() : subject);
      return this;
    }
    public Builder activeCooldowns(Set<CooldownSnapshot> activeCooldowns) {
      this.activeCooldowns = new java.util.HashSet<>(Objects.requireNonNull(activeCooldowns));
      return this;
    }
    public Builder activeCooldown(CooldownSnapshot snapshot) {
      if (this.activeCooldowns == null || this.activeCooldowns.isEmpty()) {
        this.activeCooldowns = new java.util.HashSet<>();
      }
      this.activeCooldowns.add(Objects.requireNonNull(snapshot));
      return this;
    }

    public Builder invulnerableTicks(@Nullable Integer invulnerableTicks) {
      this.invulnerableTicks = invulnerableTicks;
      return this;
    }

    public Builder advancements(Set<String> advancements) {
      this.advancements = Objects.requireNonNull(advancements);
      return this;
    }

    public Builder vehicle(@Nullable ConditionContext vehicle) {
      this.vehicle = vehicle;
      return this;
    }

    public Builder passengers(List<ConditionContext> passengers) {
      this.passengers = Objects.requireNonNull(passengers);
      return this;
    }

    public Builder ticksLived(@Nullable Integer ticksLived) {
      this.ticksLived = ticksLived;
      return this;
    }

    public Builder random(@Nullable Random random) {
      this.random = random;
      return this;
    }
    public Builder standingOn(@Nullable ConditionContext standingOn) {
      this.standingOn = standingOn;
      return this;
    }
    public Builder offsetResolver(@Nullable OffsetContextResolver offsetResolver) {
      this.offsetResolver = offsetResolver;
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
          attacker,
          directAttacker,
          attackingPlayer,
          targetEntity,
          interactingEntity,
          x,
          y,
          z,
          lightLevel,
          skyLight,
          blockLight,
          canSeeSky,
          team,
          ping,
          emptyMain,
          emptyHotbar,
          emptyOffhand,
          sheared,
          woolColor,
          frogVariant,
          catVariant,
          wolfVariant,
          tropicalFishVariant,
          tropicalFishPattern,
          tropicalFishBaseColor,
          tropicalFishPatternColor,
          villagerType,
          villagerProfession,
          villagerLevel,
          horseColor,
          horseArmor,
          itemCounts,
          playtime,
          armorSet,
          offhandItem,
          itemSubject,
          equipment,
          activeCooldowns,
          invulnerableTicks,
          advancements,
          vehicle,
          passengers,
          ticksLived,
          random,
          standingOn,
          offsetResolver,
          scores,
          extras);
    }
  }
}
