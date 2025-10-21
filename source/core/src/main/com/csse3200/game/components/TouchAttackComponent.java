package com.csse3200.game.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;

/**
 * When this entity touches a valid enemy's hitbox, deal damage and optional knockback.
 * Compatible with pooled projectiles by resetting per-shot state on "projectile.activated".
 */
public class TouchAttackComponent extends Component {
  private short targetLayer;
  private float knockbackForce = 0f;
  private CombatStatsComponent combatStats;
  private HitboxComponent hitboxComponent;
  private boolean hasAttacked = false;
  private boolean attackedPlayerBase = false; // Track if we attacked the player base

  /**
   * Create a component which attacks entities on collision, without knockback.
   * @param targetLayer The physics layer of the target's collider.
   */
  public TouchAttackComponent(short targetLayer) {
    this.targetLayer = targetLayer;
  }

  /**
   * Create a component which attacks entities on collision, with knockback.
   * @param targetLayer The physics layer of the target's collider.
   * @param knockback The magnitude of the knockback applied to the entity.
   */
  public TouchAttackComponent(short targetLayer, float knockback) {
    this.targetLayer = targetLayer;
    this.knockbackForce = knockback;
  }

  /**
   * Register collision and animation listeners and cache required components.
   * Resets one-shot attack state when the projectile is reactivated from the pool.
   */
  @Override
  public void create() {
    entity.getEvents().addListener("collisionStart", this::onCollisionStart);
    combatStats = entity.getComponent(CombatStatsComponent.class);
    hitboxComponent = entity.getComponent(HitboxComponent.class);
    
    entity.getEvents().addListener("attackAnimationComplete", this::onAttackComplete);

    // Reset per-shot state when a pooled projectile is reactivated
    entity.getEvents().addListener("projectile.activated", () -> {
      hasAttacked = false;
      attackedPlayerBase = false;
    });
  }

  /**
   * Handle collision start events. Applies damage once per shot to the first valid target,
   * triggers attack animation hooks, and optionally applies knockback if the target has physics.
   * @param me    this entity's fixture
   * @param other the other entity's fixture
   */
  private void onCollisionStart(Fixture me, Fixture other) {
    if (hasAttacked) {
      return;
    }
    
    if (hitboxComponent.getFixture() != me) {
      // Not triggered by hitbox, ignore
      return;
    }

    if (!PhysicsLayer.contains(targetLayer, other.getFilterData().categoryBits)) {
      // Doesn't match our target layer, ignore
      return;
    }

    // Try to attack target.
    Entity target = ((BodyUserData) other.getBody().getUserData()).entity;
    CombatStatsComponent targetStats = target.getComponent(CombatStatsComponent.class);
    if (targetStats != null) {
      targetStats.hit(combatStats);
      target.getEvents().trigger("showDamage", combatStats.getBaseAttack(), target.getCenterPosition().cpy());
      entity.getEvents().trigger("attackStart");
      hasAttacked = true;
      attackedPlayerBase = false; // This was NOT a base attack
      Gdx.app.log("ATTACK", "attackStart fired by " + entity.getId() + " on enemy");
    } else {
      Entity target2 = ((BodyUserData) other.getBody().getUserData()).entity;
      PlayerCombatStatsComponent playerStats = target2.getComponent(PlayerCombatStatsComponent.class);
      if (playerStats != null) {
        playerStats.hit(combatStats);
        hasAttacked = true;
        attackedPlayerBase = true; // This WAS a base attack
        
        // Mark this enemy as having reached the base
        com.csse3200.game.components.ReachedBaseComponent reachedBase = 
            entity.getComponent(com.csse3200.game.components.ReachedBaseComponent.class);
        if (reachedBase != null) {
            reachedBase.markAsReachedBase();
            //Gdx.app.log("CURRENCY", "Enemy " + entity.getId() + " - Marked as reached base");
        }

        entity.getEvents().trigger("stopMovement");

        //Gdx.app.log("ATTACK", "Enemy " + entity.getId() + " attacking player base - triggering attackStart");

        entity.getEvents().trigger("attackStart");
      }
    }

    // Apply knockback
    PhysicsComponent physicsComponent = target.getComponent(PhysicsComponent.class);
    if (physicsComponent != null && knockbackForce > 0f) {
      Body targetBody = physicsComponent.getBody();
      Vector2 direction = target.getCenterPosition().sub(entity.getCenterPosition());
      Vector2 impulse = direction.setLength(knockbackForce);
      targetBody.applyLinearImpulse(impulse, targetBody.getWorldCenter(), true);
    }
  }
  
  /**
   * Called when the attack animation finishes. Only triggers entity death if the
   * attack was against the player base (suicide attack), not when attacking projectiles.
   */
  private void onAttackComplete() {
    if (attackedPlayerBase) {
      Gdx.app.log("ATTACK", "Attack animation complete for " + entity.getId() + " after base attack - triggering entityDeath");
      entity.getEvents().trigger("entityDeath");
    } else {
      Gdx.app.log("ATTACK", "Attack animation complete for " + entity.getId() + " but was not base attack - NOT triggering death");
    }
  }
}