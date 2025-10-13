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
// 省略 package/import（与原文件保持一致）

public class HeroOneShotFormSwitchComponent extends InputComponent {
    private static final Logger logger = LoggerFactory.getLogger(HeroOneShotFormSwitchComponent.class);
    private static final float POLL_INTERVAL_SEC = 0.25f;

    private final HeroConfig  cfg1;   // Form 1
    private final HeroConfig2 cfg2;   // Form 2
    private final HeroConfig3 cfg3;   // Form 3

    private Entity hero;
    private boolean locked = false;
    private Timer.Task pollTask;

    // ── 删除：hotkeyAdapter / keyDown 相关
    private int currentForm = 0;

    public HeroOneShotFormSwitchComponent(HeroConfig cfg1, HeroConfig2 cfg2, HeroConfig3 cfg3) {
        super(1000);
        this.cfg1 = cfg1;
        this.cfg2 = cfg2;
        this.cfg3 = cfg3;
    }

    @Override
    public void create() {
        super.create();

        // 预加载纹理（保持你原逻辑）
        var textures = new ArrayList<String>();
        if (cfg1 != null && cfg1.heroTexture != null && !cfg1.heroTexture.isBlank()) textures.add(cfg1.heroTexture);
        if (cfg2 != null && cfg2.heroTexture != null && !cfg2.heroTexture.isBlank()) textures.add(cfg2.heroTexture);
        if (cfg3 != null && cfg3.heroTexture != null && !cfg3.heroTexture.isBlank()) textures.add(cfg3.heroTexture);
        if (!textures.isEmpty()) {
            ResourceService rs = ServiceLocator.getResourceService();
            rs.loadTextures(textures.toArray(new String[0]));
            while (!rs.loadForMillis(10)) { /* wait */ }
        }

        if (!tryFindHero()) {
            armHeroPolling();
        } else {
            hookUpgradeLock();
            bootToForm1();
        }

        // === 关键改动：监听 UI 事件，而不是键盘 ===
        // "ui:weapon:switch"(Integer form)  -> 首次成功手动切换后锁定，并广播 "ui:weapon:locked"
        this.getEntity().getEvents().addListener("ui:weapon:switch", (Integer form) -> {
            if (locked) return;
            if (hero == null && !tryFindHero()) return;

            int target = (form != null ? form : 0);
            if (target < 1 || target > 3) return;
            if (target == currentForm) return;

            boolean ok = applyForm(target, "ui-" + target);
            if (ok) {
                locked = true; // 一次性
                // 通知 UI（置灰按钮）
                hero.getEvents().trigger("ui:weapon:locked");
                // 如需彻底卸载该组件的输入/定时器可：
                dispose();
                Gdx.app.log("HeroSkinSwitch", "locked by UI switch to form " + target);
            }
        });
    }

    private void armHeroPolling() {
        if (pollTask != null) return;
        pollTask = Timer.schedule(new Timer.Task() {
            @Override public void run() {
                if (locked) { cancel(); pollTask = null; return; }
                if (tryFindHero()) {
                    cancel(); pollTask = null;
                    hookUpgradeLock();
                    bootToForm1();
                }
            }
        }, 0f, POLL_INTERVAL_SEC);
    }

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

    private void hookUpgradeLock() {
        if (hero == null) return;

        hero.getEvents().addListener("hero.level", (EventListener1<Integer>) newLevel -> {
            if (!locked && newLevel != null && newLevel > 1) {
                locked = true;
                hero.getEvents().trigger("ui:weapon:locked");
                dispose();
                Gdx.app.log("HeroSkinSwitch", "locked by levelUp: level=" + newLevel);
            }
        });

        hero.getEvents().addListener("upgraded",
                (EventListener3<Integer, Object, Integer>) (newLevel, _costType, _cost) -> {
                    if (!locked && newLevel != null && newLevel > 1) {
                        locked = true;
                        hero.getEvents().trigger("ui:weapon:locked");
                        dispose();
                        Gdx.app.log("HeroSkinSwitch", "locked by upgraded: level=" + newLevel);
                    }
                });
    }

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

    private boolean applyForm(int form, String tag) {
        String tex = switch (form) {
            case 1 -> (cfg1 != null ? cfg1.heroTexture : null);
            case 2 -> (cfg2 != null ? cfg2.heroTexture : null);
            case 3 -> (cfg3 != null ? cfg3.heroTexture : null);
            default -> null;
        };
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

    private void updateBulletTexture(String bulletTexture) {
        if (hero == null) return;
        HeroTurretAttackComponent atc = hero.getComponent(HeroTurretAttackComponent.class);
        if (atc != null) atc.setBulletTexture(bulletTexture);
    }

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

    private void cancelTimers() {
        if (pollTask != null) { pollTask.cancel(); pollTask = null; }
    }

    @Override
    public void dispose() {
        super.dispose();
        cancelTimers();
        // 无键盘处理器可移除
    }
}



