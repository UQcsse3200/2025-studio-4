package com.csse3200.game.components;

public class TowerStatsComponent extends Component {
    private int damage;
    private float range;
    private float attackSpeed;

    public TowerStatsComponent(int damage, float range, float attackSpeed) {
        this.damage = damage;
        this.range = range;
        this.attackSpeed = attackSpeed;
    }

    public int getDamage() { return damage; }
    public float getRange() { return range; }
    public float getAttackSpeed() { return attackSpeed; }

    public void setDamage(int damage) { this.damage = damage; }
    public void setRange(float range) { this.range = range; }
    public void setAttackSpeed(float attackSpeed) { this.attackSpeed = attackSpeed; }
}