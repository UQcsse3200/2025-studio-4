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
import com.csse3200.game.events.listeners.EventListener1;
import com.csse3200.game.events.listeners.EventListener3; // 用于 upgraded 三参数事件


import java.util.ArrayList;

/**
 * Hero form switcher:
 * - Boot to form-1 immediately (texture + bullet + params) once hero is available.
 * - No time limit for the first manual switch.
 * - Locks after the first successful manual switch.
 * - Also locks immediately after the hero upgrades (level > 1 or "upgraded" event).
 */
public class HeroOneShotFormSwitchComponent extends InputComponent {
    private static final Logger logger = LoggerFactory.getLogger(HeroOneShotFormSwitchComponent.class);

    private static final float POLL_INTERVAL_SEC = 0.25f; // Poll hero existence

    // Configs for different hero forms
    private final HeroConfig cfg1;   // Form 1
    private final HeroConfig2 cfg2;  // Form 2
    private final HeroConfig3 cfg3;  // Form 3

    private Entity hero;              // Reference to the hero entity
    private boolean locked = false;   // Lock after first manual switch or any upgrade

    // Polling timer (no auto-default timer anymore)
    private Timer.Task pollTask;

    // InputAdapter registered at index 0 to intercept keys 1/2/3
    private InputAdapter hotkeyAdapter;

    // Track current form (1/2/3). 0 means not initialized yet.
    private int currentForm = 0;

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

        // === Find hero; if not ready, poll; when ready, hook upgrade lock & boot to form-1 ===
        if (!tryFindHero()) {
            armHeroPolling(); // will call hookUpgradeLock() + bootToForm1() when hero is found
        } else {
            hookUpgradeLock();
            bootToForm1();
        }

