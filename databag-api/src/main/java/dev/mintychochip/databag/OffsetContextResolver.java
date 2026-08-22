package dev.mintychochip.databag;

import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface OffsetContextResolver {

  @Nullable
  ConditionContext resolve(int offsetX, int offsetY, int offsetZ);
}
