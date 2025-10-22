package com.csse3200.game.components.towers;

import com.csse3200.game.components.Component;
/**
 * Component storing stats for a tower, such as health, damage, range, and attack cooldown.
 * Upgrade paths now only increment level; no stats are changed.
 */
public class TowerStatsComponent extends Component {
    private int health;
    private float damage;
    private float range; // Attack range in game units
    private float attackCooldown; // Time between attacks (seconds)
    private float attackTimer = 0f; // Internal timer
    private int level_A = 0; // start at 0, upgrade goes 1-4
    private int level_B = 0;
    private float projectileSpeed = 50f;
    private float projectileLife = 1f;
    private String projectileTexture = "projectiles/bullet.png";

    // NEW: Base stats that upgrades should set. Current stats are derived from these via multipliers.
    private float baseDamage;
    private float baseRange;
    private float baseAttackCooldown;
    private float baseProjectileSpeed;

    // NEW: Cumulative permanent multipliers (from totems, etc.).
    // For cooldown, we use a divisor (>=1 means faster; applied as baseCooldown / cooldownDivisor).
    private float multDamage = 1f;
    private float multRange = 1f;
    private float multProjectileSpeed = 1f;
    private float cooldownDivisor = 1f;

    private static final float MIN_COOLDOWN = 0.001f;

    /**
     * Constructs a TowerStatsComponent with the given stats.
     */
    public TowerStatsComponent(int health, float damage, float range, float attackCooldown,
                               float projectileSpeed, float projectileLife,
                               String projectileTexture, int level_A, int level_B) {
        this.health = health;
        // Initialize base stats and multipliers, then compute current stats
        this.baseDamage = Math.max(0, damage);
        this.baseRange = Math.max(0, range);
        this.baseAttackCooldown = Math.max(0, attackCooldown);
        this.baseProjectileSpeed = Math.max(0, projectileSpeed);
        this.multDamage = 1f;
        this.multRange = 1f;
        this.multProjectileSpeed = 1f;
        this.cooldownDivisor = 1f;
        recomputeFromBaseMultipliers();

        this.projectileLife = projectileLife;
        this.projectileTexture = projectileTexture;
        this.level_A = level_A;
        this.level_B = level_B;
    }

    /** Increments the level of upgrade path A by 1, max 4. Only updates level. */
    public void incrementLevel_A() {
        if (this.level_A < 5) {
            this.level_A += 1;
        }
    }

    /** Increments the level of upgrade path B by 1, max 4. Only updates level. */
    public void incrementLevel_B() {
        if (this.level_B < 5) {
            this.level_B += 1;
        }
    }

    public int getLevel_A() {
        return level_A;
    }

    public int getLevel_B() {
        return level_B;
    }

    public void setLevel_A(int level) {
        this.level_A = Math.max(0, Math.min(5, level));
    }

    public void setLevel_B(int level) {
        this.level_B = Math.max(0, Math.min(5, level));
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = Math.max(0, health);
    }

    public float getDamage() {
        return damage;
    }

    // CHANGED: Setters now set base stats and recompute current using permanent multipliers.
    public void setDamage(float damage) {
        this.baseDamage = Math.max(0, damage);
        this.damage = this.baseDamage * this.multDamage;
    }

    public float getRange() {
        return range;
    }

    public void setRange(float range) {
        this.baseRange = Math.max(0, range);
        this.range = this.baseRange * this.multRange;
    }

    public float getAttackCooldown() {
        return attackCooldown;
    }

    public void setAttackCooldown(float cooldown) {
        this.baseAttackCooldown = Math.max(0, cooldown);
        this.attackCooldown = Math.max(MIN_COOLDOWN, this.baseAttackCooldown / Math.max(1f, this.cooldownDivisor));
    }

    public float getProjectileSpeed() {
        return projectileSpeed;
    }

    public void setProjectileSpeed(float speed) {
        this.baseProjectileSpeed = Math.max(0, speed);
        this.projectileSpeed = this.baseProjectileSpeed * this.multProjectileSpeed;
    }

    public float getProjectileLife() {
        return projectileLife;
    }

    public void setProjectileLife(float projectileLife) {
        this.projectileLife = projectileLife;
    }

    public String getProjectileTexture() {
        return projectileTexture;
    }

    public void setProjectileTexture(String projectileTexture) {
        this.projectileTexture = projectileTexture;
    }

    public float getAttackTimer() {
        return attackTimer;
    }

    public void resetAttackTimer() {
        attackTimer = 0f;
    }

    public void updateAttackTimer(float delta) {
        attackTimer += delta;
    }

    public boolean canAttack() {
        return attackTimer >= attackCooldown;
    }

    public float getAttackSpeed() {
        return attackCooldown != 0f ? 1f / attackCooldown : 0f;
    }

    // NEW: Apply permanent (stacking) multipliers to stats.
    // dmgMul/rangeMul/projMul multiply those stats; cooldownDiv multiplies attack speed (divides cooldown).
    public void applyPermanentBoostMultipliers(float dmgMul, float rangeMul, float projMul, float cooldownDiv) {
        if (dmgMul > 0f) this.multDamage *= dmgMul;
        if (rangeMul > 0f) this.multRange *= rangeMul;
        if (projMul > 0f) this.multProjectileSpeed *= projMul;
        if (cooldownDiv > 0f) this.cooldownDivisor *= cooldownDiv;
        recomputeFromBaseMultipliers();
    }

    // NEW: Remove previously applied multipliers by dividing them back out, then recompute.
    public void removePermanentBoostMultipliers(float dmgMul, float rangeMul, float projMul, float cooldownDiv) {
        if (dmgMul > 0f) this.multDamage /= dmgMul;
        if (rangeMul > 0f) this.multRange /= rangeMul;
        if (projMul > 0f) this.multProjectileSpeed /= projMul;
        if (cooldownDiv > 0f) this.cooldownDivisor /= cooldownDiv;
        recomputeFromBaseMultipliers();
    }

    // NEW: Recompute current stats from base stats and cumulative multipliers.
    public void recomputeFromBaseMultipliers() {
        this.damage = Math.max(0f, this.baseDamage * this.multDamage);
        this.range = Math.max(0f, this.baseRange * this.multRange);
        this.projectileSpeed = Math.max(0f, this.baseProjectileSpeed * this.multProjectileSpeed);
        this.attackCooldown = Math.max(MIN_COOLDOWN, this.baseAttackCooldown / Math.max(1f, this.cooldownDivisor));
    }

    // OPTIONAL: Expose base getters if needed elsewhere.
    public float getBaseDamage() { return baseDamage; }
    public float getBaseRange() { return baseRange; }
    public float getBaseAttackCooldown() { return baseAttackCooldown; }
    public float getBaseProjectileSpeed() { return baseProjectileSpeed; }
}
