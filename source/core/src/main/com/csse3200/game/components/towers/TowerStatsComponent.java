package core.src.main.com.csse3200.game.components.towers;

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

    /**
     * Constructs a TowerStatsComponent with the given stats.
     */
    public TowerStatsComponent(int health, float damage, float range, float attackCooldown,
                               float projectileSpeed, float projectileLife,
                               String projectileTexture, int level_A, int level_B) {
        this.health = health;
        this.damage = damage;
        this.range = range;
        this.attackCooldown = attackCooldown;
        this.projectileSpeed = projectileSpeed;
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

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = Math.max(0, health);
    }

    public float getDamage() {
        return damage;
    }

    public void setDamage(float damage) {
        this.damage = Math.max(0, damage);
    }

    public float getRange() {
        return range;
    }

    public void setRange(float range) {
        this.range = Math.max(0, range);
    }

    public float getAttackCooldown() {
        return attackCooldown;
    }

    public void setAttackCooldown(float attackCooldown) {
        this.attackCooldown = attackCooldown;
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


}
