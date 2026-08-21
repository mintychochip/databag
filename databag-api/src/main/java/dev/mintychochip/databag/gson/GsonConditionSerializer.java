package dev.mintychochip.databag.gson;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import dev.mintychochip.databag.AllOfCondition;
import dev.mintychochip.databag.AlwaysCondition;
import dev.mintychochip.databag.AnyOfCondition;
import dev.mintychochip.databag.BabyCondition;
import dev.mintychochip.databag.BiomeCondition;
import dev.mintychochip.databag.BlockIdCondition;
import dev.mintychochip.databag.BlockPropertyCondition;
import dev.mintychochip.databag.Condition;
import dev.mintychochip.databag.ConditionHandler;
import dev.mintychochip.databag.ConditionHandlers;
import dev.mintychochip.databag.ConditionSerializer;
import dev.mintychochip.databag.Conditions;
import dev.mintychochip.databag.DataBag;
import dev.mintychochip.databag.EntityTypeCondition;
import dev.mintychochip.databag.FluidCondition;
import dev.mintychochip.databag.FlyingCondition;
import dev.mintychochip.databag.GameModeCondition;
import dev.mintychochip.databag.GlidingCondition;
import dev.mintychochip.databag.InvertedCondition;
import dev.mintychochip.databag.JobCondition;
import dev.mintychochip.databag.OnFireCondition;
import dev.mintychochip.databag.OnGroundCondition;
import dev.mintychochip.databag.PlayerResourceCondition;
import dev.mintychochip.databag.PlayerResourceType;
import dev.mintychochip.databag.PotionAmplifierCondition;
import dev.mintychochip.databag.PotionDurationCondition;
import dev.mintychochip.databag.PotionPresentCondition;
import dev.mintychochip.databag.RelationalOperator;
import dev.mintychochip.databag.SneakingCondition;
import dev.mintychochip.databag.SprintingCondition;
import dev.mintychochip.databag.SwimmingCondition;
import dev.mintychochip.databag.WeatherCondition;
import dev.mintychochip.databag.WeatherState;
import dev.mintychochip.databag.WorldCondition;
import net.kyori.adventure.key.Key;

/**
 * Vanilla loot-condition JSON reader/writer. Bytes are UTF-8 JSON objects
 * with a {@code condition} id (or legacy {@code type} bag on read).
 */
public final class GsonConditionSerializer implements ConditionSerializer {

  private static final GsonConditionSerializer INSTANCE = new GsonConditionSerializer();

  private GsonConditionSerializer() {}

  public static ConditionSerializer gson() {
    return INSTANCE;
  }

  @Override
  public byte[] write(Condition condition) {
    return writeElement(condition).toString().getBytes(StandardCharsets.UTF_8);
  }

