package com.csse3200.game.components.hero.samurai.attacks;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;

/**
 * Quick single-thrust sword attack (“Jab”).
 * <p>
 * Features:
 * <ul>
 *   <li>Short animation that eases out from a resting radius and returns.</li>
 *   <li>Respects global skill cooldowns and a small per-skill debounce (mini cooldown).</li>
 *   <li>Uses {@link AbstractSwordAttackComponent#emitSwordQiAtAngle} to spawn a short-lived slash projectile.</li>
 *   <li>Cooperates with {@link AttackLockComponent} to avoid overlapping actions.</li>
 * </ul>
 */
public class JabAttackComponent extends AbstractSwordAttackComponent {
    /**
     * Whether the jab animation is currently playing.
     */
    private boolean active = false;

    /**
     * Local timeline (seconds) for the current jab; progresses from 0 to {@link #duration}.
     */
    private float T = 0f;

    /**
     * Total time (seconds) of the jab motion.
     */
    private float duration = 0.18f;

    /**
     * Extra outward distance added during the peak of the jab motion.
     */
    private float extra = 0.8f;

    /**
     * Facing angle (degrees) used for pose and projectile direction.
     */
    private float angleDeg = 0f;

    // --- Debounce (mini cooldown) ---
    /**
     * Small per-skill cooldown to prevent rapid retriggering even if the global CD allows.
     */
    private float miniCooldown = 0.0f;
    /**
     * Remaining time (seconds) before another jab can be triggered due to mini cooldown.
     */
    private float miniTimer = 0f;

    public JabAttackComponent(com.csse3200.game.entities.Entity owner, float restRadius) {
        super(owner, restRadius);
    }

    /**
     * Configure motion timing and extra outward displacement.
     */
    public JabAttackComponent setParams(float duration, float extra) {
        if (duration > 0) this.duration = duration;
        if (extra > 0) this.extra = extra;
        return this;
    }

    /**
     * Set a small per-skill cooldown to avoid spamming.
     */
    public JabAttackComponent setMiniCooldown(float s) {
        this.miniCooldown = Math.max(0f, s);
        return this;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public boolean canTrigger() {
        boolean cdOk = (cds == null) || cds.isReady("jab");   // global cooldown system
        boolean notBusy = (lock == null) || !lock.isBusy();   // no other attack holding the lock
        boolean miniOk = miniTimer <= 0f;                     // local debounce ok
        return !active && cdOk && notBusy && miniOk;
    }

    @Override
    public void trigger(Vector2 target) {
        if (target == null || !canTrigger()) return;

        // Compute facing from owner center → target.
        getEntityCenter(owner, ownerCenter);
        ownerCenter.add(pivotOffset);
        float dx = target.x - ownerCenter.x, dy = target.y - ownerCenter.y;
        if (Math.abs(dx) < 1e-5f && Math.abs(dy) < 1e-5f) return;

        this.angleDeg = (float) Math.toDegrees(Math.atan2(dy, dx));
        this.active = true;
        this.T = 0f;

        // Acquire attack lock and trigger shared cooldown.
        if (lock != null) lock.tryAcquire("jab");
        if (cds != null) cds.trigger("jab");

        // --- Projectile tuning for a “jab” feel ---
        float jabSpeed = 3.0f;   // slower speed; short range feel (also controlled by life)
        float jabLife = 0.30f;  // short lifetime
        int jabDamage = 30;     // example damage (final damage comes from currentDamage())
        float drawW = 1.6f, drawH = 1.6f; // slightly larger visuals for readability

        // Spawn a short, looping 6-frame thick slash aligned with jab direction.
        emitSwordQiAtAngle(
                angleDeg,
                "images/samurai/slash_red_thick_Heavy_6x1_64.png",
                6, 1, 64, 64, 0.08f, true,
                jabSpeed, jabLife, jabDamage,
                drawW, drawH
        );
    }

    @Override
    public void update(float dt) {
        // Update local debounce
        if (miniTimer > 0f) miniTimer -= dt;

        if (!active) return;

        // Advance jab motion curve
        T += dt;
        float a = Math.min(T / duration, 1f);        // normalized [0,1]
        float bump = 4f * a * (1f - a);              // simple bell-shaped ease
        float curRadius = restRadius + bump * extra; // push forward then retract

        // Pose the sword (physics + visual rotation)
        setPose(angleDeg, curRadius);

        // End of animation
        if (a >= 1f) {
            active = false;
            miniTimer = miniCooldown;
            if (lock != null) lock.release("jab");
        }
    }

    @Override
    public void cancel() {
        if (active) {
            active = false;
            if (lock != null) lock.release("jab");
        }
    }
}
