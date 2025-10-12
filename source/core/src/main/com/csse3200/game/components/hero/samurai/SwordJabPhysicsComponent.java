package com.csse3200.game.components.hero.samurai;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.ProjectileFactory;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.RotatingTextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;

public class SwordJabPhysicsComponent extends Component {
    private final Entity owner;

    // ===== 静止参数 =====
    private float restRadius;
    private float spriteForwardOffsetDeg = 0f; // 贴图默认朝右=0，朝上=90
    private float centerToHandle = -0.25f;     // 贴图中心至剑柄偏移（负值常见）
    private final Vector2 pivotOffset = new Vector2();

    // 物理&状态
    private PhysicsComponent physics;
    private final Vector2 ownerCenter = new Vector2();
    private float facingDeg = 0f;

    // ===== Jab（突刺）=====
    private boolean jabbing = false;
    private float jabT = 0f;
    private float jabDuration = 0.18f;
    private float jabExtra = 0.8f;
    private float jabAngleDeg = 0f;
    // 将这组变量当“最小间隔/防抖”而非真正技能CD
    private float jabCooldown = 0f;
    private float jabCdTimer = 0f;

    // ===== Sweep（横砍）=====
    private boolean sweeping = false;
    private float sweepT = 0f;
    private float sweepDuration = 0.22f;
    private float sweepExtra = 0.35f;
    private float sweepCooldown = 0f;
    private float sweepCdTimer = 0f;

    private float sweepArcDeg = 60f;
    private float sweepBaseDeg = 0f;
    private float sweepDir = +1f;

    // ===== Spin（整圈旋转）=====
    private boolean spinning = false;
    private float spinT = 0f;
    private float spinDuration = 0.5f;
    private float spinExtra = 0.25f;
    private float spinCooldown = 0f;
    private float spinCdTimer = 0f;
    private float spinStartDeg = 0f;
    private float spinDir = +1f;
    private float spinTurns = 1f;

    // 统一参数（可根据手感调整）
    private String qiTexture = "images/samurai/Stab.png";
    private float qiSpeed = 10f;      // 单位/秒
    private float qiLife  = 0.60f;    // 存活秒数
    private int   qiDamage = 30;      // 剑气伤害
    private float qiFanOffsetDeg = 15f; // 扇形偏角
    private int   qiSpinCount = 8;    // Spin 一圈发射数量
    private float qiSpawnForward =  restRadius + 0.25f;

    // NEW: 多技能冷却
    private SkillCooldowns cds;

    public SwordJabPhysicsComponent(Entity owner, float restRadius) {
        this.owner = owner;
        this.restRadius = restRadius;
    }

    // ===== 配置项 =====
    public SwordJabPhysicsComponent setSpriteForwardOffsetDeg(float deg) {
        this.spriteForwardOffsetDeg = deg;
        return this;
    }

    public SwordJabPhysicsComponent setCenterToHandle(float d) {
        this.centerToHandle = d;
        return this;
    }

    public SwordJabPhysicsComponent setPivotOffset(float ox, float oy) {
        this.pivotOffset.set(ox, oy);
        return this;
    }

    public SwordJabPhysicsComponent setRestRadius(float r) {
        this.restRadius = r;
        return this;
    }

    public SwordJabPhysicsComponent setJabParams(float duration, float extra) {
        if (duration > 0f) this.jabDuration = duration;
        if (extra > 0f) this.jabExtra = extra;
        return this;
    }

    public SwordJabPhysicsComponent setJabCooldown(float seconds) {
        this.jabCooldown = Math.max(0f, seconds);
        return this;
    }

    public SwordJabPhysicsComponent setSweepParams(float duration, float extra) {
        if (duration > 0f) this.sweepDuration = duration;
        if (extra >= 0f) this.sweepExtra = extra;
        return this;
    }

    public SwordJabPhysicsComponent setSweepCooldown(float seconds) {
        this.sweepCooldown = Math.max(0f, seconds);
        return this;
    }

