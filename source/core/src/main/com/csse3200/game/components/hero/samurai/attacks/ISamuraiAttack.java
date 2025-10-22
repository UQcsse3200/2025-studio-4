package com.csse3200.game.components.hero.samurai.attacks;

import com.badlogic.gdx.math.Vector2;

/**
 * Contract for Samurai attack behaviors.
 * Implementations should manage their own lifecycle, cooldowns, locks, and facing.
 */
public interface ISamuraiAttack {
    /**
     * @return whether the attack is currently active (in-progress).
     */
    boolean isActive();

    /**
     * Check if the attack can be triggered now.
     * Should include checks for cooldown, lock/mutex, and any internal debouncing.
     */
    boolean canTrigger();

    /**
     * Start the attack.
     * For jab/sweep, {@code targetOrNull} is the aim/target point.
     * For spin/area attacks, {@code targetOrNull} may be {@code null}.
     */
    void trigger(Vector2 targetOrNull);

    /**
     * Interrupt/stop the attack due to being hit or switching actions.
     */
    void cancel();

    /**
     * Advance the attack state machine each frame.
     */
    void update(float dt);

    /**
     * @return the most recent facing direction in degrees,
     * used to align the character's visual facing with the attack.
     */
    float getFacingDeg();
}
