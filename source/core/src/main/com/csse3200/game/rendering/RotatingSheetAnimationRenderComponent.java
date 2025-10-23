package com.csse3200.game.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;

/**
 * Render component for frame animations cut from a PNG sprite sheet (not .atlas),
 * supporting grid slicing with rotation, scaling, custom origin, a completion event
 * ({@code animationFinished}), and layer/z-index control.
 */
public class RotatingSheetAnimationRenderComponent extends RenderComponent implements AutoCloseable {
    private final GameTime time = ServiceLocator.getTimeSource();
    private final Animation<TextureRegion> animation;
    private final String animationName;
    private final Texture ownedTexture;

    private float playTime = 0f;

    /** Final render angle in degrees. */
    private float rotationDeg = 0f;
    /** Base facing of the source art (e.g., up=90, right=0). */
    private float baseRotationDeg = 0f;
    private float scaleX = 1f, scaleY = 1f;
    /** Origin in entity-scale units; negative = use frame center. */
    private float originX = -1f, originY = -1f;
    /** If true in NORMAL mode, stop updating once the animation ends. */
    private boolean autoRemoveOnFinish = false;

    // ===== Layer & zIndex =====
    /** Default to a foreground-ish layer (RenderComponent default is 1). */
    private int layer = 5;
    /** If non-null, forces zIndex instead of using the default -pos.y scheme. */
    private Float zOverride = null;

    // Debug: print only for the first few frames
    private int dbgCount = 0;

    private RotatingSheetAnimationRenderComponent(TextureRegion[] frames,
                                                  float frameDur,
                                                  PlayMode playMode,
                                                  String animName,
                                                  Texture owned) {
        this.animation = new Animation<>(frameDur, frames);
        this.animation.setPlayMode(playMode == null ? PlayMode.NORMAL : playMode);
        this.animationName = animName == null ? "anim" : animName;
        this.ownedTexture = owned;
    }

    /**
     * Build from a grid-sliced PNG sheet. This constructor loads the Texture internally.
     *
     * @param pngPath  path to the PNG sprite sheet
     * @param cols     number of columns
     * @param rows     number of rows
     * @param frameW   frame width in pixels
     * @param frameH   frame height in pixels
     * @param frameDur duration of each frame in seconds
     * @param playMode libGDX {@link PlayMode}
     * @param animName logical animation name for events/logging
     */
    public static RotatingSheetAnimationRenderComponent fromSheet(String pngPath,
                                                                  int cols, int rows,
                                                                  int frameW, int frameH,
                                                                  float frameDur,
                                                                  PlayMode playMode,
                                                                  String animName) {
        Texture sheet = new Texture(pngPath);
        TextureRegion[][] raw = TextureRegion.split(sheet, frameW, frameH);
        TextureRegion[] frames = new TextureRegion[cols * rows];
        int idx = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                frames[idx++] = raw[r][c];
            }
        }
        return new RotatingSheetAnimationRenderComponent(frames, frameDur, playMode, animName, sheet);
    }

    // ===== Public control API =====
    public void start() { playTime = 0f; }
    public void setRotation(float deg) { this.rotationDeg = deg; }
    public void setBaseRotation(float deg) { this.baseRotationDeg = deg; }
    public void setScale(float sx, float sy) { this.scaleX = sx; this.scaleY = sy; }
    /** Origin relative to entity scale; negative values use the frame center. */
    public void setOrigin(float ox, float oy) { this.originX = ox; this.originY = oy; }
    /** In NORMAL mode, automatically stop updating after the animation completes. */
    public void setAutoRemoveOnFinish(boolean v) { this.autoRemoveOnFinish = v; }
    /** Assign render layer; the RenderService buckets by layer. */
    public void setLayer(int layer) { this.layer = layer; }
    /** Force a fixed zIndex (disables the default -pos.y behavior). */
    public void setZIndexOverride(Float z) { this.zOverride = z; }

    @Override
    public int getLayer() {
        return layer;
    }

    @Override
    public float getZIndex() {
        if (zOverride != null) return zOverride;
        return super.getZIndex(); // Default: -entity.getPosition().y
    }

    @Override
    public void create() {
        super.create(); // Register with RenderService
        // Gdx.app.log("SwordQi", "created layer=" + layer + " z=" + getZIndex());
    }

    @Override
    protected void draw(SpriteBatch batch) {
        TextureRegion frame = animation.getKeyFrame(playTime);
        if (frame == null) return;

        var pos = entity.getPosition();
        var size = entity.getScale(); // Width/height driven by entity scale
        float w = size.x, h = size.y;

        float ox = originX < 0 ? w * 0.5f : originX;
        float oy = originY < 0 ? h * 0.5f : originY;

        if (dbgCount < 3) {
            // Print a few frames only to confirm draw is called
            // Gdx.app.log("SwordQi.draw", "pos=" + pos + " size=" + size + " layer=" + getLayer() + " z=" + getZIndex());
            dbgCount++;
        }

        batch.draw(frame,
                pos.x, pos.y,
                ox, oy,
                w, h,
                scaleX, scaleY,
                rotationDeg + baseRotationDeg
        );

        float last = playTime;
        playTime += time.getDeltaTime();

        if (animation.getPlayMode() == PlayMode.NORMAL
                && last < animation.getAnimationDuration()
                && playTime >= animation.getAnimationDuration()) {
            entity.getEvents().trigger("animationFinished", animationName);
            if (autoRemoveOnFinish) {
                // Depending on your engine, you might disable/flag visibility instead.
                // Here we clamp playTime to just before the last frame to stop progress.
                playTime = animation.getAnimationDuration() - 0.0001f;
            }
        }
    }

    @Override
    public void dispose() {
        if (ownedTexture != null) ownedTexture.dispose();
        super.dispose();
    }

    @Override
    public void close() { dispose(); }
}
