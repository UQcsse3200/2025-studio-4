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
import com.csse3200.game.entities.configs.SamuraiConfig;
import com.csse3200.game.services.ServiceLocator;

// ✅ 新增：引入你刚拆分出来的攻击组件与控制器
import com.csse3200.game.components.hero.samurai.attacks.AttackLockComponent;
import com.csse3200.game.components.hero.samurai.attacks.JabAttackComponent;
import com.csse3200.game.components.hero.samurai.attacks.SweepAttackComponent;
import com.csse3200.game.components.hero.samurai.attacks.SpinAttackComponent;
import com.csse3200.game.components.hero.samurai.attacks.SamuraiSwordController;

/**
 * 负责：生成“剑”实体 + 安装三种攻击组件 + 处理输入并转发给控制器。
 * 注意：SwordFactory.createSword(...) 需要只创建“视觉+物理”的剑，
 * 不要再往里挂旧的 SwordJabPhysicsComponent。
 */
public class SamuraiSpinAttackComponent extends Component {
    private final float restRadius;
    private final String swordTexture;
    private final Camera camera;
    private final SamuraiConfig cfg;

    // 视觉/手感参数
    private float spriteForwardOffsetDeg = 0f;
    private float centerToHandle = -1.0f;

    // jab / sweep 默认参数（可继续从外部 set）
    private float jabDuration = 0.18f, jabExtra = 0.8f, jabCooldown = 0f;
    private float sweepDuration = 0.22f, sweepExtra = 0.35f, sweepCooldown = 0f;

    // 组件引用
    private Entity sword;                 // 剑实体（承载物理 + 渲染 + 攻击组件）
    private SamuraiSwordController ctrl;  // 控制器
    private InputAdapter adapter;         // 输入适配器

