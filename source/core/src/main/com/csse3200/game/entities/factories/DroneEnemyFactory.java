package com.csse3200.game.entities.factories;

import com.badlogic.gdx.audio.Sound;
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
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.utils.Difficulty;

import java.util.HashMap;
import java.util.Map;
import com.csse3200.game.components.PlayerScoreComponent;
import com.csse3200.game.components.effects.SlowEffectComponent;

public class DroneEnemyFactory {
    // Default drone configuration
    // IF YOU WANT TO MAKE A NEW ENEMY, THIS IS THE VARIABLE STUFF YOU CHANGE
    ///////////////////////////////////////////////////////////////////////////////////////////////
    private static final int DEFAULT_HEALTH = 25;
    private static final int DEFAULT_DAMAGE = 10;
    private static final DamageTypeConfig DEFAULT_RESISTANCE = DamageTypeConfig.None;
    private static final DamageTypeConfig DEFAULT_WEAKNESS = DamageTypeConfig.Fire;
    private static final Vector2 DEFAULT_SPEED = new Vector2(1.2f, 1.2f);
    private static final String DEFAULT_TEXTURE = "images/drone_enemy.png";
    private static final String DEFAULT_NAME = "Drone Enemy";
    private static final float DEFAULT_CLICKRADIUS = 0.7f;
    private static final Map<CurrencyType, Integer> DEFAULT_CURRENCY_DROPS = Map.of(
    CurrencyType.METAL_SCRAP, 75,
    CurrencyType.TITANIUM_CORE, 25,
    CurrencyType.NEUROCHIP, 5
    );
    private static final int DEFAULT_POINTS = 100;
    private static final float SPEED_EPSILON = 0.001f;
    private static final String DEFAULT_DEATH_SOUND_PATH = "sounds/mixkit-arcade-game-explosion-2759.wav";
    ///////////////////////////////////////////////////////////////////////////////////////////////
    
    // Configurable properties (these can remain static as they're defaults)
    private static int health = DEFAULT_HEALTH;
    private static int damage = DEFAULT_DAMAGE;
    private static DamageTypeConfig resistance = DEFAULT_RESISTANCE;
    private static DamageTypeConfig weakness = DEFAULT_WEAKNESS;
    private static Vector2 speed = new Vector2(DEFAULT_SPEED);
    private static String texturePath = DEFAULT_TEXTURE;
    private static String displayName = DEFAULT_NAME;
    private static float clickRadius = DEFAULT_CLICKRADIUS;
    private static Map<CurrencyType, Integer> currencyDrops = new HashMap<>(DEFAULT_CURRENCY_DROPS);
    private static int points = DEFAULT_POINTS;
    private static String deathSoundPath = DEFAULT_DEATH_SOUND_PATH;

    /**
     * Creates a drone enemy with current configuration.
     *
     * @param waypoints a list of entities that follow the path of the level
     * @param player the player entity reference
     * @return entity
     */
    public static Entity createDroneEnemy(java.util.List<Entity> waypoints, Entity player, Difficulty difficulty) {
        return createDroneEnemy(waypoints, player, difficulty, 0);
    }

    /** Overload: start from specific waypoint index (for save/load resume). */
    public static Entity createDroneEnemy(java.util.List<Entity> waypoints, Entity player, Difficulty difficulty, int startWaypointIndex) {
        int idx = Math.max(0, Math.min(waypoints.size() - 1, startWaypointIndex));
        Entity drone = EnemyFactory.createBaseEnemyAnimated(waypoints.get(idx), new Vector2(speed), waypoints,
        "images/drone_basic_spritesheet.atlas", 0.5f, 0.18f, idx);

        // Add drone-specific waypoint component
        WaypointComponent waypointComponent = new WaypointComponent(waypoints, player, speed);
        waypointComponent.setCurrentWaypointIndex(idx);
        waypointComponent.setCurrentTarget(waypoints.get(idx));
        drone.addComponent(waypointComponent);
        applySpeedModifier(drone, waypointComponent, waypoints.get(idx));

        drone
            .addComponent(new CombatStatsComponent(health * difficulty.getMultiplier(), damage * difficulty.getMultiplier(), resistance, weakness))
            .addComponent(new com.csse3200.game.components.enemy.EnemyTypeComponent("drone"))
            .addComponent(new DeckComponent.EnemyDeckComponent(DEFAULT_NAME, DEFAULT_HEALTH, DEFAULT_DAMAGE, DEFAULT_RESISTANCE, DEFAULT_WEAKNESS, DEFAULT_TEXTURE))
            .addComponent(new clickable(clickRadius))
            .addComponent(new SlowEffectComponent()); // 添加减速特效组件
            CombatStatsComponent combatStats = drone.getComponent(CombatStatsComponent.class);
            if (combatStats != null) combatStats.setIsEnemy(true);


        drone.getEvents().addListener("entityDeath", () -> destroyEnemy(drone));

        // Each drone handles its own waypoint progression
        drone.getEvents().addListener("chaseTaskFinished", () -> {
            WaypointComponent dwc = drone.getComponent(WaypointComponent.class);
            
            if (dwc == null) {
                return;
            }
            
            Entity currentTarget = dwc.getCurrentTarget();
            
            // Check if we've reached the final waypoint
            if (!dwc.hasMoreWaypoints()) {
                
                // If we're far from the final waypoint, keep chasing it
                if (currentTarget != null) {
                    float distanceToTarget = drone.getPosition().dst(currentTarget.getPosition());
                    
                    if (distanceToTarget > 0.5f) {
                        updateChaseTarget(drone, currentTarget);
                        return;
                    }
                }
                
                return;
            }
            
            if (currentTarget != null) {
                float distanceToTarget = drone.getPosition().dst(currentTarget.getPosition());
                
                // If we're far from current waypoint (happens after unpause), 
                // create a new task to continue toward CURRENT waypoint
                if (distanceToTarget > 0.5f) {
                    updateChaseTarget(drone, currentTarget);
                    return;
                }
            }
            
            // We're close to current waypoint, advance to next
            Entity nextTarget = dwc.getNextWaypoint();
            if (nextTarget != null) {
                applySpeedModifier(drone, dwc, nextTarget);
                updateChaseTarget(drone, nextTarget);
            }
        });

        var sz = drone.getScale(); 
        drone.setScale(sz.x * 0.8f, sz.y * 0.8f);

        return drone;
    }

