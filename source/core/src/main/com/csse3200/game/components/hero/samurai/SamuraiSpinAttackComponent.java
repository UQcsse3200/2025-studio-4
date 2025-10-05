package com.csse3200.game.components.hero.samurai;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.entities.configs.SamuraiConfig;

public class SamuraiSpinAttackComponent extends Component {
    private final float restRadius;
    private final String swordTexture;
    private final Camera camera;

    private float spriteForwardOffsetDeg = 0f;
    private float centerToHandle = -1.0f;
    private float jabDuration = 0.18f, jabExtra = 0.8f, jabCooldown = 0f;
    private float sweepDuration = 0.22f, sweepExtra = 0.35f, sweepCooldown = 0f;
    private final SamuraiConfig cfg;


    private Entity sword;
    private InputAdapter adapter; // 保存起来用于 dispose 时移除

    public SamuraiSpinAttackComponent(float restRadius, String swordTexture, SamuraiConfig cfg,Camera camera) {
        this.restRadius = restRadius;
        this.swordTexture = swordTexture;
        this.cfg = cfg;
        this.camera = camera;
    }

    public SamuraiSpinAttackComponent setSpriteForwardOffsetDeg(float deg) { this.spriteForwardOffsetDeg = deg; return this; }
    public SamuraiSpinAttackComponent setCenterToHandle(float d) { this.centerToHandle = d; return this; }
    public SamuraiSpinAttackComponent setJabParams(float duration, float extra, float cooldown) {
        if (duration > 0f) this.jabDuration = duration;
        if (extra > 0f) this.jabExtra = extra;
        this.jabCooldown = Math.max(0f, cooldown);
        return this;
    }
    public SamuraiSpinAttackComponent setSweepParams(float duration, float extra, float cooldown) {
        if (duration > 0f) this.sweepDuration = duration;
        if (extra >= 0f) this.sweepExtra = extra;
        this.sweepCooldown = Math.max(0f, cooldown);
        return this;
    }

    @Override
    public void create() {
        super.create();

        // 1) 创建剑（角速度参数给 0 即可）
        sword = com.csse3200.game.entities.factories.SwordFactory.createSword(
                this.entity, cfg,swordTexture, restRadius, 0f
        );

        // 2) 挂运动控制（J/K/L）
        var motion = new SwordJabPhysicsComponent(this.entity, restRadius)
                .setSpriteForwardOffsetDeg(spriteForwardOffsetDeg)
                .setCenterToHandle(centerToHandle)
                .setJabParams(jabDuration, jabExtra)
                .setJabCooldown(jabCooldown)
                .setSweepParams(sweepDuration, sweepExtra)
                .setSweepCooldown(sweepCooldown)
                .setSweepArcDegrees(60f)
                .setSpinParams(0.5f, 0.25f)
                .setSpinCooldown(0.6f)
                .setSpinDirectionCCW(true)
                .setSpinTurns(1f);
        sword.addComponent(motion);

        // 3) 命中组件（确保 sword 有 Hitbox/Collider）
        sword.addComponent(new AttackOnContactIfAttacking(
                25,
                com.csse3200.game.physics.PhysicsLayer.ENEMY,
                0.10f
        ));

        // 4) 注册到实体服务
        var es = ServiceLocator.getEntityService();
        if (es != null) Gdx.app.postRunnable(() -> es.register(sword));

        // 5) 绑定 J/K/L 输入
        adapter = new InputAdapter() {
            private final Vector3 tmp = new Vector3();
            private final Vector2 mouseWorld = new Vector2();

            @Override public boolean keyDown(int keycode) {
                if (sword == null || camera == null) return false; // 不吞事件

                // 屏幕 -> 世界
                tmp.set(Gdx.input.getX(), Gdx.input.getY(), 0f);
                camera.unproject(tmp);
                mouseWorld.set(tmp.x, tmp.y);

                // ✅ 拿回“当前剑”的控制组件（注意类型是 SwordJabPhysicsComponent）
                var ctrl = sword.getComponent(SwordJabPhysicsComponent.class);
                if (ctrl == null) return false;

                if (keycode == Input.Keys.J) {
                    ctrl.triggerJabTowards(mouseWorld);
                    return true;
                } else if (keycode == Input.Keys.K) {
                    ctrl.triggerSweepToward(mouseWorld);
                    return true;
                } else if (keycode == Input.Keys.L) {
                    ctrl.triggerSpin(true); // 逆时针
                    return true;
                }
                return false;
            }
        };

        var cur = Gdx.input.getInputProcessor();
        if (cur instanceof InputMultiplexer mux) {
            mux.addProcessor(0, adapter);
        } else {
            var mux = new InputMultiplexer();
            mux.addProcessor(adapter);
            if (cur != null) mux.addProcessor(cur);
            Gdx.input.setInputProcessor(mux);
        }
    }

    @Override
    public void dispose() {
        super.dispose();

        // 从输入多路复用器移除 adapter，避免泄漏
        var cur = Gdx.input.getInputProcessor();
        if (adapter != null && cur instanceof InputMultiplexer mux) {
            mux.removeProcessor(adapter);
            adapter = null;
        }

        if (sword != null) {
            sword.dispose();
            sword = null;
        }
    }
}


