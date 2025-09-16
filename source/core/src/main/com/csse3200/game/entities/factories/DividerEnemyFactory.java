package com.csse3200.game.entities.factories;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.AITaskComponent;
import com.csse3200.game.areas.ForestGameArea;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.enemy.clickable;
import com.csse3200.game.components.tasks.ChaseTask;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.DamageTypeConfig;

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

    // 可配置项
    private static int health = DEFAULT_HEALTH;
    private static int damage = DEFAULT_DAMAGE;
    private static DamageTypeConfig resistance = DEFAULT_RESISTANCE;
    private static DamageTypeConfig weakness = DEFAULT_WEAKNESS;
    private static Vector2 speed = new Vector2(DEFAULT_SPEED);
    private static String texturePath = DEFAULT_TEXTURE;
    private static String displayName = DEFAULT_NAME;
    private static float clickRadius = DEFAULT_CLICKRADIUS;

    private static int priorityTaskCount = 1;

    private DividerEnemyFactory() {
        throw new IllegalStateException("Instantiating static util class");
    }

    /**
     * 创建可分裂敌人。
     * @param target 追击目标（可为 null）
     * @param area   游戏区域（用于 spawn 子体）
     */
    public static Entity createDividerEnemy(Entity target, GameArea area) {
        Entity divider = EnemyFactory.createBaseEnemyAnimated(
                target, new Vector2(speed),
                "images/divider_enemy_spritesheet.atlas", 0.5f, 0.18f);

        divider
                .addComponent(new CombatStatsComponent(health, damage, resistance, weakness))
                .addComponent(new clickable(clickRadius));

        // ⚠️ 监听死亡：用闭包把 divider/target/area 捕获进去，避免 static 共享状态
        divider.getEvents().addListener("entityDeath", () -> destroyEnemy(divider, target, area));

        var sz = divider.getScale();
        divider.setScale(sz.x * 1.5f, sz.y * 1.5f);
        return divider;
    }

    /** 敌人死亡：统一延迟执行“销毁 + 分裂 + 计数” */
    private static void destroyEnemy(Entity parent, Entity target, GameArea area) {
        if (parent == null) return;

        // 先缓存位置（下一帧 parent 可能已销毁）
        final Vector2 pos = parent.getPosition().cpy();
        final Vector2[] offsets = new Vector2[]{
                new Vector2(+0.3f, 0f),
                new Vector2(-0.3f, 0f),
                new Vector2(0f, +0.3f)
        };

        Gdx.app.postRunnable(() -> {
            // 1) 直接销毁父体（没有 isDisposed() 就不判断，通常是幂等的）
            parent.dispose();

            // 2) 生成 3 个子体
            if (area != null) {
                // 目标可能为 null，子体/AI 内部要能处理 target == null 的情况
                final Entity safeTarget = target;  // 允许为 null
                for (Vector2 off : offsets) {
                    Entity child = DividerChildEnemyFactory.createDividerChildChildEnemy(safeTarget);
                    area.customSpawnEntityAt(child, pos.cpy().add(off));
                }
            }

            // 3) 计数
            ForestGameArea.NUM_ENEMIES_DEFEATED += 1;
            ForestGameArea.checkEnemyCount();
        });

    }

    /** 可选：在运行时调整速度（示例保留） */
    @SuppressWarnings("unused")
    private static void updateSpeed(Entity self, Entity target, Vector2 newSpeed) {
        priorityTaskCount += 1;
        self.getComponent(AITaskComponent.class)
                .addTask(new ChaseTask(target, priorityTaskCount, 100f, 100f, newSpeed));
    }

    // Getters / Setters / Reset（与原版一致，略微收敛空值）
    public static DamageTypeConfig getResistance() { return resistance; }
    public static DamageTypeConfig getWeakness() { return weakness; }
    public static Vector2 getSpeed() { return new Vector2(speed); }
    public static String getTexturePath() { return texturePath; }
    public static String getDisplayName() { return displayName; }

    public static void setResistance(DamageTypeConfig r) { resistance = (r != null) ? r : DEFAULT_RESISTANCE; }
    public static void setWeakness(DamageTypeConfig w) { weakness = (w != null) ? w : DEFAULT_WEAKNESS; }
    public static void setSpeed(Vector2 s) { if (s != null) speed.set(s); }
    public static void setSpeed(float x, float y) { speed.set(x, y); }
    public static void setTexturePath(String p) { texturePath = (p != null && !p.trim().isEmpty()) ? p : DEFAULT_TEXTURE; }
    public static void setDisplayName(String n) { displayName = (n != null && !n.trim().isEmpty()) ? n : DEFAULT_NAME; }

    public static void resetToDefaults() {
        health = DEFAULT_HEALTH;
        damage = DEFAULT_DAMAGE;
        resistance = DEFAULT_RESISTANCE;
        weakness = DEFAULT_WEAKNESS;
        speed.set(DEFAULT_SPEED);
        texturePath = DEFAULT_TEXTURE;
        displayName = DEFAULT_NAME;
    }
}