    public SwordJabPhysicsComponent setSweepArcDegrees(float arcDeg) {
        this.sweepArcDeg = Math.max(0f, arcDeg);
        return this;
    }

    public SwordJabPhysicsComponent setSpinParams(float duration, float extra) {
        if (duration > 0f) this.spinDuration = duration;
        if (extra >= 0f) this.spinExtra = extra;
        return this;
    }

    public SwordJabPhysicsComponent setSpinCooldown(float seconds) {
        this.spinCooldown = Math.max(0f, seconds);
        return this;
    }

    public SwordJabPhysicsComponent setSpinDirectionCCW(boolean ccw) {
        this.spinDir = ccw ? +1f : -1f;
        return this;
    }

    public SwordJabPhysicsComponent setSpinTurns(float turns) {
        this.spinTurns = Math.max(0f, turns);
        return this;
    }

    public boolean isAttacking() {
        return jabbing || sweeping || spinning;
    }

    public float getFacingDeg() {
        return facingDeg;
    }

    @Override
    public void create() {
        physics = entity.getComponent(PhysicsComponent.class);
        if (physics == null) throw new IllegalStateException("SwordJabPhysicsComponent requires PhysicsComponent.");

        // NEW: 拿到多技能冷却组件
        cds = entity.getComponent(SkillCooldowns.class);

        autoCalibratePivotFromOwnerOrigin();
    }

    @Override
    public void update() {
        if (owner == null || physics == null || physics.getBody() == null) return;

        float dt = (Gdx.graphics != null) ? Gdx.graphics.getDeltaTime() : 1f / 60f;

        // 内部最小间隔计时（保留即可；若你不用内部间隔，可删）
        if (jabCdTimer > 0f) jabCdTimer -= dt;
        if (sweepCdTimer > 0f) sweepCdTimer -= dt;
        if (spinCdTimer > 0f) spinCdTimer -= dt;

        // 1) 取武士中心 + 可视枢轴偏移
        getEntityCenter(owner, ownerCenter);
        ownerCenter.add(pivotOffset);

        // 2) 决定当前角与半径
        float workAngleDeg = facingDeg;
        float curRadius = restRadius;

        if (jabbing) {
            jabT += dt;
            float a = Math.min(jabT / jabDuration, 1f);
            float bump = 4f * a * (1f - a);
            curRadius = restRadius + bump * jabExtra;
            workAngleDeg = jabAngleDeg;

            if (a >= 1f) {
                jabbing = false;
                facingDeg = jabAngleDeg;
                // 这里不再设置真正技能CD，交给 SkillCooldowns；若想保留防抖，可保留：
                jabCdTimer = jabCooldown;
            }
        } else if (sweeping) {
            sweepT += dt;
            float a = Math.min(sweepT / sweepDuration, 1f);
            float rel = (-sweepArcDeg) + a * (2f * sweepArcDeg);
            workAngleDeg = sweepBaseDeg + sweepDir * rel;
            curRadius = restRadius + sweepExtra;

            if (a >= 1f) {
                sweeping = false;
                facingDeg = sweepBaseDeg + sweepDir * sweepArcDeg;
                sweepCdTimer = sweepCooldown;
            }
        } else if (spinning) {
            spinT += dt;
            float a = Math.min(spinT / spinDuration, 1f);
            float totalDeg = 360f * spinTurns;
            workAngleDeg = spinStartDeg + spinDir * (a * totalDeg);
            curRadius = restRadius + spinExtra;

            if (a >= 1f) {
                spinning = false;
                facingDeg = spinStartDeg + spinDir * totalDeg;
                spinCdTimer = spinCooldown;
            }
        }

        // 3) 位姿计算
        float rad = (float) Math.toRadians(workAngleDeg);
        float dx = (float) Math.cos(rad);
        float dy = (float) Math.sin(rad);

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
    }

