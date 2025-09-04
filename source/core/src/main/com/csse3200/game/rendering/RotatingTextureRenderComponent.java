package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.services.ServiceLocator;

/**
 * A render component that supports rotating a texture around its center.
 * <p>
 * Extends {@link RenderComponent} and draws a texture with rotation applied.
 * Rotation is specified in degrees and can be set dynamically by other
 * components (e.g., {@code HeroTurretAttackComponent}) to make the sprite
 * face a target direction.
 * </p>
 */
public class RotatingTextureRenderComponent extends RenderComponent {
  /** Texture to render */
  private final Texture texture;

  /** Current rotation angle in degrees */
  private float rotationDeg = 0f;

  /**
   * Create a rotating texture render component from an existing {@link Texture}.
   *
   * @param texture the texture to render
   */
  public RotatingTextureRenderComponent(Texture texture) {
    this.texture = texture;
  }

  /**
   * Create a rotating texture render component from a texture path.
   * The texture is loaded via the {@link ServiceLocator}'s ResourceService.
   *
   * @param texturePath path to the texture asset
   */
  public RotatingTextureRenderComponent(String texturePath) {
    this(ServiceLocator.getResourceService().getAsset(texturePath, Texture.class));
  }

  /**
   * Set the rotation angle.
   *
   * @param deg rotation in degrees
   */
  public void setRotation(float deg) {
    this.rotationDeg = deg;
  }

  /**
   * Get the current rotation angle.
   *
   * @return rotation in degrees
   */
  public float getRotation() {
    return rotationDeg;
  }

  /**
   * Draw the texture with rotation around its center.
   * <p>
   * Uses {@link SpriteBatch#draw(Texture, float, float, float, float, float, float, float, float, float,
   * int, int, int, int, boolean, boolean)} which supports origin and rotation.
   * </p>
   *
   * @param batch the sprite batch to draw with
   */
  @Override
  protected void draw(SpriteBatch batch) {
    if (entity == null || texture == null) return;

    Vector2 pos = entity.getPosition();
    Vector2 size = entity.getScale();
    float w = (size == null ? 1f : size.x);
    float h = (size == null ? 1f : size.y);

    // Draw with rotation around the center of the sprite
    batch.draw(
            texture,
            pos.x, pos.y,
            w / 2f, h / 2f,   // originX, originY (rotate around center)
            w, h,             // width, height
            1f, 1f,           // scaleX, scaleY
            rotationDeg,      // rotation in degrees
            0, 0,
            texture.getWidth(), texture.getHeight(),
            false, false
    );
  }
}

