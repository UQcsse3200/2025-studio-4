package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Exactly like AnimationRenderComponent but supports rotation around the sprite center.
 */
public class RotatingAnimationRenderComponent extends RenderComponent {
    private static final Logger logger = LoggerFactory.getLogger(RotatingAnimationRenderComponent.class);

    private final GameTime timeSource;
    private TextureAtlas atlas;
    private final Map<String, Animation<TextureRegion>> animations = new HashMap<>(4);

    private Animation<TextureRegion> currentAnimation;
    private String currentAnimationName;
    private float animationPlayTime;
    private String currentAtlasPath;
    private int currentLevel = 1; // track tower level for idle/fire naming

    /** rotation in degrees  */
    private float rotationDeg = 0f;
    private float baseRotationDeg = -90f; // change this to whatever makes the sprite face correctly

    public void setBaseRotation(float deg) {
        this.baseRotationDeg = deg;
    }

    public float getBaseRotation() {
        return baseRotationDeg;
    }

    public RotatingAnimationRenderComponent(TextureAtlas atlas) {
        this.atlas = atlas;
        this.timeSource = ServiceLocator.getTimeSource();

    }

    /** Register an animation by name from the atlas. */
    public boolean addAnimation(String name, float frameDuration) {
        return addAnimation(name, frameDuration, PlayMode.NORMAL);
    }
    /**
     */
    public void setLevel(int level) {
        this.currentLevel = level;
    }

    /** Register an animation with a specific PlayMode. */
    public boolean addAnimation(String name, float frameDuration, PlayMode playMode) {
        Array<AtlasRegion> regions = atlas.findRegions(name);
        if (regions == null || regions.size == 0) {
            logger.warn("Animation {} not found in texture atlas", name);
            return false;
        } else if (animations.containsKey(name)) {
            logger.warn("Animation {} already added. Add each animation once.", name);
            return false;
        }
        animations.put(name, new Animation<>(frameDuration, regions, playMode));
        logger.debug("Adding animation {}", name);
        return true;
    }

    /** Start playback of a previously added animation. */
    public void startAnimation(String name) {
        Animation<TextureRegion> animation = animations.get(name);
        if (animation == null) {
            logger.error("Attempted to play unknown animation {}. Add it first.", name);
            return;
        }
        currentAnimation = animation;
        currentAnimationName = name;
        animationPlayTime = 0f;
        logger.debug("Starting animation {}", name);
    }

    /** Stop playback if any. */
    public boolean stopAnimation() {
        if (currentAnimation == null) return false;
        logger.debug("Stopping animation {}", currentAnimationName);
        currentAnimation = null;
        currentAnimationName = null;
        animationPlayTime = 0f;
        return true;
    }

    /** Name of the currently playing animation, or null. */
    public String getCurrentAnimation() {
        return currentAnimationName;
    }

    /** True if current (non-looping) animation has finished. */
    public boolean isFinished() {
        return currentAnimation != null && currentAnimation.isAnimationFinished(animationPlayTime);
    }

    /** Set rotation in degrees (around the sprite center). */
    public void setRotation(float degrees) {
        this.rotationDeg = degrees;
    }

    /** Get rotation in degrees. */
    public float getRotation() {
        return rotationDeg;
    }

    /** Optional helper: scale entity width to 1, keep texture aspect ratio. */
    public void scaleEntity() {
        TextureRegion r = atlas.findRegion("default");
        if (r != null) {
            entity.setScale(1f, (float) r.getRegionHeight() / r.getRegionWidth());
        }
    }

    @Override
    protected void draw(SpriteBatch batch) {
        if (currentAnimation == null) return;

        TextureRegion region = currentAnimation.getKeyFrame(animationPlayTime);
        Vector2 pos = entity.getPosition();
        Vector2 scale = entity.getScale();
        float w = (scale == null ? 1f : scale.x);
        float h = (scale == null ? 1f : scale.y);

        // rotate about center (origin = w/2, h/2)
        batch.draw(
                region,
                pos.x, pos.y,
                w * 0.5f, h * 0.5f,  // origin
                w, h,                // size in world units
                1f, 1f,              // extra scale
                rotationDeg + baseRotationDeg          // rotation
        );

        animationPlayTime += timeSource.getDeltaTime();
    }
    public void setNewTexture(String texturePath, int currentLevel) {
    TextureAtlas atlas = ServiceLocator.getResourceService().getAsset(texturePath, TextureAtlas.class);
        if (atlas == null) {
        System.err.println("[RotatingAnim] Missing atlas: " + texturePath);
        return;
    }

        this.atlas = atlas;
        animations.clear();

    // Try to automatically add idle/fire animations for current level
    String idle = "idle_";
    String fire = "fire_";

        addAnimation(idle, 0.1f, Animation.PlayMode.LOOP);
        addAnimation(fire, 0.20f, Animation.PlayMode.NORMAL);

    startAnimation(idle);
    currentAtlasPath = texturePath;

        System.out.println("[RotatingAnim] Loaded atlas '" + texturePath + "' and started idle animation");
        debugListRegions(texturePath);
}

    @Override
    public void dispose() {
        super.dispose();
    }
    private void debugListRegions(String atlasPath) {
        if (atlas == null) return;
        var regs = atlas.getRegions();
        System.out.println("[RotatingAnim] Regions in " + atlasPath + ": count=" + (regs == null ? 0 : regs.size));
        if (regs != null) {
            for (var r : regs) {
                System.out.println("  - name=" + r.name + " index=" + r.index + " size=" + r.packedWidth + "x" + r.packedHeight);
            }
        }
    }
}
