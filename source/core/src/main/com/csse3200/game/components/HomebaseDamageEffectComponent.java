package com.csse3200.game.components;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.csse3200.game.rendering.SwitchableTextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Component that handles visual damage effects for the homebase.
 * Changes the texture and color when the homebase takes damage to provide visual feedback.
 * @author Team1 
 * @since Sprint 3
 */
public class HomebaseDamageEffectComponent extends Component {
    private static final Logger logger = LoggerFactory.getLogger(HomebaseDamageEffectComponent.class);
    
    // Default texture paths
    private static final String DEFAULT_NORMAL_TEXTURE_PATH = "images/basement.png";
    
    // Sound effect path
    private static final String HIT_SOUND_PATH = "sounds/homebase_hit_sound.mp3";
    
    // Damage effect duration (seconds)
    private static final float DAMAGE_EFFECT_DURATION = 0.3f;
    
    // Damage color (red flash effect)
    private static final Color DAMAGE_COLOR = new Color(1f, 0.3f, 0.3f, 1f); // Red
    private static final Color NORMAL_COLOR = new Color(1f, 1f, 1f, 1f); // White (normal)
    
    // Instance variables for storing texture paths
    private final String normalTexturePath;
    private final String damagedTexturePath;
    
    private SwitchableTextureRenderComponent textureComponent;
    private Texture normalTexture;
    private Texture damagedTexture;
    private Sound hitSound;
    private boolean isShowingDamageEffect = false;
    private float damageEffectTimer = 0f;
    private int previousHealth = -1;
    private boolean hasDamagedTexture = false;
    
    /**
     * Create with default texture paths
     */
    public HomebaseDamageEffectComponent() {
        this(DEFAULT_NORMAL_TEXTURE_PATH);
    }
    
    /**
     * Create with custom texture path
     * @param normalTexturePath path to the normal homebase texture
     */
    public HomebaseDamageEffectComponent(String normalTexturePath) {
        this.normalTexturePath = normalTexturePath;
        // Generate damaged texture path by appending "_damaged" before file extension
        int dotIndex = normalTexturePath.lastIndexOf('.');
        if (dotIndex > 0) {
            this.damagedTexturePath = normalTexturePath.substring(0, dotIndex) + "_damaged" + normalTexturePath.substring(dotIndex);
        } else {
            this.damagedTexturePath = normalTexturePath + "_damaged";
        }
    }
    
    @Override
    public void create() {
        super.create();
        
        // Get texture render component
        textureComponent = entity.getComponent(SwitchableTextureRenderComponent.class);
        if (textureComponent == null) {
            logger.error("HomebaseDamageEffectComponent requires SwitchableTextureRenderComponent");
            return;
        }
        
        // Load textures and sound effects
        loadTextures();
        loadHitSound();
        
        // Get initial health value
        PlayerCombatStatsComponent combatStats = entity.getComponent(PlayerCombatStatsComponent.class);
        if (combatStats != null) {
            previousHealth = combatStats.getHealth();
            logger.debug("Initial health set to: {}", previousHealth);
        }
        
        // Listen for damage events
        entity.getEvents().addListener("updateHealth", this::onHealthUpdate);
        logger.debug("Added updateHealth event listener");
        
        logger.debug("HomebaseDamageEffectComponent created with damaged texture: {}", hasDamagedTexture);
    }
    
    /**
     * Check if damaged texture is available
     * @return true if damaged texture exists, false if only color effect is used
     */
    public boolean hasDamagedTexture() {
        return hasDamagedTexture;
    }
    
    /**
     * Get damage effect duration
     * @return duration in seconds
     */
    public float getDamageEffectDuration() {
        return DAMAGE_EFFECT_DURATION;
    }
    
    /**
     * Check if damage effect is currently being displayed
     * @return true if damage effect is being shown, false otherwise
     */
    public boolean isShowingDamageEffect() {
        return isShowingDamageEffect;
    }
    
