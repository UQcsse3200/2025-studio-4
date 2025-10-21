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
import com.csse3200.game.components.enemy.SpeedWaypointComponent;
import com.csse3200.game.components.enemy.WaypointComponent;
import com.csse3200.game.components.tasks.ChaseTask;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.DamageTypeConfig;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.utils.Difficulty;

import java.util.HashMap;
import java.util.Map;
import com.csse3200.game.components.PlayerScoreComponent;

public class DividerChildEnemyFactory {
    // Default divider child configuration
    // IF YOU WANT TO MAKE A NEW ENEMY, THIS IS THE VARIABLE STUFF YOU CHANGE
    ///////////////////////////////////////////////////////////////////////////////////////////////
    private static final int DEFAULT_HEALTH = 50;
    private static final int DEFAULT_DAMAGE = 20;
    private static final DamageTypeConfig DEFAULT_RESISTANCE = DamageTypeConfig.None;
    private static final DamageTypeConfig DEFAULT_WEAKNESS = DamageTypeConfig.None;
    private static final Vector2 DEFAULT_SPEED = new Vector2(1.5f, 1.5f);
    private static final String DEFAULT_TEXTURE = "images/divider_enemy.png";
    private static final String DEFAULT_NAME = "Divider Child Enemy";
    private static final float DEFAULT_CLICKRADIUS = 0.3f;
    private static final Map<CurrencyType, Integer> DEFAULT_CURRENCY_DROPS = Map.of(
    CurrencyType.METAL_SCRAP, 15,
    CurrencyType.TITANIUM_CORE, 10,
    CurrencyType.NEUROCHIP, 5
    );
    private static final int DEFAULT_POINTS = 100;
    private static final float SPEED_EPSILON = 0.001f;
    private static final String DEFAULT_DEATH_SOUND_PATH = "sounds/mixkit-arcade-game-explosion-2759.wav";
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
    private static Map<CurrencyType, Integer> currencyDrops = new HashMap<>(DEFAULT_CURRENCY_DROPS);
    private static int points = DEFAULT_POINTS;
    private static Entity self;
    private static Entity currentTarget;
    private static int priorityTaskCount = 1;
    private static String deathSoundPath = DEFAULT_DEATH_SOUND_PATH;

    /**
     * Creates a DividerChild enemy with current configuration.
     *
     * @param target entity to chase
     * @return entity
     */
    public static Entity createDividerChildEnemy(Entity target, java.util.List<Entity> waypoints, int waypointIndex, Difficulty difficulty) {
        Entity DividerChild = EnemyFactory.createBaseEnemyAnimated(waypoints.get(waypointIndex), new Vector2(speed), waypoints,
        "images/divider_enemy_spritesheet.atlas", 0.5f, 0.18f, waypointIndex);

        WaypointComponent waypointComponent = new WaypointComponent(waypoints, target, speed);
        waypointComponent.setCurrentWaypointIndex(waypointIndex);
        DividerChild.addComponent(waypointComponent);

        DividerChild
            .addComponent(new CombatStatsComponent(health, damage, resistance, weakness))
            .addComponent(new com.csse3200.game.components.enemy.EnemyTypeComponent("divider_child"))
            .addComponent(new DeckComponent.EnemyDeckComponent(DEFAULT_NAME, DEFAULT_HEALTH, DEFAULT_DAMAGE, DEFAULT_RESISTANCE, DEFAULT_WEAKNESS, DEFAULT_TEXTURE))
            .addComponent(new clickable(clickRadius))
            .addComponent(new com.csse3200.game.components.ReachedBaseComponent());
            CombatStatsComponent combatStats = DividerChild.getComponent(CombatStatsComponent.class);
            if (combatStats != null) combatStats.setIsEnemy(true);


        DividerChild.getEvents().addListener("entityDeath", () -> destroyEnemy(DividerChild));

        // Each DividerChild handles its own waypoint progression
        DividerChild.getEvents().addListener("chaseTaskFinished", () -> {
            WaypointComponent dwc = DividerChild.getComponent(WaypointComponent.class);
            
            if (dwc == null) {
                return;
            }
            
            Entity currentTarget = dwc.getCurrentTarget();
            
            // Check if we've reached the final waypoint
            if (!dwc.hasMoreWaypoints()) {
                
                // If we're far from the final waypoint, keep chasing it
                if (currentTarget != null) {
                    float distanceToTarget = DividerChild.getPosition().dst(currentTarget.getPosition());
                    
                    if (distanceToTarget > 0.5f) {
                        updateChaseTarget(DividerChild, currentTarget);
                        return;
                    }
                }
                
                return;
            }
            
            if (currentTarget != null) {
                float distanceToTarget = DividerChild.getPosition().dst(currentTarget.getPosition());
                
                // If we're far from current waypoint (happens after unpause), 
                // create a new task to continue toward CURRENT waypoint
                if (distanceToTarget > 0.5f) {
                    updateChaseTarget(DividerChild, currentTarget);
                    return;
                }
            }
            
            // We're close to current waypoint, advance to next
            Entity nextTarget = dwc.getNextWaypoint();
            if (nextTarget != null) {
                applySpeedModifier(DividerChild, dwc, nextTarget);
                updateChaseTarget(DividerChild, nextTarget);
            }
        });

        var sz = DividerChild.getScale(); 
        DividerChild.setScale(sz.x * 0.85f, sz.y * 0.85f);

        updateChaseTarget(DividerChild, waypoints.get(waypointIndex));

        self = DividerChild;
        currentTarget = target;

        return DividerChild;
    }