    public void triggerJabTowards(Vector2 targetWorld) {
        if (targetWorld == null) return;
        if (jabbing || sweeping || spinning) return;
        if (cds != null && !cds.isReady("jab")) return;   // NEW: 主CD判定
        if (jabCdTimer > 0f) return;                      // （可选）内部最小间隔

        getEntityCenter(owner, ownerCenter);
        ownerCenter.add(pivotOffset);

        float dx = targetWorld.x - ownerCenter.x;
        float dy = targetWorld.y - ownerCenter.y;
        if (Math.abs(dx) < 1e-5f && Math.abs(dy) < 1e-5f) return;

        this.jabAngleDeg = (float) Math.toDegrees(Math.atan2(dy, dx));
        this.jabbing = true;
        this.jabT = 0f;

        emitSwordQiAtAngle(jabAngleDeg);
        if (cds != null) cds.trigger("jab");             // NEW: 进入主CD
    }

    public void triggerSweepToward(Vector2 targetWorld) {
        if (targetWorld == null) return;
        if (sweeping || jabbing || spinning) return;
        if (cds != null && !cds.isReady("sweep")) return; // NEW: 主CD判定
        if (sweepCdTimer > 0f) return;                    // （可选）内部最小间隔

        getEntityCenter(owner, ownerCenter);
        ownerCenter.add(pivotOffset);

        float dx = targetWorld.x - ownerCenter.x;
        float dy = targetWorld.y - ownerCenter.y;
        if (Math.abs(dx) < 1e-5f && Math.abs(dy) < 1e-5f) return;

        float aimDeg = (float) Math.toDegrees(Math.atan2(dy, dx));
        this.sweepBaseDeg = aimDeg;

        float d = aimDeg - facingDeg;
        while (d <= -180f) d += 360f;
        while (d > 180f) d -= 360f;
        this.sweepDir = (d >= 0f) ? +1f : -1f;

        this.sweeping = true;
        this.sweepT = 0f;

        emitSwordQiAtAngle(sweepBaseDeg); // 中线
        emitSwordQiAtAngle(sweepBaseDeg + qiFanOffsetDeg);
        emitSwordQiAtAngle(sweepBaseDeg - qiFanOffsetDeg);

        if (cds != null) cds.trigger("sweep");           // NEW: 进入主CD
    }

    public void triggerSpin(boolean ccw) {
        if (spinning || jabbing || sweeping) return;
        if (cds != null && !cds.isReady("spin")) return;  // NEW: 主CD判定
        if (spinCdTimer > 0f) return;                     // （可选）内部最小间隔

        this.spinStartDeg = facingDeg;
        this.spinDir = ccw ? +1f : -1f;
        this.spinning = true;
        this.spinT = 0f;

        float step = 360f / Math.max(1, qiSpinCount);
        float start = spinStartDeg; // 也可用 facingDeg
        for (int i = 0; i < qiSpinCount; i++) {
            float ang = start + i * step;
            emitSwordQiAtAngle(ang);
        }

        if (cds != null) cds.trigger("spin");            // NEW: 进入主CD
    }

    // ===== Helpers =====
    private static void getEntityCenter(Entity e, Vector2 out) {
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
        out.set(
                pos.x + (scale != null ? scale.x * 0.5f : 0.5f),
                pos.y + (scale != null ? scale.y * 0.5f : 0.5f)
        );
    }

