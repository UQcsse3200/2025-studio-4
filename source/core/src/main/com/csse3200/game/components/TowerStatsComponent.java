package com.csse3200.game.components;

/**
 * Component storing stats for a tower, such as health, damage, range, and attack cooldown.
 */
public class TowerStatsComponent extends Component {
    private int health;
    private float damage;
    private float range; // Attack range in game units
    private float attackCooldown; // Time between attacks (seconds)
    private float attackTimer = 0f; // Internal timer

    private float projectileSpeed = 50f;
    private float projectileLife = 1f;
    private String projectileTexture = "projectiles/bullet.png";

    /**
     * Constructs a TowerStatsComponent with the given stats.
     *
     * @param health           Initial health of the tower
     * @param damage           Damage dealt per attack
     * @param range            Attack range in game units
     * @param attackCooldown   Time between attacks (seconds)
     * @param projectileSpeed  Speed of the projectile
     * @param projectileLife   Lifetime of the projectile
     * @param projectileTexture Texture path for the projectile
     */
    public TowerStatsComponent(int health, float damage, float range, float attackCooldown,
                               float projectileSpeed, float projectileLife, String projectileTexture) {
        this.health = health;
        this.damage = damage;
        this.range = range;
        this.attackCooldown = attackCooldown;
        this.projectileSpeed = projectileSpeed;
        this.projectileLife = projectileLife;
        this.projectileTexture = projectileTexture;
    }

    /**
     * Returns the speed of the projectile fired by the tower.
     *
     * @return projectile speed
     */
    public float getProjectileSpeed() {
        return projectileSpeed;
    }

    /**
     * Sets the speed of the projectile fired by the tower.
     *
     * @param projectileSpeed the new projectile speed
     */
    public void setProjectileSpeed(float projectileSpeed) {
        this.projectileSpeed = projectileSpeed;
    }

    /**
     * Returns the lifetime of the projectile fired by the tower.
     *
     * @return projectile lifetime
     */
    public float getProjectileLife() {
        return projectileLife;
    }

    /**
     * Sets the lifetime of the projectile fired by the tower.
     *
     * @param projectileLife the new projectile lifetime
     */
    public void setProjectileLife(float projectileLife) {
        this.projectileLife = projectileLife;
    }

    /**
     * Returns the texture path of the projectile fired by the tower.
     *
     * @return projectile texture path
     */
    public String getProjectileTexture() {
        return projectileTexture;
    }

    /**
     * Sets the texture path of the projectile fired by the tower.
     *
     * @param projectileTexture the new projectile texture path
     */
    public void setProjectileTexture(String projectileTexture) {
        this.projectileTexture = projectileTexture;
    }

    /**
     * Returns the current health of the tower.
     *
     * @return current health
     */
    public int getHealth() {
        return health;
    }

    /**
     * Sets the tower's health, clamped to a minimum of 0.
     *
     * @param health new health value
     */
    public void setHealth(int health) {
        this.health = Math.max(0, health);
    }

    /**
     * Returns the damage dealt per attack.
     *
     * @return damage per attack
     */
    public float getDamage() {
        return damage;
    }

    /**
     * Returns the attack range in game units.
     *
     * @return attack range
     */
    public float getRange() {
        return range;
    }

    /**
     * Returns the cooldown between attacks (seconds).
     *
     * @return attack cooldown
     */
    public float getAttackCooldown() {
        return attackCooldown;
    }

    /**
     * Sets the attack cooldown (seconds).
     *
     * @param attackCooldown new attack cooldown
     */
    public void setAttackCooldown(float attackCooldown) {
        this.attackCooldown = attackCooldown;
    }

    /**
     * Returns the internal attack timer.
     *
     * @return attack timer
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
     *
     * @param delta time to increment by (seconds)
     */
    public void updateAttackTimer(float delta) {
        attackTimer += delta;
    }

    /**
     * Returns true if the tower can attack (timer >= cooldown).
     *
     * @return true if tower can attack, false otherwise
     */
    public boolean canAttack() {
        return attackTimer >= attackCooldown;
    }

    /**
     * Returns the attack speed (attacks per second).
     * If attackCooldown is 0, returns 0 to avoid division by zero.
     *
     * @return attack speed (attacks per second)
     */
    public float getAttackSpeed() {
        return attackCooldown != 0f ? 1f / attackCooldown : 0f;
    }
}
