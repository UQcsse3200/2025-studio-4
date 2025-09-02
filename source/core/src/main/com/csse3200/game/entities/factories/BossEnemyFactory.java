package com.csse3200.game.entities.factories;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.enemy.clickable;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.DamageTypeConfig;
import com.csse3200.game.physics.PhysicsUtils;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.rendering.TextureRenderComponent;

public class BossEnemyFactory {
    // Default boss configuration
    // IF YOU WANT TO MAKE A NEW ENEMY, THIS IS THE VARIABLE STUFF YOU CHANGE
    ///////////////////////////////////////////////////////////////////////////////////////////////
    private static final int DEFAULT_HEALTH = 200;
    private static final int DEFAULT_DAMAGE = 20;
    private static final DamageTypeConfig DEFAULT_RESISTANCE = DamageTypeConfig.None;
    private static final DamageTypeConfig DEFAULT_WEAKNESS = DamageTypeConfig.None;
    private static final Vector2 DEFAULT_SPEED = new Vector2(0.5f, 0.5f);
    private static final String DEFAULT_TEXTURE = "images/boss_enemy.png";
    private static final String DEFAULT_NAME = "Boss Enemy";
    private static final float DEFAULT_CLICKRADIUS = 1.2f;
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

    /**
     * Creates a boss enemy with current configuration.
     *
     * @param target entity to chase
     * @return entity
     */
    public static Entity createBossEnemy(Entity target) {
        Entity boss = EnemyFactory.createBaseEnemy(target, new Vector2(speed));

        // Loads image at texturePath and keeps in memory
        TextureRenderComponent imageTexture = new TextureRenderComponent(texturePath);

        boss
                .addComponent(new CombatStatsComponent(health, damage, resistance, weakness))
                .addComponent(imageTexture)
                .addComponent(new clickable(clickRadius));

        // Makes the visual scale match the texture scale so proportions are correct (image is not just a 1x1 square)
        imageTexture.scaleEntity();
        // Scale up x and y coordinates, keeping same proportions
        boss.setScale(boss.getScale().x * 1.8f, boss.getScale().y * 1.8f);

        // Update physics engine to suit a large boss
        PhysicsUtils.setScaledCollider(boss, 0.6f, 0.6f);    // unsure if these numbers are suitable
        boss.getComponent(ColliderComponent.class).setDensity(1.5f);

        return boss;
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
    }

    private BossEnemyFactory() {
        throw new IllegalStateException("Instantiating static util class");
    }
}