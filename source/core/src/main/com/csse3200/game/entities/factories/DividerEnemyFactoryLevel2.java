package com.csse3200.game.entities.factories;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.AITaskComponent;
import com.csse3200.game.areas.ForestGameArea;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.areas2.MapTwo.GameArea2;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.currencysystem.CurrencyComponent.CurrencyType;
import com.csse3200.game.components.currencysystem.CurrencyManagerComponent;
import com.csse3200.game.components.deck.DeckComponent;
import com.csse3200.game.components.enemy.SpeedWaypointComponent;
import com.csse3200.game.components.enemy.WaypointComponent;
import com.csse3200.game.components.enemy.clickable;
import com.csse3200.game.components.tasks.ChaseTask;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.DamageTypeConfig;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.utils.Difficulty;

import java.util.HashMap;
import java.util.Map;
import com.csse3200.game.components.PlayerScoreComponent;


public class DividerEnemyFactoryLevel2 {
    // Default divider configuration
    // IF YOU WANT TO MAKE A NEW ENEMY, THIS IS THE VARIABLE STUFF YOU CHANGE
    ///////////////////////////////////////////////////////////////////////////////////////////////
    private static final int DEFAULT_HEALTH = 150;
    private static final int DEFAULT_DAMAGE = 5;
    private static final DamageTypeConfig DEFAULT_RESISTANCE = DamageTypeConfig.Electricity;
    private static final DamageTypeConfig DEFAULT_WEAKNESS = DamageTypeConfig.None;
    private static final Vector2 DEFAULT_SPEED = new Vector2(0.75f, 0.75f);
    private static final String DEFAULT_TEXTURE = "images/divider_enemy.png";
    private static final String DEFAULT_NAME = "Divider Enemy";
    private static final float DEFAULT_CLICKRADIUS = 0.7f;
    private static final Map<CurrencyType, Integer> DEFAULT_CURRENCY_DROPS = Map.of(
    CurrencyType.METAL_SCRAP, 50,
    CurrencyType.TITANIUM_CORE, 25,
    CurrencyType.NEUROCHIP, 15
    );
    private static final int DEFAULT_POINTS = 300;
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
    private static String deathSoundPath = DEFAULT_DEATH_SOUND_PATH;

    private static int priorityTaskCount = 1;
    private static java.util.List<Entity> savedWaypoints;
    private static int currentWaypointIndex = 0;
    private static Difficulty difficulty = Difficulty.MEDIUM;

    private DividerEnemyFactoryLevel2() {
        throw new IllegalStateException("Instantiating static util class");
    }


    public static Entity createDividerEnemy(java.util.List<Entity> waypoints, GameArea2 area, Entity player, Difficulty difficulty) {
        Entity divider = EnemyFactory.createBaseEnemyAnimated(waypoints.get(currentWaypointIndex), new Vector2(speed), waypoints,
                "images/divider_enemy_spritesheet.atlas", 0.5f, 0.18f, 0);

        WaypointComponent waypointComponent = new WaypointComponent(waypoints, player, speed);
        divider.addComponent(waypointComponent);

        setDifficulty(difficulty);

        divider
                .addComponent(new CombatStatsComponent(health * difficulty.getMultiplier(), damage * difficulty.getMultiplier(), resistance, weakness))
                .addComponent(new DeckComponent.EnemyDeckComponent(DEFAULT_NAME, DEFAULT_HEALTH, DEFAULT_DAMAGE, DEFAULT_RESISTANCE, DEFAULT_WEAKNESS, DEFAULT_TEXTURE))
                .addComponent(new clickable(clickRadius))
                .addComponent(new com.csse3200.game.components.ReachedBaseComponent()); // ADD THIS LINE
                
        CombatStatsComponent combatStats = divider.getComponent(CombatStatsComponent.class);
        if (combatStats != null) combatStats.setIsEnemy(true);

        divider.getEvents().addListener("entityDeath", () -> destroyEnemy(divider, player, area));


        // Each divider handles its own waypoint progression
        divider.getEvents().addListener("chaseTaskFinished", () -> {
            WaypointComponent dwc = divider.getComponent(WaypointComponent.class);
            
            if (dwc == null) {
                return;
            }
            
            Entity currentTarget = dwc.getCurrentTarget();
            
            // Check if we've reached the final waypoint
            if (!dwc.hasMoreWaypoints()) {
                
                // If we're far from the final waypoint, keep chasing it
                if (currentTarget != null) {
                    float distanceToTarget = divider.getPosition().dst(currentTarget.getPosition());
                    
                    if (distanceToTarget > 0.5f) {
                        updateChaseTarget(divider, currentTarget);
                        return;
                    }
                }
                
                return;
            }
            
            if (currentTarget != null) {
                float distanceToTarget = divider.getPosition().dst(currentTarget.getPosition());
                
                // If we're far from current waypoint (happens after unpause), 
                // create a new task to continue toward CURRENT waypoint
                if (distanceToTarget > 0.5f) {
                    updateChaseTarget(divider, currentTarget);
                    return;
                }
            }
            
            // We're close to current waypoint, advance to next
            Entity nextTarget = dwc.getNextWaypoint();
            if (nextTarget != null) {
                applySpeedModifier(divider, dwc, nextTarget);
                updateChaseTarget(divider, nextTarget);
            }
        });

        var sz = divider.getScale();
        divider.setScale(sz.x * 1.5f, sz.y * 1.5f);

        savedWaypoints = waypoints;

        return divider;
    }

