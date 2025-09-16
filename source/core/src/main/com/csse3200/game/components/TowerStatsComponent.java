package com.csse3200.game.components;

import com.badlogic.gdx.utils.TimeUtils;

/**
 * Component storing stats for a tower, such as health, damage, range, and attack cooldown.
 */
public class TowerStatsComponent extends Component {
    private int health;
    private float damage;
    private float range;      // attack range in game units
    private float attackCooldown; // time between attacks
    private float attackTimer = 0f; // internal timer

    private float projectileSpeed = 50f;
    private float projectileLife = 1f;
    private String projectileTexture = "projectiles/bullet.png";




    /**
     * Constructs a TowerStatsComponent with the given stats.
     *
     * @param health         Initial health of the tower
     * @param damage         Damage dealt per attack
     * @param range          Attack range in game units
     * @param attackCooldown Time between attacks (seconds)
     */
    public TowerStatsComponent(int health, float damage, float range, float attackCooldown, float projectileSpeed
            , float projectileLife, String projectileTexture) {
        this.health = health;
        this.damage = damage;
        this.range = range;
        this.attackCooldown = attackCooldown;
        this.projectileSpeed = projectileSpeed;
        this.projectileLife = projectileLife;
        this.projectileTexture = projectileTexture;
    }

    public float getProjectileSpeed() {
        return projectileSpeed;
    }

    public void setProjectileSpeed(float projectileSpeed) {
        this.projectileSpeed = projectileSpeed;
    }

    public float getProjectileLife() {
        return projectileLife;
    }

    public void setProjectileLife(float projectileLife) {
    }

    public String getProjectileTexture() {
        return projectileTexture;
    }

    public void setProjectileTexture(String projectileTexture) {
        this.projectileTexture = projectileTexture;
    }

    /**
     * @return Current health of the tower
     */
    public int getHealth() {
        return health;
    }

    /**
     * Sets the tower's health, clamped to a minimum of 0.
     */
    public void setHealth(int health) {
        this.health = Math.max(0, health);
    }

    /**
     * @return Damage dealt per attack
     */
    public float getDamage() {
        return damage;
    }

    /**
     * @return Attack range in game units
     */
    public float getRange() {
        return range;
    }

    /**
     * @return Cooldown between attacks (seconds)
     */
    public float getAttackCooldown() {
        return attackCooldown;
    }

    /**
     * Sets the attack cooldown (seconds).
     */
    public void setAttackCooldown(float attackCooldown) {
        this.attackCooldown = attackCooldown;
    }

    /**
     * @return Internal attack timer
     */
    public float getAttackTimer() {
        return attackTimer;
    }

    /**
     * Resets the attack timer to zero.
     */
    public void resetAttackTimer() {
        attackTimer = 0f;
    }

    /**
     * Increments the attack timer by delta seconds.
     */
    public void updateAttackTimer(float delta) {
        attackTimer += delta;
    }

    /**
     * @return True if the tower can attack (timer >= cooldown)
     */
    public boolean canAttack() {
        return attackTimer >= attackCooldown;
    }

    /**
     * Returns the attack speed (attacks per second).
     * If attackCooldown is 0, returns 0 to avoid division by zero.
     *
     * @return Attack speed (attacks per second)
     */
    public float getAttackSpeed() {
        return attackCooldown != 0f ? 1f / attackCooldown : 0f;
    }
}
