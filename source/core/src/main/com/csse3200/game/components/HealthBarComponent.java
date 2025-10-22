package com.csse3200.game.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.rendering.RenderComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Component that renders a health bar above an entity.
 * The health bar shows current health as a percentage of max health.
 * @author Team1 
 */
public class HealthBarComponent extends RenderComponent {
    private static final Logger logger = LoggerFactory.getLogger(HealthBarComponent.class);
    
    // Default health bar dimensions
    private static final float DEFAULT_WIDTH = 1.5f;
    private static final float DEFAULT_HEIGHT = 0.15f;
    private static final float DEFAULT_OFFSET_Y = 0.8f;
    
    // Health bar colors
    private static final Color HEALTH_BAR_BACKGROUND = new Color(0.3f, 0.3f, 0.3f, 0.8f); // Dark gray background
    private static final Color HEALTH_BAR_FULL = new Color(0.2f, 0.8f, 0.2f, 0.9f); // Green (full health)
    private static final Color HEALTH_BAR_MEDIUM = new Color(1.0f, 0.8f, 0.0f, 0.9f); // Yellow (medium health)
    private static final Color HEALTH_BAR_LOW = new Color(0.8f, 0.2f, 0.2f, 0.9f); // Red (low health)
    
    // Configurable health bar dimensions
    private float width;
    private float height;
    private float offsetY;
    
    private PlayerCombatStatsComponent combatStats;
    private int maxHealth;
    private int currentHealth;
    private boolean isVisible = true;
    private ShapeRenderer shapeRenderer;
    
    public HealthBarComponent() {
        this(DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_OFFSET_Y);
    }
    
    public HealthBarComponent(float width, float height, float offsetY) {
        this.width = width;
        this.height = height;
        this.offsetY = offsetY;
    }
    
    @Override
    public void create() {
        super.create();
        
        // Initialize ShapeRenderer
        shapeRenderer = new ShapeRenderer();
        
        // Get combat stats component
        combatStats = entity.getComponent(PlayerCombatStatsComponent.class);
        if (combatStats == null) {
            logger.error("HealthBarComponent requires PlayerCombatStatsComponent");
            return;
        }
        
        // Get initial health value
        currentHealth = combatStats.getHealth();
        maxHealth = currentHealth; // Assume initial health is max health
        
        // Set max health for health bar component
        setMaxHealth(maxHealth);
        
        // Listen for health change events
        entity.getEvents().addListener("updateHealth", this::onHealthUpdate);
        
        logger.debug("HealthBarComponent created for entity with max health: {}", maxHealth);
    }
    
    /**
     * Called when health is updated
     */
    private void onHealthUpdate(int newHealth) {
        currentHealth = newHealth;
        logger.debug("Health updated: {}/{}", currentHealth, maxHealth);
    }
    
    /**
     * Set whether the health bar is visible
     */
    public void setVisible(boolean visible) {
        this.isVisible = visible;
    }
    
    /**
     * Get whether the health bar is visible
     */
    public boolean isVisible() {
        return isVisible;
    }
    
    /**
     * Set maximum health (for initialization)
     */
    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
        logger.debug("Max health set to: {}", maxHealth);
    }
    
    @Override
    protected void draw(SpriteBatch batch) {
        if (!isVisible || combatStats == null || maxHealth <= 0) {
            return;
        }
        
        // Calculate health percentage
        float healthPercentage = Math.max(0f, Math.min(1f, (float) currentHealth / maxHealth));
        
        // Get entity position
        Vector2 entityPos = entity.getCenterPosition();
        if (entityPos == null) {
            return;
        }
        
        // Calculate health bar position (above entity)
        float barX = entityPos.x - width / 2f;
        float barY = entityPos.y + offsetY;
        
        // Save current render state
        batch.end();
        
        // Enable blending for transparency support
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Draw health bar background
        shapeRenderer.setColor(HEALTH_BAR_BACKGROUND);
        shapeRenderer.rect(barX, barY, width, height);
        
        // Draw health bar
        Color healthColor = getHealthColor(healthPercentage);
        shapeRenderer.setColor(healthColor);
        shapeRenderer.rect(barX, barY, width * healthPercentage, height);
        
        shapeRenderer.end();
        
        // Restore render state
        batch.begin();
    }
    
    /**
     * Get health bar color based on health percentage
     */
    private Color getHealthColor(float percentage) {
        if (percentage > 0.6f) {
            return HEALTH_BAR_FULL; // Green
        } else if (percentage > 0.3f) {
            return HEALTH_BAR_MEDIUM; // Yellow
        } else {
            return HEALTH_BAR_LOW; // Red
        }
    }
    
    @Override
    public void dispose() {
        super.dispose();
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
            shapeRenderer = null;
        }
        logger.debug("HealthBarComponent disposed");
    }
}