package dev.mintychochip.databag;

/**
 * Matches whether the snapshot reports an unobstructed view of the sky.
 */
public record CanSeeSkyCondition(boolean expected) implements Condition {

  @Override
  public boolean test(ConditionContext context) {
    Boolean canSeeSky = context.canSeeSky();
    return canSeeSky != null && canSeeSky == expected;
  }
}
