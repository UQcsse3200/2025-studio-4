package com.csse3200.game.components.hero;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.HeroConfig;
import com.csse3200.game.entities.configs.HeroConfig2;
import com.csse3200.game.entities.configs.HeroConfig3;
import com.csse3200.game.input.InputComponent;
import com.csse3200.game.rendering.RotatingTextureRenderComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * One-shot "hero skin switch" component.
 * - Polls for the hero entity until found.
 * - Automatically applies the default (form-1) after 5 seconds if no manual input is given.
 * - Designed as one-shot: no removeComponent fallback, disposes itself after applying a form.
 */
public class HeroOneShotFormSwitchComponent extends InputComponent {
    private static final Logger logger = LoggerFactory.getLogger(HeroOneShotFormSwitchComponent.class);

    private static final float POLL_INTERVAL_SEC = 0.25f; // Interval for polling hero entity
    private static final float AUTO_DEFAULT_DELAY_SEC = 5f; // Auto-default to form-1 after 5s

    private final HeroConfig  cfg1; // Form 1 (default)
    private final HeroConfig2 cfg2; // Form 2
    private final HeroConfig3 cfg3; // Form 3

    private Entity hero;
    private boolean locked = false;

    private Timer.Task pollTask;        // Periodic polling task
    private Timer.Task autoDefaultTask; // Auto-default task

    public HeroOneShotFormSwitchComponent(HeroConfig cfg1, HeroConfig2 cfg2, HeroConfig3 cfg3) {
        super(1000); // High priority to capture key 1/2/3 first
        this.cfg1 = cfg1;
        this.cfg2 = cfg2;
        this.cfg3 = cfg3;
    }

    @Override
    public void create() {
        super.create();
        // Preload all three texture sets to prevent missing resources during switch
        ResourceService rs = ServiceLocator.getResourceService();
        rs.loadTextures(new String[]{
                cfg1 != null ? cfg1.heroTexture : null,
                cfg2 != null ? cfg2.heroTexture : null,
                cfg3 != null ? cfg3.heroTexture : null
        });
        while (!rs.loadForMillis(5)) { /* spin briefly */ }

        // Try once immediately; if found, start auto-default timer, else start polling
        if (!tryFindHero()) {
            // Hero not spawned yet → start polling
            armHeroPolling();
        } else {
            // Hero already exists → start auto-default countdown
            armAutoDefaultTimer();
        }
    }

    private void armHeroPolling() {
        if (pollTask != null) return;
        pollTask = Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                if (locked) { cancel(); pollTask = null; return; }
                if (tryFindHero()) {
                    logger.info("[HeroSkinSwitch] Found hero via polling. Arming auto-default timer ({}s).", (int)AUTO_DEFAULT_DELAY_SEC);
                    cancel();            // Stop polling
                    pollTask = null;
                    armAutoDefaultTimer();
                }
            }
        }, 0f, POLL_INTERVAL_SEC); // Start immediately, check every 0.25s
        logger.info("[HeroSkinSwitch] Polling for hero every {}s...", POLL_INTERVAL_SEC);
    }

    /** Try to find the "real hero" (with HeroTurretAttackComponent). Return true if found. */
    private boolean tryFindHero() {
        for (Entity e : ServiceLocator.getEntityService().getEntities()) {
            if (e.getComponent(HeroTurretAttackComponent.class) != null) {
                hero = e;
                return true;
            }
        }
        return false;
    }

    private void armAutoDefaultTimer() {
        if (autoDefaultTask != null || locked || hero == null) return;

        autoDefaultTask = Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                if (locked || hero == null) return;
                boolean ok = switchOnce(cfg1 != null ? cfg1.heroTexture : null, "auto-1");
                if (!ok) {
                    logger.warn("[HeroSkinSwitch] Auto-default to form-1 FAILED (bad texture path?). User can still choose manually.");
                }
            }
        }, AUTO_DEFAULT_DELAY_SEC);

        logger.info("[HeroSkinSwitch] Auto-default to form-1 in {}s if no selection.", (int)AUTO_DEFAULT_DELAY_SEC);
    }

    private void cancelTimers() {
        if (pollTask != null) {
            pollTask.cancel();
            pollTask = null;
        }
        if (autoDefaultTask != null) {
            autoDefaultTask.cancel();
            autoDefaultTask = null;
        }
    }

    @Override
    public boolean keyDown(int keycode) {
        if (locked) return false;
        if (hero == null && !tryFindHero()) return false; // Ignore input if hero not found yet

        if (keycode == Input.Keys.NUM_1 || keycode == Input.Keys.NUMPAD_1) {
            return switchOnce(cfg1 != null ? cfg1.heroTexture : null, "1");
        } else if (keycode == Input.Keys.NUM_2 || keycode == Input.Keys.NUMPAD_2) {
            return switchOnce(cfg2 != null ? cfg2.heroTexture : null, "2");
        } else if (keycode == Input.Keys.NUM_3 || keycode == Input.Keys.NUMPAD_3) {
            return switchOnce(cfg3 != null ? cfg3.heroTexture : null, "3");
        }
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        if (locked) return false;
        if (hero == null && !tryFindHero()) return false;

        if (character == '1') return switchOnce(cfg1 != null ? cfg1.heroTexture : null, "1");
        if (character == '2') return switchOnce(cfg2 != null ? cfg2.heroTexture : null, "2");
        if (character == '3') return switchOnce(cfg3 != null ? cfg3.heroTexture : null, "3");
        return false;
    }

    private boolean switchOnce(String texturePath, String keyTag) {
        if (texturePath == null || texturePath.isBlank()) {
            logger.warn("[HeroSkinSwitch] Texture path for key {} is EMPTY. Ignored.", keyTag);
            return false;
        }
        boolean ok = applyTextureToHero(texturePath);
        if (ok) {
            locked = true;
            cancelTimers();
            logger.info("[HeroSkinSwitch] Applied texture via key {} -> {}. Locked.", keyTag, texturePath);
            this.entity.dispose(); // One-shot: dispose itself (unregisters from InputService)
        }
        return ok;
    }

    /**
     * Attempts to apply a new texture to the hero:
     * A) First tries reflection on setTexture(String).
     * B) If unavailable, falls back to hiding the old renderer (set alpha=0, dispose if possible),
     *    then adds a new RotatingTextureRenderComponent with the new texture.
     */
    private boolean applyTextureToHero(String texturePath) {
        try {
            RotatingTextureRenderComponent rot = hero.getComponent(RotatingTextureRenderComponent.class);
            if (rot == null) {
                logger.warn("[HeroSkinSwitch] Hero has no RotatingTextureRenderComponent.");
                return false;
            }

            // A) Reflection call: setTexture(String)
            try {
                Method m = rot.getClass().getMethod("setTexture", String.class);
                m.invoke(rot, texturePath);
                return true;
            } catch (NoSuchMethodException noSetter) {
                // B) Fallback: hide old render (transparent) + dispose + add new renderer
                try {
                    Method setColor = rot.getClass().getMethod("setColor", float.class, float.class, float.class, float.class);
                    setColor.invoke(rot, 1f, 1f, 1f, 0f);
                } catch (Throwable ignore) {}
                try {
                    Method dispose = rot.getClass().getMethod("dispose");
                    dispose.invoke(rot);
                } catch (Throwable ignore) {}

                hero.addComponent(new RotatingTextureRenderComponent(texturePath));
                return true;
            }

        } catch (Exception e) {
            logger.error("[HeroSkinSwitch] Failed to switch texture to {}: {}", texturePath, e.getMessage());
            return false;
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        cancelTimers();
    }
}

