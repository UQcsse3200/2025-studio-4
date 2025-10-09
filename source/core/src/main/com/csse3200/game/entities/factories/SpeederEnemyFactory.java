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
import com.csse3200.game.components.movement.AccelerateOverTimeComponent;
import com.csse3200.game.components.tasks.ChaseTask;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.DamageTypeConfig;
import com.csse3200.game.utils.Difficulty;
import java.util.Map;
import com.csse3200.game.components.PlayerScoreComponent;

/**
 * Factory to create Speeder enemies.
 * Speeder enemies start slow but accelerate over time, creating urgency to kill them quickly.
 */
public class SpeederEnemyFactory {
    // Default speeder configuration
    // IF YOU WANT TO MAKE A NEW ENEMY, THIS IS THE VARIABLE STUFF YOU CHANGE
    ///////////////////////////////////////////////////////////////////////////////////////////////
    private static final int DEFAULT_HEALTH = 150;  // High health - hard to kill quickly
    private static final int DEFAULT_DAMAGE = 45;   // TRIPLE damage to base (15 * 3)
    private static final DamageTypeConfig DEFAULT_RESISTANCE = DamageTypeConfig.None;
    private static final DamageTypeConfig DEFAULT_WEAKNESS = DamageTypeConfig.Electricity;
    private static final Vector2 DEFAULT_SPEED = new Vector2(0.15f, 0.15f); // Starts VERY VERY slow - creeping
    private static final float DEFAULT_MAX_SPEED = 2.5f; // Gets extremely fast!
    private static final float DEFAULT_ACCELERATION_RATE = 0.06f; // Much slower ramp - builds tension over time
    private static final String DEFAULT_TEXTURE = "images/speedster_enemy.png";
    private static final String DEFAULT_NAME = "Speeder Enemy";
    private static final float DEFAULT_CLICKRADIUS = 1f;  // Bigger click radius for boss
    private static final int DEFAULT_CURRENCY_AMOUNT = 150;  // More reward for tough enemy
    private static final CurrencyType DEFAULT_CURRENCY_TYPE = CurrencyType.METAL_SCRAP;
    private static final int DEFAULT_POINTS = 400;  // Double points for mini-boss
    ///////////////////////////////////////////////////////////////////////////////////////////////

    // Configurable properties
    private static int health = DEFAULT_HEALTH;
    private static int damage = DEFAULT_DAMAGE;
    private static DamageTypeConfig resistance = DEFAULT_RESISTANCE;
    private static DamageTypeConfig weakness = DEFAULT_WEAKNESS;
    private static Vector2 speed = new Vector2(DEFAULT_SPEED);
    private static float maxSpeed = DEFAULT_MAX_SPEED;
    private static float accelerationRate = DEFAULT_ACCELERATION_RATE;
    private static String texturePath = DEFAULT_TEXTURE;
    private static String displayName = DEFAULT_NAME;
    private static float clickRadius = DEFAULT_CLICKRADIUS;
    private static int currencyAmount = DEFAULT_CURRENCY_AMOUNT;
    private static CurrencyType currencyType = DEFAULT_CURRENCY_TYPE;
    private static int points = DEFAULT_POINTS;

    /**
     * Creates a speeder enemy with current configuration.
     *
     * @param waypoints List of waypoint entities for the speeder to follow
     * @param player Reference to the player entity
     * @param difficulty Game difficulty setting
     * @return entity
     */
    public static Entity createSpeederEnemy(java.util.List<Entity> waypoints, Entity player, Difficulty difficulty) {
        return createSpeederEnemy(waypoints, player, difficulty, 0);
    }

    /** Overload: start from specific waypoint index (for save/load resume). */
    public static Entity createSpeederEnemy(java.util.List<Entity> waypoints, Entity player, Difficulty difficulty, int startWaypointIndex) {
        int idx = Math.max(0, Math.min(waypoints.size() - 1, startWaypointIndex));

        // Create base enemy
        Entity speeder = EnemyFactory.createBaseEnemyAnimated(waypoints.get(idx), new Vector2(speed), waypoints,
                "images/Speedster_Spritesheet.atlas", 0.1f, 0.18f, idx);

        // Add waypoint component for independent waypoint tracking
        WaypointComponent waypointComponent = new WaypointComponent(waypoints, player, speed);
        waypointComponent.setCurrentWaypointIndex(idx);
        waypointComponent.setCurrentTarget(waypoints.get(idx));
        speeder.addComponent(waypointComponent);

        speeder
                .addComponent(new CombatStatsComponent(health * difficulty.getMultiplier(), damage * difficulty.getMultiplier(), resistance, weakness))
                .addComponent(new com.csse3200.game.components.enemy.EnemyTypeComponent("speeder"))
                .addComponent(new DeckComponent.EnemyDeckComponent(DEFAULT_NAME, DEFAULT_HEALTH, DEFAULT_DAMAGE, DEFAULT_RESISTANCE, DEFAULT_WEAKNESS, DEFAULT_TEXTURE))
                .addComponent(new clickable(clickRadius))
                // KEY COMPONENT: Add acceleration mechanic
                .addComponent(new AccelerateOverTimeComponent(speed.x, maxSpeed, accelerationRate));

        speeder.getEvents().addListener("entityDeath", () -> destroyEnemy(speeder));

        // Handle waypoint progression
        speeder.getEvents().addListener("chaseTaskFinished", () -> {
            WaypointComponent wc = speeder.getComponent(WaypointComponent.class);
            if (wc != null && wc.hasMoreWaypoints()) {
                Entity nextTarget = wc.getNextWaypoint();
                if (nextTarget != null) {
                    updateChaseTarget(speeder, nextTarget);
                }
            }
        });

        var sz = speeder.getScale();
        speeder.setScale(sz.x * 1.2f, sz.y * 1.2f);

        return speeder;
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

        // Drop currency upon defeat
        WaypointComponent wc = entity.getComponent(WaypointComponent.class);
        if (wc != null && wc.getPlayerRef() != null) {
            Entity player = wc.getPlayerRef();
            CurrencyManagerComponent currencyManager = player.getComponent(CurrencyManagerComponent.class);
            if (currencyManager != null) {
                Map<CurrencyType, Integer> drops = Map.of(currencyType, currencyAmount);
                player.getEvents().trigger("dropCurrency", drops);
            }

            // Award points to player upon defeating enemy
            PlayerScoreComponent totalScore = wc.getPlayerRef().getComponent(PlayerScoreComponent.class);
            if (totalScore != null) {
                totalScore.addPoints(points);
            }

            // Track kill for ranking component
            com.csse3200.game.components.PlayerRankingComponent prc = wc.getPlayerRef().getComponent(com.csse3200.game.components.PlayerRankingComponent.class);
            if (prc != null) {
                prc.addKill();
            }
        }

        Gdx.app.postRunnable(entity::dispose);
    }

