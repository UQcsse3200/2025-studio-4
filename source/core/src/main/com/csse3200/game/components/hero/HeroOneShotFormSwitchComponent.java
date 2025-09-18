package com.csse3200.game.components.hero;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.components.CombatStatsComponent;
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

import java.util.ArrayList;

/**
 * One-shot component for switching hero form (weapon/skin).
 *
 * <p>Features:
 * <ul>
 *   <li>Preloads hero textures to avoid missing assets when switching.</li>
 *   <li>Polls for the hero entity until it is found.</li>
 *   <li>If no manual input is received within 5 seconds, defaults to form-1 automatically.</li>
 *   <li>Intercepts keys 1/2/3 by registering an InputAdapter at index 0 of the InputMultiplexer
 *       (highest priority, ensures events are not consumed by UI or camera).</li>
 *   <li>Switch is one-shot: after applying a form, the component locks and disposes itself.</li>
 * </ul>
 */
public class HeroOneShotFormSwitchComponent extends InputComponent {
    private static final Logger logger = LoggerFactory.getLogger(HeroOneShotFormSwitchComponent.class);

    private static final float POLL_INTERVAL_SEC = 0.25f;     // Interval for polling hero existence
    private static final float AUTO_DEFAULT_DELAY_SEC = 5f;   // Delay before auto-default to form 1

    // Configs for different hero forms
    private final HeroConfig cfg1;   // Form 1
    private final HeroConfig2 cfg2;  // Form 2
    private final HeroConfig3 cfg3;  // Form 3

    private Entity hero;              // Reference to the hero entity
    private boolean locked = false;   // Lock once a form is selected

    // Timers for polling and auto-default
    private Timer.Task pollTask;
    private Timer.Task autoDefaultTask;

    // InputAdapter registered at index 0 to capture keys 1/2/3
    private InputAdapter hotkeyAdapter;

    public HeroOneShotFormSwitchComponent(HeroConfig cfg1, HeroConfig2 cfg2, HeroConfig3 cfg3) {
        super(1000); // High priority, but actual capture is via mux[0]
        this.cfg1 = cfg1;
        this.cfg2 = cfg2;
        this.cfg3 = cfg3;
    }

    @Override
    public void create() {
        super.create();

        // === Preload hero textures (filter out null or empty paths) ===
        var textures = new ArrayList<String>();
        if (cfg1 != null && cfg1.heroTexture != null && !cfg1.heroTexture.isBlank()) textures.add(cfg1.heroTexture);
        if (cfg2 != null && cfg2.heroTexture != null && !cfg2.heroTexture.isBlank()) textures.add(cfg2.heroTexture);
        if (cfg3 != null && cfg3.heroTexture != null && !cfg3.heroTexture.isBlank()) textures.add(cfg3.heroTexture);
        if (!textures.isEmpty()) {
            ResourceService rs = ServiceLocator.getResourceService();
            rs.loadTextures(textures.toArray(new String[0]));
            while (!rs.loadForMillis(10)) { /* wait until loaded */ }
        }

        // === Check if hero already exists; start polling or auto-default ===
        if (!tryFindHero()) {
            armHeroPolling();
        } else {
            armAutoDefaultTimer();
        }

        // === Register hotkeyAdapter at index 0 to intercept keys 1/2/3 ===
        if (hotkeyAdapter == null) {
            hotkeyAdapter = new InputAdapter() {
                @Override
                public boolean keyDown(int keycode) {
                    if (locked) return true; // already locked → swallow event
                    if (hero == null && !tryFindHero()) return false;

                    if (keycode == Input.Keys.NUM_1 || keycode == Input.Keys.NUMPAD_1) {
                        boolean ok = switchOnce(cfg1 != null ? cfg1.heroTexture : null, "1");
                        if (ok) {
                            updateBulletTexture(cfg1 != null ? cfg1.bulletTexture : null);
                            applyConfigToHero(cfg1);
                        }
                        return ok;
                    }
                    if (keycode == Input.Keys.NUM_2 || keycode == Input.Keys.NUMPAD_2) {
                        boolean ok = switchOnce(cfg2 != null ? cfg2.heroTexture : null, "2");
                        if (ok) {
                            updateBulletTexture(cfg2 != null ? cfg2.bulletTexture : null);
                            applyConfigToHero2(cfg2);
                        }
                        return ok;
                    }
                    if (keycode == Input.Keys.NUM_3 || keycode == Input.Keys.NUMPAD_3) {
                        boolean ok = switchOnce(cfg3 != null ? cfg3.heroTexture : null, "3");
                        if (ok) {
                            updateBulletTexture(cfg3 != null ? cfg3.bulletTexture : null);
                            applyConfigToHero3(cfg3);
                        }
                        return ok;
                    }
                    return false; // let other keys pass through
                }

                @Override
                public boolean keyTyped(char character) {
                    // Optional: not strictly necessary since keyDown is enough
                    if (locked) return true;
                    if (hero == null && !tryFindHero()) return false;
                    if (character == '1') return switchOnce(cfg1 != null ? cfg1.heroTexture : null, "1");
                    if (character == '2') return switchOnce(cfg2 != null ? cfg2.heroTexture : null, "2");
                    if (character == '3') return switchOnce(cfg3 != null ? cfg3.heroTexture : null, "3");
                    return false;
                }
            };

            // Add to InputMultiplexer at index 0
            if (Gdx.input.getInputProcessor() instanceof InputMultiplexer mux) {
                mux.addProcessor(0, hotkeyAdapter);
            } else {
                InputMultiplexer mux = new InputMultiplexer();
                mux.addProcessor(hotkeyAdapter);
                if (Gdx.input.getInputProcessor() != null) mux.addProcessor(Gdx.input.getInputProcessor());
                Gdx.input.setInputProcessor(mux);
            }
        }
    }

