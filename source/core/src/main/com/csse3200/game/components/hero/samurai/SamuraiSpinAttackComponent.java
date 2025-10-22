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

// Attack modules & controller (decoupled from the sword factory)
import com.csse3200.game.components.hero.samurai.attacks.AttackLockComponent;
import com.csse3200.game.components.hero.samurai.attacks.JabAttackComponent;
import com.csse3200.game.components.hero.samurai.attacks.SweepAttackComponent;
import com.csse3200.game.components.hero.samurai.attacks.SpinAttackComponent;
import com.csse3200.game.components.hero.samurai.attacks.SamuraiSwordController;

/**
 * Responsibilities:
 * - Spawn the "sword" entity (visual + physics only)
 * - Attach three attack components (jab / sweep / spin) to the sword
 * - Handle input and forward it to the controller
 * <p>
 * Notes:
 * - SwordFactory.createSword(...) MUST create ONLY the visual + physics sword.
 * Do NOT attach legacy components such as SwordJabPhysicsComponent inside the factory.
 */
public class SamuraiSpinAttackComponent extends Component {
    private final float restRadius;
    private final String swordTexture;
    private final Camera camera;
    private final SamuraiConfig cfg;

    // === SFX keys & volumes ===
    private String jabSfxKey = "sounds/katana_stab_2s.ogg";   // jab
    private String sweepSfxKey = "sounds/katana_slash_2s.ogg";  // sweep
    private String spinSfxKey = "sounds/katana_spin_2s.ogg";   // spin

    private float jabSfxVol = 1.0f;
    private float sweepSfxVol = 1.0f;
    private float spinSfxVol = 1.0f;

    // SFX rate-limit to avoid overlapping spam
    private float sfxMinInterval = 0.06f;
    private float jabSfxCd = 0f, sweepSfxCd = 0f, spinSfxCd = 0f;

    // Visual/feel parameters
    private float spriteForwardOffsetDeg = 0f;
    private float centerToHandle = -1.0f;

    // Default jab/sweep params (can be overridden via setters)
    private float jabDuration = 0.18f, jabExtra = 0.8f, jabCooldown = 0f;
    private float sweepDuration = 0.22f, sweepExtra = 0.35f, sweepCooldown = 0f;

    // Component references
    private Entity sword;                 // Sword entity (physics + rendering + attack components)
    private SamuraiSwordController ctrl;  // Controller
    private InputAdapter adapter;         // Keyboard adapter

    public SamuraiSpinAttackComponent(float restRadius, String swordTexture, SamuraiConfig cfg, Camera camera) {
        this.restRadius = restRadius;
        this.swordTexture = swordTexture;
        this.cfg = cfg;
        this.camera = camera;
    }

    public SamuraiSpinAttackComponent setSpriteForwardOffsetDeg(float deg) {
        this.spriteForwardOffsetDeg = deg;
        return this;
    }

    public SamuraiSpinAttackComponent setCenterToHandle(float d) {
        this.centerToHandle = d;
        return this;
    }

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

        // 1) Create the sword entity with visual + physics only.
        //    WARNING: Do NOT attach legacy SwordJabPhysicsComponent in the factory.
        sword = com.csse3200.game.entities.factories.SwordFactory.createSword(
                this.entity, cfg, swordTexture, restRadius, /*initialFacing*/0f
        );

        // 2) Attach "mutex lock + three attacks + controller" onto the sword
        var lock = new AttackLockComponent();

        var jab = new JabAttackComponent(this.entity /* owner = samurai entity */, restRadius)
                .setParams(jabDuration, jabExtra)
                .setMiniCooldown(jabCooldown)
                .setSpriteForwardOffsetDeg(spriteForwardOffsetDeg)
                .setCenterToHandle(centerToHandle)
                .setPivotOffset(0f, 0f)
                // Inject jab damage table with reasonable fallback to swordDamageByLevel
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
                // Inject sweep damage table with fallback
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
                // Inject spin damage table with fallback
                .setDamageTable(
                        (cfg.spinDamageByLevel != null && cfg.spinDamageByLevel.length > 0)
                                ? cfg.spinDamageByLevel : cfg.swordDamageByLevel,
                        (cfg.spinDamageByLevel != null && cfg.spinDamageByLevel.length > 0)
                                ? cfg.spinDamageByLevel[0]
                                : (cfg.swordDamageByLevel != null && cfg.swordDamageByLevel.length > 0
                                ? cfg.swordDamageByLevel[0] : 10)
                );

        ctrl = new SamuraiSwordController(jab, sweep, spin);

        // UI → attack trigger bridge
        this.entity.getEvents().addListener("ui:samurai:attack", (String type) -> {
            if (ctrl == null || camera == null) return;

            // Reuse world-mouse conversion logic (same as keyboard handler)
            com.badlogic.gdx.math.Vector3 tmp = new com.badlogic.gdx.math.Vector3(Gdx.input.getX(), Gdx.input.getY(), 0f);
            camera.unproject(tmp);
            com.badlogic.gdx.math.Vector2 mouseWorld = new com.badlogic.gdx.math.Vector2(tmp.x, tmp.y);

            boolean ok = false;
            switch (type) {
                case "jab" -> ok = triggerJabWithSfx(mouseWorld);
                case "sweep" -> ok = triggerSweepWithSfx(mouseWorld);
                case "spin" -> ok = triggerSpinWithSfx(true); // CCW
            }

            if (!ok) {
                // Optional: notify UI about cooldown/lock
                this.entity.getEvents().trigger("ui:toast", "Cannot use now");
            }
        });

        sword.addComponent(lock)
                .addComponent(jab)
                .addComponent(sweep)
                .addComponent(spin)
                .addComponent(ctrl);