    private static void updateChaseTarget(Entity speeder, Entity newTarget) {
        WaypointComponent wc = speeder.getComponent(WaypointComponent.class);
        if (wc != null) {
            wc.incrementPriorityTaskCount();
            wc.setCurrentTarget(newTarget);

            // Get current speed from acceleration component
            AccelerateOverTimeComponent accel = speeder.getComponent(AccelerateOverTimeComponent.class);
            float currentSpeed = (accel != null) ? accel.getCurrentSpeed() : speed.x;

            speeder.getComponent(AITaskComponent.class).addTask(
                new ChaseTask(newTarget, wc.getPriorityTaskCount(), 100f, 100f, new Vector2(currentSpeed, currentSpeed)));
        }
    }

    // Getters
    public static int getHealth() {
        return health;
    }

    public static int getDamage() {
        return damage;
    }

    public static DamageTypeConfig getResistance() {
        return resistance;
    }

    public static DamageTypeConfig getWeakness() {
        return weakness;
    }

    public static Vector2 getSpeed() {
        return new Vector2(speed);
    }

    public static float getMaxSpeed() {
        return maxSpeed;
    }

    public static float getAccelerationRate() {
        return accelerationRate;
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
    public static void setHealth(int health) {
        SpeederEnemyFactory.health = (health > 0) ? health : DEFAULT_HEALTH;
    }

    public static void setDamage(int damage) {
        SpeederEnemyFactory.damage = (damage > 0) ? damage : DEFAULT_DAMAGE;
    }

    public static void setResistance(DamageTypeConfig resistance) {
        SpeederEnemyFactory.resistance = (resistance != null) ? resistance : DEFAULT_RESISTANCE;
    }

    public static void setWeakness(DamageTypeConfig weakness) {
        SpeederEnemyFactory.weakness = (weakness != null) ? weakness : DEFAULT_WEAKNESS;
    }

    public static void setSpeed(Vector2 speed) {
        if (speed != null) {
            SpeederEnemyFactory.speed.set(speed);
        }
    }

    public static void setSpeed(float x, float y) {
        SpeederEnemyFactory.speed.set(x, y);
    }

    public static void setMaxSpeed(float maxSpeed) {
        SpeederEnemyFactory.maxSpeed = (maxSpeed > 0) ? maxSpeed : DEFAULT_MAX_SPEED;
    }

    public static void setAccelerationRate(float rate) {
        SpeederEnemyFactory.accelerationRate = (rate > 0) ? rate : DEFAULT_ACCELERATION_RATE;
    }

    public static void setTexturePath(String texturePath) {
        SpeederEnemyFactory.texturePath = (texturePath != null && !texturePath.trim().isEmpty())
                ? texturePath : DEFAULT_TEXTURE;
    }

    public static void setDisplayName(String displayName) {
        SpeederEnemyFactory.displayName = (displayName != null && !displayName.trim().isEmpty())
                ? displayName : DEFAULT_NAME;
    }

    /**
     * Resets all speeder configuration to default values.
     */
    public static void resetToDefaults() {
        health = DEFAULT_HEALTH;
        damage = DEFAULT_DAMAGE;
        resistance = DEFAULT_RESISTANCE;
        weakness = DEFAULT_WEAKNESS;
        speed.set(DEFAULT_SPEED);
        maxSpeed = DEFAULT_MAX_SPEED;
        accelerationRate = DEFAULT_ACCELERATION_RATE;
        texturePath = DEFAULT_TEXTURE;
        displayName = DEFAULT_NAME;
        points = DEFAULT_POINTS;
    }

    private SpeederEnemyFactory() {
        throw new IllegalStateException("Instantiating static util class");
    }
}
