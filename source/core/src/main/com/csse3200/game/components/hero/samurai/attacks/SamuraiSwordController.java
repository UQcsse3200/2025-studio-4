package com.csse3200.game.components.hero.samurai.attacks;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;

/**
 * Orchestrates Samurai sword attacks (jab / sweep / spin).
 * Delegates per-frame updates and exposes trigger helpers with simple gating.
 */
public class SamuraiSwordController extends Component {
    private final ISamuraiAttack jab, sweep, spin;

    public SamuraiSwordController(ISamuraiAttack jab, ISamuraiAttack sweep, ISamuraiAttack spin) {
        this.jab = jab;
        this.sweep = sweep;
        this.spin = spin;
    }

    @Override
    public void update() {
        float dt = (com.badlogic.gdx.Gdx.graphics != null) ? com.badlogic.gdx.Gdx.graphics.getDeltaTime() : 1f / 60f;
        if (jab != null) jab.update(dt);
        if (sweep != null) sweep.update(dt);
        if (spin != null) spin.update(dt);
    }

    /**
     * Attempt to start a Jab toward {@code target}. Returns true if triggered.
     */
    public boolean triggerJab(Vector2 target) {
        if (jab != null && jab.canTrigger()) {
            jab.trigger(target);
            return true;
        }
        return false;
    }

    /**
     * Attempt to start a Sweep toward {@code target}. Returns true if triggered.
     */
    public boolean triggerSweep(Vector2 target) {
        if (sweep != null && sweep.canTrigger()) {
            sweep.trigger(target);
            return true;
        }
        return false;
    }

    /**
     * Attempt to start a Spin attack.
     * The {@code ccw} flag sets spin direction via the public setter if available.
     *
     * @return true if triggered.
     */
    public boolean triggerSpin(boolean ccw) {
        if (spin instanceof SpinAttackComponent) {
            // Use the public setter instead of touching private fields.
            ((SpinAttackComponent) spin).setDirectionCCW(ccw);
        }
        if (spin != null && spin.canTrigger()) {
            spin.trigger(null); // spin doesn't require a target
            return true;
        }
        return false;
    }

    /**
     * Get the current facing in degrees.
     * Prefers the active attack; if none active, falls back to jab's last facing.
     */
    public float getFacingDeg() {
        if (jab != null && jab.isActive()) return jab.getFacingDeg();
        if (sweep != null && sweep.isActive()) return sweep.getFacingDeg();
        if (spin != null && spin.isActive()) return spin.getFacingDeg();
        return (jab != null) ? jab.getFacingDeg() : 0f;
    }

    /**
     * @return true if any sword attack is currently active.
     */
    public boolean isAttacking() {
        return (jab != null && jab.isActive())
                || (sweep != null && sweep.isActive())
                || (spin != null && spin.isActive());
    }
}
