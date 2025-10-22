package com.csse3200.game.components.hero.samurai.attacks;

import com.csse3200.game.components.Component;

/**
 * Simple mutex-style lock to prevent overlapping Samurai attack actions.
 * <p>
 * Usage:
 * <pre>
 *   if (lock.tryAcquire("dash-slash")) {
 *       // perform the attack...
 *       lock.release("dash-slash");
 *   }
 * </pre>
 * Only one holder can own the lock at a time. The holder's name is tracked for
 * easier debugging and optional safe release.
 */
public class AttackLockComponent extends Component {
    /**
     * Whether the lock is currently held.
     */
    private boolean busy = false;
    /**
     * Identifier of the current holder (e.g., the skill name).
     */
    private String who = "";

    /**
     * Attempt to acquire the lock.
     *
     * @param name holder identifier (recommended: the attack/skill name)
     * @return true if acquired; false if already held by someone else
     */
    public boolean tryAcquire(String name) {
        if (busy) return false;
        busy = true;
        who = name;
        return true;
    }

    /**
     * Release the lock.
     * <p>
     * If {@code name} matches the current holder or is {@code null}, the lock is released.
     * Passing {@code null} acts as a force-release (use with care).
     *
     * @param name holder attempting to release; {@code null} to force release
     */
    public void release(String name) {
        if (busy && (who.equals(name) || name == null)) {
            busy = false;
            who = "";
        }
    }

    /**
     * @return whether the lock is currently held
     */
    public boolean isBusy() {
        return busy;
    }

    /**
     * @return the current holder's identifier, or empty string if free
     */
    public String holder() {
        return who;
    }
}