        // 3) Register spawned entity into the EntityService
        var es = ServiceLocator.getEntityService();
        if (es != null) Gdx.app.postRunnable(() -> es.register(sword));

        // 4) Position the sword at rest (right-facing) around the hero
        Gdx.app.postRunnable(() -> {
            if (sword == null) return;

            var phys = sword.getComponent(com.csse3200.game.physics.components.PhysicsComponent.class);
            var rend = sword.getComponent(com.csse3200.game.rendering.RotatingTextureRenderComponent.class);
            if (phys == null || phys.getBody() == null) return;

            // Get hero center
            com.badlogic.gdx.math.Vector2 heroCenter =
                    (entity != null && entity.getCenterPosition() != null)
                            ? new com.badlogic.gdx.math.Vector2(entity.getCenterPosition())
                            : new com.badlogic.gdx.math.Vector2(entity.getPosition()).add(
                            entity.getScale() != null ? entity.getScale().x * 0.5f : 0.5f,
                            entity.getScale() != null ? entity.getScale().y * 0.5f : 0.5f
                    );

            float facingDeg = 0f; // initial facing: right
            float rad = (float) Math.toRadians(facingDeg);
            float dx = (float) Math.cos(rad), dy = (float) Math.sin(rad);

            float restRadius = this.restRadius;
            float centerToHandle = -0.25f; // matches previous setup (can be turned into a field)

            // Handle position = heroCenter + radius * facing
            float handleX = heroCenter.x + restRadius * dx;
            float handleY = heroCenter.y + restRadius * dy;

            // Sprite center = handle - centerToHandle * facing
            float centerX = handleX - centerToHandle * dx;
            float centerY = handleY - centerToHandle * dy;

            phys.getBody().setTransform(centerX, centerY, (float) Math.toRadians(facingDeg + spriteForwardOffsetDeg));

            // Optionally sync visible rotation (to avoid looking skewed)
            if (rend != null) {
                float vis = ((facingDeg + spriteForwardOffsetDeg) % 360f + 360f) % 360f;
                rend.setRotation(vis);
                // If supported, set origin to texture center
                try {
                    rend.getClass().getMethod("setOriginToCenter").invoke(rend);
                } catch (Throwable ignored) {
                }
            }
        });

        // 5) Keyboard binding: map 1/2/3 to jab/sweep/spin and forward to controller
        adapter = new InputAdapter() {
            private final Vector3 tmp = new Vector3();
            private final Vector2 mouseWorld = new Vector2();

            @Override
            public boolean keyDown(int keycode) {
                if (sword == null || camera == null || ctrl == null) return false;

                tmp.set(Gdx.input.getX(), Gdx.input.getY(), 0f);
                camera.unproject(tmp);
                mouseWorld.set(tmp.x, tmp.y);

                if (keycode == Input.Keys.NUM_1) {
                    return triggerJabWithSfx(mouseWorld);
                }
                if (keycode == Input.Keys.NUM_2) {
                    return triggerSweepWithSfx(mouseWorld);
                }
                if (keycode == Input.Keys.NUM_3) {
                    return triggerSpinWithSfx(true);
                } // true = CCW
                return false;
            }
        };

        // Join current input chain via InputMultiplexer
        var cur = Gdx.input.getInputProcessor();
        if (cur instanceof InputMultiplexer mux) mux.addProcessor(0, adapter);
        else {
            var mux = new InputMultiplexer();
            mux.addProcessor(adapter);
            if (cur != null) mux.addProcessor(cur);
            Gdx.input.setInputProcessor(mux);
        }
    }

    // Tick SFX cooldowns per frame (if you add update(), call this from update; otherwise the per-call limiter is enough)
    private void tickSfxCooldowns(float dt) {
        if (jabSfxCd > 0f) jabSfxCd -= dt;
        if (sweepSfxCd > 0f) sweepSfxCd -= dt;
        if (spinSfxCd > 0f) spinSfxCd -= dt;
    }

    // Wrapper: trigger + play SFX on success
    private boolean triggerJabWithSfx(Vector2 mouseWorld) {
        boolean ok = (ctrl != null) && ctrl.triggerJab(mouseWorld);
        if (ok) playSfxOnce(jabSfxKey, jabSfxVol, /*useCooldown*/false, /*which*/"jab");
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

    /**
     * Generic SFX play with ResourceService-first, newSound fallback, and optional rate limiting.
     *
     * @param key         asset path
     * @param vol         volume [0,1]
     * @param useCooldown whether to enforce short anti-spam cooldown
     * @param which       "jab" / "sweep" / "spin"
     */
    private void playSfxOnce(String key, float vol, boolean useCooldown, String which) {
        if (key == null || key.isBlank()) return;

        // Anti-spam limiter
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
                try {
                    s = rs.getAsset(key, com.badlogic.gdx.audio.Sound.class);
                } catch (Throwable ignored) {
                }
            }
            if (s != null) {
                s.play(v);
            } else {
                if (!Gdx.files.internal(key).exists() || Gdx.audio == null) return;
                com.badlogic.gdx.audio.Sound s2 = Gdx.audio.newSound(Gdx.files.internal(key));
                s2.play(v);
            }
        } catch (Throwable t) {
            // ignore or log if needed
        }

        // Set short cooldown if enabled
        if (useCooldown) {
            if ("jab".equals(which)) jabSfxCd = sfxMinInterval;
            else if ("sweep".equals(which)) sweepSfxCd = sfxMinInterval;
            else if ("spin".equals(which)) spinSfxCd = sfxMinInterval;
        }
    }

    @Override
    public void dispose() {
        super.dispose();

        // Remove input adapter from the multiplexer to avoid leaks
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



