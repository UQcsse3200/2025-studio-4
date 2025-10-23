package com.csse3200.game.components.enemy;

import com.badlogic.gdx.graphics.Color;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;

/**
 * Component that tints enemy sprites based on their current health percentage.
 * Lower health = more red tint, providing visual feedback without UI clutter.
 */
public class EnemyHealthTintComponent extends Component {
    private int maxHealth;
    private AnimationRenderComponent animator;
    private CombatStatsComponent combatStats;

    @Override
    public void create() {
        super.create();

        // Get references to required components
        combatStats = entity.getComponent(CombatStatsComponent.class);
        animator = entity.getComponent(AnimationRenderComponent.class);

        if (combatStats == null || animator == null) {
            return;
        }

        // Store initial max health
        maxHealth = combatStats.getHealth();

        // Listen for health updates
        entity.getEvents().addListener("updateHealth", this::onHealthChanged);

        // Apply initial tint
        updateTint();
    }

    /**
     * Called when the entity's health changes
     * @param newHealth the new health value
     */
    private void onHealthChanged(int newHealth) {
        updateTint();
    }

    /**
     * Update the sprite tint based on current health percentage
     */
    private void updateTint() {
        if (combatStats == null || animator == null || maxHealth <= 0) {
            return;
        }

        float healthPercentage = (float) combatStats.getHealth() / maxHealth;
        Color tint = calculateHealthTint(healthPercentage);
        animator.setColor(tint);
    }

    /**
     * Calculate the tint color based on health percentage.
     * Interpolates from white (full health) to red (low health).
     *
     * @param healthPercentage value between 0 and 1 representing health ratio
     * @return Color to apply as tint
     */
    private Color calculateHealthTint(float healthPercentage) {
        // Keep red channel at full
        float red = 1f;

        // Reduce green and blue as health decreases
        // This creates a white -> red gradient
        float green = healthPercentage;
        float blue = healthPercentage;

        return new Color(red, green, blue, 1f);
    }
}

