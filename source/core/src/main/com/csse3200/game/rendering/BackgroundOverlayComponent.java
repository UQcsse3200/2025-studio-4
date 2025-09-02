package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.services.ServiceLocator;

/**
 * Render a full-map background overlay by stretching a texture to a target world size.
 * Intended to draw above terrain (layer 1) but below entities (default entity layers/zIndex).
 */
public class BackgroundOverlayComponent extends RenderComponent {
  private final Texture texture;
  private final Vector2 worldSize;
  private final float zIndex;

  /**
   * @param texturePath Asset path of the overlay texture (must be preloaded)
   * @param worldWidth  Target width in world units
   * @param worldHeight Target height in world units
   * @param zIndexOverride Fixed z-index to ensure draw order (smaller draws later). Use 0 < z < entities.
   */
  public BackgroundOverlayComponent(String texturePath, float worldWidth, float worldHeight, float zIndexOverride) {
    this.texture = ServiceLocator.getResourceService().getAsset(texturePath, Texture.class);
    this.worldSize = new Vector2(worldWidth, worldHeight);
    this.zIndex = zIndexOverride;
  }

  @Override
  protected void draw(SpriteBatch batch) {
    Vector2 pos = entity.getPosition();
    batch.draw(texture, pos.x, pos.y, worldSize.x, worldSize.y);
  }

  @Override
  public float getZIndex() {
    return zIndex;
  }

  @Override
  public int getLayer() {
    // Draw far below the terrain to guarantee all TiledMap layers render above it
    return -1000;
  }
}


