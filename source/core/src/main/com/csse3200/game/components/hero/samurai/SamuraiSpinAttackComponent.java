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
import com.csse3200.game.components.hero.samurai.attacks.*;

public class SamuraiSpinAttackComponent extends Component {
    private final float restRadius;
    private final String swordTexture;
    private final Camera camera;
    private final SamuraiConfig cfg;

    private String jabSfxKey   = "sounds/katana_stab_2s.ogg";
    private String sweepSfxKey = "sounds/katana_slash_2s.ogg";
    private String spinSfxKey  = "sounds/katana_spin_2s.ogg";

    private float jabSfxVol = 1.0f, sweepSfxVol = 1.0f, spinSfxVol = 1.0f;
    private float sfxMinInterval = 0.06f;
    private float jabSfxCd = 0f, sweepSfxCd = 0f, spinSfxCd = 0f;

    private float spriteForwardOffsetDeg = 0f;
    private float centerToHandle = -1.0f;

    private float jabDuration = 0.18f, jabExtra = 0.8f, jabCooldown = 0f;
    private float sweepDuration = 0.22f, sweepExtra = 0.35f, sweepCooldown = 0f;

    private Entity sword;
    private SamuraiSwordController ctrl;
    private InputAdapter adapter;

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

        sword = com.csse3200.game.entities.factories.SwordFactory.createSword(
                this.entity, cfg, swordTexture, restRadius, 0f);

        var lock = new AttackLockComponent();

        var jab = new JabAttackComponent(this.entity, restRadius)
                .setParams(jabDuration, jabExtra)
                .setMiniCooldown(jabCooldown)
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
                .setParams(0.5f, 0.25f)
                .setTurns(1f)
                .setMiniCooldown(0.6f)
                .setSpriteForwardOffsetDeg(spriteForwardOffsetDeg)
                .setCenterToHandle(centerToHandle)
                .setPivotOffset(0f, 0f);

        ctrl = new SamuraiSwordController(jab, sweep, spin);

        this.entity.getEvents().addListener("ui:samurai:attack", (String type) -> {
            if (ctrl == null || camera == null) return;
            Vector3 tmp = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0f);
            camera.unproject(tmp);
            Vector2 mouseWorld = new Vector2(tmp.x, tmp.y);

            boolean ok = false;
            switch (type) {
                case "jab"   -> ok = triggerJabWithSfx(mouseWorld);
                case "sweep" -> ok = triggerSweepWithSfx(mouseWorld);
                case "spin"  -> ok = triggerSpinWithSfx(true);
            }
            if (!ok) this.entity.getEvents().trigger("ui:toast", "Cannot use now");
        });

        sword.addComponent(lock)
                .addComponent(jab)
                .addComponent(sweep)
                .addComponent(spin)
                .addComponent(ctrl);

        var es = ServiceLocator.getEntityService();
        if (es != null) Gdx.app.postRunnable(() -> es.register(sword));

        // 初始放置位置/角度（略）... 保留你原来的 Gdx.app.postRunnable 块

        adapter = new InputAdapter() {
            private final Vector3 tmp = new Vector3();
            private final Vector2 mouseWorld = new Vector2();
            @Override public boolean keyDown(int keycode) {
                if (sword == null || camera == null || ctrl == null) return false;
                tmp.set(Gdx.input.getX(), Gdx.input.getY(), 0f);
                camera.unproject(tmp);
                mouseWorld.set(tmp.x, tmp.y);
                if (keycode == Input.Keys.NUM_1) return triggerJabWithSfx(mouseWorld);
                if (keycode == Input.Keys.NUM_2) return triggerSweepWithSfx(mouseWorld);
                if (keycode == Input.Keys.NUM_3) return triggerSpinWithSfx(true);
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

        // ★★★ 监听武器切换：hero 触发 "samurai:weapon:set" → 转成贴图 → 通知剑换皮
        this.entity.getEvents().addListener("samurai:weapon:set", (String label) -> {
            if (sword == null) return;
            String path = (cfg != null) ? cfg.getSwordTextureForLabel(label) : null;
            if (path != null && !path.isBlank()) {
                sword.getEvents().trigger("sword:skin:set", path);
            }
        });
    }

    private boolean triggerJabWithSfx(Vector2 mouseWorld) {
        boolean ok = (ctrl != null) && ctrl.triggerJab(mouseWorld);
        if (ok) playSfxOnce(jabSfxKey, jabSfxVol, false, "jab");
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

    private void playSfxOnce(String key, float vol, boolean useCooldown, String which) {
        if (key == null || key.isBlank()) return;
        if (useCooldown) {
            if ("jab".equals(which) && jabSfxCd > 0f) return;
            if ("sweep".equals(which) && sweepSfxCd > 0f) return;
            if ("spin".equals(which) && spinSfxCd > 0f) return;
        }
        float v = Math.max(0f, Math.min(1f, vol));
        try {
            var rs = ServiceLocator.getResourceService();
            com.badlogic.gdx.audio.Sound s = null;
            if (rs != null) {
                try { s = rs.getAsset(key, com.badlogic.gdx.audio.Sound.class); } catch (Throwable ignored) {}
            }
            if (s != null) s.play(v);
            else if (Gdx.audio != null && Gdx.files.internal(key).exists()) {
                com.badlogic.gdx.audio.Sound s2 = Gdx.audio.newSound(Gdx.files.internal(key));
                s2.play(v);
            }
        } catch (Throwable ignored) {}
        if (useCooldown) {
            if ("jab".equals(which)) jabSfxCd = sfxMinInterval;
            else if ("sweep".equals(which)) sweepSfxCd = sfxMinInterval;
            else if ("spin".equals(which)) spinSfxCd = sfxMinInterval;
        }
    }

    @Override
    public void dispose() {
        super.dispose();
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
