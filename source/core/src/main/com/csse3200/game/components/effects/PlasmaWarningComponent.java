package com.csse3200.game.components.effects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.rendering.RenderComponent;
import com.csse3200.game.services.ServiceLocator;

public class PlasmaWarningComponent extends RenderComponent {
  private static TextureRegion texture;
  private final float duration;
  private final float size;
  private float elapsed;
  private boolean disposeScheduled;

  public PlasmaWarningComponent(float duration, float size) {
    this.duration = duration;
    this.size = size;
  }

  private static void ensureTexture() {
    if (texture != null) {
      return;
    }
    Pixmap pixmap = new Pixmap(24, 48, Pixmap.Format.RGBA8888);
    pixmap.setColor(0f, 0f, 0f, 0f);
    pixmap.fill();
    pixmap.setColor(1f, 0.95f, 0.25f, 1f);
    pixmap.fillRectangle(10, 16, 4, 24);
    pixmap.fillRectangle(10, 4, 4, 8);
    pixmap.setColor(0.15f, 0.05f, 0f, 1f);
    pixmap.drawRectangle(9, 15, 6, 26);
    pixmap.drawRectangle(9, 3, 6, 10);
    Texture tex = new Texture(pixmap);
    tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    texture = new TextureRegion(tex);
    pixmap.dispose();
  }

  @Override
  public void create() {
    super.create();
    ensureTexture();
    elapsed = 0f;
  }

  @Override
  public void update() {
    elapsed += ServiceLocator.getTimeSource().getDeltaTime();
    if (elapsed >= duration) {
      scheduleDispose();
    }
  }

  @Override
  protected void draw(SpriteBatch batch) {
    float pulse = 0.85f + 0.15f * MathUtils.sin(elapsed * 8f);
    float width = size * pulse;
    float height = width * 2f;
    float x = entity.getPosition().x - width / 2f;
    float y = entity.getPosition().y - height / 2f;
    Color previous = batch.getColor();
    batch.setColor(1f, 0.9f, 0.2f, 1f);
    batch.draw(texture, x, y, width, height);
    batch.setColor(previous);
  }

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