  @Override
  public Condition read(byte[] bytes) {
    JsonElement element = JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8));
    if (!element.isJsonObject()) {
      throw new IllegalArgumentException("condition JSON must be an object");
    }
    return readObject(element.getAsJsonObject());
  }

  JsonElement writeElement(Condition condition) {
    return switch (condition) {
      case AlwaysCondition ignored -> allOfJson(List.of());
      case AllOfCondition all -> all.terms().isEmpty()
          ? allOfJson(List.of())
          : allOfJson(all.terms().stream().map(this::writeElement).toList());
      case AnyOfCondition any -> anyOfJson(any.terms().stream().map(this::writeElement).toList());
      case InvertedCondition inverted -> invertedJson(writeElement(inverted.term()));
      case SneakingCondition sneak -> entityFlags(flagObject("is_sneaking", sneak.expected()));
      case SprintingCondition sprint -> entityFlags(flagObject("is_sprinting", sprint.expected()));
      case OnFireCondition fire -> entityFlags(flagObject("is_on_fire", fire.expected()));
      case OnGroundCondition ground -> entityFlags(flagObject("is_on_ground", ground.expected()));
      case SwimmingCondition swim -> entityFlags(flagObject("is_swimming", swim.expected()));
      case BabyCondition baby -> entityFlags(flagObject("is_baby", baby.expected()));
      case GlidingCondition glide -> entityFlags(flagObject("is_gliding", glide.expected()));
      case FlyingCondition fly -> entityFlags(flagObject("is_flying", fly.expected()));
      case EntityTypeCondition type -> entityTypeJson(type.entityType().asString());
      case GameModeCondition mode -> gameModeJson(mode.gameMode());
      case BlockIdCondition block -> blockStateJson(block.blockId().asString(), null, null);
      case BlockPropertyCondition prop -> blockStateJson(null, prop.name(), prop.value());
      case BiomeCondition biome -> entityLocationBiome(biome.biomeKey().asString());
      case WorldCondition world -> modular("modularjobs:world", obj -> obj.addProperty("world", world.worldName()));
      case WeatherCondition weather -> weatherJson(weather.state());
      case FluidCondition fluid -> locationFluid(fluid.fluidKey().asString());
      case PlayerResourceCondition resource -> playerResourceJson(resource);
      case PotionPresentCondition potion -> effectRoot(potion.effectKey().asString(), new JsonObject());
      case PotionAmplifierCondition potion -> effectJson(
          potion.effectKey().asString(), "amplifier", potion.operator(), potion.expected());
      case PotionDurationCondition potion -> effectJson(
          potion.effectKey().asString(), "duration", potion.operator(), potion.expected());
      case JobCondition job -> jobJson(job);
      default -> writeRegistered(condition);
    };
  }

  private JsonObject writeRegistered(Condition condition) {
    ConditionHandler handler = ConditionHandlers.findWriter(condition).orElseThrow(
        () -> new IllegalArgumentException(
            "Cannot serialize condition type: " + condition.getClass().getName()));
    DataBag arguments = handler.write(condition).orElseThrow();
    JsonObject root = new JsonObject();
    root.addProperty("condition", handler.id().asString());
    ArgumentBags.writeFields(root, arguments);
    return root;
  }

  private Condition readObject(JsonObject json) {
    String id = conditionId(json);
    String normalized = stripNamespace(id).toLowerCase(Locale.ROOT);
    return switch (normalized) {
      case "all_of", "and" -> readAllOf(json);
      case "any_of", "or" -> readAnyOf(json);
      case "inverted", "not" -> readInverted(json);
      case "entity_properties" -> readEntityProperties(json);
      case "block_state_property" -> readBlockStateProperty(json);
      case "weather_check", "weather" -> readWeather(json);
      case "location_check" -> readLocationCheck(json);
      case "world" -> Conditions.world(requiredString(json, json.has("world") ? "world" : "value"));
      case "player_resource" -> readPlayerResource(json);
      case "job" -> readJob(json);
      case "always" -> Conditions.always();
      case "sneaking" -> Conditions.sneaking(json.get("value").getAsBoolean());
      case "sprinting" -> Conditions.sprinting(json.get("value").getAsBoolean());
      case "biome" -> Conditions.biome(Key.key(requiredString(json, "value")));
      case "liquid" -> Conditions.fluid(Key.key(liquidKey(json)));
      case "potion_effect" -> readLegacyPotion(json);
      default -> readRegistered(id, json);
    };
  }

  private Condition readRegistered(String id, JsonObject json) {
    ConditionHandler handler = ConditionHandlers.get(id).orElseThrow(
        () -> new IllegalArgumentException("Unknown condition type: " + id));
    return handler.read(ArgumentBags.fromJson(json));
  }

  private Condition readAllOf(JsonObject json) {
    List<Condition> terms = readTerms(json, json.has("terms") ? "terms" : "conditions");
    if (terms.isEmpty()) {
      return Conditions.always();
    }
    if (terms.size() == 1) {
      return terms.getFirst();
    }
    return Conditions.allOf(terms.toArray(Condition[]::new));
  }

  private Condition readAnyOf(JsonObject json) {
    List<Condition> terms = readTerms(json, json.has("terms") ? "terms" : "conditions");
    return Conditions.anyOf(terms.toArray(Condition[]::new));
  }

  private Condition readInverted(JsonObject json) {
    JsonElement inner = json.has("term") ? json.get("term") : json.get("condition");
    if (inner == null || !inner.isJsonObject()) {
      throw new IllegalArgumentException("inverted condition requires 'term'");
    }
    return Conditions.inverted(readObject(inner.getAsJsonObject()));
  }

  private Condition readEntityProperties(JsonObject json) {
    if (json.has("entity") && !"this".equals(json.get("entity").getAsString())) {
      throw new IllegalArgumentException("entity_properties only supports entity=this");
    }
    JsonObject predicate = json.has("predicate") && json.get("predicate").isJsonObject()
        ? json.getAsJsonObject("predicate")
        : json;
    List<Condition> parts = new ArrayList<>();
    if (predicate.has("type") && predicate.get("type").isJsonPrimitive()) {
      parts.add(Conditions.entityType(Key.key(predicate.get("type").getAsString())));
    }
    if (predicate.has("flags") && predicate.get("flags").isJsonObject()) {
      JsonObject flags = predicate.getAsJsonObject("flags");
      addFlag(parts, flags, "is_sneaking", Conditions::sneaking);
      addFlag(parts, flags, "is_sprinting", Conditions::sprinting);
      addFlag(parts, flags, "is_on_fire", Conditions::onFire);
      addFlag(parts, flags, "is_on_ground", Conditions::onGround);
      addFlag(parts, flags, "is_swimming", Conditions::swimming);
      addFlag(parts, flags, "is_baby", Conditions::baby);
      addFlag(parts, flags, "is_gliding", Conditions::gliding);
      addFlag(parts, flags, "is_flying", Conditions::flying);
    }
    if (predicate.has("type_specific") && predicate.get("type_specific").isJsonObject()) {
      JsonObject specific = predicate.getAsJsonObject("type_specific");
      if (specific.has("gamemode")) {
        Condition modes = anyOfConditions(stringList(specific.get("gamemode")).stream()
            .map(Conditions::gameMode)
            .toList());
        if (modes != null) {
          parts.add(modes);
        }
      }
    }
    if (predicate.has("location") && predicate.get("location").isJsonObject()) {
      JsonObject location = predicate.getAsJsonObject("location");
      if (location.has("biomes")) {
        Condition biomes = anyOfConditions(stringList(location.get("biomes")).stream()
            .map(biome -> Conditions.biome(Key.key(biome)))
            .toList());
        if (biomes != null) {
          parts.add(biomes);
        }
      }
    }
    if (predicate.has("effects") && predicate.get("effects").isJsonObject()) {
      for (var entry : predicate.getAsJsonObject("effects").entrySet()) {
        parts.add(readEffectEntry(entry.getKey(), entry.getValue()));
      }
    }
    if (parts.isEmpty()) {
      throw new IllegalArgumentException("entity_properties predicate has no supported fields");
    }
    if (parts.size() == 1) {
      return parts.getFirst();
    }
    return Conditions.allOf(parts.toArray(Condition[]::new));
  }

  private Condition readEffectEntry(String effectId, JsonElement spec) {
    Key key = Key.key(effectId);
    if (spec == null || spec.isJsonNull() || !spec.isJsonObject() || spec.getAsJsonObject().isEmpty()) {
      return Conditions.potionPresent(key);
    }
    JsonObject obj = spec.getAsJsonObject();
    List<Condition> parts = new ArrayList<>();
    if (obj.has("amplifier")) {
      parts.add(boundedEffect(key, true, obj.get("amplifier")));
    }
    if (obj.has("duration")) {
      parts.add(boundedEffect(key, false, obj.get("duration")));
    }
    if (parts.isEmpty()) {
      return Conditions.potionPresent(key);
    }
    if (parts.size() == 1) {
      return parts.getFirst();
    }
    return Conditions.allOf(parts.toArray(Condition[]::new));
  }

  private Condition boundedEffect(Key key, boolean amplifier, JsonElement bound) {
    if (!bound.isJsonObject()) {
      return amplifier
          ? Conditions.potionAmplifier(key, RelationalOperator.EQUAL, bound.getAsInt())
          : Conditions.potionDuration(key, RelationalOperator.EQUAL, bound.getAsInt());
    }
    JsonObject bounds = bound.getAsJsonObject();
    if (bounds.has("min")) {
      return amplifier
          ? Conditions.potionAmplifier(
              key, RelationalOperator.GREATER_THAN_OR_EQUAL, bounds.get("min").getAsInt())
          : Conditions.potionDuration(
              key, RelationalOperator.GREATER_THAN_OR_EQUAL, bounds.get("min").getAsInt());
    }
    if (bounds.has("max")) {
      return amplifier
          ? Conditions.potionAmplifier(
              key, RelationalOperator.LESS_THAN_OR_EQUAL, bounds.get("max").getAsInt())
          : Conditions.potionDuration(
              key, RelationalOperator.LESS_THAN_OR_EQUAL, bounds.get("max").getAsInt());
    }
    return Conditions.potionPresent(key);
  }

  private Condition readWeather(JsonObject json) {
    if (json.has("value")) {
      return Conditions.weather(WeatherState.valueOf(json.get("value").getAsString().toUpperCase(Locale.ROOT)));
    }
    boolean thundering = json.has("thundering") && json.get("thundering").getAsBoolean();
    boolean raining = json.has("raining") && json.get("raining").getAsBoolean();
    if (thundering) {
      return Conditions.weather(WeatherState.THUNDERING);
    }
    if (raining) {
      return Conditions.weather(WeatherState.RAINING);
    }
    return Conditions.weather(WeatherState.CLEAR);
  }

  private Condition readLocationCheck(JsonObject json) {
    JsonObject predicate = json.has("predicate") ? json.getAsJsonObject("predicate") : json;
    if (predicate.has("fluid")) {
      JsonElement fluid = predicate.get("fluid");
      List<String> fluids = fluid.isJsonObject() && fluid.getAsJsonObject().has("fluids")
          ? stringList(fluid.getAsJsonObject().get("fluids"))
          : stringList(fluid);
      Condition anyFluid = anyOfConditions(fluids.stream()
          .map(key -> Conditions.fluid(Key.key(key)))
          .toList());
      if (anyFluid != null) {
        return anyFluid;
      }
    }
    if (predicate.has("biomes")) {
      Condition anyBiome = anyOfConditions(stringList(predicate.get("biomes")).stream()
          .map(biome -> Conditions.biome(Key.key(biome)))
          .toList());
      if (anyBiome != null) {
        return anyBiome;
      }
    }
    if (predicate.has("block") && predicate.get("block").isJsonObject()) {
      return readLocationBlock(predicate.getAsJsonObject("block"));
    }
    throw new IllegalArgumentException("location_check requires fluid, biomes, or block");
  }

  private Condition readLocationBlock(JsonObject block) {
    List<Condition> parts = new ArrayList<>();
    if (block.has("blocks")) {
      Condition blocks = anyOfConditions(stringList(block.get("blocks")).stream()
          .map(id -> Conditions.blockId(Key.key(id)))
          .toList());
      if (blocks != null) {
        parts.add(blocks);
      }
    } else if (block.has("block")) {
      parts.add(Conditions.blockId(Key.key(block.get("block").getAsString())));
    }
    if (block.has("state") && block.get("state").isJsonObject()) {
      for (var entry : block.getAsJsonObject("state").entrySet()) {
        parts.add(Conditions.blockProperty(entry.getKey(), firstString(entry.getValue())));
      }
    }
    if (parts.isEmpty()) {
      throw new IllegalArgumentException("location_check block requires blocks or state");
    }
    if (parts.size() == 1) {
      return parts.getFirst();
    }
    return Conditions.allOf(parts.toArray(Condition[]::new));
  }

  private Condition readBlockStateProperty(JsonObject json) {
    List<Condition> parts = new ArrayList<>();
    if (json.has("block")) {
      parts.add(Conditions.blockId(Key.key(json.get("block").getAsString())));
    }
    if (json.has("properties") && json.get("properties").isJsonObject()) {
      for (var entry : json.getAsJsonObject("properties").entrySet()) {
        parts.add(Conditions.blockProperty(entry.getKey(), firstString(entry.getValue())));
      }
    }
    if (parts.isEmpty()) {
      throw new IllegalArgumentException("block_state_property requires block or properties");
    }
    if (parts.size() == 1) {
      return parts.getFirst();
    }
    return Conditions.allOf(parts.toArray(Condition[]::new));
  }

  private Condition readPlayerResource(JsonObject json) {
    String resource = json.has("resource")
        ? json.get("resource").getAsString()
        : requiredString(json, "resourceType");
    String operator = json.has("operator") ? json.get("operator").getAsString() : "==";
    double value = json.get("value").getAsDouble();
    return Conditions.playerResource(parseResource(resource), parseOperator(operator), value);
  }

  private Condition readJob(JsonObject json) {
    if (json.has("jobs") && json.get("jobs").isJsonArray()) {
      List<String> keys = new ArrayList<>();
      for (JsonElement el : json.getAsJsonArray("jobs")) {
        keys.add(el.getAsString());
      }
      return Conditions.jobAny(keys.toArray(String[]::new));
    }
    if (json.has("value")) {
      return Conditions.job(json.get("value").getAsString());
    }
    if (json.has("values") && json.get("values").isJsonArray()) {
      List<String> keys = new ArrayList<>();
      for (JsonElement el : json.getAsJsonArray("values")) {
        keys.add(el.getAsString());
      }
      return Conditions.jobAny(keys.toArray(String[]::new));
    }
    throw new IllegalArgumentException("job condition requires jobs, value, or values");
  }

  private Condition readLegacyPotion(JsonObject json) {
    String effect = requiredString(json, "effect");
    Key key = Key.key(effect.contains(":") ? effect : "minecraft:" + effect);
    if (json.has("amplifier") && json.has("operator")) {
      return Conditions.potionAmplifier(
          key, parseOperator(json.get("operator").getAsString()), json.get("amplifier").getAsInt());
    }
    return Conditions.potionPresent(key);
  }

  private List<Condition> readTerms(JsonObject json, String field) {
    if (!json.has(field) || !json.get(field).isJsonArray()) {
      return List.of();
    }
    List<Condition> terms = new ArrayList<>();
    for (JsonElement el : json.getAsJsonArray(field)) {
      if (!el.isJsonObject()) {
        throw new IllegalArgumentException(field + " entries must be objects");
      }
      terms.add(readObject(el.getAsJsonObject()));
    }
    return terms;
  }

  private static void addFlag(
      List<Condition> parts,
      JsonObject flags,
      String name,
      java.util.function.Function<Boolean, Condition> factory) {
    if (flags.has(name)) {
      parts.add(factory.apply(flags.get(name).getAsBoolean()));
    }
  }

  private static JsonObject entityTypeJson(String type) {
    JsonObject predicate = new JsonObject();
    predicate.addProperty("type", type);
    JsonObject root = new JsonObject();
    root.addProperty("condition", "minecraft:entity_properties");
    root.addProperty("entity", "this");
    root.add("predicate", predicate);
    return root;
  }

  private static JsonObject gameModeJson(String gameMode) {
    JsonObject specific = new JsonObject();
    specific.addProperty("type", "player");
    com.google.gson.JsonArray modes = new com.google.gson.JsonArray();
    modes.add(gameMode);
    specific.add("gamemode", modes);
    JsonObject predicate = new JsonObject();
    predicate.add("type_specific", specific);
    JsonObject root = new JsonObject();
    root.addProperty("condition", "minecraft:entity_properties");
    root.addProperty("entity", "this");
    root.add("predicate", predicate);
    return root;
  }

  private static JsonObject blockStateJson(
      String blockId, String propertyName, String propertyValue) {
    JsonObject root = new JsonObject();
    root.addProperty("condition", "minecraft:block_state_property");
    if (blockId != null) {
      root.addProperty("block", blockId);
    }
    if (propertyName != null) {
      JsonObject properties = new JsonObject();
      properties.addProperty(propertyName, propertyValue);
      root.add("properties", properties);
    }
    return root;
  }

  private static JsonObject entityFlags(JsonObject flags) {
    JsonObject predicate = new JsonObject();
    predicate.add("flags", flags);
    JsonObject root = new JsonObject();
    root.addProperty("condition", "minecraft:entity_properties");
    root.addProperty("entity", "this");
    root.add("predicate", predicate);
    return root;
  }

  private static JsonObject flagObject(String name, boolean value) {
    JsonObject flags = new JsonObject();
    flags.addProperty(name, value);
    return flags;
  }

  private static JsonObject entityLocationBiome(String biome) {
    JsonObject location = new JsonObject();
    location.addProperty("biomes", biome);
    JsonObject predicate = new JsonObject();
    predicate.add("location", location);
    JsonObject root = new JsonObject();
    root.addProperty("condition", "minecraft:entity_properties");
    root.addProperty("entity", "this");
    root.add("predicate", predicate);
    return root;
  }

  /**
   * Strict operators cannot be expressed by vanilla's inclusive min/max
   * bounds, so they are encoded losslessly as inversions: {@code >X} as
   * {@code not(<=X)}, {@code <X} as {@code not(>=X)}, {@code !=X} as
   * {@code not(=X)}.
   */
  private static JsonElement effectJson(
      String effect, String field, RelationalOperator operator, int expected) {
    return switch (operator) {
      case GREATER_THAN -> invertedJson(
          effectJson(effect, field, RelationalOperator.LESS_THAN_OR_EQUAL, expected));
      case LESS_THAN -> invertedJson(
          effectJson(effect, field, RelationalOperator.GREATER_THAN_OR_EQUAL, expected));
      case NOT_EQUAL -> invertedJson(effectJson(effect, field, RelationalOperator.EQUAL, expected));
      case GREATER_THAN_OR_EQUAL -> effectRoot(effect, boundSpec(field, "min", expected));
      case LESS_THAN_OR_EQUAL -> effectRoot(effect, boundSpec(field, "max", expected));
      case EQUAL -> effectRoot(effect, plainSpec(field, expected));
    };
  }

  private static JsonObject boundSpec(String field, String bound, int expected) {
    JsonObject bounds = new JsonObject();
    bounds.addProperty(bound, expected);
    JsonObject spec = new JsonObject();
    spec.add(field, bounds);
    return spec;
  }

  private static JsonObject plainSpec(String field, int expected) {
    JsonObject spec = new JsonObject();
    spec.addProperty(field, expected);
    return spec;
  }

  private static JsonObject effectRoot(String effect, JsonObject spec) {
    JsonObject effects = new JsonObject();
    effects.add(effect, spec);
    JsonObject predicate = new JsonObject();
    predicate.add("effects", effects);
    JsonObject root = new JsonObject();
    root.addProperty("condition", "minecraft:entity_properties");
    root.addProperty("entity", "this");
    root.add("predicate", predicate);
    return root;
  }

  private static JsonObject locationFluid(String fluid) {
    JsonObject fluidObj = new JsonObject();
    fluidObj.addProperty("fluids", fluid);
    JsonObject predicate = new JsonObject();
    predicate.add("fluid", fluidObj);
    JsonObject root = new JsonObject();
    root.addProperty("condition", "minecraft:location_check");
    root.add("predicate", predicate);
    return root;
  }

  private static JsonObject weatherJson(WeatherState state) {
    JsonObject root = new JsonObject();
    root.addProperty("condition", "minecraft:weather_check");
    switch (state) {
      case THUNDERING -> root.addProperty("thundering", true);
      case RAINING -> root.addProperty("raining", true);
      case CLEAR -> {
        root.addProperty("raining", false);
        root.addProperty("thundering", false);
      }
    }
    return root;
  }

  private static JsonObject playerResourceJson(PlayerResourceCondition resource) {
    JsonObject root = new JsonObject();
    root.addProperty("condition", "modularjobs:player_resource");
    root.addProperty("resource", resource.type().name().toLowerCase(Locale.ROOT));
    root.addProperty("operator", operatorName(resource.operator()));
    root.addProperty("value", resource.expected());
    return root;
  }

  private static JsonObject jobJson(JobCondition job) {
    JsonObject root = new JsonObject();
    root.addProperty("condition", "modularjobs:job");
    JsonArray jobs = new JsonArray();
    for (String key : job.jobKeys()) {
      jobs.add(key);
    }
    root.add("jobs", jobs);
    return root;
  }

  private static JsonObject allOfJson(List<JsonElement> terms) {
    JsonObject root = new JsonObject();
    root.addProperty("condition", "minecraft:all_of");
    JsonArray array = new JsonArray();
    for (JsonElement term : terms) {
      array.add(term);
    }
    root.add("terms", array);
    return root;
  }

  private static JsonObject anyOfJson(List<JsonElement> terms) {
    JsonObject root = new JsonObject();
    root.addProperty("condition", "minecraft:any_of");
    JsonArray array = new JsonArray();
    for (JsonElement term : terms) {
      array.add(term);
    }
    root.add("terms", array);
    return root;
  }

  private static JsonObject invertedJson(JsonElement term) {
    JsonObject root = new JsonObject();
    root.addProperty("condition", "minecraft:inverted");
    root.add("term", term);
    return root;
  }

  private static JsonObject modular(String id, java.util.function.Consumer<JsonObject> fields) {
    JsonObject root = new JsonObject();
    root.addProperty("condition", id);
    fields.accept(root);
    return root;
  }

  private static String conditionId(JsonObject json) {
    if (json.has("condition") && json.get("condition").isJsonPrimitive()) {
      return json.get("condition").getAsString();
    }
    if (json.has("type") && json.get("type").isJsonPrimitive()) {
      return json.get("type").getAsString();
    }
    throw new IllegalArgumentException("missing condition id");
  }

  private static String stripNamespace(String id) {
    int colon = id.indexOf(':');
    return colon >= 0 ? id.substring(colon + 1) : id;
  }

  private static String requiredString(JsonObject json, String field) {
    if (!json.has(field) || json.get(field).isJsonNull()) {
      throw new IllegalArgumentException("missing " + field);
    }
    return json.get(field).getAsString();
  }

  private static String firstString(JsonElement element) {
    if (element.isJsonArray() && !element.getAsJsonArray().isEmpty()) {
      return element.getAsJsonArray().get(0).getAsString();
    }
    return element.getAsString();
  }

  private static List<String> stringList(JsonElement element) {
    if (element.isJsonArray()) {
      List<String> values = new ArrayList<>();
      for (JsonElement entry : element.getAsJsonArray()) {
        values.add(entry.getAsString());
      }
      return values;
    }
    return List.of(element.getAsString());
  }

  private static Condition anyOfConditions(List<Condition> terms) {
    if (terms.isEmpty()) {
      return null;
    }
    if (terms.size() == 1) {
      return terms.getFirst();
    }
    return Conditions.anyOf(terms.toArray(Condition[]::new));
  }

  private static String liquidKey(JsonObject json) {
    if (json.has("value") && json.get("value").isJsonPrimitive()) {
      return json.get("value").getAsString();
    }
    throw new IllegalArgumentException("liquid condition requires 'value'");
  }

  private static PlayerResourceType parseResource(String raw) {
    return switch (raw.toUpperCase(Locale.ROOT)) {
      case "HEALTH", "HP" -> PlayerResourceType.HEALTH;
      case "HUNGER", "FOOD", "FOOD_LEVEL" -> PlayerResourceType.HUNGER;
      case "EXPERIENCE", "XP", "EXP" -> PlayerResourceType.EXPERIENCE;
      default -> throw new IllegalArgumentException("Unknown player resource type: " + raw);
    };
  }

  private static RelationalOperator parseOperator(String raw) {
    return switch (raw.toLowerCase(Locale.ROOT)) {
      case "less_than", "<" -> RelationalOperator.LESS_THAN;
      case "less_than_or_equal", "<=" -> RelationalOperator.LESS_THAN_OR_EQUAL;
      case "greater_than", ">" -> RelationalOperator.GREATER_THAN;
      case "greater_than_or_equal", ">=" -> RelationalOperator.GREATER_THAN_OR_EQUAL;
      case "equal", "==" -> RelationalOperator.EQUAL;
      case "not_equal", "!=" -> RelationalOperator.NOT_EQUAL;
      default -> throw new IllegalArgumentException("Unknown operator: " + raw);
    };
  }

  private static String operatorName(RelationalOperator operator) {
    return switch (operator) {
      case LESS_THAN -> "<";
      case LESS_THAN_OR_EQUAL -> "<=";
      case GREATER_THAN -> ">";
      case GREATER_THAN_OR_EQUAL -> ">=";
      case EQUAL -> "==";
      case NOT_EQUAL -> "!=";
    };
  }
}
