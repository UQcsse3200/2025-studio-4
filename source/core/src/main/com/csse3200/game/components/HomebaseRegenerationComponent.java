package com.csse3200.game.components;

import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Homebase regeneration component.
 * Regenerates 5 HP every 4 seconds after 4 seconds without damage.
 * 
 * @author MapTeam
 * @since Sprint 4
 */
public class HomebaseRegenerationComponent extends Component {
    private static final Logger logger = LoggerFactory.getLogger(HomebaseRegenerationComponent.class);
    
    // Configuration constants
    private static final float TIME_WITHOUT_DAMAGE_REQUIRED = 4f; // How many seconds without damage required to start regeneration
    private static final float REGENERATION_INTERVAL = 4.0f; // How often to regenerate (in seconds)
    private static final int REGENERATION_AMOUNT = 5; // Amount of health regenerated each time
    
    // State tracking
    private long lastDamageTime; // Time of last damage taken (in milliseconds)
    private long lastRegenerationTime; // Time of last regeneration (in milliseconds)
    private int previousHealth = -1; // Previous health value, used to detect damage
    private boolean isRegenerating = false; // Whether currently regenerating
    
    private GameTime gameTime;
    private PlayerCombatStatsComponent combatStats;
    
    /**
     * Initializes the component.
     */
    @Override
    public void create() {
        super.create();
        
        // Get game time service
        gameTime = ServiceLocator.getTimeSource();
        if (gameTime == null) {
            logger.error("GameTime service not available");
            return;
        }
        
        // Get combat stats component
        combatStats = entity.getComponent(PlayerCombatStatsComponent.class);
        if (combatStats == null) {
            logger.error("HomebaseRegenerationComponent requires PlayerCombatStatsComponent");
            return;
        }
        
        // Initialize time values
        lastDamageTime = gameTime.getTime();
        lastRegenerationTime = gameTime.getTime();
        previousHealth = combatStats.getHealth();
        
        // Listen for health update events
        entity.getEvents().addListener("updateHealth", this::onHealthUpdate);
        
        logger.info("HomebaseRegenerationComponent created - will regenerate {} HP every {} seconds after {} seconds without damage",
                REGENERATION_AMOUNT, REGENERATION_INTERVAL, TIME_WITHOUT_DAMAGE_REQUIRED);
    }
    
    /**
     * Handles health updates to detect damage.
     * @param newHealth the new health value
     */
    private void onHealthUpdate(int newHealth) {
        // Check if damage was taken (health decreased)
        if (previousHealth > 0 && newHealth < previousHealth) {
            // Damage taken, reset last damage time
            lastDamageTime = gameTime.getTime();
            isRegenerating = false;
            logger.debug("Homebase took damage. Regeneration paused.");
        }
        
        // Update previousHealth
        previousHealth = newHealth;
    }
    
    /**
     * Updates regeneration logic every frame.
     */
    @Override
    public void update() {
        if (combatStats == null || gameTime == null) {
            return;
        }
        
        long currentTime = gameTime.getTime();
        
        // Calculate time since last damage (in seconds)
        float timeSinceLastDamage = (currentTime - lastDamageTime) / 1000.0f;
        
        // Check if conditions are met to start regeneration
        if (timeSinceLastDamage >= TIME_WITHOUT_DAMAGE_REQUIRED) {
            if (!isRegenerating) {
                isRegenerating = true;
                lastRegenerationTime = currentTime;
                logger.debug("Homebase regeneration started");
            }
            
            // Calculate time since last regeneration (in seconds)
            float timeSinceLastRegen = (currentTime - lastRegenerationTime) / 1000.0f;
            
            // Check if it's time to regenerate
            if (timeSinceLastRegen >= REGENERATION_INTERVAL) {
                // Execute regeneration
                regenerateHealth();
                lastRegenerationTime = currentTime;
            }
        } else {
            // Haven't met conditions to start regeneration yet
            if (isRegenerating) {
                isRegenerating = false;
                logger.debug("Homebase regeneration stopped due to recent damage");
            }
        }
    }
    
    /**
     * Executes health regeneration.
     */
    private void regenerateHealth() {
        if (combatStats == null) {
            return;
        }
        
        int currentHealth = combatStats.getHealth();
        int maxHealth = combatStats.getMaxHealth();
        
        // If already at full health, no regeneration needed
        if (currentHealth >= maxHealth) {
            logger.debug("Homebase already at full health, no regeneration needed");
            return;
        }
        
        // Calculate actual regeneration amount (not exceeding max health)
        int actualRegenAmount = Math.min(REGENERATION_AMOUNT, maxHealth - currentHealth);
        
        // Regenerate health
        combatStats.addHealth(actualRegenAmount);
        
        // Show regeneration number (use negative value to indicate regeneration, displayed in green)
        showRegenerationNumber(actualRegenAmount);
        
        logger.info("Homebase regenerated {} HP (current: {}/{})", 
                actualRegenAmount, combatStats.getHealth(), maxHealth);
    }
    
    /**
     * Shows regeneration number above homebase.
     * @param regenAmount amount of health regenerated
     */
    private void showRegenerationNumber(int regenAmount) {
        try {
            // Trigger display event, use negative value to indicate regeneration (will be displayed in green by DamagePopupComponent)
            entity.getEvents().trigger("showDamage", -regenAmount, entity.getCenterPosition().cpy());
            logger.debug("Triggered regeneration number display: +{} HP", regenAmount);
        } catch (Exception e) {
            logger.error("Failed to show regeneration number: {}", e.getMessage());
        }
    }
    
    /**
     * Get whether currently regenerating
     * @return true if regenerating, false otherwise
     */
    public boolean isRegenerating() {
        return isRegenerating;
    }
    
    /**
     * Get time since last damage (in seconds)
     * @return Seconds since last damage
     */
    public float getTimeSinceLastDamage() {
        if (gameTime == null) {
            return 0;
        }
        return (gameTime.getTime() - lastDamageTime) / 1000.0f;
    }
    
    /**
     * Get time until next regeneration (in seconds)
     * @return Seconds until next regeneration, returns -1 if not regenerating
     */
    public float getTimeUntilNextRegen() {
        if (!isRegenerating || gameTime == null) {
            return -1;
        }
        float timeSinceLastRegen = (gameTime.getTime() - lastRegenerationTime) / 1000.0f;
        return Math.max(0, REGENERATION_INTERVAL - timeSinceLastRegen);
    }
}

