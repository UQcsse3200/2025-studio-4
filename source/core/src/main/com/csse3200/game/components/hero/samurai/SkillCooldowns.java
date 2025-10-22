package com.csse3200.game.components.hero.samurai;

import com.csse3200.game.components.Component;
import com.csse3200.game.services.ServiceLocator;

import java.util.HashMap;
import java.util.Map;

/**
 * Multi-skill independent cooldown manager.
 * Usage:
 * <ul>
 *   <li>{@link #setTotal(String, float)} — configure a skill's total cooldown duration</li>
 *   <li>{@link #trigger(String)} — start cooldown for the given skill (emits an update immediately)</li>
 *   <li>{@link #isReady(String)} — query readiness</li>
 *   <li>{@link #update()} — tick down each frame and emit throttled updates</li>
 * </ul>
 *
 * Events emitted by this component:
 * <ul>
 *   <li><b>"skill:cooldown"</b> — payload: {@link SkillCooldownInfo}</li>
 *   <li><b>"skill:ready"</b> — payload: {@link String} skill name</li>
 * </ul>
 */
public class SkillCooldowns extends Component {
    /** Quantization step (seconds) for throttled emits; matches Engineer's 0.05s throttle. */
    private static final float QUANT_STEP = 0.05f;

    /** Per-skill cooldown data. */
    private static class CD {
        float remain;    // remaining time (s)
        float total;     // total duration (s)
        float lastEmitQ; // last emitted quantized 'remain' (for throttling)
    }

    /** Event payload: single object to avoid overloaded trigger signatures. */
    public static class SkillCooldownInfo {
        public final String skill;
        public final float remain;
        public final float total;
        /** 0..1 where 1.0 means ready. */
        public final float progress01;

        public SkillCooldownInfo(String skill, float remain, float total, float progress01) {
            this.skill = skill;
            this.remain = remain;
            this.total = total;
            this.progress01 = progress01;
        }
    }

    private final Map<String, CD> cds = new HashMap<>();

    /** Configure a skill's total cooldown (does not start the cooldown). */
    public SkillCooldowns setTotal(String skill, float totalSec) {
        CD c = cds.computeIfAbsent(skill, k -> new CD());
        c.total = Math.max(0f, totalSec);
        return this;
    }

    /** @return true if the skill is ready (cooldown finished). */
    public boolean isReady(String skill) {
        CD c = cds.get(skill);
        return c == null || c.remain <= 0f;
    }

    /**
     * Start cooldown from the configured total and emit an immediate update.
     * If the skill was unseen, it is created with total=0 (i.e., instantly ready next time).
     */
    public void trigger(String skill) {
        CD c = cds.get(skill);
        if (c == null) {
            // Allow dynamic triggering: create with total=0 => ready after this immediate tick
            c = new CD();
            cds.put(skill, c);
        }
        c.remain = c.total;
        emit(skill, c); // emit immediately, consistent with Engineer
    }

    /**
     * Optional: shorten a cooldown (e.g., on-kill refund).
     * Emits an update immediately and fires "skill:ready" if it reaches zero.
     */
    public void reduce(String skill, float deltaSec) {
        CD c = cds.get(skill);
        if (c == null || deltaSec <= 0f) return;
        c.remain = Math.max(0f, c.remain - deltaSec);
        emit(skill, c);
        if (c.remain == 0f) {
            entity.getEvents().trigger("skill:ready", skill);
        }
    }

    /** Optional: reset a cooldown to ready immediately. */
    public void reset(String skill) {
        CD c = cds.get(skill);
        if (c == null) return;
        c.remain = 0f;
        emit(skill, c);
        entity.getEvents().trigger("skill:ready", skill);
    }

    /** Query remaining seconds for a skill. */
    public float getRemain(String skill) {
        CD c = cds.get(skill);
        return c == null ? 0f : Math.max(0f, c.remain);
    }

    /** Query normalized progress in [0..1], where 1 means ready. */
    public float getProgress01(String skill) {
        CD c = cds.get(skill);
        if (c == null || c.total <= 0f) return 1f;
        return 1f - (Math.max(0f, c.remain) / c.total);
    }

    @Override
    public void update() {
        float dt = ServiceLocator.getTimeSource().getDeltaTime();

        for (Map.Entry<String, CD> e : cds.entrySet()) {
            String skill = e.getKey();
            CD c = e.getValue();
            if (c.remain > 0f) {
                float prev = c.remain;
                c.remain = Math.max(0f, c.remain - dt);

                // Throttled emits based on quantized remain; also emit when crossing to zero.
                float quantPrev = (float) Math.floor(prev / QUANT_STEP) * QUANT_STEP;
                float quantNow  = (float) Math.floor(c.remain / QUANT_STEP) * QUANT_STEP;

                if (quantNow != c.lastEmitQ || (prev > 0f && c.remain == 0f)) {
                    c.lastEmitQ = quantNow;
                    emit(skill, c);
                }

                if (c.remain == 0f) {
                    entity.getEvents().trigger("skill:ready", skill);
                }
            }
        }
    }

    /** Emit a single "skill:cooldown" event with remain/total/progress01. */
    private void emit(String skill, CD c) {
        float total = c.total;
        float remain = Math.max(0f, c.remain);
        float progress = (total <= 0f) ? 1f : (1f - remain / total);

        entity.getEvents().trigger(
                "skill:cooldown",
                new SkillCooldownInfo(skill, remain, total, progress)
        );
    }
}




