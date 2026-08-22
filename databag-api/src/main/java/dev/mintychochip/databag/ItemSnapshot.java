package dev.mintychochip.databag;

import net.kyori.adventure.key.Key;
import java.util.List;
import org.jetbrains.annotations.Nullable;

/** Immutable metadata snapshot of an inventory item stack. */
public record ItemSnapshot(
    @Nullable Key material,
    int amount,
    @Nullable Integer customModelData,
    @Nullable String displayName,
    List<String> lore) {

  public ItemSnapshot {
    lore = List.copyOf(lore == null ? List.of() : lore);
    if (amount < 0) {
      throw new IllegalArgumentException("amount must be >= 0");
    }
  }

  public static ItemSnapshot empty() {
    return new ItemSnapshot(null, 0, null, null, List.of());
  }
}
