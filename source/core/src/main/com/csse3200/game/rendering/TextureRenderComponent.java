package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.services.ServiceLocator;

/**
 * A render component for drawing a static texture.
 * <p>
 * The entity's position defines the bottom-left corner of the texture.
 * The entity's scale defines the width and height.
 * Supports tinting (including transparency) and rotation (not applied in draw yet).
 */
public class TextureRenderComponent extends RenderComponent {
  private final Texture texture;
  private float rotationDeg = 0f;

  // Tint color applied when drawing (RGBA, includes alpha)
  private final Color tint = new Color(1f, 1f, 1f, 1f);

  /**
   * Load the texture from the asset service by its path.
   */
  public TextureRenderComponent(String texturePath) {
    this(ServiceLocator.getResourceService().getAsset(texturePath, Texture.class));
  }

  /**
   * Create a render component with an already loaded texture.
   */
  public TextureRenderComponent(Texture texture) {
    this.texture = texture;
  }

  /**
   * Compatibility helper for legacy factory code.
   * Sets the entity scale to 1x1 so the texture renders at natural size.
   */
  public void scaleEntity() {
    if (entity != null) {
      entity.setScale(1f, 1f);
    }
  }

  /** Set rotation in degrees (not applied in draw yet). */
  public void setRotation(float degrees) { this.rotationDeg = degrees; }

  /** Get current rotation in degrees. */
  public float getRotation() { return rotationDeg; }

  /** Set the tint color (including alpha). */
  public void setColor(Color color) {
    if (color != null) {
      this.tint.set(color);
    }
  }

  /** Get the current tint color. */
  public Color getColor() {
    return new Color(tint);
  }

  /** Set only the alpha (transparency) value, clamped to [0,1]. */
  public void setAlpha(float a) {
    this.tint.a = Math.max(0f, Math.min(a, 1f));
  }

  @Override
  protected void draw(SpriteBatch batch) {
    if (entity == null || texture == null) return;

    Vector2 pos = entity.getPosition();
    Vector2 size = entity.getScale();
    float w = (size == null ? 1f : size.x);
    float h = (size == null ? 1f : size.y);

    // Temporarily apply the tint color (with alpha), then restore old color
    Color old = batch.getColor();
    batch.setColor(tint);

    // Draw texture at (x, y) with width and height from entity scale
    batch.draw(texture, pos.x, pos.y, w, h);

    batch.setColor(old);
  }
}

