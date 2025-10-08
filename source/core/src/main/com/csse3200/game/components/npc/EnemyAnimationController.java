package com.csse3200.game.components.npc;

import com.badlogic.gdx.Gdx;
import com.csse3200.game.components.Component;
import com.csse3200.game.rendering.AnimationRenderComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for enemies: switches between idle, walk and other animations.
 */
public class EnemyAnimationController extends Component {
  private static final Logger logger = LoggerFactory.getLogger(EnemyAnimationController.class);
  
  public static final float ATTACK_FRAME_DUR = 0.5f; 
  public static final float DEATH_FRAME_DUR  = 0.5f;
  
  private AnimationRenderComponent animator;
  private boolean waitingForAttackFinish = false;
  private boolean waitingForDeathFinish = false;
  private boolean isDead = false;
  private boolean deathAnimationQueued = false;

  @Override
  public void create() {
    animator = entity.getComponent(AnimationRenderComponent.class);

    entity.getEvents().addListener("wanderStart", this::onWander);
    entity.getEvents().addListener("chaseStart", this::onChase);
    entity.getEvents().addListener("attackStart", this::onAttack);
    entity.getEvents().addListener("entityDeath", this::onDeath);
    entity.getEvents().addListener("animationFinished", this::onAnimationFinished);

    // Default state
    startSafe("idle");
    
    logger.info("EnemyAnimationController created for entity {}", entity.getId());
    logger.info("  - Has attack: {}", animator.hasAnimation("attack"));
    logger.info("  - Has death: {}", animator.hasAnimation("death"));
  }

  private void onWander() {
    if (!waitingForAttackFinish && !waitingForDeathFinish && !isDead) {
      logger.debug("{} - onWander -> idle", entity.getId());
      startSafe("idle");
    }
  }

  private void onChase() {
    if (!waitingForAttackFinish && !waitingForDeathFinish && !isDead) {
      logger.debug("{} - onChase -> walk", entity.getId());
      startSafe("walk");
    }
  }

  private void onAttack() {
    logger.info("{} - onAttack called (isDead={}, waitingForDeath={}, hasAttack={})", 
                entity.getId(), isDead, waitingForDeathFinish, animator.hasAnimation("attack"));
    
    if (isDead) {
      logger.info("{} - Already dead, ignoring attack", entity.getId());
      return;
    }
    
    if (animator.hasAnimation("attack")) {
      logger.info("{} - Starting attack animation", entity.getId());
      animator.startAnimation("attack");
      waitingForAttackFinish = true;
    } else {
      logger.info("{} - No attack animation available", entity.getId());
    }
  }

  private void onAnimationFinished(String animName) {
    logger.info("{} - Animation finished: {} (waitingAttack={}, waitingDeath={}, isDead={}, deathQueued={})", 
                entity.getId(), animName, waitingForAttackFinish, waitingForDeathFinish, isDead, deathAnimationQueued);
    
    if (waitingForAttackFinish && "attack".equals(animName)) {
      waitingForAttackFinish = false;
      logger.info("{} - Attack animation complete", entity.getId());
      
      // Notify that attack animation is complete (for suicide attacks)
      entity.getEvents().trigger("attackAnimationComplete");
      
      // Check if we died during the attack
      if (deathAnimationQueued) {
        logger.info("{} - Death was queued, playing now", entity.getId());
        deathAnimationQueued = false;
        playDeathAnimation();
      } else if (!isDead) {
        logger.info("{} - Still alive, returning to idle", entity.getId());
        startSafe("idle");
      }
    } else if (waitingForDeathFinish && "death".equals(animName)) {
      waitingForDeathFinish = false;
      logger.info("{} - Death animation complete, disposing", entity.getId());
      onDeathFinished();
    }
  }

  private void onDeath() {
    logger.info("{} - onDeath called (isDead={}, waitingAttack={}, waitingDeath={})", 
                entity.getId(), isDead, waitingForAttackFinish, waitingForDeathFinish);
    
    if (isDead) {
      logger.info("{} - Already processed death, ignoring", entity.getId());
      return;
    }
    
    isDead = true;
    
    // Stop all movement immediately
    entity.getEvents().trigger("stopMovement");
    logger.info("{} - Triggered stopMovement", entity.getId());
    
    // If we're currently playing an attack animation, queue the death animation
    if (waitingForAttackFinish) {
      logger.info("{} - Attack in progress, queueing death animation", entity.getId());
      deathAnimationQueued = true;
      return;
    }
    
    // Not attacking - play death animation immediately
    playDeathAnimation();
  }

  private void playDeathAnimation() {
    logger.info("{} - playDeathAnimation called (hasDeath={})", 
                entity.getId(), animator.hasAnimation("death"));
    
    if (animator.hasAnimation("death")) {
      logger.info("{} - Starting death animation", entity.getId());
      animator.startAnimation("death");
      waitingForDeathFinish = true;
    } else {
      logger.info("{} - No death animation, disposing immediately", entity.getId());
      onDeathFinished();
    }
  }

  private void onDeathFinished() {
    logger.info("{} - Scheduling entity disposal", entity.getId());
    Gdx.app.postRunnable(() -> {
      logger.info("{} - Disposing entity now", entity.getId());
      entity.dispose();
    });
  }

  /**
   * Start the animation if it exists, otherwise fall back to walk.
   */
  private void startSafe(String name) {
    if (animator == null || isDead) return;
    if (animator.hasAnimation(name)) {
      animator.startAnimation(name);
    } else if (animator.hasAnimation("walk")) {
      animator.startAnimation("walk");
    }
  }
}