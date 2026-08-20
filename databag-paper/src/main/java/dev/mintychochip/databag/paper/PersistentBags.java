package dev.mintychochip.databag.paper;

import dev.mintychochip.databag.DataBag;
import java.util.Optional;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

/**
 * Embeds a {@link DataBag} on an item as a PDC {@code BYTE_ARRAY}.
 */
public final class PersistentBags {

  private PersistentBags() {}

  public static void write(ItemStack stack, NamespacedKey key, DataBag bag) {
    byte[] bytes = bag.toBytes();
    stack.editPersistentDataContainer(pdc -> {
      pdc.set(key, PersistentDataType.BYTE_ARRAY, bytes);
    });
  }

  public static Optional<DataBag> read(ItemStack stack, NamespacedKey key) {
    var pdc = stack.getPersistentDataContainer();
    if (!pdc.has(key, PersistentDataType.BYTE_ARRAY)) {
      return Optional.empty();
    }
    byte[] blob = pdc.get(key, PersistentDataType.BYTE_ARRAY);
    if (blob == null || blob.length == 0) {
      return Optional.empty();
    }
    return Optional.of(DataBag.fromBytes(blob));
  }
}
