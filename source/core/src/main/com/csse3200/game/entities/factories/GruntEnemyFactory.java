package com.csse3200.game.entities.factories;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.AITaskComponent;
import com.csse3200.game.areas.ForestGameArea;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.currencysystem.CurrencyComponent.CurrencyType;
import com.csse3200.game.components.currencysystem.CurrencyManagerComponent;
import com.csse3200.game.components.enemy.clickable;
import com.csse3200.game.components.enemy.WaypointComponent;
import com.csse3200.game.components.tasks.ChaseTask;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.DamageTypeConfig;

import java.util.Map;

public class GruntEnemyFactory {
    // Default grunt configuration
    // IF YOU WANT TO MAKE A NEW ENEMY, THIS IS THE VARIABLE STUFF YOU CHANGE
    ///////////////////////////////////////////////////////////////////////////////////////////////
    private static final int DEFAULT_HEALTH = 75;
    private static final int DEFAULT_DAMAGE = 12;
    private static final DamageTypeConfig DEFAULT_RESISTANCE = DamageTypeConfig.None;
    private static final DamageTypeConfig DEFAULT_WEAKNESS = DamageTypeConfig.None;
    private static final Vector2 DEFAULT_SPEED = new Vector2(0.8f, 0.8f);
    private static final String DEFAULT_TEXTURE = "images/grunt_enemy.png";
    private static final String DEFAULT_NAME = "Grunt Enemy";
    private static final float DEFAULT_CLICKRADIUS = 0.7f;
    private static final int DEFAULT_CURRENCY_AMOUNT = 50;
    private static final CurrencyType DEFAULT_CURRENCY_TYPE = CurrencyType.METAL_SCRAP;
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
    private static int currencyAmount = DEFAULT_CURRENCY_AMOUNT;
    private static CurrencyType currencyType = DEFAULT_CURRENCY_TYPE;

    /**
     * Creates a grunt enemy with current configuration.
     *
     * @param waypoints List of waypoint entities for the grunt to follow
     * @param player Reference to the player entity
     * @return entity
     */

    public static Entity createGruntEnemy(java.util.List<Entity> waypoints, Entity player) {
        Entity grunt = EnemyFactory.createBaseEnemyAnimated(waypoints.get(0), new Vector2(speed), waypoints,
        "images/grunt_basic_spritesheet.atlas", 0.5f, 0.18f);

        // Add waypoint component for independent waypoint tracking
        WaypointComponent waypointComponent = new WaypointComponent(waypoints, player, speed);
        grunt.addComponent(waypointComponent);

        grunt
            .addComponent(new com.csse3200.game.rendering.TextureRenderComponent(texturePath))
            .addComponent(new CombatStatsComponent(health, damage, resistance, weakness))
            .addComponent(new clickable(clickRadius));

        grunt.getEvents().addListener("entityDeath", () -> destroyEnemy(grunt));

        // Handle waypoint progression for this specific grunt
        grunt.getEvents().addListener("chaseTaskFinished", () -> {
            WaypointComponent wc = grunt.getComponent(WaypointComponent.class);
            if (wc != null && wc.hasMoreWaypoints()) {
                Entity nextTarget = wc.getNextWaypoint();
                if (nextTarget != null) {
                    updateChaseTarget(grunt, nextTarget);
                }
            }
        });

        return grunt;
    }

    private static void destroyEnemy(Entity entity) {
        ForestGameArea.NUM_ENEMIES_DEFEATED += 1;
        ForestGameArea.checkEnemyCount();

        // Drop currency upon defeat
        WaypointComponent wc = entity.getComponent(WaypointComponent.class);
        if (wc != null && wc.getPlayerRef() != null) {
            Entity player = wc.getPlayerRef();
            CurrencyManagerComponent currencyManager = player.getComponent(CurrencyManagerComponent.class);
            if (currencyManager != null) {
                Map<CurrencyType, Integer> drops = Map.of(currencyType, currencyAmount);
                player.getEvents().trigger("dropCurrency", drops);
            }
        }

        Gdx.app.postRunnable(entity::dispose);
        //Eventually add point/score logic here maybe?
    }

    private static void updateSpeed(Entity grunt, Vector2 newSpeed) {
        WaypointComponent wc = grunt.getComponent(WaypointComponent.class);
        if (wc != null) {
            wc.incrementPriorityTaskCount();
            wc.setSpeed(newSpeed);
            grunt.getComponent(AITaskComponent.class).addTask(
                new ChaseTask(wc.getCurrentTarget(), wc.getPriorityTaskCount(), 100f, 100f, newSpeed));
        }
    }

    private static void updateChaseTarget(Entity grunt, Entity newTarget) {
        System.out.println("updating task for grunt: " + grunt.getId());
        WaypointComponent wc = grunt.getComponent(WaypointComponent.class);
        if (wc != null) {
            wc.incrementPriorityTaskCount();
            wc.setCurrentTarget(newTarget);
            grunt.getComponent(AITaskComponent.class).addTask(
                new ChaseTask(newTarget, wc.getPriorityTaskCount(), 100f, 100f, wc.getSpeed()));
        }
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
        GruntEnemyFactory.resistance = (resistance != null) ? resistance : DEFAULT_RESISTANCE;
    }
    
    public static void setWeakness(DamageTypeConfig weakness) {
        GruntEnemyFactory.weakness = (weakness != null) ? weakness : DEFAULT_WEAKNESS;
    }
    
    public static void setSpeed(Vector2 speed) {
        if (speed != null) {
            GruntEnemyFactory.speed.set(speed);
        }
    }
    
    public static void setSpeed(float x, float y) {
        GruntEnemyFactory.speed.set(x, y);
    }
    
    public static void setTexturePath(String texturePath) {
        GruntEnemyFactory.texturePath = (texturePath != null && !texturePath.trim().isEmpty()) 
            ? texturePath : DEFAULT_TEXTURE;
    }
    
    public static void setDisplayName(String displayName) {
        GruntEnemyFactory.displayName = (displayName != null && !displayName.trim().isEmpty()) 
            ? displayName : DEFAULT_NAME;
    }
    
    /**
     * Resets all grunt configuration to default values.
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

    private GruntEnemyFactory() {
        throw new IllegalStateException("Instantiating static util class");
    }
}