        // === Register hotkeyAdapter at index 0 to intercept keys 1/2/3 ===
        if (hotkeyAdapter == null) {
            hotkeyAdapter = new InputAdapter() {
                @Override
                public boolean keyDown(int keycode) {
                    if (locked) return true; // already locked → swallow event
                    if (hero == null && !tryFindHero()) return false;

                    int target = 0;
                    if (keycode == Input.Keys.NUM_1 || keycode == Input.Keys.NUMPAD_1) target = 1;
                    else if (keycode == Input.Keys.NUM_2 || keycode == Input.Keys.NUMPAD_2) target = 2;
                    else if (keycode == Input.Keys.NUM_3 || keycode == Input.Keys.NUMPAD_3) target = 3;
                    else return false; // let other keys pass through

                    // Pressing the same form does nothing (and does NOT lock)
                    if (target == currentForm) return true;

                    boolean ok = applyForm(target, "hotkey-" + target);
                    if (ok) {
                        // First successful manual switch → lock
                        locked = true;
                        // Optional: remove input hook to behave like a one-shot component
                        dispose();
                        Gdx.app.log("HeroSkinSwitch", "locked by manual switch to form " + target);
                    }
                    return ok;
                }

                @Override
                public boolean keyTyped(char character) {
                    // Not needed; keyDown is sufficient
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
     * Start polling for hero entity; once found, hook upgrade lock & boot to form-1.
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
                    hookUpgradeLock();
                    bootToForm1();
                }
            }
        }, 0f, POLL_INTERVAL_SEC);
    }

    /**
     * Try to find the hero entity (with HeroTurretAttackComponent).
     */
    private boolean tryFindHero() {
        var entityService = ServiceLocator.getEntityService();
        if (entityService == null) {
            logger.debug("[HeroSkinSwitch] EntityService not available yet");
            return false;
        }
        for (Entity e : entityService.getEntities()) {
            if (e.getComponent(HeroTurretAttackComponent.class) != null) {
                hero = e;
                return true;
            }
        }
        return false;
    }

    /**
     * Hook listeners that lock switching as soon as the hero upgrades.
     * Locks when hero.level > 1 or upgraded is triggered.
     */
    private void hookUpgradeLock() {
        if (hero == null) return;

        // Lock when hero.level > 1
        // --- Hero is locked after leveling up (hero.level event has only one parameter)---
        hero.getEvents().addListener("hero.level", (EventListener1<Integer>) newLevel -> {
            if (!locked && newLevel > 1) {
                locked = true;
                dispose(); // optional
                Gdx.app.log("HeroSkinSwitch", "locked by levelUp: level=" + newLevel);
            }
        });

// --- Hero upgrade completed and locked (upgraded event has three parameters)---
        hero.getEvents().addListener("upgraded",
                (EventListener3<Integer, Object, Integer>) (newLevel, _costType, _cost) -> {
                    if (!locked && newLevel > 1) {
                        locked = true;
                        dispose();
                        Gdx.app.log("HeroSkinSwitch", "locked by upgraded: level=" + newLevel);
                    }
                });

    }

    /**
     * Boot to form-1 once hero is ready (texture + bullet + params).
     */
    private void bootToForm1() {
        if (hero == null || cfg1 == null) {
            logger.warn("[HeroSkinSwitch] bootToForm1 skipped: hero or cfg1 is null");
            return;
        }
        boolean ok = switchTexture(cfg1.heroTexture, "boot-1");
        if (ok) {
            updateBulletTexture(cfg1.bulletTexture);
            applyConfigToHero(cfg1);
            currentForm = 1;
            logger.info("[HeroSkinSwitch] booted with form-1");
        } else {
            logger.warn("[HeroSkinSwitch] boot form-1 failed (texture unavailable?)");
        }
    }

    /**
     * Apply target form (used by the first manual switch).
     */
    private boolean applyForm(int form, String tag) {
        String tex = null;
        switch (form) {
            case 1 -> tex = (cfg1 != null ? cfg1.heroTexture : null);
            case 2 -> tex = (cfg2 != null ? cfg2.heroTexture : null);
            case 3 -> tex = (cfg3 != null ? cfg3.heroTexture : null);
            default -> {
                return false;
            }
        }
        if (tex == null || tex.isBlank()) return false;

        boolean ok = switchTexture(tex, tag);
        if (!ok) return false;

        if (form == 1 && cfg1 != null) {
            updateBulletTexture(cfg1.bulletTexture);
            applyConfigToHero(cfg1);
        } else if (form == 2 && cfg2 != null) {
            updateBulletTexture(cfg2.bulletTexture);
            applyConfigToHero2(cfg2);
        } else if (form == 3 && cfg3 != null) {
            updateBulletTexture(cfg3.bulletTexture);
            applyConfigToHero3(cfg3);
        }
        currentForm = form;
        return true;
    }

    /**
     * Update bullet texture for the current hero attack component.
     */
    private void updateBulletTexture(String bulletTexture) {
        if (hero == null) return;
        HeroTurretAttackComponent atc = hero.getComponent(HeroTurretAttackComponent.class);
        if (atc != null) atc.setBulletTexture(bulletTexture);
    }

    /**
     * Apply config values (form 1) to hero attack and combat stats.
     */
    private void applyConfigToHero(HeroConfig cfg) {
        if (hero == null || cfg == null) return;
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
     * Apply config values (form 2) to hero attack and combat stats.
     */
    private void applyConfigToHero2(HeroConfig2 cfg) {
        if (hero == null || cfg == null) return;
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
     * Apply config values (form 3) to hero attack and combat stats.
     */
    private void applyConfigToHero3(HeroConfig3 cfg) {
        if (hero == null || cfg == null) return;
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
     * Just switch the texture on RotatingTextureRenderComponent.
     * No lock or timers here (locking is controlled by manual switch or upgrade events).
     */
    private boolean switchTexture(String texturePath, String keyTag) {
        if (texturePath == null || texturePath.isBlank()) {
            logger.warn("[HeroSkinSwitch] empty texture for {}", keyTag);
            return false;
        }
        if (hero == null) {
            logger.warn("[HeroSkinSwitch] switchTexture failed: hero is null");
            return false;
        }
        RotatingTextureRenderComponent rot = hero.getComponent(RotatingTextureRenderComponent.class);
        if (rot == null) {
            logger.warn("[HeroSkinSwitch] no RotatingTextureRenderComponent on hero");
            return false;
        }
        rot.setTexture(texturePath);
        logger.info("[HeroSkinSwitch] switched by {} -> {}", keyTag, texturePath);
        return true;
    }

    /**
     * Cancel any scheduled timers.
     */
    private void cancelTimers() {
        if (pollTask != null) {
            pollTask.cancel();
            pollTask = null;
        }
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




