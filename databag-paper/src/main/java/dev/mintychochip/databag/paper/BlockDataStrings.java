package dev.mintychochip.databag.paper;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Parses Bukkit {@code BlockData#getAsString()} without depending on Bukkit at
 * runtime for the parse itself.
 */
public final class BlockDataStrings {

  private BlockDataStrings() {}

  /**
   * Turns {@code minecraft:chest[facing=north,type=single]} into a property map.
   */
  public static Map<String, String> properties(String raw) {
    if (raw == null) {
      return Map.of();
    }
    int open = raw.indexOf('[');
    int close = raw.lastIndexOf(']');
    if (open < 0 || close <= open) {
      return Map.of();
    }
    Map<String, String> properties = new LinkedHashMap<>();
    for (String part : raw.substring(open + 1, close).split(",")) {
      int eq = part.indexOf('=');
      if (eq > 0) {
        properties.put(part.substring(0, eq).trim(), part.substring(eq + 1).trim());
      }
    }
    return Map.copyOf(properties);
  }
}
