package com.csse3200.game.components.hero.samurai.attacks;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.currencysystem.CurrencyComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.RotatingTextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.components.hero.samurai.SkillCooldowns;

/**
 * Base class for Samurai sword attacks.
 * <p>
 * Responsibilities shared by concrete sword skills:
 * <ul>
 *   <li>Pose calculation (pivot/center/origin) and visual rotation sync</li>
 *   <li>Common projectile (Sword Qi) emission helpers</li>
 *   <li>Damage management via per-skill damage table and runtime multiplier</li>
 *   <li>Upgrade hooks to refresh damage when the hero levels up</li>
 * </ul>
 */
public abstract class AbstractSwordAttackComponent extends Component implements ISamuraiAttack {
    protected final Entity owner;
    protected PhysicsComponent physics;
    protected SkillCooldowns cds;
    protected AttackLockComponent lock;

    protected float restRadius;
    protected float spriteForwardOffsetDeg = 0f;
    protected float centerToHandle = -0.25f;
    protected final Vector2 pivotOffset = new Vector2();
    protected final Vector2 ownerCenter = new Vector2();
    protected float facingDeg = 0f;

    // Launch parameters (shared defaults; subclasses may override or use setters)
    protected float qiSpeed = 10f;
    protected float qiLife = 0.6f;
    protected int qiDamage = 30;
    protected float qiFanOffsetDeg = 15f;
    protected int qiSpinCount = 8;
    /**
     * Spawn offset in front of the owner; actual spawn uses (restRadius + qiSpawnForward).
     */
    protected float qiSpawnForward = 0.25f;

    // New: read from the Samurai entity and cache (used only when no per-skill table is provided)
    protected CombatStatsComponent heroStats; // owner = Samurai entity
    private int cachedDamage = 10;          // Current cached damage

    // New: per-skill damage table & cache
    protected int[] damageByLevel = null; // Injected by upper layer or subclass

    // New: runtime damage multiplier
    private float dmgMul = 1f;

    protected AbstractSwordAttackComponent(Entity owner, float restRadius) {
        this.owner = owner;
        this.restRadius = restRadius;
    }

    /**
     * Allow external injection of a per-skill damage table and a default for level 1.
     */
    public AbstractSwordAttackComponent setDamageTable(int[] table, int defaultAtLv1) {
        this.damageByLevel = table;
        this.cachedDamage = Math.max(1, defaultAtLv1);
        return this;
    }

    @Override
    public void create() {
        physics = entity.getComponent(PhysicsComponent.class);
        if (physics == null)
            throw new IllegalStateException(getClass().getSimpleName() + " requires PhysicsComponent.");
        cds = entity.getComponent(SkillCooldowns.class);
        lock = entity.getComponent(AttackLockComponent.class);
        autoCalibratePivotFromOwnerOrigin();

        // Initialize damage using the per-skill table at level 1; fall back to qiDamage if absent.
        cachedDamage = pickByLevel(damageByLevel, 1, Math.max(1, qiDamage));

        // Optional fallback: if there is no per-skill table, use heroStats.baseAttack.
        if ((damageByLevel == null || damageByLevel.length == 0) && owner != null) {
            heroStats = owner.getComponent(CombatStatsComponent.class);
            if (heroStats != null) cachedDamage = Math.max(1, heroStats.getBaseAttack());
        }

        // On upgrade: refresh using the per-skill table only.
        if (owner != null) {
            owner.getEvents().addListener("upgraded",
                    (Integer level, CurrencyComponent.CurrencyType t, Integer cost) -> {
                        cachedDamage = pickByLevel(damageByLevel, level, cachedDamage);
                    });
            // Ultimate/temporary effects can adjust a damage multiplier.
            owner.getEvents().addListener("attack.multiplier",
                    (Float mul) -> {
                        dmgMul = (mul != null && mul > 0f) ? mul : 1f;
                    });
        }
    }

    /**
     * Subclasses use this to obtain the current damage with multiplier applied.
     */
    protected int currentDamage() {
        return Math.max(1, Math.round(cachedDamage * dmgMul));
    }

    /**
     * Utility: pick a value by level from an array.
     * Out-of-range levels clamp to the last entry; falls back if the table is null/empty.
     */
    protected static int pickByLevel(int[] arr, int level, int fallback) {
        if (arr == null || arr.length == 0) return Math.max(1, fallback);
        int idx = Math.max(0, Math.min(level - 1, arr.length - 1));
        return Math.max(1, arr[idx]);
    }

    /**
     * Compute and apply sword pose (physics transform + visual rotation).
     * Also updates {@code facingDeg} so callers can query the current facing.
     */
    protected void setPose(float workAngleDeg, float curRadius) {
        float rad = (float) Math.toRadians(workAngleDeg);
        float dx = (float) Math.cos(rad);
        float dy = (float) Math.sin(rad);

        getEntityCenter(owner, ownerCenter);
        ownerCenter.add(pivotOffset);

        float handleX = ownerCenter.x + curRadius * dx;
        float handleY = ownerCenter.y + curRadius * dy;

        float centerX = handleX - centerToHandle * dx;
        float centerY = handleY - centerToHandle * dy;

        float swordAngleDeg = workAngleDeg + spriteForwardOffsetDeg;
        float swordAngleRad = (float) Math.toRadians(swordAngleDeg);

        physics.getBody().setTransform(centerX, centerY, swordAngleRad);

        var rot = entity.getComponent(RotatingTextureRenderComponent.class);
        if (rot != null) {
            float vis = ((swordAngleDeg % 360f) + 360f) % 360f;
            rot.setRotation(vis);
        }
        facingDeg = workAngleDeg; // allow external code to read current facing
    }

