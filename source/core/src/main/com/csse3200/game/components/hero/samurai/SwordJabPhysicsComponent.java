package com.csse3200.game.components.hero.samurai;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.RotatingTextureRenderComponent;

/**
 * Sword Jab + Sweep + Spin physics:
 * - Idle: 剑柄停在 owner 前方 restRadius 处，维持 facingDeg。
 * - Jab:  朝目标方向“出-回”半径动画（抛物线 bump）。
 * - Sweep: 以鼠标方向为中心，做对称半圆（[aim-arc, aim+arc] 线性）。
 * - Spin:  固定半径整圈旋转（默认逆时针 360°）。
 *
 * 只负责位姿；伤害由其他组件判定（可用 isAttacking() 做攻击窗口）。
 */
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
    private float jabExtra = 0.8f;     // 半径“出-回”额外位移
    private float jabAngleDeg = 0f;    // 本次 Jab 的角度（朝向）
    private float jabCooldown = 0f;
    private float jabCdTimer = 0f;

    // ===== Sweep（横砍，以鼠标方向为中心）=====
    private boolean sweeping = false;
    private float sweepT = 0f;
    private float sweepDuration = 0.22f;
    private float sweepExtra = 0.35f;  // 横砍时半径增加
    private float sweepCooldown = 0f;
    private float sweepCdTimer = 0f;

    private float sweepArcDeg = 60f;   // 单侧角度（总扫角 = 2*arc）
    private float sweepBaseDeg = 0f;   // 中心角（= 鼠标方向）
    private float sweepDir = +1f;      // +1：左→右；-1：右→左

    // ===== Spin（整圈旋转，L键）=====
    private boolean spinning = false;
    private float spinT = 0f;
    private float spinDuration = 0.5f; // 旋风斩时长（秒）
    private float spinExtra = 0.25f;   // 旋风斩时半径增加
    private float spinCooldown = 0f;   // 冷却（秒）
    private float spinCdTimer = 0f;
    private float spinStartDeg = 0f;   // 起始角（通常取当前 facingDeg）
    private float spinDir = +1f;       // +1 逆时针，-1 顺时针
    private float spinTurns = 1f;      // 旋转圈数（默认 1 圈 = 360°）

    public SwordJabPhysicsComponent(Entity owner, float restRadius) {
        this.owner = owner;
        this.restRadius = restRadius;
    }

    // ===== 配置项 =====
    public SwordJabPhysicsComponent setSpriteForwardOffsetDeg(float deg) { this.spriteForwardOffsetDeg = deg; return this; }
    public SwordJabPhysicsComponent setCenterToHandle(float d) { this.centerToHandle = d; return this; }
    public SwordJabPhysicsComponent setPivotOffset(float ox, float oy) { this.pivotOffset.set(ox, oy); return this; }
    public SwordJabPhysicsComponent setRestRadius(float r) { this.restRadius = r; return this; }

    public SwordJabPhysicsComponent setJabParams(float duration, float extra) {
        if (duration > 0f) this.jabDuration = duration;
        if (extra > 0f) this.jabExtra = extra;
        return this;
    }
    public SwordJabPhysicsComponent setJabCooldown(float seconds) { this.jabCooldown = Math.max(0f, seconds); return this; }

    public SwordJabPhysicsComponent setSweepParams(float duration, float extra) {
        if (duration > 0f) this.sweepDuration = duration;
        if (extra >= 0f) this.sweepExtra = extra;
        return this;
    }
    public SwordJabPhysicsComponent setSweepCooldown(float seconds) { this.sweepCooldown = Math.max(0f, seconds); return this; }
    public SwordJabPhysicsComponent setSweepArcDegrees(float arcDeg) { this.sweepArcDeg = Math.max(0f, arcDeg); return this; }

    // Spin 参数
    public SwordJabPhysicsComponent setSpinParams(float duration, float extra) {
        if (duration > 0f) this.spinDuration = duration;
        if (extra >= 0f) this.spinExtra = extra;
        return this;
    }
    public SwordJabPhysicsComponent setSpinCooldown(float seconds) { this.spinCooldown = Math.max(0f, seconds); return this; }
    public SwordJabPhysicsComponent setSpinDirectionCCW(boolean ccw) { this.spinDir = ccw ? +1f : -1f; return this; }
    public SwordJabPhysicsComponent setSpinTurns(float turns) { this.spinTurns = Math.max(0f, turns); return this; }

    /** 是否处于攻击窗口（给伤害组件判定用） */
    public boolean isAttacking() { return jabbing || sweeping || spinning; }

    /** 可选：暴露当前朝向（用于外部特效/音效朝向） */
    public float getFacingDeg() { return facingDeg; }

    @Override
    public void create() {
        physics = entity.getComponent(PhysicsComponent.class);
        if (physics == null) throw new IllegalStateException("SwordJabPhysicsComponent requires PhysicsComponent.");
        autoCalibratePivotFromOwnerOrigin();
    }

    @Override
    public void update() {
        if (owner == null || physics == null || physics.getBody() == null) return;

        float dt = (Gdx.graphics != null) ? Gdx.graphics.getDeltaTime() : 1f/60f;
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
            float a = Math.min(jabT / jabDuration, 1f); // 0..1
            float bump = 4f * a * (1f - a);             // 出回抛物线
            curRadius = restRadius + bump * jabExtra;
            workAngleDeg = jabAngleDeg;

            if (a >= 1f) {
                jabbing = false;
                facingDeg = jabAngleDeg;
                jabCdTimer = jabCooldown;
            }
        } else if (sweeping) {
            sweepT += dt;
            float a = Math.min(sweepT / sweepDuration, 1f); // 0..1
            float rel = (-sweepArcDeg) + a * (2f * sweepArcDeg); // 从 -arc 到 +arc
            workAngleDeg = sweepBaseDeg + sweepDir * rel;
            curRadius = restRadius + sweepExtra;

            if (a >= 1f) {
                sweeping = false;
                facingDeg = sweepBaseDeg + sweepDir * sweepArcDeg;
                sweepCdTimer = sweepCooldown;
            }
        } else if (spinning) {
            // === 旋风斩：整圈旋转，不做角度归一化 ===
            spinT += dt;
            float a = Math.min(spinT / spinDuration, 1f); // 0..1
            float totalDeg = 360f * spinTurns;
            workAngleDeg = spinStartDeg + spinDir * (a * totalDeg);
            curRadius = restRadius + spinExtra;

            if (a >= 1f) {
                spinning = false;
                facingDeg = spinStartDeg + spinDir * totalDeg;
                spinCdTimer = spinCooldown;
            }
        }

        // 3) 计算位姿（把“剑柄点”放在 workAngleDeg 的 curRadius 处）
        float rad = (float) Math.toRadians(workAngleDeg);
        float dx = (float) Math.cos(rad);
        float dy = (float) Math.sin(rad);

        float handleX = ownerCenter.x + curRadius * dx;
        float handleY = ownerCenter.y + curRadius * dy;

        float centerX = handleX - centerToHandle * dx;
        float centerY = handleY - centerToHandle * dy;

        float swordAngleDeg = workAngleDeg + spriteForwardOffsetDeg;
        float swordAngleRad = (float) Math.toRadians(swordAngleDeg);

        // 4) 同步物理与渲染角度
        physics.getBody().setTransform(centerX, centerY, swordAngleRad);
        var rot = entity.getComponent(RotatingTextureRenderComponent.class);
        if (rot != null) {
            float vis = ((swordAngleDeg % 360f) + 360f) % 360f; // 仅可视取模
            rot.setRotation(vis);
        }
    }

    /** 触发一次朝目标点的突刺（传入鼠标世界坐标） */
    public void triggerJabTowards(Vector2 targetWorld) {
        if (targetWorld == null) return;
        if (jabbing || sweeping || spinning) return;
        if (jabCdTimer > 0f) return;

        getEntityCenter(owner, ownerCenter);
        ownerCenter.add(pivotOffset);

        float dx = targetWorld.x - ownerCenter.x;
        float dy = targetWorld.y - ownerCenter.y;
        if (Math.abs(dx) < 1e-5f && Math.abs(dy) < 1e-5f) return;

        this.jabAngleDeg = (float) Math.toDegrees(Math.atan2(dy, dx));
        this.jabbing = true;
        this.jabT = 0f;
    }

    /** 横砍（以鼠标方向为中心） */
    public void triggerSweepToward(Vector2 targetWorld) {
        if (targetWorld == null) return;
        if (sweeping || jabbing || spinning) return;
        if (sweepCdTimer > 0f) return;

        getEntityCenter(owner, ownerCenter);
        ownerCenter.add(pivotOffset);

        float dx = targetWorld.x - ownerCenter.x;
        float dy = targetWorld.y - ownerCenter.y;
        if (Math.abs(dx) < 1e-5f && Math.abs(dy) < 1e-5f) return;

        float aimDeg = (float) Math.toDegrees(Math.atan2(dy, dx));
        this.sweepBaseDeg = aimDeg;

        float d = aimDeg - facingDeg;
        while (d <= -180f) d += 360f;
        while (d >   180f) d -= 360f;
        this.sweepDir = (d >= 0f) ? +1f : -1f;

        this.sweeping = true;
        this.sweepT = 0f;
    }

    /** 触发旋风斩（整圈），ccw=true 逆时针，false 顺时针 */
    public void triggerSpin(boolean ccw) {
        if (spinning || jabbing || sweeping) return;
        if (spinCdTimer > 0f) return;

        this.spinStartDeg = facingDeg;
        this.spinDir = ccw ? +1f : -1f;
        this.spinning = true;
        this.spinT = 0f;
    }

    // ===== Helpers =====
    private static void getEntityCenter(Entity e, Vector2 out) {
        try {
            Vector2 c = e.getCenterPosition();
            if (c != null) { out.set(c); return; }
        } catch (Throwable ignored) {}
        Vector2 pos = e.getPosition();
        Vector2 scale = e.getScale();
        out.set(
                pos.x + (scale != null ? scale.x * 0.5f : 0.5f),
                pos.y + (scale != null ? scale.y * 0.5f : 0.5f)
        );
    }

    /** 尝试从渲染组件读取 origin/width/height 微调枢轴 */
    private void autoCalibratePivotFromOwnerOrigin() {
        try {
            Object render = owner.getComponent(RotatingTextureRenderComponent.class);
            if (render == null) return;

            float originX = invokeFloat(render, "getOriginX", Float.NaN);
            float originY = invokeFloat(render, "getOriginY", Float.NaN);
            float width   = invokeFloat(render, "getWidth",   Float.NaN);
            float height  = invokeFloat(render, "getHeight",  Float.NaN);

            if (Float.isNaN(originX) || Float.isNaN(originY) || Float.isNaN(width) || Float.isNaN(height)) {
                originX = invokeFloat(render, "originX", Float.NaN);
                originY = invokeFloat(render, "originY", Float.NaN);
                width   = invokeFloat(render, "getTextureWidth", Float.NaN);
                height  = invokeFloat(render, "getTextureHeight", Float.NaN);
            }
            if (Float.isNaN(originX) || Float.isNaN(originY) || Float.isNaN(width) || Float.isNaN(height)) return;

            float dx = originX - width  * 0.5f;
            float dy = originY - height * 0.5f;
            pivotOffset.add(dx, dy);
        } catch (Throwable ignored) {}
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
}
