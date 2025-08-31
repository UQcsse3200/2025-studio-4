package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.services.ServiceLocator;
public class RotatingTextureRenderComponent extends RenderComponent {
  private final Texture texture;
  private float rotationDeg = 0f;

  public RotatingTextureRenderComponent(Texture texture) {
    this.texture = texture;
  }
  public RotatingTextureRenderComponent(String texturePath) {
    this(ServiceLocator.getResourceService().getAsset(texturePath, Texture.class));
  }

  public void setRotation(float deg) { this.rotationDeg = deg; }
  public float getRotation() { return rotationDeg; }

  @Override
  protected void draw(SpriteBatch batch) {
    if (entity == null || texture == null) return;

    Vector2 pos = entity.getPosition();
    Vector2 size = entity.getScale();
    float w = (size == null ? 1f : size.x);
    float h = (size == null ? 1f : size.y);

    // 用支持原点+旋转的重载；原点设为中心
    batch.draw(
      texture,
      pos.x, pos.y,
      w/2f, h/2f,     // originX, originY（围绕中心旋转）
      w, h,           // width, height
      1f, 1f,         // scaleX, scaleY
      rotationDeg,    // rotation (deg)
      0, 0,
      texture.getWidth(), texture.getHeight(),
      false, false
    );
  }
}
