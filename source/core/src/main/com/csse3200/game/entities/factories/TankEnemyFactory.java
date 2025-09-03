package com.csse3200.game.entities.factories;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.AITaskComponent;
import com.csse3200.game.areas.ForestGameArea;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.enemy.clickable;
import com.csse3200.game.components.tasks.ChaseTask;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.DamageTypeConfig;
import com.csse3200.game.rendering.TextureRenderComponent;

public class TankEnemyFactory {
    // Default tank configuration
    // IF YOU WANT TO MAKE A NEW ENEMY, THIS IS THE VARIABLE STUFF YOU CHANGE
    ///////////////////////////////////////////////////////////////////////////////////////////////
    private static final int DEFAULT_HEALTH = 150;
    private static final int DEFAULT_DAMAGE = 15;
    private static final DamageTypeConfig DEFAULT_RESISTANCE = DamageTypeConfig.None;
    private static final DamageTypeConfig DEFAULT_WEAKNESS = DamageTypeConfig.None;
    private static final Vector2 DEFAULT_SPEED = new Vector2(0.2f, 0.2f);
    private static final String DEFAULT_TEXTURE = "images/tank_enemy.png";
    private static final String DEFAULT_NAME = "Tank Enemy";
    private static final float DEFAULT_CLICKRADIUS = 0.7f;
    ///////////////////////////////////////////////////////////////////////////////////////////////
    
    // Configurable properties
    private static int health = DEFAULT_HEALTH;
    private static int damage = DEFAULT_DAMAGE;
    private static DamageTypeConfig resistance = DEFAULT_RESISTANCE;
    private static DamageTypeConfig weakness = DEFAULT_WEAKNESS;
    private static Vector2 speed = new Vector2(DEFAULT_SPEED);
    private static String texturePath = DEFAULT_TEXTURE;
    private static String displayName = DEFAULT_NAME;
    private static float clickRadius = DEFAULT_CLICKRADIUS;
    private static Entity self;
    private static Entity currentTarget;
    private static int priorityTaskCount = 1;

    /**
     * Creates a tank enemy with current configuration.
     *
     * @param target entity to chase
     * @return entity
     */
    public static Entity createTankEnemy(Entity target) {
        Entity tank = EnemyFactory.createBaseEnemy(target, new Vector2(speed));

        tank
            .addComponent(new CombatStatsComponent(health, damage, resistance, weakness))
            .addComponent(new TextureRenderComponent(texturePath))
            .addComponent(new clickable(clickRadius));

        tank.getEvents().addListener("entityDeath", () -> destroyEnemy(tank));

        self = tank;
        currentTarget = target;

        return tank;
    }

    private static void destroyEnemy(Entity entity) {
        ForestGameArea.NUM_ENEMIES_DEFEATED += 1;
        ForestGameArea.checkEnemyCount();
        Gdx.app.postRunnable(entity::dispose);
        //Eventually add point/score logic here maybe?
    }

    private static void updateSpeed(Vector2 speed) {
        priorityTaskCount += 1;
        self.getComponent(AITaskComponent.class).addTask(new ChaseTask(currentTarget, priorityTaskCount, 100f, 100f, speed));
    }
        
        
    // Getters    
    public static DamageTypeConfig getResistance() {
        return resistance;
    }
    
    public static DamageTypeConfig getWeakness() {
        return weakness;
    }
    
    public static Vector2 getSpeed() {
        return new Vector2(speed); // Return copy to prevent external modification
    }
    
    public static String getTexturePath() {
        return texturePath;
    }
    
    public static String getDisplayName() {
        return displayName;
    }
    
    // Setters   
    public static void setResistance(DamageTypeConfig resistance) {
        TankEnemyFactory.resistance = (resistance != null) ? resistance : DEFAULT_RESISTANCE;
    }
    
    public static void setWeakness(DamageTypeConfig weakness) {
        TankEnemyFactory.weakness = (weakness != null) ? weakness : DEFAULT_WEAKNESS;
    }
    
    public static void setSpeed(Vector2 speed) {
        if (speed != null) {
            TankEnemyFactory.speed.set(speed);
        }
    }
    
    public static void setSpeed(float x, float y) {
        TankEnemyFactory.speed.set(x, y);
    }
    
    public static void setTexturePath(String texturePath) {
        TankEnemyFactory.texturePath = (texturePath != null && !texturePath.trim().isEmpty()) 
            ? texturePath : DEFAULT_TEXTURE;
    }
    
    public static void setDisplayName(String displayName) {
        TankEnemyFactory.displayName = (displayName != null && !displayName.trim().isEmpty()) 
            ? displayName : DEFAULT_NAME;
    }
    
    /**
     * Resets all tank configuration to default values.
     */
    public static void resetToDefaults() {
        health = DEFAULT_HEALTH;
        damage = DEFAULT_DAMAGE;
        resistance = DEFAULT_RESISTANCE;
        weakness = DEFAULT_WEAKNESS;
        speed.set(DEFAULT_SPEED);
        texturePath = DEFAULT_TEXTURE;
        displayName = DEFAULT_NAME;
    }

    private TankEnemyFactory() {
        throw new IllegalStateException("Instantiating static util class");
    }
}