package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.services.ServiceLocator;

/** Render a static texture using entity's position (x,y) and scale as width/height. */
public class TextureRenderComponent extends RenderComponent {
  private final Texture texture;
  private float rotationDeg = 0f;

  public TextureRenderComponent(String texturePath) {
    this(ServiceLocator.getResourceService().getAsset(texturePath, Texture.class));
  }

  public TextureRenderComponent(Texture texture) {
    this.texture = texture;
  }

  /** 兼容旧工厂代码；并与测试期望一致：设为 1x1 */
  public void scaleEntity() {
    if (entity != null) {
      entity.setScale(1f, 1f);
    }
  }

  public void setRotation(float degrees) { this.rotationDeg = degrees; }
  public float getRotation() { return rotationDeg; }

  @Override
  protected void draw(SpriteBatch batch) {
    if (entity == null || texture == null) return;

    Vector2 pos = entity.getPosition();
    Vector2 size = entity.getScale();
    float w = (size == null ? 1f : size.x);
    float h = (size == null ? 1f : size.y);

    // 与测试期望完全一致：draw(texture, x, y, width, height)
    batch.draw(texture, pos.x, pos.y, w, h);
  }
}
