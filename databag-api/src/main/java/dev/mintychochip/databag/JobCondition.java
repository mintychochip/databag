package dev.mintychochip.databag;

import java.util.Set;

/**
 * True when the snapshot's current job key is one of {@code jobKeys}.
 */
public record JobCondition(Set<String> jobKeys) implements Condition {

  public JobCondition {
    jobKeys = Set.copyOf(jobKeys);
  }

  @Override
  public boolean test(ConditionContext context) {
    if (!context.present() || context.jobKeys().isEmpty()) {
      return false;
    }
    for (String have : context.jobKeys()) {
      if (matches(have)) {
        return true;
      }
    }
    return false;
  }

  private boolean matches(String have) {
    for (String want : jobKeys) {
      if (have.equals(want) || have.equals(namespace(want)) || namespace(have).equals(want)) {
        return true;
      }
    }
    return false;
  }

  private static String namespace(String key) {
    return key.contains(":") ? key : "modularjobs:" + key;
  }
}
