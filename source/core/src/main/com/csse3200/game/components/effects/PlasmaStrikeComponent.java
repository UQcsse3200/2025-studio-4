package com.csse3200.game.components.effects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.rendering.RenderComponent;
import com.csse3200.game.services.ServiceLocator;
import java.util.function.Consumer;

/**
 * Renders a plasma bolt falling from the sky to strike a target position.
 * 
 * <p>This component creates a visual effect of a plasma bolt descending from a specified height
 * to a target position. The bolt falls at a configurable speed and triggers an impact callback
 * when it reaches the ground.</p>
 * 
 * <p>The plasma bolt is procedurally generated as a bright blue circular texture with
 * transparency based on height. The effect automatically disposes itself upon impact.</p>
 * 
 * @author Team1
 * @since sprint 4
 */
public class PlasmaStrikeComponent extends RenderComponent {
  /** Shared texture for all plasma strikes */
  private static TextureRegion strikeTexture;
  /** Target position where the bolt will strike */
  private final Vector2 target = new Vector2();
  /** Initial height from which the bolt starts falling */
  private final float startHeight;
  /** Speed at which the bolt falls (units per second) */
  private final float fallSpeed;
  /** Callback function triggered when the bolt impacts */
  private final Consumer<Vector2> impactListener;
  /** Current height of the falling bolt */
  private float currentHeight;
  /** Whether disposal has been scheduled */
  private boolean disposeScheduled;

  /**
   * Creates a plasma strike component.
   * 
   * @param target target position for the strike
   * @param startHeight initial height from which to start falling
   * @param fallSpeed speed of descent in units per second
   * @param impactListener callback triggered when impact occurs
   */
  public PlasmaStrikeComponent(Vector2 target, float startHeight, float fallSpeed, Consumer<Vector2> impactListener) {
    this.target.set(target);
    this.startHeight = startHeight;
    this.fallSpeed = fallSpeed;
    this.impactListener = impactListener;
  }

  /**
   * Ensures the plasma strike texture is created and cached.
   * Creates a bright blue circular texture procedurally.
   */
  private static void ensureTexture() {
    if (strikeTexture != null) {
      return;
    }
    int size = 48;
    Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
    pixmap.setColor(0f, 0f, 0f, 0f);
    pixmap.fill();
    int center = size / 2;
    float radius = size / 2f;
    for (int y = 0; y < size; y++) {
      for (int x = 0; x < size; x++) {
        float dx = x - center;
        float dy = y - center;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (dist <= radius) {
          float alpha = MathUtils.clamp(1f - (dist / radius), 0f, 1f);
          pixmap.setColor(0.4f, 1f, 0.9f, alpha);
          pixmap.drawPixel(x, y);
        }
      }
    }
    Texture texture = new Texture(pixmap);
    texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    strikeTexture = new TextureRegion(texture);
    pixmap.dispose();
  }

  @Override
  public void create() {
    super.create();
    ensureTexture();
    currentHeight = startHeight;
    entity.setPosition(target.x, target.y + startHeight);
  }

  @Override
  public void update() {
    float delta = ServiceLocator.getTimeSource().getDeltaTime();
    currentHeight -= fallSpeed * delta;
    if (currentHeight <= 0f) {
      impactListener.accept(target.cpy());
      scheduleDispose();
      return;
    }
    entity.setPosition(target.x, target.y + currentHeight);
  }

  @Override
  protected void draw(SpriteBatch batch) {
    float alpha = MathUtils.clamp(currentHeight / startHeight, 0.2f, 1f);
    float size = 0.9f;
    float x = entity.getPosition().x - size / 2f;
    float y = entity.getPosition().y - size / 2f;
    Color previous = batch.getColor();
    batch.setColor(0.4f, 1f, 0.9f, alpha);
    batch.draw(strikeTexture, x, y, size, size);
    batch.setColor(previous);
  }

  /**
   * Schedules the entity for disposal after impact.
   * Uses postRunnable to ensure safe disposal.
   */
  private void scheduleDispose() {
    if (disposeScheduled) {
      return;
    }
    disposeScheduled = true;
    final Entity owningEntity = entity;
    if (owningEntity == null || !owningEntity.isActive()) {
      return;
    }
    Gdx.app.postRunnable(() -> {
      if (owningEntity.isActive()) {
        owningEntity.dispose();
      }
    });
  }
}
