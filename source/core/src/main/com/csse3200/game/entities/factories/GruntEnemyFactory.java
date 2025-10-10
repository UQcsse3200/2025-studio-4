package com.csse3200.game.entities.factories;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.AITaskComponent;
import com.csse3200.game.areas.ForestGameArea;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.currencysystem.CurrencyComponent.CurrencyType;
import com.csse3200.game.components.currencysystem.CurrencyManagerComponent;
import com.csse3200.game.components.deck.DeckComponent;
import com.csse3200.game.components.enemy.clickable;
import com.csse3200.game.components.enemy.WaypointComponent;
import com.csse3200.game.components.enemy.SpeedWaypointComponent;
import com.csse3200.game.components.tasks.ChaseTask;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.DamageTypeConfig;
import com.csse3200.game.utils.Difficulty;
import java.util.Map;
import com.csse3200.game.components.PlayerScoreComponent;
import com.csse3200.game.components.movement.AdjustSpeedByHealthComponent;
import com.csse3200.game.components.effects.SlowEffectComponent;

public class GruntEnemyFactory {
    // Default grunt configuration
    // IF YOU WANT TO MAKE A NEW ENEMY, THIS IS THE VARIABLE STUFF YOU CHANGE
    ///////////////////////////////////////////////////////////////////////////////////////////////
    private static final int DEFAULT_HEALTH = 50;
    private static final int DEFAULT_DAMAGE = 12;
    private static final DamageTypeConfig DEFAULT_RESISTANCE = DamageTypeConfig.None;
    private static final DamageTypeConfig DEFAULT_WEAKNESS = DamageTypeConfig.None;
    private static final Vector2 DEFAULT_SPEED = new Vector2(0.8f, 0.8f);
    private static final String DEFAULT_TEXTURE = "images/grunt_enemy.png";
    private static final String DEFAULT_NAME = "Grunt Enemy";
    private static final float DEFAULT_CLICKRADIUS = 0.7f;
    private static final int DEFAULT_CURRENCY_AMOUNT = 100;
    private static final CurrencyType DEFAULT_CURRENCY_TYPE = CurrencyType.METAL_SCRAP;
    private static final int DEFAULT_POINTS = 150;
    private static final float SPEED_EPSILON = 0.001f;
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
    private static int points = DEFAULT_POINTS;

    /**
     * Creates a grunt enemy with current configuration.
     *
     * @param waypoints List of waypoint entities for the grunt to follow
     * @param player Reference to the player entity
     * @return entity
     */

    public static Entity createGruntEnemy(java.util.List<Entity> waypoints, Entity player, Difficulty difficulty) {
        return createGruntEnemy(waypoints, player, difficulty, 0);
    }

    /** Overload: start from specific waypoint index (for save/load resume). */
    public static Entity createGruntEnemy(java.util.List<Entity> waypoints, Entity player, Difficulty difficulty, int startWaypointIndex) {
        int idx = Math.max(0, Math.min(waypoints.size() - 1, startWaypointIndex));
        Entity grunt = EnemyFactory.createBaseEnemyAnimated(waypoints.get(idx), new Vector2(speed), waypoints,
        "images/grunt_basic_spritesheet.atlas", 0.5f, 0.18f, idx);

        // Add waypoint component for independent waypoint tracking
        WaypointComponent waypointComponent = new WaypointComponent(waypoints, player, new Vector2(speed));
        waypointComponent.setCurrentWaypointIndex(idx);
        waypointComponent.setCurrentTarget(waypoints.get(idx));
        grunt.addComponent(waypointComponent);
        applySpeedModifier(grunt, waypointComponent, waypoints.get(idx));

        grunt
            .addComponent(new CombatStatsComponent(health * difficulty.getMultiplier(), damage * difficulty.getMultiplier(), resistance, weakness))
            .addComponent(new com.csse3200.game.components.enemy.EnemyTypeComponent("grunt"))
            .addComponent(new DeckComponent.EnemyDeckComponent(DEFAULT_NAME, DEFAULT_HEALTH, DEFAULT_DAMAGE, DEFAULT_RESISTANCE, DEFAULT_WEAKNESS, DEFAULT_TEXTURE))
            .addComponent(new clickable(clickRadius))
            .addComponent(new SlowEffectComponent()); // 添加减速特效组件
            CombatStatsComponent combatStats = grunt.getComponent(CombatStatsComponent.class);
            if (combatStats != null) combatStats.setIsEnemy(true);


        grunt.getEvents().addListener("entityDeath", () -> destroyEnemy(grunt));

        // Handle waypoint progression for this specific grunt
        grunt.getEvents().addListener("chaseTaskFinished", () -> {
            WaypointComponent wc = grunt.getComponent(WaypointComponent.class);
            if (wc != null && wc.hasMoreWaypoints()) {
                Entity nextTarget = wc.getNextWaypoint();
                if (nextTarget != null) {
                    applySpeedModifier(grunt, wc, nextTarget);
                    updateChaseTarget(grunt, nextTarget);
                }
            }
        });

        grunt.addComponent(new AdjustSpeedByHealthComponent()
                // Must add thresholds in order of ascending health percentages
                .addThreshold(0.25f, 2f)
                .addThreshold(0.5f, 1.4f)
        );
        return grunt;
    }

