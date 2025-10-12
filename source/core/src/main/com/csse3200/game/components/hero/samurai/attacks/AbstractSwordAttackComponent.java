package com.csse3200.game.components.hero.samurai.attacks;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
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

    protected AbstractSwordAttackComponent(Entity owner, float restRadius) {
        this.owner = owner;
        this.restRadius = restRadius;
    }

    @Override
    public void create() {
        physics = entity.getComponent(PhysicsComponent.class);
        if (physics == null) throw new IllegalStateException(getClass().getSimpleName()+" requires PhysicsComponent.");
        cds = entity.getComponent(SkillCooldowns.class);
        lock = entity.getComponent(AttackLockComponent.class);
        autoCalibratePivotFromOwnerOrigin();
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

        Entity qi = com.csse3200.game.entities.factories.SwordQiFactory.createSwordQi(
                spawn, ux*qiSpeed, uy*qiSpeed, qiLife, qiDamage,
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

        Entity qi = com.csse3200.game.entities.factories.SwordQiFactory.createSwordQi(
                spawn, ux*speed, uy*speed, life, damage,
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
