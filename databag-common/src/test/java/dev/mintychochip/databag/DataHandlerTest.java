package dev.mintychochip.databag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import net.kyori.adventure.key.Key;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class DataHandlerTest {

  private static final Key PARTY = Key.key("acme", "party_size");

  private static final DataHandler<Integer> PARTY_SIZE = new DataHandler<>() {
    @Override
    public Key key() {
      return PARTY;
    }

    @Override
    public int format() {
      return 1;
    }

    @Override
    public byte[] encode(Integer value) {
      return Integer.toString(value).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public Integer decode(byte[] bytes) {
      return Integer.parseInt(new String(bytes, StandardCharsets.UTF_8));
    }
  };

  @AfterEach
  void tearDown() {
    DataHandlers.unregister(PARTY);
  }

  @Test
  void registeredHandlerRoundTripsTypedValue() {
    DataHandlers.register(PARTY_SIZE);
    DataBag bag = DataBag.create().set(PARTY_SIZE, 4);
    DataBag restored = DataBag.fromBytes(bag.toBytes());
    assertEquals(Optional.of(4), restored.get(PARTY_SIZE));
    assertEquals(Optional.of(PARTY_SIZE), DataHandlers.get(PARTY));
  }

  @Test
  void wrongFormatIsEmpty() {
    DataBag bag = DataBag.create().setBytes(PARTY, 99, "4".getBytes(StandardCharsets.UTF_8));
    assertTrue(bag.get(PARTY_SIZE).isEmpty());
  }

  @Test
  void duplicateRegisterThrows() {
    DataHandlers.register(PARTY_SIZE);
    DataHandler<Integer> other = new DataHandler<>() {
      @Override
      public Key key() {
        return PARTY;
      }

      @Override
      public int format() {
        return 2;
      }

      @Override
      public byte[] encode(Integer value) {
        return new byte[0];
      }

      @Override
      public Integer decode(byte[] bytes) {
        return 0;
      }
    };
    assertThrows(IllegalStateException.class, () -> DataHandlers.register(other));
  }
}
