package com.csse3200.game.entities.factories;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.AITaskComponent;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.enemy.clickable;
import com.csse3200.game.components.tasks.ChaseTask;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.DamageTypeConfig;
import com.csse3200.game.physics.PhysicsUtils;
import com.csse3200.game.physics.components.ColliderComponent;

/**
 * Factory to create Divider enemies and DividerGroups.
 */
// Default divider configuration
// IF YOU WANT TO MAKE A NEW ENEMY, THIS IS THE VARIABLE STUFF YOU CHANGE
///////////////////////////////////////////////////////////////////////////////////////////////
public class DividerEnemyFactory {

    // Default configuration
    private static final int DEFAULT_HEALTH = 120;
    private static final int DEFAULT_DAMAGE = 5;
    private static final DamageTypeConfig DEFAULT_RESISTANCE = DamageTypeConfig.None;
    private static final DamageTypeConfig DEFAULT_WEAKNESS = DamageTypeConfig.None;
    private static final Vector2 DEFAULT_SPEED = new Vector2(0.7f, 0.7f);
    private static final String DEFAULT_TEXTURE = "images/divider_enemy.png";
    private static final String DEFAULT_NAME = "Boss Enemy";
    private static final float DEFAULT_CLICKRADIUS = 1.2f;
    ///////////////////////////////////////////////////////////////////////////////////////////////
    private static int health = DEFAULT_HEALTH;
    private static int damage = DEFAULT_DAMAGE;
    private static DamageTypeConfig resistance = DEFAULT_RESISTANCE;
    private static DamageTypeConfig weakness = DEFAULT_WEAKNESS;
    private static Vector2 speed = new Vector2(DEFAULT_SPEED);
    private static String texturePath = DEFAULT_TEXTURE;
    private static String displayName = DEFAULT_NAME;
    private static float clickRadius = DEFAULT_CLICKRADIUS;
    private static Entity self;
    private static Entity currentTarget;
    private static int priorityTaskCount = 1;


    public static class DividerGroup {
        public final Entity parent;
        public final Entity mini1;
        public final Entity mini2;

        public DividerGroup(Entity target) {
            parent = createDivider(target, speed.cpy(), true);
            mini1 = createDivider(target, speed.cpy().scl(1.2f), false);
            mini2 = createDivider(target, speed.cpy().scl(1.2f), false);

            hideMini(mini1);
            hideMini(mini2);

            parent.getEvents().addListener("entityDeath", () -> {
                revealMini(mini1, target);
                revealMini(mini2, target);
                com.badlogic.gdx.Gdx.app.postRunnable(parent::dispose);
            });
        }

        private void hideMini(Entity mini) {
            mini.setScale(0, 0);
            mini.getComponent(ColliderComponent.class).setSensor(true);
            mini.getComponent(CombatStatsComponent.class).setHealth(Integer.MAX_VALUE);
        }

        private void revealMini(Entity mini, Entity target) {
            mini.setScale(1f, 1f);

            ColliderComponent collider = mini.getComponent(ColliderComponent.class);
            collider.setSensor(false);
            collider.setDensity(1.0f);

            CombatStatsComponent stats = mini.getComponent(CombatStatsComponent.class);
            stats.setHealth(health / 2);

            clickable clickComp = mini.getComponent(clickable.class);
            clickComp.setEnabled(true);

            AITaskComponent ai = mini.getComponent(AITaskComponent.class);
            ai.update();
            ai.addTask(new ChaseTask(target, 1, 100f, 100f, speed.cpy()));

            mini.getEvents().addListener("entityDeath", () -> {
                com.badlogic.gdx.Gdx.app.postRunnable(mini::dispose);
            });
        }
    }

    private static Entity createDivider(Entity target, Vector2 speed, boolean canSplit) {
        Entity divider = EnemyFactory.createBaseEnemyAnimated(
                target, speed,
                "images/divider_enemy_spritesheet.atlas", 0.5f, 0.18f
        );

        divider.addComponent(new CombatStatsComponent(
                canSplit ? health : health,
                canSplit ? health : health,
                resistance, weakness
        ));
        divider.addComponent(new clickable(clickRadius * (canSplit ? 1f : 0.8f)));

        if (canSplit) {
            PhysicsUtils.setScaledCollider(divider, 0.6f, 0.6f);
            divider.getComponent(ColliderComponent.class).setDensity(1.5f);
        } else {
            PhysicsUtils.setScaledCollider(divider, 0.3f, 0.3f);
            divider.getComponent(ColliderComponent.class).setDensity(1.0f);
        }

        return divider;
    }

    public static int getHealth() { return health; }
    public static int getDamage() { return damage; }
    public static DamageTypeConfig getResistance() { return resistance; }
    public static DamageTypeConfig getWeakness() { return weakness; }
    public static Vector2 getSpeed() { return new Vector2(speed); }
    public static float getClickRadius() { return clickRadius; }

    public static void setHealth(int h) { health = h; }
    public static void setDamage(int d) { damage = d; }
    public static void setResistance(DamageTypeConfig r) { resistance = (r != null) ? r : DEFAULT_RESISTANCE; }
    public static void setWeakness(DamageTypeConfig w) { weakness = (w != null) ? w : DEFAULT_WEAKNESS; }
    public static void setSpeed(Vector2 s) { if (s != null) speed.set(s); }
    public static void setClickRadius(float r) { clickRadius = r; }

    public static void resetToDefaults() {
        health = DEFAULT_HEALTH;
        damage = DEFAULT_DAMAGE;
        resistance = DEFAULT_RESISTANCE;
        weakness = DEFAULT_WEAKNESS;
        speed.set(DEFAULT_SPEED);
        clickRadius = DEFAULT_CLICKRADIUS;
    }

    private DividerEnemyFactory() {
        throw new IllegalStateException("Static factory class cannot be instantiated");
    }
}
