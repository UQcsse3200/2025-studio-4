package com.csse3200.game.entities.factories;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.AITaskComponent;
import com.csse3200.game.areas.ForestGameArea;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.currencysystem.CurrencyComponent.CurrencyType;
import com.csse3200.game.components.currencysystem.CurrencyManagerComponent;
import com.csse3200.game.components.enemy.WaypointComponent;
import com.csse3200.game.components.enemy.clickable;
import com.csse3200.game.components.tasks.ChaseTask;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.DamageTypeConfig;
import com.csse3200.game.utils.Difficulty;
import java.util.Map;
import com.csse3200.game.components.PlayerScoreComponent;

/**
 * 可分裂敌人（死亡后生成 3 个子体）的工厂。
 * 关键点：所有“销毁父体 + 生成子体 + 更新计数”的操作统一放入 postRunnable，
 * 避开 Box2D Step 锁，防止 IsLocked() 断言崩溃。
 */
public class DividerEnemyFactory {
    // 默认配置（按需调整）
    private static final int DEFAULT_HEALTH = 150;
    private static final int DEFAULT_DAMAGE = 5;
    private static final DamageTypeConfig DEFAULT_RESISTANCE = DamageTypeConfig.None;
    private static final DamageTypeConfig DEFAULT_WEAKNESS = DamageTypeConfig.None;
    private static final Vector2 DEFAULT_SPEED = new Vector2(0.75f, 0.75f);
    private static final String DEFAULT_TEXTURE = "images/divider_enemy.png";
    private static final String DEFAULT_NAME = "Divider Enemy";
    private static final float DEFAULT_CLICKRADIUS = 0.7f;
    private static final int DEFAULT_CURRENCY_AMOUNT = 5;
    private static final CurrencyType DEFAULT_CURRENCY_TYPE = CurrencyType.NEUROCHIP;
<<<<<<< HEAD
    private static final int DEFAULT_POINTS = 200;
    ///////////////////////////////////////////////////////////////////////////////////////////////
    
=======
    private static final int DEFAULT_POINTS = 300;
    ///////////////////////////////////////////////////////////////////////////////////////////////

>>>>>>> origin/main
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

    private static int priorityTaskCount = 1;
    private static java.util.List<Entity> savedWaypoints;
    private static int currentWaypointIndex = 0;
    private static Difficulty difficulty = Difficulty.MEDIUM;

    private DividerEnemyFactory() {
        throw new IllegalStateException("Instantiating static util class");
    }


    public static Entity createDividerEnemy(java.util.List<Entity> waypoints, GameArea area, Entity player, Difficulty difficulty) {
        Entity divider = EnemyFactory.createBaseEnemyAnimated(waypoints.get(currentWaypointIndex), new Vector2(speed), waypoints,
                "images/divider_enemy_spritesheet.atlas", 0.5f, 0.18f, 0);

        WaypointComponent waypointComponent = new WaypointComponent(waypoints, player, speed);
        divider.addComponent(waypointComponent);

        setDifficulty(difficulty);

        divider
                .addComponent(new CombatStatsComponent(health * difficulty.getMultiplier(), damage * difficulty.getMultiplier(), resistance, weakness))
                .addComponent(new clickable(clickRadius));

        // ⚠️ 监听死亡：用闭包把 divider/target/area 捕获进去，避免 static 共享状态
        divider.getEvents().addListener("entityDeath", () -> destroyEnemy(divider, player, area));

        divider.getEvents().addListener("chaseTaskFinished", () -> {
            WaypointComponent wc = divider.getComponent(WaypointComponent.class);
            if (wc != null && wc.hasMoreWaypoints()) {
                Entity nextTarget = wc.getNextWaypoint();
                if (nextTarget != null) {
                    updateChaseTarget(divider, nextTarget);
                }
            }
        });

        var sz = divider.getScale();
        divider.setScale(sz.x * 1.5f, sz.y * 1.5f);

        savedWaypoints = waypoints;

        return divider;
    }

    /** 敌人死亡：统一延迟执行“销毁 + 分裂 + 计数” */
    private static void destroyEnemy(Entity entity, Entity target, GameArea area) {
        if (entity == null) return;

        ForestGameArea.NUM_ENEMIES_DEFEATED += 1;
        ForestGameArea.checkEnemyCount();

        final Vector2 pos = entity.getPosition().cpy();
        final Vector2[] offsets = new Vector2[]{
                new Vector2(+0.3f, 0f),
                new Vector2(-0.3f, 0f),
                new Vector2(0f, +0.3f)
        };

        // Award points to player upon defeating enemy
        WaypointComponent wcForScore = entity.getComponent(WaypointComponent.class);
        if (wcForScore != null) {
            Entity player = wcForScore.getPlayerRef();
            if (player != null) {
                PlayerScoreComponent psc = player.getComponent(PlayerScoreComponent.class);
                if (psc != null) {
                    psc.addPoints(points);
                }
            }
        }

        Gdx.app.postRunnable(() -> {

            WaypointComponent wc = entity.getComponent(WaypointComponent.class);
            // Dispose of the parent entity
            entity.dispose();

            // Spawn child entities with waypoints
            if (area != null && savedWaypoints != null) {
                for (Vector2 offset : offsets) {
                    int targetWaypointIndex = Math.max(0, wc.getCurrentWaypointIndex() - 1);
                    Entity child = DividerChildEnemyFactory.createDividerChildChildEnemy(
                            target, savedWaypoints, targetWaypointIndex, difficulty
                    );
                    if (child != null) {
                        area.customSpawnEntityAt(child, pos.cpy().add(offset));
                    }
                }
            }
        });

        WaypointComponent wc = entity.getComponent(WaypointComponent.class);
        if (wc != null && wc.getPlayerRef() != null) {
            Entity player = wc.getPlayerRef();
            CurrencyManagerComponent currencyManager = player.getComponent(CurrencyManagerComponent.class);
            if (currencyManager != null) {
                Map<CurrencyType, Integer> drops = Map.of(currencyType, currencyAmount);
                player.getEvents().trigger("dropCurrency", drops);
            }
        }
    }

    /** 可选：在运行时调整速度（示例保留） */
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

    // Getters / Setters / Reset（与原版一致，略微收敛空值）
    public static DamageTypeConfig getResistance() { return resistance; }
    public static DamageTypeConfig getWeakness() { return weakness; }
    public static Vector2 getSpeed() { return new Vector2(speed); }
    public static String getTexturePath() { return texturePath; }
    public static String getDisplayName() { return displayName; }
    public static Difficulty getDifficulty() { return difficulty; }
<<<<<<< HEAD
=======
    public static int getPoints() {
        return points;
    }
>>>>>>> origin/main

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