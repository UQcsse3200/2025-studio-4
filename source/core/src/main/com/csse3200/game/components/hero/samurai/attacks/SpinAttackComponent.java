package com.csse3200.game.components.hero.samurai.attacks;

import com.badlogic.gdx.math.Vector2;

/**
 * Spinning sword attack that emits multiple Sword Qi projectiles in batches while the hero rotates.
 * <p>
 * Key behaviors:
 * <ul>
 *   <li>Gated by global cooldown + a small per-skill debounce (mini cooldown) + a simple attack lock.</li>
 *   <li>Rotates the sword pose over {@code duration}, optionally multiple full turns ({@code turns}).</li>
 *   <li>Emits {@code qiSpinCount} projectiles spaced evenly by angle, batched over time via {@code emitInterval}.</li>
 *   <li>Auto-extends {@code duration} if needed to finish all batched emissions.</li>
 * </ul>
 */
public class SpinAttackComponent extends AbstractSwordAttackComponent {
    /**
     * Whether the spin is currently running.
     */
    private boolean active = false;
    /**
     * Local timeline (seconds) from 0 → {@link #duration}.
     */
    private float T = 0f;
    /**
     * Total spin duration in seconds. May be auto-extended to finish emissions.
     */
    private float duration = 0.5f;
    /**
     * Extra outward radius during the spin (visual offset).
     */
    private float extra = 0.25f;
    /**
     * Starting facing angle (deg) captured at trigger time.
     */
    private float startDeg = 0f;
    /**
     * Spin direction: +1 = CCW, -1 = CW.
     */
    private float dir = +1f;
    /**
     * Number of full rotations to complete over {@code duration}.
     */
    private float turns = 1f;

    // --- Debounce (mini cooldown) ---
    /**
     * Per-skill debounce time (seconds) to prevent rapid retriggering.
     */
    private float miniCooldown = 0.0f;
    /**
     * Remaining debounce time.
     */
    private float miniTimer = 0f;

    // ===== Batched emission settings =====
    /**
     * Time gap between emissions (seconds). e.g., ~0.3s as requested.
     */
    private float emitInterval = 0.30f;
    /**
     * Accumulator used to schedule emissions.
     */
    private float emitAccum = 0f;
    /**
     * Number emitted so far / total to emit this spin.
     */
    private int emitted = 0, emitTotal = 0;
    /**
     * Angle step (deg) between consecutive emissions.
     */
    private float spinStepDeg = 0f;
    /**
     * Drop-frame guard: max number of catch-up emissions allowed in a single frame.
     */
    private int emitCatchUpMaxPerFrame = 1;

    // Sprite/animation parameters for the Sword Qi effect.
    private String qiPngPath = "images/samurai/slash_red_thick_Heavy_6x1_64.png";
    private int sheetCols = 6, sheetRows = 1, frameW = 64, frameH = 64;
    private float frameDur = 0.08f;
    private boolean transparent = true;

    public SpinAttackComponent(com.csse3200.game.entities.Entity owner, float restRadius) {
        super(owner, restRadius);
    }

    /**
     * Configure spin timing and extra outward displacement.
     */
    public SpinAttackComponent setParams(float duration, float extra) {
        if (duration > 0) this.duration = duration;
        if (extra >= 0) this.extra = extra;
        return this;
    }

    /**
     * Set how many full turns to rotate during the spin.
     */
    public SpinAttackComponent setTurns(float t) {
        this.turns = Math.max(0f, t);
        return this;
    }

    /**
     * Set a mini cooldown to debounce spin triggers.
     */
    public SpinAttackComponent setMiniCooldown(float s) {
        this.miniCooldown = Math.max(0f, s);
        return this;
    }

    /**
     * Configure emission interval between projectiles. Default 0.30s.
     */
    public SpinAttackComponent setEmitInterval(float s) {
        this.emitInterval = Math.max(0f, s);
        return this;
    }

