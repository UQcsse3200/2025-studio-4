package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.services.ServiceLocator;

/**
 * A render component that can switch between different textures.
 * Useful for damage effects, state changes, etc.
 */
public class SwitchableTextureRenderComponent extends RenderComponent {
    private Texture currentTexture;
    private float rotationDeg = 0f;
    
    // Tint color applied when drawing (RGBA, includes alpha)
    private final Color tint = new Color(1f, 1f, 1f, 1f);
    
    /**
     * Create a switchable texture render component with an initial texture.
     */
    public SwitchableTextureRenderComponent(Texture initialTexture) {
        this.currentTexture = initialTexture;
    }
    
    /**
     * Create a switchable texture render component from a texture path.
     */
    public SwitchableTextureRenderComponent(String texturePath) {
        this(ServiceLocator.getResourceService().getAsset(texturePath, Texture.class));
    }
    
    /**
     * Switch to a different texture.
     */
    public void setTexture(Texture newTexture) {
        this.currentTexture = newTexture;
    }
    
    /**
     * Switch to a different texture by path.
     */
    public void setTexture(String texturePath) {
        this.currentTexture = ServiceLocator.getResourceService().getAsset(texturePath, Texture.class);
    }
    
    /**
     * Get the current texture.
     */
    public Texture getCurrentTexture() {
        return currentTexture;
    }
    
    /** Set rotation in degrees. */
    public void setRotation(float degrees) { 
        this.rotationDeg = degrees; 
    }
    
    /** Get current rotation in degrees. */
    public float getRotation() { 
        return rotationDeg; 
    }
    
    /** Set the tint color (including alpha). */
    public void setColor(Color color) {
        if (color != null) {
            this.tint.set(color);
        }
    }
    
    /** Set only the alpha (transparency) value, clamped to [0,1]. */
    public void setAlpha(float a) {
        this.tint.a = Math.max(0f, Math.min(a, 1f));
    }
    
    @Override
    protected void draw(SpriteBatch batch) {
        if (entity == null || currentTexture == null) return;
        
        Vector2 pos = entity.getPosition();
        Vector2 size = entity.getScale();
        float w = (size == null ? 1f : size.x);
        float h = (size == null ? 1f : size.y);
        
        // Temporarily apply the tint color (with alpha), then restore old color
        Color old = batch.getColor();
        batch.setColor(tint);
        
        // Draw texture at (x, y) with width and height from entity scale
        batch.draw(currentTexture, pos.x, pos.y, w, h);
        
        batch.setColor(old);
    }
}
