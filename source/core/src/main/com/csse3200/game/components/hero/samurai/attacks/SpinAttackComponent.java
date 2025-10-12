package com.csse3200.game.components.hero.samurai.attacks;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;

public class SpinAttackComponent extends AbstractSwordAttackComponent {
    private boolean active=false;
    private float T=0f, duration=0.5f, extra=0.25f;
    private float startDeg=0f, dir=+1f, turns=1f;

    private float miniCooldown=0.0f, miniTimer=0f;

    public SpinAttackComponent(com.csse3200.game.entities.Entity owner, float restRadius){
        super(owner, restRadius);
    }

    public SpinAttackComponent setParams(float duration,float extra){ if(duration>0)this.duration=duration; if(extra>=0)this.extra=extra; return this; }
    public SpinAttackComponent setTurns(float t){ this.turns=Math.max(0f,t); return this; }
    public SpinAttackComponent setMiniCooldown(float s){ this.miniCooldown=Math.max(0f,s); return this; }

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
        this.dir = +1f; // 可暴露给外部改为 CCW
        active=true; T=0f;

        if (lock!=null) lock.tryAcquire("spin");
        if (cds!=null) cds.trigger("spin");

        // 圈发：均匀发射 qiSpinCount 个
        float step = 360f / Math.max(1, qiSpinCount);
        for (int i=0;i<qiSpinCount;i++){
            float ang = startDeg + i*step;
            emitSwordQiAtAngle(ang, "images/samurai/slash_red_thick_Heavy_6x1_64.png", 6,1,64,64,0.08f,true);
        }
    }

    public SpinAttackComponent setDirectionCCW(boolean ccw) {
        this.dir = ccw ? +1f : -1f;
        return this;
    }

    @Override public void update(float dt) {
        if (miniTimer>0f) miniTimer -= dt;
        if (!active) return;

        T += dt;
        float a = Math.min(T/duration, 1f);
        float totalDeg = 360f * turns;
        float workDeg = startDeg + dir*(a*totalDeg);
        float curRadius = restRadius + extra;

        setPose(workDeg, curRadius);

        if (a>=1f) {
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
}
