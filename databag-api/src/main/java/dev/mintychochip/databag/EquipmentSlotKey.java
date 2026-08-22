package dev.mintychochip.databag;

import java.util.Locale;

/** Equipment slots supported by entity equipment predicates. */
public enum EquipmentSlotKey {
  MAINHAND,
  OFFHAND,
  HEAD,
  CHEST,
  LEGS,
  FEET;

  public String jsonName() {
    return name().toLowerCase(Locale.ROOT);
  }

  public static EquipmentSlotKey fromJson(String raw) {
    return switch (raw.toLowerCase(Locale.ROOT)) {
      case "mainhand", "main_hand" -> MAINHAND;
      case "offhand", "off_hand" -> OFFHAND;
      case "head", "helmet" -> HEAD;
      case "chest", "chestplate" -> CHEST;
      case "legs", "leggings" -> LEGS;
      case "feet", "boots" -> FEET;
      default -> throw new IllegalArgumentException("Unknown equipment slot: " + raw);
    };
  }
}
