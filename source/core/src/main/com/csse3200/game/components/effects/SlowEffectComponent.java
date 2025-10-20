package com.csse3200.game.components.effects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.enemy.WaypointComponent;
import com.csse3200.game.physics.components.PhysicsMovementComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Applies a temporary slow effect to an entity. While the effect is active the entity is tinted blue
 * and its movement speed is reduced by a configurable factor.
 */
public class SlowEffectComponent extends Component {
  private static final Logger logger = LoggerFactory.getLogger(SlowEffectComponent.class);

  /** Light blue tint used while the effect is active. */
  private static final Color SLOW_COLOR = new Color(0.3f, 0.5f, 1.0f, 1.0f);
  /** Neutral colour used when the effect is not active. */
  private static final Color NORMAL_COLOR = new Color(1.0f, 1.0f, 1.0f, 1.0f);

  /** Slow factor applied to the entity (0.5 = 50% speed). */
  private final float slowFactor;
  /** Duration of the effect in seconds. */
  private final float duration;

  private WaypointComponent waypointComponent;
  private PhysicsMovementComponent physicsMovement;
  private TextureRenderComponent textureRender;
  private AnimationRenderComponent animationRender;

  private boolean isActive = false;
  private float timeRemaining = 0f;
  private Vector2 originalSpeed;
  private Color originalColor;

  /**
   * Creates a slow effect with custom parameters.
   *
   * @param slowFactor factor to apply (0.0 - 1.0). For example 0.5 halves the speed.
   * @param duration duration in seconds.
   */
  public SlowEffectComponent(float slowFactor, float duration) {
    if (slowFactor < 0.0f || slowFactor > 1.0f) {
      throw new IllegalArgumentException("Slow factor must be between 0.0 and 1.0");
    }
    if (duration <= 0f) {
      throw new IllegalArgumentException("Duration must be greater than zero");
    }

    this.slowFactor = slowFactor;
    this.duration = duration;
  }

  /**
   * Creates a slow effect that reduces speed to 40% for one second.
   */
  public SlowEffectComponent() {
    this(0.4f, 1.0f);
  }

  @Override
  public void create() {
    super.create();

    waypointComponent = entity.getComponent(WaypointComponent.class);
    physicsMovement = entity.getComponent(PhysicsMovementComponent.class);
    textureRender = entity.getComponent(TextureRenderComponent.class);
    animationRender = entity.getComponent(AnimationRenderComponent.class);

    entity.getEvents().addListener("applySlow", this::applySlow);
    entity.getEvents().addListener("applySlowWithParams", this::applySlowWithParams);
    entity.getEvents().addListener("removeSlow", this::removeSlow);

    logger.debug("Slow effect initialised - slowFactor={}, duration={}s", slowFactor, duration);
  }

  @Override
  public void update() {
    if (!isActive) {
      return;
    }

    float deltaTime = ServiceLocator.getTimeSource().getDeltaTime();
    timeRemaining -= deltaTime;
    if (timeRemaining <= 0f) {
      removeSlow();
    }
  }

  /**
   * Applies the slow effect using the default parameters specified for this component instance.
   * If the effect is already active only the duration is refreshed.
   */
  public void applySlow() {
    if (isActive) {
      timeRemaining = duration;
      logger.debug("Refreshed slow effect duration timer");
      return;
    }

    cacheOriginalSpeed();
    applySlowFactor();

    applyBlueEffect();
    isActive = true;
    timeRemaining = duration;
    entity.getEvents().trigger("slowEffectApplied");
    logger.debug("Slow effect applied ({}% speed, {}s)", (int) (slowFactor * 100), duration);
  }

  /**
   * Applies the slow effect using custom parameters. Currently this implementation simply falls back
   * to {@link #applySlow()} after refreshing with the component defaults.
   *
   * @param customSlowFactor custom slow factor.
   * @param customDuration custom effect duration.
   */
  public void applySlowWithParams(float customSlowFactor, float customDuration) {
    // Simplified implementation: we keep behaviour aligned with the default parameters.
    applySlow();
  }

  /**
   * Removes the slow effect and restores the entity's original movement speed and colour.
   */
  public void removeSlow() {
    if (!isActive) {
      return;
    }

    restoreOriginalSpeed();
    removeBlueEffect();

    isActive = false;
    timeRemaining = 0f;
    originalSpeed = null;
    originalColor = null;

    entity.getEvents().trigger("slowEffectRemoved");
    logger.info("Slow effect removed from entity");
  }

  private void cacheOriginalSpeed() {
    if (waypointComponent != null) {
      originalSpeed = new Vector2(waypointComponent.getSpeed());
    } else if (physicsMovement != null) {
      originalSpeed = new Vector2(physicsMovement.maxSpeed);
    }
  }

  private void applySlowFactor() {
    if (originalSpeed == null) {
      return;
    }

    Vector2 slowedSpeed = new Vector2(originalSpeed.x * slowFactor, originalSpeed.y * slowFactor);

    if (waypointComponent != null) {
      waypointComponent.setSpeed(slowedSpeed);
    }

    if (physicsMovement != null) {
      physicsMovement.maxSpeed.set(slowedSpeed);
      physicsMovement.setSpeed(slowedSpeed);
    }

    logger.debug("Applied slow: original speed {}, slowed {}", originalSpeed, slowedSpeed);
  }

  private void restoreOriginalSpeed() {
    if (originalSpeed == null) {
      return;
    }

    if (waypointComponent != null) {
      try {
        waypointComponent.setSpeed(originalSpeed);
      } catch (Exception ignored) {
      }
    }

    if (physicsMovement != null) {
      physicsMovement.maxSpeed.set(originalSpeed);
      physicsMovement.setSpeed(originalSpeed);
    }

    logger.debug("Restored speed {}", originalSpeed);
  }

  private void applyBlueEffect() {
    if (animationRender != null) {
      originalColor = animationRender.getColor();
      animationRender.setColor(SLOW_COLOR);
      logger.info("Applied blue tint to animation renderer - {}", SLOW_COLOR);
      return;
    }

    if (textureRender != null) {
      originalColor = textureRender.getColor();
      textureRender.setColor(SLOW_COLOR);
      logger.info("Applied blue tint to texture renderer - {}", SLOW_COLOR);
      return;
    }

    logger.warn("Unable to apply blue tint: no renderer component found");
  }

  private void removeBlueEffect() {
    if (animationRender != null) {
      if (originalColor != null) {
        animationRender.setColor(originalColor);
        logger.info("Restored animation renderer colour to {}", originalColor);
      } else {
        animationRender.setColor(NORMAL_COLOR);
        logger.info("Restored animation renderer colour to default");
      }
      return;
    }

    if (textureRender != null) {
      if (originalColor != null) {
        textureRender.setColor(originalColor);
        logger.info("Restored texture renderer colour to {}", originalColor);
      } else {
        textureRender.setColor(NORMAL_COLOR);
        logger.info("Restored texture renderer colour to default");
      }
      return;
    }

    logger.warn("Unable to restore colour: no renderer component found");
  }

  /**
   * @return {@code true} if the slow effect is currently active.
   */
  public boolean isSlowActive() {
    return isActive;
  }

  /**
   * @return remaining effect duration in seconds.
   */
  public float getTimeRemaining() {
    return timeRemaining;
  }

  /**
   * @return configured slow factor.
   */
  public float getSlowFactor() {
    return slowFactor;
  }

  @Override
  public void dispose() {
    if (isActive) {
      removeSlow();
    }
    super.dispose();
  }
}