    private void autoCalibratePivotFromOwnerOrigin() {
        try {
            Object render = owner.getComponent(RotatingTextureRenderComponent.class);
            if (render == null) return;

            float originX = invokeFloat(render, "getOriginX", Float.NaN);
            float originY = invokeFloat(render, "getOriginY", Float.NaN);
            float width = invokeFloat(render, "getWidth", Float.NaN);
            float height = invokeFloat(render, "getHeight", Float.NaN);

            if (Float.isNaN(originX) || Float.isNaN(originY) || Float.isNaN(width) || Float.isNaN(height)) {
                originX = invokeFloat(render, "originX", Float.NaN);
                originY = invokeFloat(render, "originY", Float.NaN);
                width = invokeFloat(render, "getTextureWidth", Float.NaN);
                height = invokeFloat(render, "getTextureHeight", Float.NaN);
            }
            if (Float.isNaN(originX) || Float.isNaN(originY) || Float.isNaN(width) || Float.isNaN(height)) return;

            float dx = originX - width * 0.5f;
            float dy = originY - height * 0.5f;
            pivotOffset.add(dx, dy);
        } catch (Throwable ignored) {
        }
    }

    private static float invokeFloat(Object obj, String method, float fallback) {
        try {
            var m = obj.getClass().getMethod(method);
            Object r = m.invoke(obj);
            return (r instanceof Number) ? ((Number) r).floatValue() : fallback;
        } catch (Throwable e) {
            return fallback;
        }
    }

    // 替换你类中的 emitSwordQiAtAngle(float angleDeg)
    private void emitSwordQiAtAngle(float angleDeg) {
        // 方向向量
        float rad = (float) Math.toRadians(angleDeg);
        float ux = (float) Math.cos(rad);
        float uy = (float) Math.sin(rad);

        // owner 中心 + 可视枢轴偏移
        getEntityCenter(owner, ownerCenter);
        ownerCenter.add(pivotOffset);

        // ✅ 出生点：适当加大 forward，避免“生成即命中即销毁”
        float forward = Math.max(qiSpawnForward, 1.2f);
        Vector2 spawn = new Vector2(
                ownerCenter.x + ux * forward,
                ownerCenter.y + uy * forward
        );

        // ✅ 渲染尺寸（世界单位）
        float drawW = 1.2f;
        float drawH = 1.2f;

        // 贴图基准朝向：PNG 默认朝右=0f；若默认朝上改为 90f
        float baseRotation = 0f;

        // ✅ 为了便于“看见”，生命/帧时长稍微放宽；确认可见后再调回
        float life = Math.max(qiLife, 0.6f);
        float frameDur = 0.08f; // 比 0.06 更容易看清

        // 创建“剑气投射物”
        Entity qi = com.csse3200.game.entities.factories.SwordQiFactory.createSwordQi(
                spawn,
                ux * qiSpeed, uy * qiSpeed,   // 速度
                life,                         // 存活时间
                qiDamage,                     // 伤害
                drawW, drawH,                 // 渲染宽高（世界单位）
                "images/samurai/slash_red_thick_Heavy_6x1_64.png",
                6, 1,                         // 列、行
                64, 64,                       // 每帧像素
                frameDur,                     // 每帧时长
                angleDeg + spriteForwardOffsetDeg, // 朝向角
                baseRotation,                 // 素材基准朝向
                true                          // ✅ 先循环，确认可见；之后再改回 false
        );

        // ✅ 把动画渲染组件推到前景层，并给高 zIndex（避免被地形/单位遮住）
        var anim = qi.getComponent(com.csse3200.game.rendering.RotatingSheetAnimationRenderComponent.class);
        if (anim != null) {
            anim.setLayer(5);                // 只要比默认层(1)大就行，5 作为前景层
            anim.setZIndexOverride(9999f);   // 兜底：极高 z
        }

        // ✅ 尺寸防呆：如果 scale 很小或为 0，直接给一个可见尺寸（调试用）
        if (qi.getScale().isZero() || qi.getScale().x < 0.3f || qi.getScale().y < 0.3f) {
            qi.setScale(3.0f, 3.0f);         // 先看得见；确认后再回 1.2f
        }

        // （可选）监听动画播完事件：anim.setAutoRemoveOnFinish(true) 时可移除渲染等
        // qi.getEvents().addListener("animationFinished", (String name) -> { ... });

        // 注册实体
        ServiceLocator.getEntityService().register(qi);
    }


}

