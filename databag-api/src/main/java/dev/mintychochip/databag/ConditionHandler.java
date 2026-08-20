package dev.mintychochip.databag;

import dev.mintychochip.databag.DataBag;
import java.util.Optional;
import net.kyori.adventure.key.Key;

/**
 * SPI for a condition id that is not built into {@code dev.mintychochip.databag}.
 * JSON object fields (except {@code condition}/{@code type}) are passed as a
 * {@link DataBag} under the {@code condition} namespace. Snapshot extras for
 * evaluation live on {@link ConditionContext#extras()}.
 */
public interface ConditionHandler {

  /** Namespaced condition id, e.g. {@code acme:party_size}. */
  Key id();

  Condition read(DataBag arguments);

  /**
   * Serializes {@code condition} if this handler owns it. Empty means “not
   * mine” so another handler can try.
   */
  Optional<DataBag> write(Condition condition);
}
