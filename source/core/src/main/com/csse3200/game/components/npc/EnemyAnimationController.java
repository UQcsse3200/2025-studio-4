package com.csse3200.game.components.npc;

import com.csse3200.game.components.Component;
import com.csse3200.game.rendering.AnimationRenderComponent;

/**
 * Controller for enemies: switches between idle,walk and other animations.
 */
public class EnemyAnimationController extends Component {
  private AnimationRenderComponent animator;

  @Override
  public void create() {
    animator = entity.getComponent(AnimationRenderComponent.class);

    entity.getEvents().addListener("wanderStart", this::onWander);
    entity.getEvents().addListener("chaseStart", this::onChase);

    // Default state
    startSafe("idle");
  }

  private void onWander() {
    startSafe("idle");
  }

  private void onChase() {
    startSafe("walk");
  }

  /**
   * Start the animation if it exists, otherwise fall back to walk.
   */
  private void startSafe(String name) {
    if (animator == null) return;
    if (animator.hasAnimation(name)) {
      animator.startAnimation(name);
    } else if (animator.hasAnimation("walk")) {
      animator.startAnimation("walk");
    }
  }
}