    /**
     * Load normal and damaged state textures
     */
    private void loadTextures() {
        try {
            normalTexture = ServiceLocator.getResourceService().getAsset(normalTexturePath, Texture.class);
            logger.debug("Loaded normal homebase texture: {}", normalTexturePath);
        } catch (Exception e) {
            logger.error("Could not load normal homebase texture: {}", e.getMessage());
            return;
        }
        
        try {
            damagedTexture = ServiceLocator.getResourceService().getAsset(damagedTexturePath, Texture.class);
            hasDamagedTexture = true;
            logger.debug("Loaded damaged homebase texture: {}", damagedTexturePath);
        } catch (Exception e) {
            logger.warn("Could not load damaged texture, will use color-only effect: {}", e.getMessage());
            damagedTexture = null; // Set to null, indicating only color effect will be used
            hasDamagedTexture = false;
        }
    }
    
    /**
     * Load hit sound effect
     */
    private void loadHitSound() {
        try {
            hitSound = ServiceLocator.getResourceService().getAsset(HIT_SOUND_PATH, Sound.class);
            logger.debug("Loaded homebase hit sound: {}", HIT_SOUND_PATH);
        } catch (Exception e) {
            logger.warn("Could not load hit sound, damage effect will be visual only: {}", e.getMessage());
            hitSound = null;
        }
    }
    
    /**
     * Triggered when health is updated
     */
    private void onHealthUpdate(int newHealth) {
        logger.debug("Health update: previous={}, new={}", previousHealth, newHealth);
        
        // Check if damage was taken (health decreased)
        if (previousHealth > 0 && newHealth < previousHealth) {
            int damageAmount = previousHealth - newHealth;
            logger.debug("Damage detected! Damage amount: {}", damageAmount);
            showDamageEffect(damageAmount);
        }
        
        // Update previousHealth
        previousHealth = newHealth;
    }
    
    /**
     * Show damage effect
     */
    private void showDamageEffect(int damageAmount) {
        if (isShowingDamageEffect) {
            return; // Already showing damage effect
        }
        
        isShowingDamageEffect = true;
        damageEffectTimer = DAMAGE_EFFECT_DURATION;
        
        // Switch to damaged texture and color
        if (textureComponent != null) {
            // If damaged texture exists, switch texture; otherwise only change color
            if (hasDamagedTexture && damagedTexture != null) {
                textureComponent.setTexture(damagedTexture);
                logger.debug("Switched to damaged texture");
            } else {
                logger.debug("Using color-only damage effect");
            }
            textureComponent.setColor(DAMAGE_COLOR);
        }
        // Show damage number
        showDamageNumber(damageAmount);

        // Play hit sound effect
        if (hitSound != null) {
            hitSound.play();
            logger.debug("Playing homebase hit sound");
        }
        
        logger.debug("Showing homebase damage effect with damage: {}", damageAmount);
    }
    
    /**
     * Show damage number
     */
    private void showDamageNumber(int damageAmount) {
        try {
            // Trigger damage number display event for UI system to handle
            entity.getEvents().trigger("showDamage", damageAmount, entity.getCenterPosition().cpy());
            logger.debug("Triggered damage number display: {} damage", damageAmount);
        } catch (Exception e) {
            logger.error("Failed to show damage number: {}", e.getMessage());
        }
    }
    
    /**
     * Return to normal state
     */
    private void returnToNormal() {
        if (!isShowingDamageEffect) {
            return;
        }
        
        isShowingDamageEffect = false;
        damageEffectTimer = 0f;
        
        // Restore normal texture and color
        if (textureComponent != null) {
            textureComponent.setTexture(normalTexture);
            textureComponent.setColor(NORMAL_COLOR);
        }
        
        logger.debug("Homebase returned to normal state");
    }
    
    @Override
    public void update() {
        super.update();
        
        // Update damage effect timer
        if (isShowingDamageEffect) {
            damageEffectTimer -= ServiceLocator.getTimeSource().getDeltaTime();
            
            if (damageEffectTimer <= 0f) {
                returnToNormal();
            }
        }
    }
    
    @Override
    public void dispose() {
        super.dispose();
        // Ensure normal color is restored when disposing
        if (textureComponent != null) {
            textureComponent.setColor(NORMAL_COLOR);
        }
    }
}
