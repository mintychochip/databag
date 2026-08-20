package dev.mintychochip.databag.gson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.mintychochip.databag.Condition;
import dev.mintychochip.databag.ConditionContext;
import dev.mintychochip.databag.ConditionHandler;
import dev.mintychochip.databag.ConditionHandlers;
import dev.mintychochip.databag.ConditionSerializer;
import dev.mintychochip.databag.DataBag;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import net.kyori.adventure.key.Key;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class RegisteredConditionHandlerTest {

  private static final Key ID = Key.key("acme", "party_size");
  private static final Key SNAPSHOT = Key.key("acme", "party_size");
  private static final Key ARG_MIN = Key.key("condition", "min");
  private final ConditionSerializer serializer = GsonConditionSerializer.gson();

  private static final ConditionHandler HANDLER = new ConditionHandler() {
    @Override
    public Key id() {
      return ID;
    }

    @Override
    public Condition read(DataBag arguments) {
      return new PartySizeCondition(arguments.getInt(ARG_MIN).orElseThrow());
    }

    @Override
    public Optional<DataBag> write(Condition condition) {
      if (!(condition instanceof PartySizeCondition party)) {
        return Optional.empty();
      }
      return Optional.of(DataBag.create().setInt(ARG_MIN, party.min()));
    }
  };

  @AfterEach
  void tearDown() {
    ConditionHandlers.unregister(ID);
  }

  @Test
  void unknownIdStillThrowsWhenNotRegistered() {
    String json = """
        { "condition": "acme:party_size", "min": 3 }
        """;
    assertThrows(IllegalArgumentException.class,
        () -> serializer.read(json.getBytes(StandardCharsets.UTF_8)));
  }

  @Test
  void registeredHandlerRoundTripsJsonAndTestsExtras() {
    ConditionHandlers.register(HANDLER);
    String json = """
        { "condition": "acme:party_size", "min": 3 }
        """;
    Condition condition = serializer.read(json.getBytes(StandardCharsets.UTF_8));
    assertInstanceOf(PartySizeCondition.class, condition);
    assertEquals(3, ((PartySizeCondition) condition).min());

    DataBag extras = DataBag.create().setInt(SNAPSHOT, 5);
    assertTrue(condition.test(ConditionContext.builder().extras(extras).build()));
    assertFalse(condition.test(ConditionContext.builder().build()));

    JsonObject written = JsonParser.parseString(
            new String(serializer.write(condition), StandardCharsets.UTF_8))
        .getAsJsonObject();
    assertEquals("acme:party_size", written.get("condition").getAsString());
    assertEquals(3, written.get("min").getAsInt());
  }

  private record PartySizeCondition(int min) implements Condition {
    @Override
    public boolean test(ConditionContext context) {
      return context.extras().getInt(SNAPSHOT).orElse(Integer.MIN_VALUE) >= min;
    }
  }
}
