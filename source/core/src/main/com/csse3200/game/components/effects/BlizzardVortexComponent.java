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
 * Renders a large, animated blizzard vortex. The effect uses two layered textures which rotate in
 * opposite directions with a subtle opacity pulse to create a swirling snowstorm appearance.
 */
public class BlizzardVortexComponent extends RenderComponent {

  private static TextureRegion[] layers;
  private static TextureRegion snowflakeRegion;
  private static final int NUM_FLAKES = 60;
  private static float[] flakeBaseX;
  private static float[] flakeBaseY;
  private static float[] flakeSpeed;
  private static float[] flakeDrift;
  private static float[] flakeSize;
  private static float[] flakePhase;

  private final float width;
  private final float height;

  private float rotationA = 0f;
  private float rotationB = 0f;
  private float pulseTimer = 0f;

  /**
   * Creates a blizzard vortex covering the specified world-space width and height.
   *
   * @param width world width of the vortex
   * @param height world height of the vortex
   */
  public BlizzardVortexComponent(float width, float height) {
    this.width = width;
    this.height = height;
  }

  @Override
  public void create() {
    super.create();
    ensureTextures();
    ensureFlakeAssets();
  }

  @Override
  public void update() {
    float delta = ServiceLocator.getTimeSource().getDeltaTime();
    rotationA = (rotationA + 18f * delta) % 360f;
    rotationB = (rotationB - 26f * delta) % 360f;
    pulseTimer += delta;
  }

  @Override
  protected void draw(SpriteBatch batch) {
    if (layers == null || layers.length < 2) {
      return;
    }

    float alpha = 0.55f + 0.18f * MathUtils.sin(pulseTimer * 1.4f);
    float centerX = entity.getPosition().x;
    float centerY = entity.getPosition().y;
    float originX = width / 2f;
    float originY = height / 2f;

    Color previous = batch.getColor();
    batch.setColor(1f, 1f, 1f, alpha);
    batch.draw(
        layers[0],
        centerX - originX,
        centerY - originY,
        originX,
        originY,
        width,
        height,
        1f,
        1f,
        rotationA);

    batch.setColor(1f, 1f, 1f, alpha * 0.8f);
    batch.draw(
        layers[1],
        centerX - originX,
        centerY - originY,
        originX,
        originY,
        width,
        height,
        0.92f,
        0.92f,
        rotationB);

    if (snowflakeRegion != null && flakeBaseX != null) {
      float bottomLeftX = centerX - originX;
      float bottomLeftY = centerY - originY;
      float scaleBase = Math.min(width, height);
      Color flakeColour = new Color(0.92f, 0.95f, 1f, 0.9f);
      for (int i = 0; i < NUM_FLAKES; i++) {
        float progress = (flakeBaseY[i] - pulseTimer * flakeSpeed[i]) % 1f;
        if (progress < 0f) {
          progress += 1f;
        }
        float drift = MathUtils.sin(pulseTimer * flakeSpeed[i] + flakePhase[i]) * flakeDrift[i] * width;
        float x = bottomLeftX + flakeBaseX[i] * width + drift;
        float y = bottomLeftY + progress * height;
        float size = MathUtils.clamp(scaleBase * flakeSize[i], scaleBase * 0.02f, scaleBase * 0.12f);

        batch.setColor(flakeColour.r, flakeColour.g, flakeColour.b, flakeColour.a * (0.75f + 0.25f * MathUtils.sin(pulseTimer * flakeSpeed[i] + flakePhase[i])));
        batch.draw(snowflakeRegion, x - size / 2f, y - size / 2f, size, size);
      }
    }

    batch.setColor(previous);
  }

  private static void ensureTextures() {
    if (layers != null) {
      return;
    }
    layers = new TextureRegion[2];
    layers[0] = buildLayer(0f);
    layers[1] = buildLayer(MathUtils.PI / 3f);
  }

  private static void ensureFlakeAssets() {
    if (snowflakeRegion == null) {
      Pixmap pixmap = new Pixmap(16, 16, Pixmap.Format.RGBA8888);
      pixmap.setColor(0f, 0f, 0f, 0f);
      pixmap.fill();
      pixmap.setColor(1f, 1f, 1f, 1f);
      int radius = 7;
      int center = 8;
      for (int y = 0; y < 16; y++) {
        for (int x = 0; x < 16; x++) {
          float dx = x - center + 0.5f;
          float dy = y - center + 0.5f;
          float dist = (float) Math.sqrt(dx * dx + dy * dy);
          if (dist <= radius) {
            float alpha = MathUtils.clamp(1f - dist / radius, 0f, 1f);
            pixmap.setColor(0.95f, 0.98f, 1f, alpha);
            pixmap.drawPixel(x, y);
          }
        }
      }
      Texture texture = new Texture(pixmap);
      texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
      pixmap.dispose();
      snowflakeRegion = new TextureRegion(texture);
    }

    if (flakeBaseX != null) {
      return;
    }

    flakeBaseX = new float[NUM_FLAKES];
    flakeBaseY = new float[NUM_FLAKES];
    flakeSpeed = new float[NUM_FLAKES];
    flakeDrift = new float[NUM_FLAKES];
    flakeSize = new float[NUM_FLAKES];
    flakePhase = new float[NUM_FLAKES];
    java.util.Random random = new java.util.Random(4257);
    for (int i = 0; i < NUM_FLAKES; i++) {
      flakeBaseX[i] = random.nextFloat();
      flakeBaseY[i] = random.nextFloat();
      flakeSpeed[i] = 0.05f + random.nextFloat() * 0.25f;
      flakeDrift[i] = 0.02f + random.nextFloat() * 0.05f;
      flakeSize[i] = 0.04f + random.nextFloat() * 0.08f;
      flakePhase[i] = random.nextFloat() * MathUtils.PI2;
    }
  }

  private static TextureRegion buildLayer(float angleOffset) {
    int size = 256;
    float radius = size / 2f;
    Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
    pixmap.setColor(0f, 0f, 0f, 0f);
    pixmap.fill();

    for (int y = 0; y < size; y++) {
      for (int x = 0; x < size; x++) {
        float dx = x - radius + 0.5f;
        float dy = y - radius + 0.5f;
        float dist = (float) Math.sqrt(dx * dx + dy * dy) / radius;
        if (dist > 1f) {
          continue;
        }
        float angle = MathUtils.atan2(dy, dx) + angleOffset;
        float swirl = MathUtils.sin(angle * 4.5f + dist * 7f);
        float alpha = MathUtils.clamp((1f - dist) * 0.75f + swirl * 0.12f, 0f, 1f);
        if (alpha <= 0f) {
          continue;
        }
        float intensity = 0.9f + 0.06f * MathUtils.cos(angle * 3f);
        float adjustedAlpha = MathUtils.clamp(alpha * 1.15f, 0f, 1f);
        float r = 0.62f * intensity;
        float g = 0.74f * intensity;
        float b = 0.95f * intensity;
        pixmap.setColor(r, g, b, adjustedAlpha);
        pixmap.drawPixel(x, y);
      }
    }

    Texture texture = new Texture(pixmap);
    texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    pixmap.dispose();
    return new TextureRegion(texture);
  }
}