    private static void destroyEnemy(Entity entity) {
        // Check which game area is active and use its counters
        if (com.csse3200.game.areas2.MapTwo.ForestGameArea2.currentGameArea != null) {
            // We're in ForestGameArea2
            com.csse3200.game.areas2.MapTwo.ForestGameArea2.NUM_ENEMIES_DEFEATED += 1;
            com.csse3200.game.areas2.MapTwo.ForestGameArea2.checkEnemyCount();
        } else {
            // Default to ForestGameArea (original behavior)
            ForestGameArea.NUM_ENEMIES_DEFEATED += 1;
            ForestGameArea.checkEnemyCount();
        }

        WaypointComponent wc = entity.getComponent(WaypointComponent.class);
        if (wc != null && wc.getPlayerRef() != null) {
            Entity player = wc.getPlayerRef();

            // Drop currency upon defeat
            CurrencyManagerComponent currencyManager = player.getComponent(CurrencyManagerComponent.class);
            if (currencyManager != null) {
                Map<CurrencyType, Integer> drops = Map.of(currencyType, currencyAmount);
                player.getEvents().trigger("dropCurrency", drops);
            }

            // Award points to player upon defeating enemy
            PlayerScoreComponent totalScore = player.getComponent(PlayerScoreComponent.class);
            if (totalScore != null) {
                totalScore.addPoints(points);
            }

            // Track kill for ranking component
            com.csse3200.game.components.PlayerRankingComponent prc = player.getComponent(com.csse3200.game.components.PlayerRankingComponent.class);
            if (prc != null) {
                prc.addKill();
            }
        }

        //Gdx.app.postRunnable(entity::dispose);
        //Eventually add point/score logic here maybe?
    }

    private static void applySpeedModifier(Entity grunt, WaypointComponent waypointComponent, Entity waypoint) {
        if (waypointComponent == null || waypoint == null) {
            return;
        }

        SpeedWaypointComponent speedMarker = waypoint.getComponent(SpeedWaypointComponent.class);
        Vector2 desiredSpeed = waypointComponent.getBaseSpeed();
        if (speedMarker != null) {
            float multiplier = speedMarker.getSpeedMultiplier();
            desiredSpeed.scl(multiplier);
            
            // 如果是减速区域（倍率小于1.0），触发蓝色减速特效
            if (multiplier < 1.0f) {
                grunt.getEvents().trigger("applySlow");
            } else {
                // 如果是加速区域或正常区域，移除减速特效
                grunt.getEvents().trigger("removeSlow");
            }
        } else {
            // 如果没有速度修改器，移除减速特效
            grunt.getEvents().trigger("removeSlow");
        }

        if (!waypointComponent.getSpeed().epsilonEquals(desiredSpeed, SPEED_EPSILON)) {
            updateSpeed(grunt, desiredSpeed);
        }
    }

    public static void updateSpeed(Entity grunt, Vector2 newSpeed) {
        WaypointComponent wc = grunt.getComponent(WaypointComponent.class);
        if (wc != null) {
            wc.incrementPriorityTaskCount();
            wc.setSpeed(newSpeed);
            grunt.getComponent(AITaskComponent.class).addTask(
                new ChaseTask(wc.getCurrentTarget(), wc.getPriorityTaskCount(), 100f, 100f, newSpeed));
        }
    }

    private static void updateChaseTarget(Entity grunt, Entity newTarget) {
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

    public static int getPoints() {
        return points;
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
        points = DEFAULT_POINTS;
    }

    private GruntEnemyFactory() {
        throw new IllegalStateException("Instantiating static util class");
    }
}