    private static void applySpeedModifier(Entity divider, WaypointComponent waypointComponent, Entity waypoint) {
        if (waypointComponent == null || waypoint == null) {
            return;
        }

        SpeedWaypointComponent speedMarker = waypoint.getComponent(SpeedWaypointComponent.class);
        Vector2 desiredSpeed = waypointComponent.getBaseSpeed();
        if (speedMarker != null) {
            desiredSpeed.scl(speedMarker.getSpeedMultiplier());
        }

        if (!waypointComponent.getSpeed().epsilonEquals(desiredSpeed, SPEED_EPSILON)) {
            updateSpeed(divider, desiredSpeed);
        }
    }

    /**
     * Updates the speed of a specific divider.
     *
     * @param divider The divider entity to update
     * @param newSpeed The new speed vector
     */
    private static void updateSpeed(Entity divider, Vector2 newSpeed) {
        WaypointComponent dwc = divider.getComponent(WaypointComponent.class);
        if (dwc != null) {
            dwc.incrementPriorityTaskCount();
            dwc.setSpeed(newSpeed);
            divider.getComponent(AITaskComponent.class).addTask(
                new ChaseTask(dwc.getCurrentTarget(), dwc.getPriorityTaskCount(), 100f, 100f, newSpeed));
        }
    }

