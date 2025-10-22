package com.csse3200.game.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.rendering.RenderComponent;
import com.csse3200.game.rendering.SwitchableTextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Home base healing effect component.
 * Displays a green pulsating effect and optional healing animation when the base regenerates.
 * @author Team1 
 * @since Sprint 4
 */
public class HomebaseHealingEffectComponent extends Component {
    private static final Logger logger = LoggerFactory.getLogger(HomebaseHealingEffectComponent.class);
    
    // Duration of the healing effect in seconds
    private static final float HEALING_EFFECT_DURATION = 0.5f;

    // Optional: path to a healing video asset (uses video if available)
    private static final String HEALING_VIDEO_PATH = "images/re.mp4";
    
    // Healing colour (green pulse)
    private static final Color HEALING_COLOR = new Color(0.3f, 1f, 0.3f, 1f); // Green
    private static final Color NORMAL_COLOR = new Color(1f, 1f, 1f, 1f); // White (normal)
    
    private SwitchableTextureRenderComponent textureComponent;
    private HealingVideoOverlay videoOverlay;
    private boolean isShowingHealingEffect = false;
    private float healingEffectTimer = 0f;
    private int previousHealth = -1;
    
    /**
     * Creates the home base healing effect component.
     */
    public HomebaseHealingEffectComponent() {
    }
    
    @Override
    public void create() {
        super.create();
        
        // Acquire the switchable texture render component
        textureComponent = entity.getComponent(SwitchableTextureRenderComponent.class);
        if (textureComponent == null) {
            logger.error("HomebaseHealingEffectComponent requires SwitchableTextureRenderComponent");
            return;
        }

        // Attempt to create the healing video overlay if gdx-video and the asset are available
        try {
            FileHandle vh = Gdx.files.internal(HEALING_VIDEO_PATH);
            if (vh.exists()) {
                videoOverlay = new HealingVideoOverlay(HEALING_VIDEO_PATH);
                entity.addComponent(videoOverlay);
                logger.debug("Healing video overlay initialised for {}", HEALING_VIDEO_PATH);
            } else {
                logger.info("Healing video not found at {} — will use color pulse fallback", HEALING_VIDEO_PATH);
            }
        } catch (Exception e) {
            // Fall back to colour-only effect if file system access or other issues occur
            logger.warn("Failed to init healing video overlay: {}", e.getMessage());
        }
        
        // Capture initial health value
        PlayerCombatStatsComponent combatStats = entity.getComponent(PlayerCombatStatsComponent.class);
        if (combatStats != null) {
            previousHealth = combatStats.getHealth();
            logger.debug("Initial health set to: {}", previousHealth);
        }
        
        // Listen for health updates
        entity.getEvents().addListener("updateHealth", this::onHealthUpdate);
        
        // Listen for damage display events to detect healing (negative amounts)
        entity.getEvents().addListener("showDamage", this::onDamageShow);
        
        logger.debug("HomebaseHealingEffectComponent created successfully");
    }
    
    /**
     * Triggered when health updates.
     *
     * @param newHealth latest hit point total for the home base
     */
    private void onHealthUpdate(int newHealth) {
        // Check if health increased (indicating healing)
        if (previousHealth > 0 && newHealth > previousHealth) {
            int healAmount = newHealth - previousHealth;
            showHealingEffect(healAmount);
        }
        
        // Persist the latest health value
        previousHealth = newHealth;
    }
    
    /**
     * Triggered when damage numbers are shown (negative values mean healing).
     *
     * @param amount   damage dealt or healing received (negative for healing)
     * @param position world coordinates where the floating text is displayed
     */
    private void onDamageShow(int amount, Vector2 position) {
        // Negative amounts represent healing
        if (amount < 0) {
            showHealingEffect(-amount);
        }
    }
    
    /**
     * Display the healing effect.
     *
     * @param healAmount positive amount representing the health restored
     */
    private void showHealingEffect(int healAmount) {
        if (isShowingHealingEffect) {
            // If the effect is already active, reset the timer to extend it
            healingEffectTimer = HEALING_EFFECT_DURATION;
            return;
        }
        
        isShowingHealingEffect = true;
        healingEffectTimer = HEALING_EFFECT_DURATION;
        
        // Apply the healing effect
        if (textureComponent != null) {
            // We only adjust the colour overlay to keep the base texture
            textureComponent.setColor(HEALING_COLOR);
            logger.debug("Applied healing color effect");
        }

        // Start the video overlay if available
        if (videoOverlay != null) {
            videoOverlay.show();
        }
        
        logger.debug("Showing homebase healing effect with heal amount: {}", healAmount);
    }
    
    /**
     * Restore the base appearance.
     */
    private void returnToNormal() {
        if (!isShowingHealingEffect) {
            return;
        }
        
        isShowingHealingEffect = false;
        healingEffectTimer = 0f;
        
        // Restore the default colour
        if (textureComponent != null) {
            textureComponent.setColor(NORMAL_COLOR);
            logger.debug("Homebase returned to normal color");
        }

        // Stop the video overlay
        if (videoOverlay != null) {
            videoOverlay.hide();
        }
    }
    
        /**
     * Handles the healing animation timing for Team1's Sprint 4 feature work.
     *
     * @throws NullPointerException if the time source is not registered
     */
    
