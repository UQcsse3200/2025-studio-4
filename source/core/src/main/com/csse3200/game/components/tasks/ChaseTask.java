package com.csse3200.game.components.tasks;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.DefaultTask;
import com.csse3200.game.ai.tasks.PriorityTask;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.raycast.RaycastHit;
import com.csse3200.game.rendering.DebugRenderer;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;

/** Chases a target entity until they get too far away or line of sight is lost */
public class ChaseTask extends DefaultTask implements PriorityTask {
  public Vector2 speed;
  private final Entity target;
  private final int priority;
  private final float viewDistance;
  private final float maxChaseDistance;
  private final PhysicsEngine physics;
  private final DebugRenderer debugRenderer;
  private final RaycastHit hit = new RaycastHit();
  private MovementTask movementTask;
  public boolean finished = false;
  
  // Prevent instant completion when task starts at target
  private static final float MIN_ACTIVE_TIME = 0.05f; // 50ms minimum
  private float activeTime = 0f;

  public ChaseTask(Entity target, int priority, float viewDistance, float maxChaseDistance, Vector2 speed) {
    this.target = target;
    this.priority = priority;
    this.viewDistance = viewDistance;
    this.maxChaseDistance = maxChaseDistance;
    this.speed = speed;
    physics = ServiceLocator.getPhysicsService().getPhysics();
    debugRenderer = ServiceLocator.getRenderService().getDebug();
  }

  public boolean isFinished() {
    return finished;
  }

  @Override
  public void start() {
    super.start();
    activeTime = 0f; // Reset timer when task starts
    movementTask = new MovementTask(target.getPosition(), speed);
    movementTask.create(owner);
    movementTask.start();
    
    this.owner.getEntity().getEvents().trigger("chaseStart");
  }

  @Override
  public void update() {
    GameTime time = ServiceLocator.getTimeSource();
    if (time != null && time.getTimeScale() == 0f) {
      return; // Don't process anything while paused
    }
    
    // Track active time
    if (time != null) {
      activeTime += time.getDeltaTime();
    }
    
    movementTask.setTarget(target.getPosition());
    movementTask.update();
    
    if (movementTask.getStatus() != Status.ACTIVE) {
      // Only complete if we've been active for minimum time
      // This prevents instant completion when already at target
      if (activeTime >= MIN_ACTIVE_TIME) {
        finished = true;
        this.owner.getEntity().getEvents().trigger("chaseTaskFinished");
        stop();
      }
    }
  }

  @Override
  public void stop() {
    super.stop();
    movementTask.stop();
  }

  @Override
  public int getPriority() {
    if (status == Status.ACTIVE) {
      return getActivePriority();
    }
    return getInactivePriority();
  }

  private float getDistanceToTarget() {
    return owner.getEntity().getPosition().dst(target.getPosition());
  }

  private int getActivePriority() {
    float dst = getDistanceToTarget();
    if (dst > maxChaseDistance) {
      return -1; // Too far, stop chasing
    }
    return priority;
  }

  private int getInactivePriority() {
    float dst = getDistanceToTarget();
    if (dst <= viewDistance) {
      return priority;
    }
    return -1;
  }

  private boolean isTargetVisible() {
    return true;
  }
}