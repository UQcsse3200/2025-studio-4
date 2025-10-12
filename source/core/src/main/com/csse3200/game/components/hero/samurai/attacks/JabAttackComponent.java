package com.csse3200.game.components.hero.samurai.attacks;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;

public class JabAttackComponent extends AbstractSwordAttackComponent {
    private boolean active=false;
    private float T=0f, duration=0.18f, extra=0.8f;
    private float angleDeg=0f;

    // 防抖（保持你原先的小间隔）
    private float miniCooldown=0.0f, miniTimer=0f;

    public JabAttackComponent(com.csse3200.game.entities.Entity owner, float restRadius){
        super(owner, restRadius);
    }

    public JabAttackComponent setParams(float duration, float extra){ if(duration>0) this.duration=duration; if(extra>0) this.extra=extra; return this; }
    public JabAttackComponent setMiniCooldown(float s){ this.miniCooldown=Math.max(0f,s); return this; }

    @Override public boolean isActive(){ return active; }

    @Override public boolean canTrigger() {
        boolean cdOk = (cds==null) || cds.isReady("jab");
        boolean notBusy = (lock==null) || !lock.isBusy();
        boolean miniOk = miniTimer<=0f;
        return !active && cdOk && notBusy && miniOk;
    }

    @Override
    public void trigger(Vector2 target) {
        if (target==null || !canTrigger()) return;
        float dt = (Gdx.graphics!=null)? Gdx.graphics.getDeltaTime() : 1f/60f;

        // 角度计算（保持你原来的逻辑）
        getEntityCenter(owner, ownerCenter); ownerCenter.add(pivotOffset);
        float dx = target.x - ownerCenter.x, dy = target.y - ownerCenter.y;
        if (Math.abs(dx)<1e-5f && Math.abs(dy)<1e-5f) return;

        this.angleDeg = (float)Math.toDegrees(Math.atan2(dy, dx));
        this.active = true; this.T=0f;

        if (lock!=null) lock.tryAcquire("jab");
        if (cds!=null) cds.trigger("jab");

        // --- 只改这一段：为 Jab 指定更短生命、更高伤害、更快或更慢的速度 ---
        float jabSpeed  = 3.0f;   // 你想让它飞不远，可稍慢（也可靠 life 控制）
        float jabLife   = 0.30f;  // 寿命短（飞行时间短）
        int   jabDamage = 30;     // Jab 更痛（例子）
        float drawW = 1.6f, drawH = 1.6f; // 如需更粗/更明显，可调大

        emitSwordQiAtAngle(
                angleDeg,
                "images/samurai/slash_red_thick_Heavy_6x1_64.png",
                6, 1, 64, 64, 0.08f, true,
                jabSpeed, jabLife, jabDamage,
                drawW, drawH
        );
    }


    @Override public void update(float dt) {
        if (miniTimer>0f) miniTimer -= dt;
        if (!active) return;

        T += dt;
        float a = Math.min(T/duration, 1f);
        float bump = 4f*a*(1f-a);
        float curRadius = restRadius + bump*extra;

        setPose(angleDeg, curRadius);

        if (a>=1f) {
            active=false;
            miniTimer = miniCooldown;
            if (lock!=null) lock.release("jab");
        }
    }

    @Override public void cancel() {
        if (active) {
            active=false;
            if (lock!=null) lock.release("jab");
        }
    }
}
