package com.csse3200.game.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.csse3200.game.entities.configs.DamageTypeConfig;

/**
 * Component used to store information related to combat such as health, attack, etc. Any entities
 * which engage it combat should have an instance of this class registered. This class can be
 * extended for more specific combat needs.
 */
public class CombatStatsComponent extends Component {

  private static final Logger logger = LoggerFactory.getLogger(CombatStatsComponent.class);
  private int health;
  private int baseAttack;
  private DamageTypeConfig resistances;
  private DamageTypeConfig weaknesses;

  public CombatStatsComponent(int health, int baseAttack, DamageTypeConfig resistances, DamageTypeConfig weaknesses) {
    setHealth(health);
    setBaseAttack(baseAttack);
    this.resistances = resistances;
    this.weaknesses = weaknesses;
  }


  public DamageTypeConfig getResistances() {
    return resistances;
  }

  public DamageTypeConfig getWeaknesses() {
    return weaknesses;
  }

  /**
   * Returns true if the entity's has 0 health, otherwise false.
   *
   * @return is player dead
   */
  public Boolean isDead() {
    return health == 0;
  }

  /**
   * Returns the entity's health.
   *
   * @return entity's health
   */
  public int getHealth() {
    return health;
  }

  /**
   * Sets the entity's health. Health has a minimum bound of 0.
   *
   * @param health health
   */
  public void setHealth(int health) {
    if (health >= 0) {
      this.health = health;
    } else {
      //Enemy Death Logic
      this.health = 0;
    }
    if (entity != null) {
      entity.getEvents().trigger("updateHealth", this.health);
      if (this.health == 0) {
        entity.getEvents().trigger("entityDeath");
      }
    }
  }

  /**
   * Adds to the player's health. The amount added can be negative.
   *
   * @param health health to add
   */
  public void addHealth(int health, DamageTypeConfig damagetype) {
    if (damagetype == DamageTypeConfig.None) {
      setHealth(this.health + health);
      return;
    }
    if (damagetype == getWeaknesses()) {
      setHealth(this.health + (health * 2));
      return;
    } 
    if (damagetype == getResistances()) {
      setHealth(this.health + (int) Math.round(health * 0.5));
      return;
    } else {
        setHealth(this.health + health);
        return;
    }
  }

  /**
   * Returns the entity's base attack damage.
   *
   * @return base attack damage
   */
  public int getBaseAttack() {
    return baseAttack;
  }

  /**
   * Sets the entity's attack damage. Attack damage has a minimum bound of 0.
   *
   * @param attack Attack damage
   */
  public void setBaseAttack(int attack) {
    if (attack >= 0) {
      this.baseAttack = attack;
    } else {
      logger.error("Can not set base attack to a negative attack value");
    }
  }

  public void hit(CombatStatsComponent attacker) {
    int newHealth = getHealth() - attacker.getBaseAttack();
    setHealth(newHealth);
  }
}
