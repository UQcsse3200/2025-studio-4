package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;

import java.util.HashMap;
import java.util.Map;

/**
 * Draws an animated TextureAtlas region with rotation.
 * - Call addAnimation(name, frameDuration, mode) once per clip, then start(name).
 * - Rotate via setRotation(deg). Pivot is entity center by default.
 * - If the atlas is loaded from a global ResourceService, pass ownsAtlas=false.
 */
public class RotatingAnimationRenderComponent extends RenderComponent {
    private final TextureAtlas atlas;          // may be shared
    private final boolean ownsAtlas;           // dispose if true
    private final GameTime time;

    private final Map<String, Animation<TextureRegion>> anims = new HashMap<>();
    private Animation<TextureRegion> current;
    private String currentName;
    private float t = 0f;

    private float rotationDeg = 0f;
    private boolean originCenter = true;
    private final Color tint = new Color(1,1,1,1);

    public RotatingAnimationRenderComponent(TextureAtlas atlas, boolean ownsAtlas) {
        this.atlas = atlas;
        this.ownsAtlas = ownsAtlas;
        this.time = ServiceLocator.getTimeSource();
    }

    /** Convenience if you want to resolve atlas via ResourceService. */
    public static RotatingAnimationRenderComponent fromAsset(String atlasPath, boolean ownsAtlas) {
        TextureAtlas a = ServiceLocator.getResourceService().getAsset(atlasPath, TextureAtlas.class);
        return new RotatingAnimationRenderComponent(a, ownsAtlas);
    }

    public boolean addAnimation(String name, float frameDuration, PlayMode mode) {
        var regions = atlas.findRegions(name);
        if (regions == null || regions.size == 0) return false;
        anims.put(name, new Animation<>(frameDuration, regions, mode));
        return true;
    }

    public void start(String name) {
        Animation<TextureRegion> a = anims.get(name);
        if (a == null) return;
        current = a;
        currentName = name;
        t = 0f;
    }

    public void setRotation(float degrees) { this.rotationDeg = degrees; }
    public void setOriginCenter(boolean v) { this.originCenter = v; }
    public void setTint(Color c) { if (c != null) this.tint.set(c); }
    public void setAlpha(float a) { this.tint.a = Math.max(0f, Math.min(1f, a)); }
    public String getCurrent() { return currentName; }

    @Override
    protected void draw(SpriteBatch batch) {
        if (current == null) return;

        TextureRegion frame = current.getKeyFrame(t);
        Vector2 pos = entity.getPosition();
        Vector2 scale = entity.getScale();

        float w = (scale == null ? 1f : scale.x);
        float h = (scale == null ? 1f : scale.y);
        float ox = originCenter ? w * 0.5f : 0f;
        float oy = originCenter ? h * 0.5f : 0f;

        Color old = batch.getColor();
        batch.setColor(tint);
        batch.draw(frame, pos.x, pos.y, ox, oy, w, h, 1f, 1f, rotationDeg);
        batch.setColor(old);

        t += time.getDeltaTime();
    }

    @Override
    public void dispose() {
        if (ownsAtlas && atlas != null) atlas.dispose();
        super.dispose();
    }
}