    /**
     * Get the visual center of an entity; falls back to position + half scale when needed.
     */
    protected void getEntityCenter(Entity e, Vector2 out) {
        try {
            Vector2 c = e.getCenterPosition();
            if (c != null) {
                out.set(c);
                return;
            }
        } catch (Throwable ignored) {
        }
        Vector2 pos = e.getPosition();
        Vector2 scale = e.getScale();
        out.set(pos.x + (scale != null ? scale.x * 0.5f : 0.5f),
                pos.y + (scale != null ? scale.y * 0.5f : 0.5f));
    }

    /**
     * Try to align pivot based on the owner's render origin (if available).
     */
    protected void autoCalibratePivotFromOwnerOrigin() {
        try {
            var render = owner.getComponent(RotatingTextureRenderComponent.class);
            if (render == null) return;
            float originX = invokeFloat(render, "getOriginX", Float.NaN);
            float originY = invokeFloat(render, "getOriginY", Float.NaN);
            float width = invokeFloat(render, "getWidth", Float.NaN);
            float height = invokeFloat(render, "getHeight", Float.NaN);
            if (Float.isNaN(originX) || Float.isNaN(originY) || Float.isNaN(width) || Float.isNaN(height)) return;
            float dx = originX - width * 0.5f;
            float dy = originY - height * 0.5f;
            pivotOffset.add(dx, dy);
        } catch (Throwable ignored) {
        }
    }

    private static float invokeFloat(Object obj, String method, float fb) {
        try {
            var m = obj.getClass().getMethod(method);
            Object r = m.invoke(obj);
            return (r instanceof Number) ? ((Number) r).floatValue() : fb;
        } catch (Throwable e) {
            return fb;
        }
    }

    /**
     * Helper for subclasses: emit a Sword Qi projectile at a given angle.
     * Avoids duplication across concrete attack components.
     */
    protected void emitSwordQiAtAngle(float angleDeg, String sheetPath, int cols, int rows,
                                      int frameW, int frameH, float frameDur, boolean loop) {
        float rad = (float) Math.toRadians(angleDeg);
        float ux = (float) Math.cos(rad);
        float uy = (float) Math.sin(rad);

        getEntityCenter(owner, ownerCenter);
        ownerCenter.add(pivotOffset);

        float forward = Math.max(restRadius + qiSpawnForward, 1.2f);
        Vector2 spawn = new Vector2(ownerCenter.x + ux * forward, ownerCenter.y + uy * forward);

        float drawW = 1.2f, drawH = 1.2f;
        float baseRotation = 0f;

        int dmg = currentDamage();

        Entity qi = com.csse3200.game.entities.factories.SwordQiFactory.createSwordQi(
                spawn, ux * qiSpeed, uy * qiSpeed, qiLife, dmg,
                drawW, drawH,
                sheetPath, cols, rows, frameW, frameH, frameDur,
                angleDeg + spriteForwardOffsetDeg, baseRotation, loop
        );

        var anim = qi.getComponent(com.csse3200.game.rendering.RotatingSheetAnimationRenderComponent.class);
        if (anim != null) {
            anim.setLayer(5);
            anim.setZIndexOverride(9999f);
        }

        if (qi.getScale().isZero() || qi.getScale().x < 0.3f) qi.setScale(3f, 3f);

        ServiceLocator.getEntityService().register(qi);
    }

    /**
     * Overload: per-shot overrides for speed/life/damage/draw size.
     */
    protected void emitSwordQiAtAngle(float angleDeg, String sheetPath, int cols, int rows,
                                      int frameW, int frameH, float frameDur, boolean loop,
                                      float speed, float life, int damage,
                                      float drawW, float drawH) {
        float rad = (float) Math.toRadians(angleDeg);
        float ux = (float) Math.cos(rad);
        float uy = (float) Math.sin(rad);

        getEntityCenter(owner, ownerCenter);
        ownerCenter.add(pivotOffset);

        float forward = Math.max(restRadius + qiSpawnForward, 1.2f);
        Vector2 spawn = new Vector2(ownerCenter.x + ux * forward, ownerCenter.y + uy * forward);

        float baseRotation = 0f;

        int dmg = currentDamage(); // Use cached damage (kept in sync via upgrade listeners)

        Entity qi = com.csse3200.game.entities.factories.SwordQiFactory.createSwordQi(
                spawn, ux * speed, uy * speed, life, dmg,
                drawW, drawH,
                sheetPath, cols, rows, frameW, frameH, frameDur,
                angleDeg + spriteForwardOffsetDeg, baseRotation, loop
        );

        var anim = qi.getComponent(com.csse3200.game.rendering.RotatingSheetAnimationRenderComponent.class);
        if (anim != null) {
            anim.setLayer(5);
            anim.setZIndexOverride(9999f);
        }

        if (qi.getScale().isZero() || qi.getScale().x < 0.3f) qi.setScale(3f, 3f);

        ServiceLocator.getEntityService().register(qi);
    }

    // Optional: common setters (also available to subclasses)
    public AbstractSwordAttackComponent setSpriteForwardOffsetDeg(float deg) {
        this.spriteForwardOffsetDeg = deg;
        return this;
    }

    public AbstractSwordAttackComponent setCenterToHandle(float d) {
        this.centerToHandle = d;
        return this;
    }

    public AbstractSwordAttackComponent setPivotOffset(float ox, float oy) {
        this.pivotOffset.set(ox, oy);
        return this;
    }

    public float getFacingDeg() {
        return facingDeg;
    }
}