    /** Enemy death: unified delayed execution for "destroy + split + count" */
    private static void destroyEnemy(Entity entity, Entity target, GameArea2 area) {
        if (entity == null) return;

        // Check which game area is active and use its counters
        if (com.csse3200.game.areas2.MapTwo.ForestGameArea2.currentGameArea != null) {
            com.csse3200.game.areas2.MapTwo.ForestGameArea2.NUM_ENEMIES_DEFEATED += 1;
            com.csse3200.game.areas2.MapTwo.ForestGameArea2.checkEnemyCount();
        } else {
            ForestGameArea.NUM_ENEMIES_DEFEATED += 1;
            ForestGameArea.checkEnemyCount();
        }

        // Check if enemy reached the base (don't reward if they did)
        com.csse3200.game.components.ReachedBaseComponent reachedBaseComp = 
            entity.getComponent(com.csse3200.game.components.ReachedBaseComponent.class);
        boolean reachedBase = (reachedBaseComp != null && reachedBaseComp.hasReachedBase());
        
        Gdx.app.log("CURRENCY", "Divider " + entity.getId() + " died. ReachedBase: " + reachedBase);

        // Only award points and currency if the enemy was killed by the player (not at base)
        if (!reachedBase) {
            Gdx.app.log("CURRENCY", "Divider " + entity.getId() + " - DROPPING REWARDS (killed by player)");
            
            WaypointComponent wcForScore = entity.getComponent(WaypointComponent.class);
            if (wcForScore != null) {
                Entity player = wcForScore.getPlayerRef();
                if (player != null) {
                    // Award points
                    PlayerScoreComponent psc = player.getComponent(PlayerScoreComponent.class);
                    if (psc != null) {
                        psc.addPoints(points);
                    }

                    // Track kill for ranking component
                    com.csse3200.game.components.PlayerRankingComponent prc = 
                        player.getComponent(com.csse3200.game.components.PlayerRankingComponent.class);
                    if (prc != null) {
                        prc.addKill();
                    }

                    // Drop currency
                    CurrencyManagerComponent currencyManager = player.getComponent(CurrencyManagerComponent.class);
                    if (currencyManager != null) {
                        player.getEvents().trigger("dropCurrency", currencyDrops);
                    }
                }
            }
        } else {
            Gdx.app.log("CURRENCY", "Divider " + entity.getId() + " - NO REWARDS (reached base)");
        }

        // Play death sound regardless
        playDeathSound(deathSoundPath);

        // Store position and waypoint info before disposal
        final Vector2 pos = entity.getPosition().cpy();
        final Vector2[] offsets = new Vector2[]{
                new Vector2(+0.3f, 0f),
                new Vector2(-0.3f, 0f),
                new Vector2(0f, +0.3f)
        };

        WaypointComponent wc = entity.getComponent(WaypointComponent.class);
        
        // Clamp waypoint index to valid range
        int targetWaypointIndex = 0;
        if (wc != null && savedWaypoints != null && !savedWaypoints.isEmpty()) {
            int currentIdx = wc.getCurrentWaypointIndex();
            // Clamp to valid range: [0, savedWaypoints.size() - 1]
            targetWaypointIndex = Math.max(0, Math.min(savedWaypoints.size() - 1, currentIdx - 1));
        }
        
        final int finalTargetWaypointIndex = targetWaypointIndex;

        // Dispose and spawn children
        Gdx.app.postRunnable(() -> {
            // Dispose of the parent entity
            entity.dispose();

            // Spawn child entities with waypoints
            if (area != null && savedWaypoints != null && !savedWaypoints.isEmpty()) {
                for (Vector2 offset : offsets) {
                    Entity child = DividerChildEnemyFactory.createDividerChildEnemy(
                            target, savedWaypoints, finalTargetWaypointIndex, difficulty
                    );
                    if (child != null) {
                        area.customSpawnEntityAt(child, pos.cpy().add(offset));
                    }
                }
            }
        });
    }

    private static void playDeathSound(String soundPath) {
        ServiceLocator.getResourceService()
                .getAsset(soundPath, Sound.class)
                .play(2.0f);
    }

    /** Optional: adjust speed at runtime (example retained) */
    @SuppressWarnings("unused")
    private static void updateSpeed(Entity self, Entity target, Vector2 newSpeed) {
        priorityTaskCount += 1;
        self.getComponent(AITaskComponent.class)
                .addTask(new ChaseTask(target, priorityTaskCount, 100f, 100f, newSpeed));
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

    // Getters / Setters / Reset
    public static DamageTypeConfig getResistance() { return resistance; }
    public static DamageTypeConfig getWeakness() { return weakness; }
    public static Vector2 getSpeed() { return new Vector2(speed); }
    public static String getTexturePath() { return texturePath; }
    public static String getDisplayName() { return displayName; }
    public static Difficulty getDifficulty() { return difficulty; }
    public static int getPoints() { return points; }

    public static void setResistance(DamageTypeConfig r) { resistance = (r != null) ? r : DEFAULT_RESISTANCE; }
    public static void setWeakness(DamageTypeConfig w) { weakness = (w != null) ? w : DEFAULT_WEAKNESS; }
    public static void setSpeed(Vector2 s) { if (s != null) speed.set(s); }
    public static void setSpeed(float x, float y) { speed.set(x, y); }
    public static void setTexturePath(String p) { texturePath = (p != null && !p.trim().isEmpty()) ? p : DEFAULT_TEXTURE; }
    public static void setDisplayName(String n) { displayName = (n != null && !n.trim().isEmpty()) ? n : DEFAULT_NAME; }
    public static void setDifficulty(Difficulty d) { if (d != null) difficulty = d; }

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
}