    /**
     * Handles the destruction of a drone enemy.
     *
     * @param entity The drone entity to destroy
     */
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
                player.getEvents().trigger("dropCurrency", currencyDrops);
            }

            // Add points to score
            PlayerScoreComponent psc = player.getComponent(PlayerScoreComponent.class);
            if (psc != null) {
                psc.addPoints(points);
            }

            // Track kill for ranking component
            com.csse3200.game.components.PlayerRankingComponent prc = player.getComponent(com.csse3200.game.components.PlayerRankingComponent.class);
            if (prc != null) {
                prc.addKill();
            }
        }

        playDeathSound(deathSoundPath);
        //Gdx.app.postRunnable(entity::dispose);
        //Eventually add point/score logic here maybe?
    }

    private static void playDeathSound(String soundPath) {
        ServiceLocator.getResourceService()
                .getAsset(soundPath, Sound.class)
                .play(2.0f);
    }

    private static void applySpeedModifier(Entity drone, WaypointComponent waypointComponent, Entity waypoint) {
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
                drone.getEvents().trigger("applySlow");
            } else {
                // 如果是加速区域或正常区域，移除减速特效
                drone.getEvents().trigger("removeSlow");
            }
        } else {
            // 如果没有速度修改器，移除减速特效
            drone.getEvents().trigger("removeSlow");
        }

        if (!waypointComponent.getSpeed().epsilonEquals(desiredSpeed, SPEED_EPSILON)) {
            updateSpeed(drone, desiredSpeed);
        }
    }

    /**
     * Updates the speed of a specific drone.
     *
     * @param drone The drone entity to update
     * @param newSpeed The new speed vector
     */
    public static void updateSpeed(Entity drone, Vector2 newSpeed) {
        WaypointComponent dwc = drone.getComponent(WaypointComponent.class);
        if (dwc != null) {
            dwc.incrementPriorityTaskCount();
            dwc.setSpeed(newSpeed);
            drone.getComponent(AITaskComponent.class).addTask(
                new ChaseTask(dwc.getCurrentTarget(), dwc.getPriorityTaskCount(), 100f, 100f, newSpeed));
        }
    }

    /**
     * Updates the chase target for a specific drone.
     *
     * @param drone The drone entity to update
     * @param newTarget The new target entity
     */
    private static void updateChaseTarget(Entity drone, Entity newTarget) {
        WaypointComponent dwc = drone.getComponent(WaypointComponent.class);
        if (dwc != null) {
            dwc.incrementPriorityTaskCount();
            dwc.setCurrentTarget(newTarget);
            drone.getComponent(AITaskComponent.class).addTask(
                new ChaseTask(newTarget, dwc.getPriorityTaskCount(), 100f, 100f, dwc.getSpeed()));
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
        DroneEnemyFactory.resistance = (resistance != null) ? resistance : DEFAULT_RESISTANCE;
    }
    
    public static void setWeakness(DamageTypeConfig weakness) {
        DroneEnemyFactory.weakness = (weakness != null) ? weakness : DEFAULT_WEAKNESS;
    }
    
    public static void setSpeed(Vector2 speed) {
        if (speed != null) {
            DroneEnemyFactory.speed.set(speed);
        }
    }
    
    public static void setSpeed(float x, float y) {
        DroneEnemyFactory.speed.set(x, y);
    }
    
    public static void setTexturePath(String texturePath) {
        DroneEnemyFactory.texturePath = (texturePath != null && !texturePath.trim().isEmpty()) 
            ? texturePath : DEFAULT_TEXTURE;
    }
    
    public static void setDisplayName(String displayName) {
        DroneEnemyFactory.displayName = (displayName != null && !displayName.trim().isEmpty()) 
            ? displayName : DEFAULT_NAME;
    }
    
    /**
     * Resets all drone configuration to default values.
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

    private DroneEnemyFactory() {
        throw new IllegalStateException("Instantiating static util class");
    }
}

