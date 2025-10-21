package com.csse3200.game.components.hero.samurai.attacks;

import com.badlogic.gdx.math.Vector2;

public class SpinAttackComponent extends AbstractSwordAttackComponent {
    private boolean active=false;
    private float T=0f, duration=0.5f, extra=0.25f;
    private float startDeg=0f, dir=+1f, turns=1f;

    private float miniCooldown=0.0f, miniTimer=0f;

    // ====== 新增：分批发射相关 ======
    /** 每发间隔（秒）——你要的约 0.3s */
    private float emitInterval = 0.30f;
    /** 累积计时器 */
    private float emitAccum = 0f;
    /** 已发射数量 / 需发射总数 */
    private int emitted = 0, emitTotal = 0;
    /** 发射角步进 */
    private float spinStepDeg = 0f;
    /** 防止掉帧导致一次补发太多（每帧最多补发几发） */
    private int emitCatchUpMaxPerFrame = 1;

    // 贴图/动画参数（保持你现用的资产）
    private String qiPngPath = "images/samurai/slash_red_thick_Heavy_6x1_64.png";
    private int sheetCols = 6, sheetRows = 1, frameW = 64, frameH = 64;
    private float frameDur = 0.08f;
    private boolean transparent = true;

    public SpinAttackComponent(com.csse3200.game.entities.Entity owner, float restRadius){
        super(owner, restRadius);
    }

    public SpinAttackComponent setParams(float duration,float extra){
        if(duration>0)this.duration=duration;
        if(extra>=0)this.extra=extra;
        return this;
    }
    public SpinAttackComponent setTurns(float t){ this.turns=Math.max(0f,t); return this; }
    public SpinAttackComponent setMiniCooldown(float s){ this.miniCooldown=Math.max(0f,s); return this; }

    /** 可调：每发间隔（默认0.3s） */
    public SpinAttackComponent setEmitInterval(float s){ this.emitInterval=Math.max(0f,s); return this; }
    /** 可调：掉帧补发上限（默认1，想更快补齐可升到2~3） */
    public SpinAttackComponent setEmitCatchUpMaxPerFrame(int n){ this.emitCatchUpMaxPerFrame=Math.max(1,n); return this; }

    @Override public boolean isActive(){ return active; }

    @Override public boolean canTrigger() {
        boolean cdOk = (cds==null) || cds.isReady("spin");
        boolean notBusy = (lock==null) || !lock.isBusy();
        boolean miniOk = miniTimer<=0f;
        return !active && cdOk && notBusy && miniOk;
    }

    @Override public void trigger(Vector2 ignored) {
        if (!canTrigger()) return;

        this.startDeg = facingDeg;
        this.dir = +1f; // 如需顺/逆时针可通过 setDirectionCCW 调整
        active=true; T=0f;

        if (lock!=null) lock.tryAcquire("spin");
        if (cds!=null) cds.trigger("spin");

        // 预计算：本次总发数与角步进
        emitTotal = Math.max(1, qiSpinCount);
        spinStepDeg = 360f / (float) emitTotal;
        emitted = 0;
        emitAccum = 0f;

        // 若动画时长太短，不足以覆盖所有分批发射，自动延长到能发完为止（留点余量）
        float need = (emitTotal > 0) ? (emitInterval * (emitTotal - 1) + 0.05f) : 0f;
        if (duration < need) duration = need;

        // 立刻发第一发，手感更好
        emitOne(emitted);
        emitted++;
    }

    public SpinAttackComponent setDirectionCCW(boolean ccw) {
        this.dir = ccw ? +1f : -1f;
        return this;
    }

    @Override public void update(float dt) {
        if (miniTimer>0f) miniTimer -= dt;
        if (!active) return;

        T += dt;

        // 旋转姿态更新（与你原逻辑一致）
        float a = Math.min(T/duration, 1f);
        float totalDeg = 360f * turns;
        float workDeg = startDeg + dir*(a*totalDeg);
        float curRadius = restRadius + extra;
        setPose(workDeg, curRadius);

        // === 分批发射 ===
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

        // 结束条件：动画走完 且 全部子弹已发射
        if (a>=1f && emitted >= emitTotal) {
            active=false;
            miniTimer=miniCooldown;
            if (lock!=null) lock.release("spin");
        }
    }

    @Override public void cancel() {
        if (active) {
            active=false;
            if (lock!=null) lock.release("spin");
        }
    }

    private float spinSpeed  = 9.0f;
    private float spinLife   = 0.28f;
    private int   spinDamage = 10;
    private float spinDrawW  = 1.2f;
    private float spinDrawH  = 1.2f;

    // 公开 setter，方便从外面平衡性调整或按等级升级
    public SpinAttackComponent setSpinProjectile(float speed, float life, int damage) {
        if (speed>0) this.spinSpeed = speed;
        if (life>0)  this.spinLife  = life;
        this.spinDamage = Math.max(0, damage);
        return this;
    }
    public SpinAttackComponent setSpinDrawSize(float w, float h) {
        if (w>0) this.spinDrawW = w;
        if (h>0) this.spinDrawH = h;
        return this;
    }

    /** 实际发射一发，按“起始角 + 步进 * 序号” */
    private void emitOne(int idx){
        float ang = startDeg + idx * spinStepDeg;
        emitSwordQiAtAngle(
                ang,
                qiPngPath, sheetCols, sheetRows, frameW, frameH, frameDur, transparent,
                spinSpeed, spinLife, spinDamage, spinDrawW, spinDrawH
        );
    }
}

