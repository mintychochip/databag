package dev.mintychochip.databag;

/**
 * Entity slot referenced by {@link EntityTargetCondition}.
 */
public enum EntityTarget {
  THIS("this"),
  ATTACKER("attacker"),
  DIRECT_ATTACKER("direct_attacker"),
  ATTACKING_PLAYER("attacking_player"),
  TARGET_ENTITY("target_entity"),
  INTERACTING_ENTITY("interacting_entity");

  private final String jsonName;

  EntityTarget(String jsonName) {
    this.jsonName = jsonName;
  }

  public String jsonName() {
    return jsonName;
  }
}