    private static void applySpeedModifier(Entity DividerChild, WaypointComponent waypointComponent, Entity waypoint) {
        if (waypointComponent == null || waypoint == null) {
            return;
        }

        SpeedWaypointComponent speedMarker = waypoint.getComponent(SpeedWaypointComponent.class);
        Vector2 desiredSpeed = waypointComponent.getBaseSpeed();
        if (speedMarker != null) {
            desiredSpeed.scl(speedMarker.getSpeedMultiplier());
        }

        if (!waypointComponent.getSpeed().epsilonEquals(desiredSpeed, SPEED_EPSILON)) {
            updateSpeed(DividerChild, desiredSpeed);
        }
    }

    private static void destroyEnemy(Entity entity) {
        // Check which game area is active and use its counters
        if (com.csse3200.game.areas2.MapTwo.ForestGameArea2.currentGameArea != null) {
            com.csse3200.game.areas2.MapTwo.ForestGameArea2.NUM_ENEMIES_DEFEATED += 1;
            com.csse3200.game.areas2.MapTwo.ForestGameArea2.checkEnemyCount();
        } else {
            ForestGameArea.NUM_ENEMIES_DEFEATED += 1;
            ForestGameArea.checkEnemyCount();
        }

        // Check if enemy reached the base
        com.csse3200.game.components.ReachedBaseComponent reachedBaseComp = 
            entity.getComponent(com.csse3200.game.components.ReachedBaseComponent.class);
        boolean reachedBase = (reachedBaseComp != null && reachedBaseComp.hasReachedBase());
        
        //Gdx.app.log("CURRENCY", "Enemy " + entity.getId() + " died. ReachedBase: " + reachedBase);
        
        if (!reachedBase) {
            //Gdx.app.log("CURRENCY", "Enemy " + entity.getId() + " - DROPPING CURRENCY (killed by player)");
            
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
        } else {
            //Gdx.app.log("CURRENCY", "Enemy " + entity.getId() + " - NO CURRENCY (reached base)");
        }

        playDeathSound(deathSoundPath);
    }

    private static void playDeathSound(String soundPath) {
        ServiceLocator.getResourceService()
                .getAsset(soundPath, Sound.class)
                .play(2.0f);
    }

    /**
     * Updates the speed of a specific DividerChild.
     *
     * @param DividerChild The DividerChild entity to update
     * @param newSpeed The new speed vector
     */
    private static void updateSpeed(Entity DividerChild, Vector2 newSpeed) {
        WaypointComponent dwc = DividerChild.getComponent(WaypointComponent.class);
        if (dwc != null) {
            dwc.incrementPriorityTaskCount();
            dwc.setSpeed(newSpeed);
            DividerChild.getComponent(AITaskComponent.class).addTask(
                new ChaseTask(dwc.getCurrentTarget(), dwc.getPriorityTaskCount(), 100f, 100f, newSpeed));
        }
    }

    private static void updateChaseTarget(Entity entity, Entity newTarget) {
        WaypointComponent wc = entity.getComponent(WaypointComponent.class);
        if (wc != null) {
            wc.incrementPriorityTaskCount();
            wc.setCurrentTarget(newTarget);
            entity.getComponent(AITaskComponent.class).addTask(
                    new ChaseTask(newTarget, wc.getPriorityTaskCount(), 100f, 100f, wc.getSpeed())
            );
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
        DividerChildEnemyFactory.resistance = (resistance != null) ? resistance : DEFAULT_RESISTANCE;
    }
    
    public static void setWeakness(DamageTypeConfig weakness) {
        DividerChildEnemyFactory.weakness = (weakness != null) ? weakness : DEFAULT_WEAKNESS;
    }
    
    public static void setSpeed(Vector2 speed) {
        if (speed != null) {
            DividerChildEnemyFactory.speed.set(speed);
        }
    }
    
    public static void setSpeed(float x, float y) {
        DividerChildEnemyFactory.speed.set(x, y);
    }
    
    public static void setTexturePath(String texturePath) {
        DividerChildEnemyFactory.texturePath = (texturePath != null && !texturePath.trim().isEmpty()) 
            ? texturePath : DEFAULT_TEXTURE;
    }
    
    public static void setDisplayName(String displayName) {
        DividerChildEnemyFactory.displayName = (displayName != null && !displayName.trim().isEmpty()) 
            ? displayName : DEFAULT_NAME;
    }
    
    /**
     * Resets all DividerChild configuration to default values.
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

    private DividerChildEnemyFactory() {
        throw new IllegalStateException("Instantiating static util class");
    }
}