package com.csse3200.game.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.csse3200.game.entities.configs.DamageTypeConfig;
import com.csse3200.game.areas.ForestGameArea;

/**
 * Stores combat stats like health and base attack.
 * Any entity engaging in combat should have this component.
 */
public class CombatStatsComponent extends Component {
  private static final Logger logger = LoggerFactory.getLogger(CombatStatsComponent.class);

  private int health;
  private int baseAttack;
  private DamageTypeConfig resistances;
  private DamageTypeConfig weaknesses;
  private boolean isEnemy = false;
  private boolean deathCounted = false;

  public CombatStatsComponent(int health, int baseAttack,
                              DamageTypeConfig resistances, DamageTypeConfig weaknesses) {
    setHealth(health);
    setBaseAttack(baseAttack);
    this.resistances = resistances;
    this.weaknesses = weaknesses;
  }

  public void setIsEnemy(boolean isEnemy) {
    this.isEnemy = isEnemy;
  }

  public DamageTypeConfig getResistances() {
    return resistances;
  }

  public DamageTypeConfig getWeaknesses() {
    return weaknesses;
  }

  public void setResistances(DamageTypeConfig type) {
    this.resistances = type;
  }

  public void setWeaknesses(DamageTypeConfig type) {
    this.weaknesses = type;
  }

  /**
   * Returns true if the entity's has 0 health, otherwise false.
   *
   * @return is player dead
   */
  public Boolean isDead() {
    return health == 0;
  }

  public int getHealth() {
    return health;
  }

  /**
   * Set health (min 0). Triggers UI and death events when appropriate.
   */
  public void setHealth(int health) {
    if (health >= 0) {
      this.health = health;
    } else {
      this.health = 0;
    }
    if (entity != null) {
      entity.getEvents().trigger("updateHealth", this.health);
      if (this.health == 0 && !deathCounted) {
        deathCounted = true; // Prevent double-counting
               
        // Trigger death events
        entity.getEvents().trigger("death");
        entity.getEvents().trigger("entityDeath");
        entity.getEvents().trigger("setDead", true);
      }
    }
  }

  /**
   * Add (or subtract) health with damage type adjustment.
   * Negative 'health' means damage.
   */
  public void addHealth(int healthDelta, DamageTypeConfig damageType) {
    if (damageType == DamageTypeConfig.None) {
      setHealth(this.health + healthDelta);
      return;
    }
    if (damageType == getWeaknesses()) {
      setHealth(this.health + (healthDelta * 2));
      return;
    }
    if (damageType == getResistances()) {
      setHealth(this.health + (int) Math.round(healthDelta * 0.5));
      return;
    }
    setHealth(this.health + healthDelta);
  }

  public int getBaseAttack() {
    return baseAttack;
  }

  public void setBaseAttack(int attack) {
    if (attack >= 0) {
      this.baseAttack = attack;
    } else {
      logger.error("Cannot set base attack to a negative value");
    }
  }

  /**
   * Apply damage equal to attacker's base attack.
   * Event dispatching is centralized in setHealth() to avoid duplicates.
   */
  public void hit(CombatStatsComponent attacker) {
    setHealth(getHealth() - attacker.getBaseAttack());
  }
}
