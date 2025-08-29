package com.csse3200.game.entities.Enemies;

import com.csse3200.game.entities.Entity;

/**
 * Abstract base class for all enemy types in the game.
 * Defines core stats and behaviors shared by all enemies.
 */
public abstract class Enemy extends Entity {
    protected int maxHealth;
    protected int currentHealth;
    protected float speed;
    protected int damage;
    protected String type;

    // resistances/weaknesses could be enums
    protected DamageType[] resistances;
    protected DamageType[] weaknesses;

    public Enemy(int maxHealth, float speed, int damage, String type) {
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
        this.speed = speed;
        this.damage = damage;
        this.type = type;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public int getCurrentHealth() {
        return currentHealth;
    }

    public float getSpeed() {
        return speed;
    }

    public int getDamage() {
        return damage;
    }

    public String getType() {
        return type;
    }

    public void takeDamage(int amount) {
        this.currentHealth -= amount;
        if (this.currentHealth <= 0) {
            this.currentHealth = 0;
            onDeath();
        }
    }

    public boolean isDead() {
        return currentHealth <= 0;
    }

    protected void onDeath() {
        // Handle enemy death (e.g.,loot, play animation)
    }
}
