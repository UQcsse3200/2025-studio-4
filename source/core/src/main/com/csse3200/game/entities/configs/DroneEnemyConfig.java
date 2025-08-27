package com.csse3200.game.entities.configs;

import com.csse3200.game.entities.Enemies.DamageType;

/**
 * Defines the properties stored in ghost king config files to be loaded by the NPC Factory.
 */
public class DroneEnemyConfig extends BaseEnemyConfig {
    public String type = "Drone";
    public DamageType[] resistances = {DamageType.Electricity};
    public DamageType[] weaknesses = {DamageType.Fire};
}