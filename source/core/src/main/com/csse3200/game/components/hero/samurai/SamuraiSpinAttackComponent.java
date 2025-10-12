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

        // 1) 创建“纯可视”的剑（工厂内已挂 SwordJabPhysicsComponent）
        sword = com.csse3200.game.entities.factories.SwordFactory.createSword(
                this.entity, cfg, swordTexture, restRadius, 0f
        );

        // 可选：在这里拿到控制器做参数微调（若工厂没设）
        var ctrl = sword.getComponent(SwordJabPhysicsComponent.class);
        if (ctrl != null) {
            ctrl.setSpriteForwardOffsetDeg(spriteForwardOffsetDeg)
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
        }

        // 2) 注册实体
        var es = ServiceLocator.getEntityService();
        if (es != null) Gdx.app.postRunnable(() -> es.register(sword));

        // 3) 绑定输入（只触发 trigger...）
        adapter = new InputAdapter() {
            private final Vector3 tmp = new Vector3();
            private final Vector2 mouseWorld = new Vector2();
            @Override public boolean keyDown(int keycode) {
                if (sword == null || camera == null) return false;
                tmp.set(Gdx.input.getX(), Gdx.input.getY(), 0f);
                camera.unproject(tmp);
                mouseWorld.set(tmp.x, tmp.y);

                var c = sword.getComponent(SwordJabPhysicsComponent.class);
                if (c == null) return false;

                if (keycode == Input.Keys.NUM_1) { c.triggerJabTowards(mouseWorld); return true; }
                if (keycode == Input.Keys.NUM_2) { c.triggerSweepToward(mouseWorld); return true; }
                if (keycode == Input.Keys.NUM_3) { c.triggerSpin(true); return true; }
                return false;
            }
        };

        var cur = Gdx.input.getInputProcessor();
        if (cur instanceof InputMultiplexer mux) mux.addProcessor(0, adapter);
        else {
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


