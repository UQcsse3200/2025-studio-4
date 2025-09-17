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
 * Divider Enemy Factory (splits into 3 children on death).
 * Key point: All "destroy parent + spawn children + update counters" actions
 * are deferred with postRunnable to avoid Box2D Step lock (IsLocked() assertion).
 */
public class DividerEnemyFactory {
    // Default config (can be adjusted)
    private static final int DEFAULT_HEALTH = 150;
    private static final int DEFAULT_DAMAGE = 5;
    private static final DamageTypeConfig DEFAULT_RESISTANCE = DamageTypeConfig.None;
    private static final DamageTypeConfig DEFAULT_WEAKNESS = DamageTypeConfig.None;
    private static final Vector2 DEFAULT_SPEED = new Vector2(0.75f, 0.75f);
    private static final String DEFAULT_TEXTURE = "images/divider_enemy.png";
    private static final String DEFAULT_NAME = "Divider Enemy";
    private static final float DEFAULT_CLICKRADIUS = 0.7f;

    // Configurable fields
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
     * Create a divider enemy.
     * @param target chase target (may be null)
     * @param area   game area (used to spawn children)
     */
    public static Entity createDividerEnemy(Entity target, GameArea area) {
        Entity divider = EnemyFactory.createBaseEnemyAnimated(
                target, new Vector2(speed),
                "images/divider_enemy_spritesheet.atlas", 0.5f, 0.18f);

        divider
                .addComponent(new CombatStatsComponent(health, damage, resistance, weakness))
                .addComponent(new clickable(clickRadius));

        // Death listener: capture divider/target/area in closure, avoid static shared state
        divider.getEvents().addListener("entityDeath", () -> destroyEnemy(divider, target, area));

        var sz = divider.getScale();
        divider.setScale(sz.x * 1.5f, sz.y * 1.5f);
        return divider;
    }

    /** Enemy death: defer "destroy + split + counter update" to next frame */
    private static void destroyEnemy(Entity parent, Entity target, GameArea area) {
        if (parent == null) return;

        // Cache position before parent is disposed
        final Vector2 pos = parent.getPosition().cpy();
        final Vector2[] offsets = new Vector2[]{
                new Vector2(+0.3f, 0f),
                new Vector2(-0.3f, 0f),
                new Vector2(0f, +0.3f)
        };

        Gdx.app.postRunnable(() -> {
            // 1) Dispose parent immediately (dispose is usually idempotent)
            parent.dispose();

            // 2) Spawn 3 children
            if (area != null) {
                final Entity safeTarget = target; // may be null, children/AI must handle null target
                for (Vector2 off : offsets) {
                    Entity child = DividerChildEnemyFactory.createDividerChildChildEnemy(safeTarget);
                    area.customSpawnEntityAt(child, pos.cpy().add(off));
                }
            }

            // 3) Update counters
            ForestGameArea.NUM_ENEMIES_DEFEATED += 1;
            ForestGameArea.checkEnemyCount();
        });

    }

    /** Optional: dynamically update speed at runtime */
    @SuppressWarnings("unused")
    private static void updateSpeed(Entity self, Entity target, Vector2 newSpeed) {
        priorityTaskCount += 1;
        self.getComponent(AITaskComponent.class)
                .addTask(new ChaseTask(target, priorityTaskCount, 100f, 100f, newSpeed));
    }

    // Getters / Setters / Reset
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
