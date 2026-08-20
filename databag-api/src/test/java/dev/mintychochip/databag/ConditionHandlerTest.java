package dev.mintychochip.databag;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.mintychochip.databag.DataBag;
import java.util.Optional;
import net.kyori.adventure.key.Key;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ConditionHandlerTest {

  private static final Key ID = Key.key("acme", "party_size");
  private static final Key SNAPSHOT = Key.key("acme", "party_size");
  private static final Key ARG_MIN = Key.key("condition", "min");

  private static final ConditionHandler HANDLER = new ConditionHandler() {
    @Override
    public Key id() {
      return ID;
    }

    @Override
    public Condition read(DataBag arguments) {
      int min = arguments.getInt(ARG_MIN).orElseThrow();
      return new PartySizeCondition(min);
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
  void registeredHandlerEvaluatesAgainstExtras() {
    ConditionHandlers.register(HANDLER);
    Condition condition = HANDLER.read(DataBag.create().setInt(ARG_MIN, 3));
    DataBag extras = DataBag.create().setInt(SNAPSHOT, 4);
    assertTrue(condition.test(ConditionContext.builder().extras(extras).build()));
    assertFalse(condition.test(ConditionContext.builder()
        .extras(DataBag.create().setInt(SNAPSHOT, 2))
        .build()));
    assertFalse(condition.test(ConditionContext.absent()));
  }

  @Test
  void duplicateRegisterThrows() {
    ConditionHandlers.register(HANDLER);
    assertThrows(IllegalStateException.class, () -> ConditionHandlers.register(new ConditionHandler() {
      @Override
      public Key id() {
        return ID;
      }

      @Override
      public Condition read(DataBag arguments) {
        return Conditions.always();
      }

      @Override
      public Optional<DataBag> write(Condition condition) {
        return Optional.empty();
      }
    }));
  }

  private record PartySizeCondition(int min) implements Condition {
    @Override
    public boolean test(ConditionContext context) {
      return context.extras().getInt(SNAPSHOT).orElse(Integer.MIN_VALUE) >= min;
    }
  }
}
