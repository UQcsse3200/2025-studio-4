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
    // === SFX keys & volumes ===
    private String jabSfxKey   = "sounds/katana_stab_2s.ogg";   // 刺
    private String sweepSfxKey = "sounds/katana_slash_2s.ogg";  // 劈砍
    private String spinSfxKey  = "sounds/katana_spin_2s.ogg";   // 旋转

    private float jabSfxVol = 1.0f;
    private float sweepSfxVol = 1.0f;
    private float spinSfxVol = 1.0f;

    // 限频，避免短时间内重复播放重叠
    private float sfxMinInterval = 0.06f;
    private float jabSfxCd = 0f, sweepSfxCd = 0f, spinSfxCd = 0f;


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
                .setParams(jabDuration, jabExtra)
                .setMiniCooldown(jabCooldown)
                .setSpriteForwardOffsetDeg(spriteForwardOffsetDeg)
                .setCenterToHandle(centerToHandle)
                .setPivotOffset(0f, 0f)
                // ✨ 注入：Jab 的“按等级伤害表” + 1级默认值兜底
                .setDamageTable(
                        (cfg.jabDamageByLevel != null && cfg.jabDamageByLevel.length > 0)
                                ? cfg.jabDamageByLevel : cfg.swordDamageByLevel,
                        (cfg.jabDamageByLevel != null && cfg.jabDamageByLevel.length > 0)
                                ? cfg.jabDamageByLevel[0]
                                : (cfg.swordDamageByLevel != null && cfg.swordDamageByLevel.length > 0
                                ? cfg.swordDamageByLevel[0] : 10)
                );

        var sweep = new SweepAttackComponent(this.entity, restRadius)
                .setParams(sweepDuration, sweepExtra)
                .setArcDeg(60f)
                .setMiniCooldown(sweepCooldown)
                .setSpriteForwardOffsetDeg(spriteForwardOffsetDeg)
                .setCenterToHandle(centerToHandle)
                .setPivotOffset(0f, 0f)
                // ✨ 注入：Sweep 的伤害表
                .setDamageTable(
                        (cfg.sweepDamageByLevel != null && cfg.sweepDamageByLevel.length > 0)
                                ? cfg.sweepDamageByLevel : cfg.swordDamageByLevel,
                        (cfg.sweepDamageByLevel != null && cfg.sweepDamageByLevel.length > 0)
                                ? cfg.sweepDamageByLevel[0]
                                : (cfg.swordDamageByLevel != null && cfg.swordDamageByLevel.length > 0
                                ? cfg.swordDamageByLevel[0] : 10)
                );

        var spin = new SpinAttackComponent(this.entity, restRadius)
                .setParams(0.5f, 0.25f)
                .setTurns(1f)
                .setMiniCooldown(0.6f)
                .setSpriteForwardOffsetDeg(spriteForwardOffsetDeg)
                .setCenterToHandle(centerToHandle)
                .setPivotOffset(0f, 0f)
                // ✨ 注入：Spin 的伤害表
                .setDamageTable(
                        (cfg.spinDamageByLevel != null && cfg.spinDamageByLevel.length > 0)
                                ? cfg.spinDamageByLevel : cfg.swordDamageByLevel,
                        (cfg.spinDamageByLevel != null && cfg.spinDamageByLevel.length > 0)
                                ? cfg.spinDamageByLevel[0]
                                : (cfg.swordDamageByLevel != null && cfg.swordDamageByLevel.length > 0
                                ? cfg.swordDamageByLevel[0] : 10)
                );


        ctrl = new SamuraiSwordController(jab, sweep, spin);

        this.entity.getEvents().addListener("ui:samurai:attack", (String type) -> {
            if (ctrl == null || camera == null) return;

            // 复用你键盘里同样的“鼠标世界坐标”逻辑
            com.badlogic.gdx.math.Vector3 tmp = new com.badlogic.gdx.math.Vector3(Gdx.input.getX(), Gdx.input.getY(), 0f);
            camera.unproject(tmp);
            com.badlogic.gdx.math.Vector2 mouseWorld = new com.badlogic.gdx.math.Vector2(tmp.x, tmp.y);

            boolean ok = false;
            switch (type) {
                case "jab"   -> ok = triggerJabWithSfx(mouseWorld);
                case "sweep" -> ok = triggerSweepWithSfx(mouseWorld);
                case "spin"  -> ok = triggerSpinWithSfx(true); // 逆时针
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

                if (keycode == Input.Keys.NUM_1) { return triggerJabWithSfx(mouseWorld); }
                if (keycode == Input.Keys.NUM_2) { return triggerSweepWithSfx(mouseWorld); }
                if (keycode == Input.Keys.NUM_3) { return triggerSpinWithSfx(true); } // true=CCW
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

    // 更新每帧的 SFX 冷却（如果你本组件有 update，可放 update；没有就靠限频足够）
    private void tickSfxCooldowns(float dt) {
        if (jabSfxCd > 0f)   jabSfxCd   -= dt;
        if (sweepSfxCd > 0f) sweepSfxCd -= dt;
        if (spinSfxCd > 0f)  spinSfxCd  -= dt;
    }

    // 包装触发 + 成功后播音
    private boolean triggerJabWithSfx(Vector2 mouseWorld) {
        boolean ok = (ctrl != null) && ctrl.triggerJab(mouseWorld);
        if (ok) playSfxOnce(jabSfxKey, jabSfxVol, /*cd*/false, /*which*/"jab");
        return ok;
    }
    private boolean triggerSweepWithSfx(Vector2 mouseWorld) {
        boolean ok = (ctrl != null) && ctrl.triggerSweep(mouseWorld);
        if (ok) playSfxOnce(sweepSfxKey, sweepSfxVol, false, "sweep");
        return ok;
    }
    private boolean triggerSpinWithSfx(boolean ccw) {
        boolean ok = (ctrl != null) && ctrl.triggerSpin(ccw);
        if (ok) playSfxOnce(spinSfxKey, spinSfxVol, false, "spin");
        return ok;
    }

    // 通用播放（带 ResourceService 优先，newSound 回退 + 限频）
// which: "jab"/"sweep"/"spin"
    private void playSfxOnce(String key, float vol, boolean useCooldown, String which) {
        if (key == null || key.isBlank()) return;

        // 限频
        if (useCooldown) {
            if ("jab".equals(which) && jabSfxCd > 0f)   return;
            if ("sweep".equals(which) && sweepSfxCd > 0f) return;
            if ("spin".equals(which) && spinSfxCd > 0f)  return;
        }

        float v = Math.max(0f, Math.min(1f, vol));
        try {
            var rs = ServiceLocator.getResourceService();
            com.badlogic.gdx.audio.Sound s = null;
            if (rs != null) {
                try { s = rs.getAsset(key, com.badlogic.gdx.audio.Sound.class); } catch (Throwable ignored) {}
            }
            if (s != null) {
                s.play(v);
            } else {
                if (!Gdx.files.internal(key).exists() || Gdx.audio == null) return;
                com.badlogic.gdx.audio.Sound s2 = Gdx.audio.newSound(Gdx.files.internal(key));
                s2.play(v);
            }
        } catch (Throwable t) {
            // 忽略或记日志都行
        }

        // 设置限频冷却
        if (useCooldown) {
            if ("jab".equals(which))      jabSfxCd = sfxMinInterval;
            else if ("sweep".equals(which)) sweepSfxCd = sfxMinInterval;
            else if ("spin".equals(which))  spinSfxCd = sfxMinInterval;
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