    /**
     * Limit how many catch-up emissions can happen in one frame. Default 1.
     */
    public SpinAttackComponent setEmitCatchUpMaxPerFrame(int n) {
        this.emitCatchUpMaxPerFrame = Math.max(1, n);
        return this;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public boolean canTrigger() {
        boolean cdOk = (cds == null) || cds.isReady("spin");
        boolean notBusy = (lock == null) || !lock.isBusy();
        boolean miniOk = miniTimer <= 0f;
        return !active && cdOk && notBusy && miniOk;
    }

    @Override
    public void trigger(Vector2 ignored) {
        if (!canTrigger()) return;

        // Capture current facing and (re)initialise state.
        this.startDeg = facingDeg;
        this.dir = +1f; // Use setDirectionCCW() to choose direction.
        active = true;
        T = 0f;

        if (lock != null) lock.tryAcquire("spin");
        if (cds != null) cds.trigger("spin");

        // Precompute the emission plan for this spin.
        emitTotal = Math.max(1, qiSpinCount);
        spinStepDeg = 360f / (float) emitTotal;
        emitted = 0;
        emitAccum = 0f;

        // Ensure duration is long enough to complete all emissions (add a small tail).
        float need = (emitTotal > 0) ? (emitInterval * (emitTotal - 1) + 0.05f) : 0f;
        if (duration < need) duration = need;

        // Fire the first projectile immediately for better feel.
        emitOne(emitted);
        emitted++;
    }

    /**
     * Set spin direction: CCW = true, CW = false.
     */
    public SpinAttackComponent setDirectionCCW(boolean ccw) {
        this.dir = ccw ? +1f : -1f;
        return this;
    }

    @Override
    public void update(float dt) {
        // Update local debounce
        if (miniTimer > 0f) miniTimer -= dt;
        if (!active) return;

        T += dt;

        // Pose update: rotate over time while holding a slightly extended radius.
        float a = Math.min(T / duration, 1f);
        float totalDeg = 360f * turns;
        float workDeg = startDeg + dir * (a * totalDeg);
        float curRadius = restRadius + extra;
        setPose(workDeg, curRadius);

        // Batched emissions at fixed interval, with drop-frame catch-up cap.
        if (emitted < emitTotal) {
            emitAccum += dt;
            int firedThisFrame = 0;
            while (emitAccum >= emitInterval && emitted < emitTotal && firedThisFrame < emitCatchUpMaxPerFrame) {
                emitAccum -= emitInterval;
                emitOne(emitted);
                emitted++;
                firedThisFrame++;
            }
        }

        // Finish when animation is done and all projectiles are emitted.
        if (a >= 1f && emitted >= emitTotal) {
            active = false;
            miniTimer = miniCooldown;
            if (lock != null) lock.release("spin");
        }
    }

    @Override
    public void cancel() {
        if (active) {
            active = false;
            if (lock != null) lock.release("spin");
        }
    }

    // ---- Projectile tuning (speed, life, damage, visuals) ----
    private float spinSpeed = 9.0f;
    private float spinLife = 0.28f;
    private int spinDamage = 10;
    private float spinDrawW = 1.2f;
    private float spinDrawH = 1.2f;

    /**
     * Configure projectile speed/life/damage for the spin.
     */
    public SpinAttackComponent setSpinProjectile(float speed, float life, int damage) {
        if (speed > 0) this.spinSpeed = speed;
        if (life > 0) this.spinLife = life;
        this.spinDamage = Math.max(0, damage);
        return this;
    }

    /**
     * Configure visual draw size for the Sword Qi projectiles.
     */
    public SpinAttackComponent setSpinDrawSize(float w, float h) {
        if (w > 0) this.spinDrawW = w;
        if (h > 0) this.spinDrawH = h;
        return this;
    }

    /**
     * Emit one projectile at angle = startDeg + step * index.
     */
    private void emitOne(int idx) {
        float ang = startDeg + idx * spinStepDeg;
        emitSwordQiAtAngle(
                ang,
                qiPngPath, sheetCols, sheetRows, frameW, frameH, frameDur, transparent,
                spinSpeed, spinLife, spinDamage, spinDrawW, spinDrawH
        );
    }
}

