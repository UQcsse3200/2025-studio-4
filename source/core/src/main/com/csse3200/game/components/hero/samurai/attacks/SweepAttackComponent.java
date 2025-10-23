package com.csse3200.game.components.hero.samurai.attacks;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;

/**
 * One-arc sword sweep.
 * <p>
 * Behavior:
 * <ul>
 *   <li>Aims toward the target, decides sweep direction (clockwise/counter-clockwise) based on current facing.</li>
 *   <li>Interpolates across an angular arc over {@code duration} while extending the sword radius by {@code extra}.</li>
 *   <li>Spawns a medium-speed, medium-life Sword Qi for visual/impact feedback.</li>
 *   <li>Gated by global cooldown + small per-skill debounce (mini cooldown) + an attack lock.</li>
 * </ul>
 */
public class SweepAttackComponent extends AbstractSwordAttackComponent {
    private boolean active = false;
    /**
     * Local timeline (seconds) for this sweep.
     */
    private float T = 0f;
    /**
     * Total sweep duration.
     */
    private float duration = 0.22f;
    /**
     * Extra outward radius during sweep.
     */
    private float extra = 0.35f;

    /**
     * Half-arc in degrees; sweep covers [-arcDeg, +arcDeg] relative to aim.
     */
    private float arcDeg = 60f;
    /**
     * Aim direction captured at trigger time (deg).
     */
    private float baseDeg = 0f;
    /**
     * Sweep direction: +1 = CCW, -1 = CW (chosen vs current facing).
     */
    private float dir = +1f;

    // Mini debounce to avoid rapid retriggering even if global CD allows
    private float miniCooldown = 0.0f;
    private float miniTimer = 0f;

    public SweepAttackComponent(com.csse3200.game.entities.Entity owner, float restRadius) {
        super(owner, restRadius);
    }

    /**
     * Configure timing and radius extension.
     */
    public SweepAttackComponent setParams(float duration, float extra) {
        if (duration > 0) this.duration = duration;
        if (extra >= 0) this.extra = extra;
        return this;
    }

    /**
     * Configure half-arc size (deg).
     */
    public SweepAttackComponent setArcDeg(float deg) {
        this.arcDeg = Math.max(0f, deg);
        return this;
    }

    /**
     * Configure mini debounce (seconds).
     */
    public SweepAttackComponent setMiniCooldown(float s) {
        this.miniCooldown = Math.max(0f, s);
        return this;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public boolean canTrigger() {
        boolean cdOk = (cds == null) || cds.isReady("sweep"); // global cooldown
        boolean notBusy = (lock == null) || !lock.isBusy();   // no other attack holding the lock
        boolean miniOk = miniTimer <= 0f;                     // local debounce
        return !active && cdOk && notBusy && miniOk;
    }

    @Override
    public void trigger(Vector2 target) {
        if (target == null || !canTrigger()) return;

        // Compute aim direction from owner center → target
        getEntityCenter(owner, ownerCenter);
        ownerCenter.add(pivotOffset);
        float dx = target.x - ownerCenter.x, dy = target.y - ownerCenter.y;
        if (Math.abs(dx) < 1e-5f && Math.abs(dy) < 1e-5f) return;

        float aimDeg = (float) Math.toDegrees(Math.atan2(dy, dx));
        this.baseDeg = aimDeg;

        // Choose sweep direction that turns toward the target by the shortest path
        float d = aimDeg - facingDeg;
        while (d <= -180f) d += 360f;
        while (d > 180f) d -= 360f;
        this.dir = (d >= 0f) ? +1f : -1f;

        active = true;
        T = 0f;
        if (lock != null) lock.tryAcquire("sweep");
        if (cds != null) cds.trigger("sweep");

        // Visual/impact projectile: medium speed/life, modest damage
        float swSpeed = 9.0f;
        float swLife = 0.30f;
        int swDamage = 15;
        float drawW = 1.4f, drawH = 1.4f;

        emitSwordQiAtAngle(
                baseDeg,
                "images/samurai/slash_red_thick_Heavy_6x1_64.png",
                6, 1, 64, 64, 0.08f, true,
                swSpeed, swLife, swDamage,
                drawW, drawH
        );
    }

    @Override
    public void update(float dt) {
        // Update local debounce
        if (miniTimer > 0f) miniTimer -= dt;
        if (!active) return;

        // Progress sweep along the arc from -arcDeg → +arcDeg
        T += dt;
        float a = Math.min(T / duration, 1f);
        float rel = (-arcDeg) + a * (2f * arcDeg); // [-arc, +arc]
        float workDeg = baseDeg + dir * rel;
        float curRadius = restRadius + extra;

        setPose(workDeg, curRadius);

        // Finish when animation completes
        if (a >= 1f) {
            active = false;
            miniTimer = miniCooldown;
            if (lock != null) lock.release("sweep");
        }
    }

    @Override
    public void cancel() {
        if (active) {
            active = false;
            if (lock != null) lock.release("sweep");
        }
    }
}

