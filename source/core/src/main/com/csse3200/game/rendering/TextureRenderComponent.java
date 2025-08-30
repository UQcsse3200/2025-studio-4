package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.services.ServiceLocator;
import com.badlogic.gdx.graphics.g2d.TextureRegion;


/** Render a static texture. */
public class TextureRenderComponent extends RenderComponent {
  // 原本是 Texture
  private final TextureRegion region;
  private float rotationDeg = 0f;

  public TextureRenderComponent(String texturePath) {
    this(ServiceLocator.getResourceService().getAsset(texturePath, Texture.class));
  }

  public TextureRenderComponent(Texture texture) {
    // 包装成 TextureRegion
    this.region = new TextureRegion(texture);
  }

  public void scaleEntity() {
    entity.setScale(1f, (float) region.getRegionHeight() / region.getRegionWidth());
  }

  public void setRotation(float degrees) {
    this.rotationDeg = degrees;
  }

  public float getRotation() {
    return rotationDeg;
  }

  @Override
  protected void draw(SpriteBatch batch) {
    Vector2 position = entity.getPosition();
    Vector2 size = entity.getScale();

    float originX = size.x * 0.5f;
    float originY = size.y * 0.5f;

    batch.draw(
            region,
            position.x, position.y,
            originX, originY,
            size.x, size.y,
            1f, 1f,
            rotationDeg
    );
  }
}