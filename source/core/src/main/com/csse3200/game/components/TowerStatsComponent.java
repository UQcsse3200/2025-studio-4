package com.csse3200.game.components;

public class TowerStatsComponent extends Component {
    private int health;
    private float damage;
    private float range;      // attack range in game units
    private float attackCooldown; // time between attacks
    private float attackTimer = 0f; // internal timer

    public TowerStatsComponent(int health, float damage, float range, float attackCooldown) {
        this.health = health;
        this.damage = damage;
        this.range = range;
        this.attackCooldown = attackCooldown;
    }

    public int getHealth() { return health; }
    public void setHealth(int health) { this.health = Math.max(0, health); }

    public float getDamage() { return damage; }
    public float getRange() { return range; }

    public float getAttackCooldown() { return attackCooldown; }
    public void setAttackCooldown(float attackCooldown) { this.attackCooldown = attackCooldown; }
    public float getAttackTimer() { return attackTimer; }
    public void resetAttackTimer() { attackTimer = 0f; }
    public void updateAttackTimer(float delta) { attackTimer += delta; }
    public boolean canAttack() { return attackTimer >= attackCooldown; }

    /**
     * Returns the attack speed (attacks per second).
     * If attackCooldown is 0, returns 0 to avoid division by zero.
     */
    public float getAttackSpeed() {
        return attackCooldown != 0f ? 1f / attackCooldown : 0f;
    }
}
