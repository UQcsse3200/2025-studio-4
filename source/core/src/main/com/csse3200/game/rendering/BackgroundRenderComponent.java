package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.csse3200.game.services.ServiceLocator;

/**
 * A render component specifically for rendering a background texture that fills the entire camera viewport.
 * This component renders on a very low layer to ensure it's behind all other game elements.
 */
public class BackgroundRenderComponent extends RenderComponent {
  private static final int BACKGROUND_LAYER = -1000;
  private Texture backgroundTexture;
  
  /**
   * Create a background render component with the specified texture path.
   * @param texturePath Path to the background texture asset
   */
  public BackgroundRenderComponent(String texturePath) {
    try {
      this.backgroundTexture = ServiceLocator.getResourceService().getAsset(texturePath, Texture.class);
      if (this.backgroundTexture != null) {
        this.backgroundTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
      }
    } catch (Exception e) {
      // If texture loading fails, continue without background
      this.backgroundTexture = null;
    }
  }
  
  @Override
  protected void draw(SpriteBatch batch) {
    if (backgroundTexture == null) {
      return;
    }
    
    // Get the camera to determine viewport bounds
    OrthographicCamera camera = (OrthographicCamera) Renderer.getCurrentRenderer().getCamera().getCamera();
    
    // Calculate viewport bounds in world coordinates
    float camX = camera.position.x;
    float camY = camera.position.y;
    
    // Use zoom to calculate the actual visible area
    float camWidth = camera.viewportWidth * camera.zoom;
    float camHeight = camera.viewportHeight * camera.zoom;
    
    // Calculate bottom-left corner of the viewport
    float x = camX - camWidth / 2f;
    float y = camY - camHeight / 2f;
    
    // Draw the background texture to fill the entire viewport
    batch.draw(backgroundTexture, x, y, camWidth, camHeight);
  }
  
  @Override
  public int getLayer() {
    return BACKGROUND_LAYER;
  }
  
  @Override
  public float getZIndex() {
    // Background should be at the very back - use minimum value
    return Float.MIN_VALUE;
  }
  
  @Override
  public void dispose() {
    super.dispose();
    // Don't dispose texture as it's managed by ResourceService
  }
}

