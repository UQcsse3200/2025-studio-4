package com.csse3200.game.components.hero.samurai.attacks;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;

public class SweepAttackComponent extends AbstractSwordAttackComponent {
    private boolean active=false;
    private float T=0f, duration=0.22f, extra=0.35f;
    private float arcDeg=60f, baseDeg=0f, dir=+1f;

    private float miniCooldown=0.0f, miniTimer=0f;

    public SweepAttackComponent(com.csse3200.game.entities.Entity owner, float restRadius){
        super(owner, restRadius);
    }

    public SweepAttackComponent setParams(float duration,float extra){ if(duration>0)this.duration=duration; if(extra>=0)this.extra=extra; return this; }
    public SweepAttackComponent setArcDeg(float deg){ this.arcDeg=Math.max(0f,deg); return this; }
    public SweepAttackComponent setMiniCooldown(float s){ this.miniCooldown=Math.max(0f,s); return this; }

    @Override public boolean isActive(){ return active; }

    @Override public boolean canTrigger() {
        boolean cdOk = (cds==null) || cds.isReady("sweep");
        boolean notBusy = (lock==null) || !lock.isBusy();
        boolean miniOk = miniTimer<=0f;
        return !active && cdOk && notBusy && miniOk;
    }

    @Override public void trigger(Vector2 target) {
        if (target==null || !canTrigger()) return;

        getEntityCenter(owner, ownerCenter); ownerCenter.add(pivotOffset);
        float dx = target.x - ownerCenter.x, dy = target.y - ownerCenter.y;
        if (Math.abs(dx)<1e-5f && Math.abs(dy)<1e-5f) return;

        float aimDeg = (float)Math.toDegrees(Math.atan2(dy, dx));
        this.baseDeg = aimDeg;

        float d = aimDeg - facingDeg;
        while (d<=-180f) d+=360f;
        while (d>180f)  d-=360f;
        this.dir = (d>=0f)? +1f : -1f;

        active=true; T=0f;
        if (lock!=null) lock.tryAcquire("sweep");
        if (cds!=null) cds.trigger("sweep");

        // 三扇形即时剑气
        emitSwordQiAtAngle(baseDeg, "images/samurai/slash_red_thick_Heavy_6x1_64.png", 6,1,64,64,0.08f,true);
        emitSwordQiAtAngle(baseDeg + qiFanOffsetDeg, "images/samurai/slash_red_thick_Heavy_6x1_64.png", 6,1,64,64,0.08f,true);
        emitSwordQiAtAngle(baseDeg - qiFanOffsetDeg, "images/samurai/slash_red_thick_Heavy_6x1_64.png", 6,1,64,64,0.08f,true);
    }

    @Override public void update(float dt) {
        if (miniTimer>0f) miniTimer -= dt;
        if (!active) return;

        T += dt;
        float a = Math.min(T/duration, 1f);
        float rel = (-arcDeg) + a*(2f*arcDeg);
        float workDeg = baseDeg + dir*rel;
        float curRadius = restRadius + extra;

        setPose(workDeg, curRadius);

        if (a>=1f) {
            active=false;
            miniTimer=miniCooldown;
            if (lock!=null) lock.release("sweep");
        }
    }

    @Override public void cancel() {
        if (active) {
            active=false;
            if (lock!=null) lock.release("sweep");
        }
    }
}