    public SamuraiSpinAttackComponent(float restRadius, String swordTexture, SamuraiConfig cfg, Camera camera) {
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

        // 1) 仅创建“纯可视+物理”的剑实体（⚠️ 工厂里请不要再挂旧的 SwordJabPhysicsComponent）
        sword = com.csse3200.game.entities.factories.SwordFactory.createSword(
                this.entity, cfg, swordTexture, restRadius, /*initialFacing*/0f
        );

        // 2) 在剑实体上安装“互斥锁 + 三种攻击 + 控制器”
        var lock = new AttackLockComponent();

        var jab = new JabAttackComponent(this.entity /*owner=武士本体*/, restRadius)
                .setParams(jabDuration, jabExtra)        // 动作时长/位移感
                .setMiniCooldown(jabCooldown)            // 内部最小间隔（可与 SkillCooldowns 并存）
                .setSpriteForwardOffsetDeg(spriteForwardOffsetDeg)
                .setCenterToHandle(centerToHandle)
                .setPivotOffset(0f, 0f);

        var sweep = new SweepAttackComponent(this.entity, restRadius)
                .setParams(sweepDuration, sweepExtra)
                .setArcDeg(60f)
                .setMiniCooldown(sweepCooldown)
                .setSpriteForwardOffsetDeg(spriteForwardOffsetDeg)
                .setCenterToHandle(centerToHandle)
                .setPivotOffset(0f, 0f);

        var spin = new SpinAttackComponent(this.entity, restRadius)
                .setParams(0.5f, 0.25f)       // duration, extra
                .setTurns(1f)
                .setMiniCooldown(0.6f)        // 你原先的 spin 冷却
                .setSpriteForwardOffsetDeg(spriteForwardOffsetDeg)
                .setCenterToHandle(centerToHandle)
                .setPivotOffset(0f, 0f);


        ctrl = new SamuraiSwordController(jab, sweep, spin);

        this.entity.getEvents().addListener("ui:samurai:attack", (String type) -> {
            if (ctrl == null || camera == null) return;

            // 复用你键盘里同样的“鼠标世界坐标”逻辑
            com.badlogic.gdx.math.Vector3 tmp = new com.badlogic.gdx.math.Vector3(Gdx.input.getX(), Gdx.input.getY(), 0f);
            camera.unproject(tmp);
            com.badlogic.gdx.math.Vector2 mouseWorld = new com.badlogic.gdx.math.Vector2(tmp.x, tmp.y);

            boolean ok = false;
            switch (type) {
                case "jab"   -> ok = ctrl.triggerJab(mouseWorld);
                case "sweep" -> ok = ctrl.triggerSweep(mouseWorld);
                case "spin"  -> ok = ctrl.triggerSpin(true); // 这里默认逆时针，可按需改参
            }
            if (!ok) {
                // 如果触发失败（冷却/锁），你可以在这里给 UI 回发提示或冷却时间
                this.entity.getEvents().trigger("ui:toast", "Cannot use now");
            }
        });

        sword.addComponent(lock)
                .addComponent(jab)
                .addComponent(sweep)
                .addComponent(spin)
                .addComponent(ctrl);

        // 3) 注册实体（放到 EntityService）
        var es = ServiceLocator.getEntityService();
        if (es != null) Gdx.app.postRunnable(() -> es.register(sword));

        Gdx.app.postRunnable(() -> {
            if (sword == null) return;

            var phys = sword.getComponent(com.csse3200.game.physics.components.PhysicsComponent.class);
            var rend = sword.getComponent(com.csse3200.game.rendering.RotatingTextureRenderComponent.class);
            if (phys == null || phys.getBody() == null) return;

            // 取英雄中心
            com.badlogic.gdx.math.Vector2 heroCenter =
                    (entity != null && entity.getCenterPosition() != null)
                            ? new com.badlogic.gdx.math.Vector2(entity.getCenterPosition())
                            : new com.badlogic.gdx.math.Vector2(entity.getPosition()).add(
                            entity.getScale() != null ? entity.getScale().x * 0.5f : 0.5f,
                            entity.getScale() != null ? entity.getScale().y * 0.5f : 0.5f
                    );

            float facingDeg = 0f;                      // 初始朝向：向右
            float rad = (float)Math.toRadians(facingDeg);
            float dx = (float)Math.cos(rad), dy = (float)Math.sin(rad);

            float restRadius = this.restRadius;        // 你构造传入的半径
            float centerToHandle = -0.25f;             // 和你之前一致（可用字段）

            // 剑柄位置 = 英雄中心 + 半径 * 朝向
            float handleX = heroCenter.x + restRadius * dx;
            float handleY = heroCenter.y + restRadius * dy;

            // 贴图中心 = 剑柄 - centerToHandle * 朝向
            float centerX = handleX - centerToHandle * dx;
            float centerY = handleY - centerToHandle * dy;

            phys.getBody().setTransform(centerX, centerY, (float)Math.toRadians(facingDeg + spriteForwardOffsetDeg));

            // 可选：把贴图的可视角度也对齐（避免看起来歪）
            if (rend != null) {
                float vis = ((facingDeg + spriteForwardOffsetDeg) % 360f + 360f) % 360f;
                rend.setRotation(vis);
                // 如果你的渲染组件支持，建议把原点设为居中
                try { rend.getClass().getMethod("setOriginToCenter").invoke(rend); } catch (Throwable ignored) {}
            }
        });

        // 4) 绑定输入：把键盘 1/2/3 转发给控制器
        adapter = new InputAdapter() {
            private final Vector3 tmp = new Vector3();
            private final Vector2 mouseWorld = new Vector2();
            @Override public boolean keyDown(int keycode) {
                if (sword == null || camera == null || ctrl == null) return false;

                tmp.set(Gdx.input.getX(), Gdx.input.getY(), 0f);
                camera.unproject(tmp);
                mouseWorld.set(tmp.x, tmp.y);

                if (keycode == Input.Keys.NUM_1) { return ctrl.triggerJab(mouseWorld); }
                if (keycode == Input.Keys.NUM_2) { return ctrl.triggerSweep(mouseWorld); }
                if (keycode == Input.Keys.NUM_3) { return ctrl.triggerSpin(true); } // true=CCW，false=CW
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


