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
import com.csse3200.game.components.tasks.ChaseTask;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.DamageTypeConfig;
import com.csse3200.game.utils.Difficulty;
import java.util.Map;
import com.csse3200.game.components.PlayerScoreComponent;


public class BossEnemyFactory {
    // Default boss configuration
    // IF YOU WANT TO MAKE A NEW ENEMY, THIS IS THE VARIABLE STUFF YOU CHANGE
    ///////////////////////////////////////////////////////////////////////////////////////////////
    private static final int DEFAULT_HEALTH = 300;
    private static final int DEFAULT_DAMAGE = 20;
    private static final DamageTypeConfig DEFAULT_RESISTANCE = DamageTypeConfig.None;
    private static final DamageTypeConfig DEFAULT_WEAKNESS = DamageTypeConfig.None;
    private static final Vector2 DEFAULT_SPEED = new Vector2(0.5f, 0.5f);
    private static final String DEFAULT_TEXTURE = "images/boss_enemy.png";
    private static final String DEFAULT_NAME = "Boss Enemy";
    private static final float DEFAULT_CLICKRADIUS = 1.2f;
    private static final int DEFAULT_CURRENCY_AMOUNT = 10;
    private static final CurrencyType DEFAULT_CURRENCY_TYPE = CurrencyType.NEUROCHIP;
    private static final int DEFAULT_POINTS = 600;
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
     * Creates a boss enemy with current configuration.
     *
     * @param waypoints List of waypoint entities for the boss to follow
     * @param player Reference to the player entity
     * @return entity
     */
    public static Entity createBossEnemy(java.util.List<Entity> waypoints, Entity player, Difficulty difficulty) {
        return createBossEnemy(waypoints, player, difficulty, 0);
    }

    /** Overload: start from specific waypoint index (for save/load resume). */
    public static Entity createBossEnemy(java.util.List<Entity> waypoints, Entity player, Difficulty difficulty, int startWaypointIndex) {
        int idx = Math.max(0, Math.min(waypoints.size() - 1, startWaypointIndex));
        Entity boss = EnemyFactory.createBaseEnemyAnimated(waypoints.get(idx), new Vector2(speed), waypoints,
        "images/boss_basic_spritesheet.atlas", 0.5f, 0.18f, idx);

        // Add waypoint component for independent waypoint tracking
        WaypointComponent waypointComponent = new WaypointComponent(waypoints, player, speed);
        waypointComponent.setCurrentWaypointIndex(idx);
        waypointComponent.setCurrentTarget(waypoints.get(idx));
        boss.addComponent(waypointComponent);

        boss
                .addComponent(new CombatStatsComponent(health * difficulty.getMultiplier(), damage * difficulty.getMultiplier(), resistance, weakness))
                .addComponent(new com.csse3200.game.components.enemy.EnemyTypeComponent("boss"))
                .addComponent(new DeckComponent.EnemyDeckComponent(DEFAULT_NAME, DEFAULT_HEALTH, DEFAULT_DAMAGE, DEFAULT_RESISTANCE, DEFAULT_WEAKNESS, DEFAULT_TEXTURE))
                .addComponent(new clickable(clickRadius));

        boss.getEvents().addListener("entityDeath", () -> destroyEnemy(boss));

        // Handle waypoint progression for this specific boss
        boss.getEvents().addListener("chaseTaskFinished", () -> {
            WaypointComponent wc = boss.getComponent(WaypointComponent.class);
            if (wc != null && wc.hasMoreWaypoints()) {
                Entity nextTarget = wc.getNextWaypoint();
                if (nextTarget != null) {
                    updateChaseTarget(boss, nextTarget);
                }
            }
        });

        // Set custom boss size
        var sz = boss.getScale(); 
        boss.setScale(sz.x * 1.8f, sz.y * 1.8f);

        return boss;
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
        //Eventually add point/score logic here maybe?
    }

    private static void updateSpeed(Entity boss, Vector2 newSpeed) {
        WaypointComponent wc = boss.getComponent(WaypointComponent.class);
        if (wc != null) {
            wc.incrementPriorityTaskCount();
            wc.setSpeed(newSpeed);
            boss.getComponent(AITaskComponent.class).addTask(
                new ChaseTask(wc.getCurrentTarget(), wc.getPriorityTaskCount(), 100f, 100f, newSpeed));
        }
    }

    private static void updateChaseTarget(Entity boss, Entity newTarget) {
        WaypointComponent wc = boss.getComponent(WaypointComponent.class);
        if (wc != null) {
            wc.incrementPriorityTaskCount();
            wc.setCurrentTarget(newTarget);
            boss.getComponent(AITaskComponent.class).addTask(
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
        BossEnemyFactory.resistance = (resistance != null) ? resistance : DEFAULT_RESISTANCE;
    }

    public static void setWeakness(DamageTypeConfig weakness) {
        BossEnemyFactory.weakness = (weakness != null) ? weakness : DEFAULT_WEAKNESS;
    }

    public static void setSpeed(Vector2 speed) {
        if (speed != null) {
            BossEnemyFactory.speed.set(speed);
        }
    }

    public static void setSpeed(float x, float y) {
        BossEnemyFactory.speed.set(x, y);
    }

    public static void setTexturePath(String texturePath) {
        BossEnemyFactory.texturePath = (texturePath != null && !texturePath.trim().isEmpty())
                ? texturePath : DEFAULT_TEXTURE;
    }

    public static void setDisplayName(String displayName) {
        BossEnemyFactory.displayName = (displayName != null && !displayName.trim().isEmpty())
                ? displayName : DEFAULT_NAME;
    }

    /**
     * Resets all boss configuration to default values.
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

    private BossEnemyFactory() {
        throw new IllegalStateException("Instantiating static util class");
    }

}