package com.csse3200.game.components.effects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.csse3200.game.rendering.RenderComponent;
import com.csse3200.game.services.ServiceLocator;

/**
 * Renders a subtle animated overlay to represent a slow-zone tile (e.g., swamp, ice).
 * The component procedurally generates a soft radial texture and pulsates its alpha/scale
 * to keep the effect lightweight without extra art assets.
 */
public class SlowZoneVisualComponent extends RenderComponent {
  private static TextureRegion cachedRegion;

  private final float tileSize;
  private final Color tint;
  private float elapsed = 0f;

  public SlowZoneVisualComponent(float tileSize) {
    this(tileSize, new Color(1.0f, 1.0f, 1.0f, 1f));
  }

  public SlowZoneVisualComponent(float tileSize, Color tint) {
    this.tileSize = tileSize;
    this.tint = new Color(tint);
  }

  @Override
  public void create() {
    super.create();
    ensureRegion();
  }

  @Override
  public void update() {
    elapsed += ServiceLocator.getTimeSource().getDeltaTime();
  }

  @Override
  protected void draw(SpriteBatch batch) {
    if (cachedRegion == null) {
      return;
    }

    float pulse = 1.0f; // 设置为不透明
    float scale = 1.5f + 0.1f * MathUtils.sin(elapsed * 2.6f); // 增大基础尺寸
    float width = tileSize * scale;
    float height = tileSize * scale;

    float offsetX = (tileSize - width) / 2f;
    float offsetY = (tileSize - height) / 2f;

    Color previous = batch.getColor();
    batch.setColor(tint.r, tint.g, tint.b, pulse);
    batch.draw(cachedRegion, entity.getPosition().x + offsetX, entity.getPosition().y + offsetY, width, height);
    float highlightAlpha = 1.0f; // 设置为不透明
    batch.setColor(1.0f, 1.0f, 1.0f, highlightAlpha); // 改为白色
    float innerScale = scale * 0.85f; // 稍微调整内层比例
    float innerWidth = tileSize * innerScale;
    float innerHeight = tileSize * innerScale;
    float innerOffsetX = (tileSize - innerWidth) / 2f;
    float innerOffsetY = (tileSize - innerHeight) / 2f;
    batch.draw(cachedRegion, entity.getPosition().x + innerOffsetX, entity.getPosition().y + innerOffsetY,
        innerWidth, innerHeight);
    batch.setColor(previous);
  }

  private static void ensureRegion() {
    if (cachedRegion != null) {
      return;
    }
    final int size = 128;
    Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
    int centerX = size / 2;
    int centerY = size / 2;
    float maxRadiusSq = centerX * centerX;
    for (int y = 0; y < size; y++) {
      for (int x = 0; x < size; x++) {
        float dx = x - centerX;
        float dy = y - centerY;
        float distSq = dx * dx + dy * dy;
        float radiusNorm = MathUtils.clamp((float) Math.sqrt(distSq / maxRadiusSq), 0f, 1f);
        float baseAlpha;
        if (radiusNorm < 0.45f) {
          baseAlpha = 0.95f;
        } else if (radiusNorm < 0.7f) {
          baseAlpha = 0.65f;
        } else if (radiusNorm < 0.95f) {
          baseAlpha = 0.3f;
        } else {
          baseAlpha = 0f;
        }
        float angle = MathUtils.atan2(dy, dx);
        float ripple = 0.6f + 0.4f * MathUtils.sin(angle * 6f + radiusNorm * 8f);
        float alpha = MathUtils.clamp(baseAlpha * ripple, 0f, 1f);
        pixmap.setColor(1f, 1f, 1f, alpha);
        pixmap.drawPixel(x, y);
      }
    }
    Texture texture = new Texture(pixmap);
    texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    pixmap.dispose();
    cachedRegion = new TextureRegion(texture);
  }
}
