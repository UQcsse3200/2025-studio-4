package com.csse3200.game.components.hero.samurai.attacks;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;

public class SamuraiSwordController extends Component {
    private final ISamuraiAttack jab, sweep, spin;

    public SamuraiSwordController(ISamuraiAttack jab, ISamuraiAttack sweep, ISamuraiAttack spin) {
        this.jab = jab; this.sweep = sweep; this.spin = spin;
    }

    @Override
    public void update() {
        float dt = (com.badlogic.gdx.Gdx.graphics!=null)? com.badlogic.gdx.Gdx.graphics.getDeltaTime() : 1f/60f;
        if (jab!=null) jab.update(dt);
        if (sweep!=null) sweep.update(dt);
        if (spin!=null) spin.update(dt);
    }

    public boolean triggerJab(Vector2 target){ if (jab!=null && jab.canTrigger()){ jab.trigger(target); return true;} return false; }
    public boolean triggerSweep(Vector2 target){ if (sweep!=null && sweep.canTrigger()){ sweep.trigger(target); return true;} return false; }
    public boolean triggerSpin(boolean ccw) {
        if (spin instanceof SpinAttackComponent) {
            SpinAttackComponent s = (SpinAttackComponent) spin; // 兼容老版 Java 的写法
            s.setDirectionCCW(ccw);                             // ✅ 用公开 setter，别直接改私有字段
        }
        if (spin != null && spin.canTrigger()) {
            spin.trigger(null);
            return true;
        }
        return false;
    }


    public float getFacingDeg() {
        // 谁在动取谁；都不动则从 jab 取（它维护 facingDeg）
        if (jab!=null && jab.isActive()) return jab.getFacingDeg();
        if (sweep!=null && sweep.isActive()) return sweep.getFacingDeg();
        if (spin!=null && spin.isActive()) return spin.getFacingDeg();
        return jab!=null? jab.getFacingDeg() : 0f;
    }

    public boolean isAttacking() {
        return (jab!=null && jab.isActive()) || (sweep!=null && sweep.isActive()) || (spin!=null && spin.isActive());
    }
}
