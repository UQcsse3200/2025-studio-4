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

    // 发射参数（共享默认，可由子类覆盖/Setter注入）
    protected float qiSpeed = 10f;
    protected float qiLife = 0.6f;
    protected int   qiDamage = 30;
    protected float qiFanOffsetDeg = 15f;
    protected int   qiSpinCount = 8;
    protected float qiSpawnForward = 0.25f; // 注意：真正使用时会 + restRadius
    // ===== 新增：从“武士本体”读数并缓存 =====
    protected CombatStatsComponent heroStats; // owner=武士本体
    private   int cachedDamage = 10;           // 当前缓存伤害
    // ===== 新增：每个技能自己的伤害表 & 缓存 =====
    protected int[] damageByLevel = null; // 由上层或子类注入

    // ===== 在字段里新增：伤害倍率 =====
    private float dmgMul = 1f;

    protected AbstractSwordAttackComponent(Entity owner, float restRadius) {
        this.owner = owner;
        this.restRadius = restRadius;
    }

    // 允许外部注入：表 + 默认值（用于 level=1 或无表时）
    public AbstractSwordAttackComponent setDamageTable(int[] table, int defaultAtLv1) {
        this.damageByLevel = table;
        this.cachedDamage  = Math.max(1, defaultAtLv1);
        return this;
    }

    @Override
    public void create() {
        physics = entity.getComponent(PhysicsComponent.class);
        if (physics == null) throw new IllegalStateException(getClass().getSimpleName()+" requires PhysicsComponent.");
        cds  = entity.getComponent(SkillCooldowns.class);
        lock = entity.getComponent(AttackLockComponent.class);
        autoCalibratePivotFromOwnerOrigin();

        // ✅ 按 1 级从“本技能伤害表”初始化；无表用 qiDamage 兜底
        cachedDamage = pickByLevel(damageByLevel, 1, Math.max(1, qiDamage));

        // （可选兜底）只有当“没有伤害表”时，才尝试用 heroStats 作为 fallback
        if ((damageByLevel == null || damageByLevel.length == 0) && owner != null) {
            heroStats = owner.getComponent(CombatStatsComponent.class);
            if (heroStats != null) cachedDamage = Math.max(1, heroStats.getBaseAttack());
        }

        // 升级：仅按“技能表”刷新
        if (owner != null) {
            owner.getEvents().addListener("upgraded",
                    (Integer level, CurrencyComponent.CurrencyType t, Integer cost) -> {
                        cachedDamage = pickByLevel(damageByLevel, level, cachedDamage);
                    });
            // ✨ 终极技能 → 刷新伤害倍率
            owner.getEvents().addListener("attack.multiplier",
                    (Float mul) -> { dmgMul = (mul != null && mul > 0f) ? mul : 1f; });
        }
    }


    // 子类统一调用，拿当前伤害
    protected int currentDamage() {
        return Math.max(1, Math.round(cachedDamage * dmgMul));   // 直接用表维护好的缓存
    }


    // 小工具：按等级从数组里取值（越界取最后一档；无表用 fallback）
    protected static int pickByLevel(int[] arr, int level, int fallback) {
        if (arr == null || arr.length == 0) return Math.max(1, fallback);
        int idx = Math.max(0, Math.min(level - 1, arr.length - 1));
        return Math.max(1, arr[idx]);
    }


    protected void setPose(float workAngleDeg, float curRadius) {
        float rad = (float)Math.toRadians(workAngleDeg);
        float dx = (float)Math.cos(rad);
        float dy = (float)Math.sin(rad);

        getEntityCenter(owner, ownerCenter);
        ownerCenter.add(pivotOffset);

        float handleX = ownerCenter.x + curRadius * dx;
        float handleY = ownerCenter.y + curRadius * dy;

        float centerX = handleX - centerToHandle * dx;
        float centerY = handleY - centerToHandle * dy;

        float swordAngleDeg = workAngleDeg + spriteForwardOffsetDeg;
        float swordAngleRad = (float)Math.toRadians(swordAngleDeg);

        physics.getBody().setTransform(centerX, centerY, swordAngleRad);

        var rot = entity.getComponent(RotatingTextureRenderComponent.class);
        if (rot != null) {
            float vis = ((swordAngleDeg % 360f) + 360f) % 360f;
            rot.setRotation(vis);
        }
        facingDeg = workAngleDeg; // 让外部能拿到当前朝向
    }

    protected void getEntityCenter(Entity e, Vector2 out) {
        try {
            Vector2 c = e.getCenterPosition();
            if (c != null) { out.set(c); return; }
        } catch (Throwable ignored) {}
        Vector2 pos = e.getPosition();
        Vector2 scale = e.getScale();
        out.set(pos.x + (scale != null ? scale.x * 0.5f : 0.5f),
                pos.y + (scale != null ? scale.y * 0.5f : 0.5f));
    }

    protected void autoCalibratePivotFromOwnerOrigin() {
        try {
            var render = owner.getComponent(RotatingTextureRenderComponent.class);
            if (render == null) return;
            float originX = invokeFloat(render,"getOriginX", Float.NaN);
            float originY = invokeFloat(render,"getOriginY", Float.NaN);
            float width   = invokeFloat(render,"getWidth", Float.NaN);
            float height  = invokeFloat(render,"getHeight", Float.NaN);
            if (Float.isNaN(originX) || Float.isNaN(originY) || Float.isNaN(width) || Float.isNaN(height)) return;
            float dx = originX - width * 0.5f;
            float dy = originY - height * 0.5f;
            pivotOffset.add(dx, dy);
        } catch (Throwable ignored) {}
    }

    private static float invokeFloat(Object obj, String method, float fb) {
        try { var m=obj.getClass().getMethod(method); Object r=m.invoke(obj);
            return (r instanceof Number)?((Number)r).floatValue():fb; }
        catch (Throwable e){ return fb; }
    }

    /** 子类统一用它发射剑气，避免重复 */
    protected void emitSwordQiAtAngle(float angleDeg, String sheetPath, int cols, int rows,
                                      int frameW, int frameH, float frameDur, boolean loop) {
        float rad = (float)Math.toRadians(angleDeg);
        float ux = (float)Math.cos(rad);
        float uy = (float)Math.sin(rad);

        getEntityCenter(owner, ownerCenter);
        ownerCenter.add(pivotOffset);

        float forward = Math.max(restRadius + qiSpawnForward, 1.2f);
        Vector2 spawn = new Vector2(ownerCenter.x + ux*forward, ownerCenter.y + uy*forward);

        float drawW = 1.2f, drawH = 1.2f;
        float baseRotation = 0f;


        int dmg = currentDamage();

        Entity qi = com.csse3200.game.entities.factories.SwordQiFactory.createSwordQi(
                spawn, ux*qiSpeed, uy*qiSpeed, qiLife, dmg,
                drawW, drawH,
                sheetPath, cols, rows, frameW, frameH, frameDur,
                angleDeg + spriteForwardOffsetDeg, baseRotation, loop
        );

        var anim = qi.getComponent(com.csse3200.game.rendering.RotatingSheetAnimationRenderComponent.class);
        if (anim != null) { anim.setLayer(5); anim.setZIndexOverride(9999f); }

        if (qi.getScale().isZero() || qi.getScale().x < 0.3f) qi.setScale(3f,3f);

        ServiceLocator.getEntityService().register(qi);
    }

    // 新增：允许每次发射临时覆盖 speed/life/damage/渲染大小
    protected void emitSwordQiAtAngle(float angleDeg, String sheetPath, int cols, int rows,
                                      int frameW, int frameH, float frameDur, boolean loop,
                                      float speed, float life, int damage,
                                      float drawW, float drawH) {
        float rad = (float)Math.toRadians(angleDeg);
        float ux = (float)Math.cos(rad);
        float uy = (float)Math.sin(rad);

        getEntityCenter(owner, ownerCenter);
        ownerCenter.add(pivotOffset);

        float forward = Math.max(restRadius + qiSpawnForward, 1.2f);
        Vector2 spawn = new Vector2(ownerCenter.x + ux*forward, ownerCenter.y + uy*forward);

        float baseRotation = 0f;

        int dmg = currentDamage(); // ★ 这里：从缓存（监听升级）里取“当前伤害”

        Entity qi = com.csse3200.game.entities.factories.SwordQiFactory.createSwordQi(
                spawn, ux*speed, uy*speed, life, dmg,
                drawW, drawH,
                sheetPath, cols, rows, frameW, frameH, frameDur,
                angleDeg + spriteForwardOffsetDeg, baseRotation, loop
        );

        var anim = qi.getComponent(com.csse3200.game.rendering.RotatingSheetAnimationRenderComponent.class);
        if (anim != null) { anim.setLayer(5); anim.setZIndexOverride(9999f); }

        if (qi.getScale().isZero() || qi.getScale().x < 0.3f) qi.setScale(3f,3f);

        ServiceLocator.getEntityService().register(qi);
    }


    // 可选：通用 setter，子类也能用
    public AbstractSwordAttackComponent setSpriteForwardOffsetDeg(float deg){ this.spriteForwardOffsetDeg=deg; return this; }
    public AbstractSwordAttackComponent setCenterToHandle(float d){ this.centerToHandle=d; return this; }
    public AbstractSwordAttackComponent setPivotOffset(float ox,float oy){ this.pivotOffset.set(ox,oy); return this; }
    public float getFacingDeg(){ return facingDeg; }
}