    /**
     * Start polling for hero entity; once found, start auto-default timer.
     */
    private void armHeroPolling() {
        if (pollTask != null) return;
        pollTask = Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                if (locked) {
                    cancel();
                    pollTask = null;
                    return;
                }
                if (tryFindHero()) {
                    cancel();
                    pollTask = null;
                    armAutoDefaultTimer();
                }
            }
        }, 0f, POLL_INTERVAL_SEC);
    }

    /**
     * Try to find the hero entity (with HeroTurretAttackComponent).
     */
    private boolean tryFindHero() {
        for (Entity e : ServiceLocator.getEntityService().getEntities()) {
            if (e.getComponent(HeroTurretAttackComponent.class) != null) {
                hero = e;
                return true;
            }
        }
        return false;
    }

    /**
     * Schedule auto-default to form 1 after a delay, if no manual selection occurs.
     */
    private void armAutoDefaultTimer() {
        if (autoDefaultTask != null || locked || hero == null) return;
        autoDefaultTask = Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                if (locked || hero == null) return;
                boolean ok = switchOnce(cfg1 != null ? cfg1.heroTexture : null, "auto-1");
                if (!ok) logger.warn("[HeroSkinSwitch] auto-default to form-1 failed");
            }
        }, AUTO_DEFAULT_DELAY_SEC);
    }

    /** Cancel any scheduled timers. */
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

    /** Update bullet texture for the current hero attack component. */
    private void updateBulletTexture(String bulletTexture) {
        HeroTurretAttackComponent atc = hero.getComponent(HeroTurretAttackComponent.class);
        if (atc != null) atc.setBulletTexture(bulletTexture);
    }

    /** Apply config values (form 1) to hero attack and combat stats. */
    private void applyConfigToHero(HeroConfig cfg) {
        if (cfg == null) return;
        HeroTurretAttackComponent atc = hero.getComponent(HeroTurretAttackComponent.class);
        CombatStatsComponent stats = hero.getComponent(CombatStatsComponent.class);
        if (atc != null) {
            atc.setCooldown(cfg.attackCooldown)
                    .setBulletParams(cfg.bulletSpeed, cfg.bulletLife)
                    .setBulletTexture(cfg.bulletTexture);
        }
        if (stats != null) stats.setBaseAttack(cfg.baseAttack);
    }

    /** Apply config values (form 2) to hero attack and combat stats. */
    private void applyConfigToHero2(HeroConfig2 cfg) {
        if (cfg == null) return;
        HeroTurretAttackComponent atc = hero.getComponent(HeroTurretAttackComponent.class);
        CombatStatsComponent stats = hero.getComponent(CombatStatsComponent.class);
        if (atc != null) {
            atc.setCooldown(cfg.attackCooldown)
                    .setBulletParams(cfg.bulletSpeed, cfg.bulletLife)
                    .setBulletTexture(cfg.bulletTexture);
        }
        if (stats != null) stats.setBaseAttack(cfg.baseAttack);
    }

    /** Apply config values (form 3) to hero attack and combat stats. */
    private void applyConfigToHero3(HeroConfig3 cfg) {
        if (cfg == null) return;
        HeroTurretAttackComponent atc = hero.getComponent(HeroTurretAttackComponent.class);
        CombatStatsComponent stats = hero.getComponent(CombatStatsComponent.class);
        if (atc != null) {
            atc.setCooldown(cfg.attackCooldown)
                    .setBulletParams(cfg.bulletSpeed, cfg.bulletLife)
                    .setBulletTexture(cfg.bulletTexture);
        }
        if (stats != null) stats.setBaseAttack(cfg.baseAttack);
    }

    /**
     * Switch hero texture once (one-shot).
     * - Applies new texture to RotatingTextureRenderComponent.
     * - Cancels timers and locks further changes.
     * - Disposes this component to unregister from input system.
     */
    private boolean switchOnce(String texturePath, String keyTag) {
        if (texturePath == null || texturePath.isBlank()) {
            logger.warn("[HeroSkinSwitch] empty texture for {}", keyTag);
            return false;
        }
        RotatingTextureRenderComponent rot = hero.getComponent(RotatingTextureRenderComponent.class);
        if (rot == null) {
            logger.warn("[HeroSkinSwitch] no RotatingTextureRenderComponent on hero");
            return false;
        }
        rot.setTexture(texturePath);
        locked = true;
        cancelTimers();
        logger.info("[HeroSkinSwitch] switched by {} -> {}, locked", keyTag, texturePath);
        this.entity.dispose();
        return true;
    }

    @Override
    public void dispose() {
        super.dispose();
        cancelTimers();
        if (hotkeyAdapter != null && Gdx.input.getInputProcessor() instanceof InputMultiplexer mux) {
            mux.removeProcessor(hotkeyAdapter);
        }
        hotkeyAdapter = null;
    }
}


