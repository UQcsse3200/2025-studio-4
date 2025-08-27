package com.csse3200.game.entities.Enemies;

/**
 * A basic Drone enemy. Moves towards the base and deals damage on contact.
 */
public class DroneEnemy extends Enemy {
    public DroneEnemy() {
        super(50, 2.0f, 10, "Drone");
        this.resistances = new DamageType[] {DamageType.Electricity};
        this.weaknesses = new DamageType[] {DamageType.Fire};
    }

    @Override
    protected void onDeath() {
        // TODO: Add death animation, currency drop, etc.
        System.out.println("DroneEnemy has died.");
    }
}