    @Override
    public void update() {
        super.update();
        // Update the healing effect timer
        if (isShowingHealingEffect) {
            healingEffectTimer -= ServiceLocator.getTimeSource().getDeltaTime();
            
            // Add pulsating effect by modulating the colour intensity over time
            if (textureComponent != null) {
                float progress = healingEffectTimer / HEALING_EFFECT_DURATION;
                float alpha = 0.5f + 0.5f * (float)Math.sin(progress * Math.PI * 4); 
                Color pulseColor = new Color(
                    0.3f + alpha * 0.3f,  // R: 0.3 - 0.6
                    1f,                     // G: 1.0 
                    0.3f + alpha * 0.3f,  // B: 0.3 - 0.6
                    1f                      // A: 1.0 
                );
                textureComponent.setColor(pulseColor);
            }
            
            if (healingEffectTimer <= 0f) {
                returnToNormal();
            }
        }
    }
    
    /**
     * Check whether the healing effect is active.
     * @return true if the healing effect is showing, false otherwise
     */
    public boolean isShowingHealingEffect() {
        return isShowingHealingEffect;
    }
    
    /**
     * Get the healing effect duration.
     * @return duration in seconds
     */
    public float getHealingEffectDuration() {
        return HEALING_EFFECT_DURATION;
    }
    
    @Override
    public void dispose() {
        super.dispose();
        // Ensure the colour resets when disposed
        if (textureComponent != null) {
            textureComponent.setColor(NORMAL_COLOR);
        }
        if (videoOverlay != null) {
            try {
                videoOverlay.hide();
            } catch (Exception ignored) {
            }
        }
    }
}


/**
 * Uses reflection to prefer gdx-video for playing the MP4 overlay; if the library is missing,
 * this component stays idle so the colour pulse acts as the fallback.
 * Renders alongside the entity at the same position.
 */
class HealingVideoOverlay extends RenderComponent {
    private static final Logger logger = LoggerFactory.getLogger(HealingVideoOverlay.class);

    private final String videoPath;
    private boolean active = false;

    // Cached reflection handles
    private boolean reflectionReady = false;
    private Object videoPlayer; // com.badlogic.gdx.video.VideoPlayer
    private java.lang.reflect.Method mPlay;        // play(FileHandle)
    private java.lang.reflect.Method mStop;        // stop()
    private java.lang.reflect.Method mSetLooping;  // setLooping(boolean)
    private java.lang.reflect.Method mGetTexture;  // getTexture() -> Texture or TextureRegion

    HealingVideoOverlay(String videoPath) {
        this.videoPath = videoPath;
    }

    @Override
    public void create() {
        super.create();
        initReflection();
    }

    private void initReflection() {
        try {
            Class<?> creatorClz = Class.forName("com.badlogic.gdx.video.VideoPlayerCreator");
            java.lang.reflect.Method create = creatorClz.getMethod("createVideoPlayer");
            this.videoPlayer = create.invoke(null);

            Class<?> playerClz = Class.forName("com.badlogic.gdx.video.VideoPlayer");
            this.mPlay = playerClz.getMethod("play", FileHandle.class);
            this.mStop = playerClz.getMethod("stop");
            this.mSetLooping = playerClz.getMethod("setLooping", boolean.class);

            // getTexture could return Texture or TextureRegion depending on implementation
            // We will try getTexture() first and handle both types at call site
            try {
                this.mGetTexture = playerClz.getMethod("getTexture");
            } catch (NoSuchMethodException nsme) {
                // Fallback: some forks expose getTextureRegion()
                this.mGetTexture = playerClz.getMethod("getTextureRegion");
            }

            // Disable looping by default; duration is controlled externally
            mSetLooping.invoke(videoPlayer, false);
            reflectionReady = true;
            logger.info("gdx-video detected, healing MP4 overlay enabled");
        } catch (Throwable t) {
            reflectionReady = false;
            logger.info("gdx-video not available, using color-only healing effect ({}).", t.getClass().getSimpleName());
        }
    }

    public void show() {
        active = true;
        if (reflectionReady) {
            try {
                FileHandle fh = Gdx.files.internal(videoPath);
                if (fh.exists()) {
                    mPlay.invoke(videoPlayer, fh);
                } else {
                    logger.warn("Healing video file not found: {}", videoPath);
                }
            } catch (Exception e) {
                logger.warn("Failed to play healing video: {}", e.getMessage());
            }
        }
    }

    public void hide() {
        active = false;
        if (reflectionReady) {
            try {
                mStop.invoke(videoPlayer);
            } catch (Exception ignored) {
            }
        }
    }
    /**
     * Draws the current healing video frame above the home base sprite.
     *
     * @param batch sprite batch used by the renderer
     */
    @Override
    protected void draw(SpriteBatch batch) {
        if (!active) return;
        if (!reflectionReady) return; // Without the video library nothing is drawn (colour effect remains)

        try {
            Object frameObj = mGetTexture.invoke(videoPlayer);
            if (frameObj == null) return;

            Vector2 pos = entity.getPosition();
            Vector2 size = entity.getScale();
            float w = (size == null ? 1f : size.x);
            float h = (size == null ? 1f : size.y);

            if (frameObj instanceof TextureRegion region) {
                batch.draw(region, pos.x, pos.y, w, h);
            } else if (frameObj instanceof Texture tex) {
                batch.draw(tex, pos.x, pos.y, w, h);
            } else {
                // Unknown type, skip rendering
            }
        } catch (Exception e) {
            logger.debug("Video frame render failed: {}", e.getMessage());
        }
    }
    /**
     * Provides a slight offset so the overlay renders above the base.
     *
     * @return z-index adjustment for the overlay
     */
    @Override
    public float getZIndex() {
        // Slightly raise the Z order so the overlay sits above the base sprite
        return -entity.getPosition().y - 0.001f;
    }
}
