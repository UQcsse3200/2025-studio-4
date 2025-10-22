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

public class PlasmaImpactComponent extends RenderComponent {
  private static TextureRegion texture;
  private final float duration;
  private float elapsed;
  private boolean disposeScheduled;

  public PlasmaImpactComponent(float duration) {
    this.duration = duration;
  }

  private static void ensureTexture() {
    if (texture != null) {
      return;
    }
    int size = 64;
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
          float alpha = MathUtils.clamp(1f - dist / radius, 0f, 1f);
          pixmap.setColor(1f, 0.95f, 0.4f, alpha);
          pixmap.drawPixel(x, y);
        }
      }
    }
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
    if (ServiceLocator.getAudioService() != null) {
      ServiceLocator.getAudioService().playSound("plasma_impact", 1f);
    }
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
    float progress = MathUtils.clamp(elapsed / duration, 0f, 1f);
    float size = 1.2f + progress * 1.8f;
    float alpha = MathUtils.clamp(1f - progress, 0f, 1f);
    float x = entity.getPosition().x - size / 2f;
    float y = entity.getPosition().y - size / 2f;
    Color previous = batch.getColor();
    batch.setColor(1f, 0.95f, 0.6f, alpha);
    batch.draw(texture, x, y, size, size);
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
