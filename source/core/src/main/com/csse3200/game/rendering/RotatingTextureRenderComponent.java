package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.services.ServiceLocator;

/**
 * A render component that supports rotation around the center of the sprite
 * and allows switching textures at runtime.
 */
public class RotatingTextureRenderComponent extends RenderComponent {
    /**
     * Current texture object (not final, allows switching).
     */
    private Texture texture;
    /**
     * Path of the current texture (may be null if constructed from a Texture directly).
     */
    private String texturePath;

    /**
     * Current rotation angle in degrees.
     */
    private float rotationDeg = 0f;

    /**
     * Construct with an existing Texture.
     */
    public RotatingTextureRenderComponent(Texture texture) {
        this.texture = texture;
        this.texturePath = null;
    }

    /**
     * Construct with a texture path (retrieved from ResourceService).
     */
    public RotatingTextureRenderComponent(String texturePath) {
        this.texturePath = texturePath;
        this.texture = ServiceLocator.getResourceService().getAsset(texturePath, Texture.class);
    }

    /**
     * Set rotation angle in degrees.
     */
    public void setRotation(float deg) {
        this.rotationDeg = deg;
    }

    /**
     * Get rotation angle in degrees.
     */
    public float getRotation() {
        return rotationDeg;
    }

    /**
     * Get the current texture (may be null if the asset failed to load).
     */
    public Texture getTexture() {
        return texture;
    }

    /**
     * Get the current texture path (may be null).
     */
    public String getTexturePath() {
        return texturePath;
    }

    /**
     * Switch texture at runtime using a path.
     * If the resource has not been preloaded, it will be synchronously loaded,
     * which may cause a brief stall. Preloading textures is recommended.
     */
    public void setTexture(String newPath) {
        if (newPath == null || newPath.isBlank()) return;
        if (newPath.equals(this.texturePath) && this.texture != null) return;

        var rs = ServiceLocator.getResourceService();
        Texture tex = rs.getAsset(newPath, Texture.class);
        if (tex == null) {
            // Fallback: load texture synchronously (not recommended for runtime performance)
            rs.loadTextures(new String[]{newPath});
            while (!rs.loadForMillis(5)) { /* spin briefly */ }
            tex = rs.getAsset(newPath, Texture.class);
            if (tex == null) return; // Abort if still failed
        }

        this.texture = tex;
        this.texturePath = newPath;
        // Optional: notify rendering system about the texture change (e.g., via event or dirty flag)
    }

    /**
     * Switch texture at runtime by directly providing a Texture.
     * Note: The caller must ensure the texture's lifecycle is managed
     * (e.g., by ResourceService) or properly disposed at the end of the scene.
     */
    public void setTexture(Texture newTexture) {
        if (newTexture == null || newTexture == this.texture) return;
        this.texture = newTexture;
        // Do not update texturePath (only tracked when loaded from a path).
    }

    /**
     * Draws the texture rotated around its center.
     */
    @Override
    protected void draw(SpriteBatch batch) {
        if (entity == null || texture == null) return;

        Vector2 pos = entity.getPosition();
        Vector2 size = entity.getScale();
        float w = (size == null ? 1f : size.x);
        float h = (size == null ? 1f : size.y);

        batch.draw(
                texture,
                pos.x, pos.y,
                w / 2f, h / 2f,     // Origin (center)
                w, h,               // Width & height (entity scale as world size)
                1f, 1f,             // Scale
                rotationDeg,        // Rotation angle in degrees
                0, 0,
                texture.getWidth(), texture.getHeight(),
                false, false
        );
    }
}



