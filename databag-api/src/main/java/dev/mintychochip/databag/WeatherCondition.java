package dev.mintychochip.databag;

/**
 * Matches snapshot weather against {@code state}.
 */
public record WeatherCondition(WeatherState state) implements Condition {

  @Override
  public boolean test(ConditionContext context) {
    if (!context.present() || context.weather() == null) {
      return false;
    }
    return state == context.weather();
  }
}
