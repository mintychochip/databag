package dev.mintychochip.databag.paper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

class BlockDataPropertiesTest {

  @Test
  void parsesVanillaBlockDataString() {
    Map<String, String> properties = BlockDataStrings.properties(
        "minecraft:chest[facing=north,type=single,waterlogged=false]");
    assertEquals("north", properties.get("facing"));
    assertEquals("single", properties.get("type"));
    assertEquals("false", properties.get("waterlogged"));
  }

  @Test
  void emptyWhenNoBrackets() {
    assertTrue(BlockDataStrings.properties("minecraft:stone").isEmpty());
  }
}
