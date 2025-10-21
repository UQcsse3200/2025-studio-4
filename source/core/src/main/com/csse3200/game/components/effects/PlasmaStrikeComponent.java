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

public class PlasmaStrikeComponent extends RenderComponent {
  private static TextureRegion strikeTexture;
  private final Vector2 target = new Vector2();
  private final float startHeight;
  private final float fallSpeed;
  private final Consumer<Vector2> impactListener;
  private float currentHeight;
  private boolean disposeScheduled;

  public PlasmaStrikeComponent(Vector2 target, float startHeight, float fallSpeed, Consumer<Vector2> impactListener) {
    this.target.set(target);
    this.startHeight = startHeight;
    this.fallSpeed = fallSpeed;
    this.impactListener = impactListener;
  }

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
