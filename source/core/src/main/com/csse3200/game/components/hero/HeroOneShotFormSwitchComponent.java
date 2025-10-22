package com.csse3200.game.components.hero;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.HeroConfig;
import com.csse3200.game.entities.configs.HeroConfig2;
import com.csse3200.game.entities.configs.HeroConfig3;
import com.csse3200.game.events.listeners.EventListener1;
import com.csse3200.game.events.listeners.EventListener3;
import com.csse3200.game.input.InputComponent;
import com.csse3200.game.rendering.RotatingTextureRenderComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * Central controller for the default Hero’s appearance/form/skin/upgrade flow.
 * <ul>
 *   <li>Three forms (1/2/3) with cooldown-based switching</li>
 *   <li>Skin changes (hot-swaps body & bullet textures)</li>
 *   <li>On upgrade: lock weapon bar, upgrade bullet texture, and switch body texture by level</li>
 *   <li>Preloads heroTexture / bulletTexture / levelTextures[*] to avoid flicker on upgrade</li>
 * </ul>
 */
public class HeroOneShotFormSwitchComponent extends InputComponent {
    private static final Logger logger = LoggerFactory.getLogger(HeroOneShotFormSwitchComponent.class);

    private static final float POLL_INTERVAL_SEC = 0.25f;
    private static final long SWITCH_COOLDOWN_MS = 3000L; // 3s cooldown

    // Form configs
    private final HeroConfig cfg1;  // form 1
    private final HeroConfig2 cfg2;  // form 2
    private final HeroConfig3 cfg3;  // form 3

    private Entity hero;
    private boolean locked = false;
    private Timer.Task pollTask;

    private long nextSwitchAtMs = 0L;
    private int currentForm = 0;

    private AutoCloseable unsubSkinChanged;

    public HeroOneShotFormSwitchComponent(HeroConfig cfg1, HeroConfig2 cfg2, HeroConfig3 cfg3) {
        super(1000);
        this.cfg1 = cfg1;
        this.cfg2 = cfg2;
        this.cfg3 = cfg3;
    }

    @Override
    public void create() {
        super.create();

        // ✅ Bind to this component's entity directly; no global scan/polling needed.
        this.hero = this.getEntity();

        // Preload textures for all three forms (including levelTextures[*]).
        preloadFormTextures(cfg1, cfg2, cfg3);

        // Register upgrade/lock listeners before booting.
        hookUpgradeLock();

        // ✅ Switch to form 1 on the next frame so it becomes the final texture writer.
        Gdx.app.postRunnable(this::bootToForm1);

        // Listen to UI weapon switching (removed tryFindHero checks).
        this.getEntity().getEvents().addListener("ui:weapon:switch", (Integer form) -> {
            if (locked) return;

            long now = TimeUtils.millis();
            if (now < nextSwitchAtMs) {
                long remainMs = nextSwitchAtMs - now;
                hero.getEvents().trigger("ui:weapon:cooldown", remainMs);
                return;
            }

            int target = (form != null ? form : 0);
            if (target < 1 || target > 3) return;
            if (target == currentForm) return;

            if (applyForm(target, "ui-" + target)) {
                nextSwitchAtMs = now + SWITCH_COOLDOWN_MS;
                hero.getEvents().trigger("ui:weapon:cooldown:start", SWITCH_COOLDOWN_MS);
                Gdx.app.log("HeroSkinSwitch", "switched by UI to form " + target + " (cooldown 3s)");
            }
        });

        // Skin changes (kept).
        var gs = ServiceLocator.getGameStateService();
        if (gs != null) {
            unsubSkinChanged = gs.onSkinChanged((who, newSkin) -> {
                if (who != com.csse3200.game.services.GameStateService.HeroType.HERO) return;

                applySkinToForm(cfg1, newSkin);
                applySkinToForm(cfg2, newSkin);
                applySkinToForm(cfg3, newSkin);

                preloadFormTextures(cfg1, cfg2, cfg3);

                Object cur = switch (currentForm) {
                    case 1 -> cfg1;
                    case 2 -> cfg2;
                    case 3 -> cfg3;
                    default -> null;
                };
                if (cur != null) {
                    String bodyTex = pickBodyTextureForLevel(cur, 1);
                    if (bodyTex != null && !bodyTex.isBlank()) switchTexture(bodyTex, "skin-changed");
                    String bullet = reflectGet(cur, "bulletTexture");
                    if (bullet != null && !bullet.isBlank()) updateBulletTexture(bullet);
                }
            });
        }
    }


    /* ========================== Skin/appearance helpers ========================== */

    /**
     * Reflectively get a String field; return null on failure.
     */
    private static String reflectGet(Object bean, String field) {
        if (bean == null) return null;
        try {
            Object v = bean.getClass().getField(field).get(bean);
            return (v instanceof String s) ? s : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Apply a skin key to a single form config:
     * updates heroTexture / bulletTexture (if mapped) / levelTextures[0] to avoid reverting.
     */
    private void applySkinToForm(Object formCfg, String skinKey) {
        if (formCfg == null || skinKey == null || skinKey.isBlank()) return;
        try {
            // Body
            var heroTexF = formCfg.getClass().getField("heroTexture");
            String body = com.csse3200.game.entities.configs.HeroSkinAtlas.body(
                    com.csse3200.game.services.GameStateService.HeroType.HERO, skinKey);
            heroTexF.set(formCfg, body);

            // Bullet (if skin maps a bullet)
            try {
                var bulletTexF = formCfg.getClass().getField("bulletTexture");
                String bullet = com.csse3200.game.entities.configs.HeroSkinAtlas.bullet(
                        com.csse3200.game.services.GameStateService.HeroType.HERO, skinKey);
                if (bullet != null && !bullet.isBlank()) bulletTexF.set(formCfg, bullet);
            } catch (NoSuchFieldException ignore) {
            }

            // Prevent levelTextures[0] from overriding the skin back to default
            try {
                var levelsF = formCfg.getClass().getField("levelTextures");
                Object arr = levelsF.get(formCfg);
                if (arr instanceof String[] levels) {
                    if (levels.length > 0) levels[0] = body;
                    else levelsF.set(formCfg, new String[]{body});
                } else {
                    levelsF.set(formCfg, new String[]{body});
                }
            } catch (NoSuchFieldException ignore) {
            }
        } catch (Exception e) {
            Gdx.app.error("HeroSkinSwitch", "applySkinToForm failed", e);
        }
    }

    /**
     * Preload textures for each form: heroTexture / bulletTexture(s) / levelTextures[*].
     */
    private void preloadFormTextures(Object... forms) {
        ResourceService rs = ServiceLocator.getResourceService();
        ArrayList<String> toLoad = new ArrayList<>();
        for (Object f : forms) {
            if (f == null) continue;
            // body
            String ht = reflectGet(f, "heroTexture");
            if (ht != null && !ht.isBlank()) toLoad.add(ht);
            // bullet (single/array/Lv2)
            addBulletTextures(toLoad, f);
            // levelTextures[*]
            try {
                var levelsF = f.getClass().getField("levelTextures");
                Object arr = levelsF.get(f);
                if (arr instanceof String[] levels) {
                    for (String s : levels) if (s != null && !s.isBlank()) toLoad.add(s);
                }
            } catch (Exception ignore) {
            }
        }
        if (!toLoad.isEmpty()) {
            rs.loadTextures(toLoad.toArray(new String[0]));
            while (!rs.loadForMillis(10)) { /* spin until loaded */ }
        }
    }

    /**
     * Collect bullet textures from single/array/Lv2 fields.
     */
    private static void addBulletTextures(ArrayList<String> textures, Object cfg) {
        if (cfg == null) return;
        try {
            var single = cfg.getClass().getField("bulletTexture");
            Object v = single.get(cfg);
            if (v instanceof String s && !s.isBlank()) textures.add(s);
        } catch (Exception ignored) {
        }
        try {
            var arr = cfg.getClass().getField("bulletTextures");
            Object v = arr.get(cfg);
            if (v instanceof String[] a) for (String s : a) if (s != null && !s.isBlank()) textures.add(s);
        } catch (Exception ignored) {
        }
        try {
            var lv2 = cfg.getClass().getField("bulletTextureLv2");
            Object v = lv2.get(cfg);
            if (v instanceof String s && !s.isBlank()) textures.add(s);
        } catch (Exception ignored) {
        }
    }

    /**
     * Choose body texture by level for upgrades.
     * Prefers {@code levelTextures[level-1]}; falls back to {@code heroTexture}.
     */
    private String pickBodyTextureForLevel(Object cfg, int level) {
        if (cfg == null) return null;
        // levelTextures
        try {
            var levelsF = cfg.getClass().getField("levelTextures");
            Object arr = levelsF.get(cfg);
            if (arr instanceof String[] levels) {
                int idx = Math.max(0, level - 1);
                if (idx < levels.length) {
                    String s = levels[idx];
                    if (s != null && !s.isBlank()) return s;
                }
            }
        } catch (Exception ignore) {
        }
        // fallback: heroTexture
        return reflectGet(cfg, "heroTexture");
    }

    /**
     * Choose upgraded bullet texture for a form/level (Lv2 > array[1] > original).
     */
    private String pickUpgradedBulletTexture(int form, int level) {
        if (level <= 1) return null;
        Object cfg = switch (form) {
            case 1 -> cfg1;
            case 2 -> cfg2;
            case 3 -> cfg3;
            default -> null;
        };
        if (cfg == null) return null;

        try {
            var lv2 = cfg.getClass().getField("bulletTextureLv2");
            Object v = lv2.get(cfg);
            if (v instanceof String s && s != null && !s.isBlank()) return s;
        } catch (Exception ignored) {
        }

        try {
            var arrField = cfg.getClass().getField("bulletTextures");
            Object v = arrField.get(cfg);
            if (v instanceof String[] arr && arr.length > 1 && arr[1] != null && !arr[1].isBlank()) {
                return arr[1];
            }
        } catch (Exception ignored) {
        }

        return reflectGet(cfg, "bulletTexture");
    }

    /* ========================== Hero discovery/initialization ========================== */

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
     * Identify the default Hero by the presence of {@link HeroTurretAttackComponent},
     * and localize the {@code hero} reference.
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
     * Upgrade listeners:
     * on upgrade → lock weapons + upgrade bullet + upgrade body texture.
     */
    private void hookUpgradeLock() {
        if (hero == null) return;

        // Direct level broadcast
        hero.getEvents().addListener("hero.level", (EventListener1<Integer>) newLevel -> {
            if (!locked && newLevel != null && newLevel > 1) {
                onUpgraded(newLevel, "hero.level");
            }
        });

        // Generic "upgraded" event (signature: Integer, Object, Integer)
        hero.getEvents().addListener("upgraded",
                (EventListener3<Integer, Object, Integer>) (newLevel, _costType, _cost) -> {
                    if (!locked && newLevel != null && newLevel > 1) {
                        onUpgraded(newLevel, "upgraded");
                    }
                });
    }

    /**
     * Central entry point for upgrade handling.
     */
    private void onUpgraded(int newLevel, String tag) {
        // 1) Bullet texture upgrade
        switchToUpgradedBulletIfAvailable(newLevel);

        // 2) Body texture upgrade for current form
        Object bodyCfg = switch (currentForm) {
            case 1 -> cfg1;
            case 2 -> cfg2;
            case 3 -> cfg3;
            default -> null;
        };
        String bodyTex = pickBodyTextureForLevel(bodyCfg, newLevel);
        if (bodyTex != null && !bodyTex.isBlank()) {
            switchTexture(bodyTex, tag + "-bodyLv" + newLevel);
        }

        // 3) Lock weapon bar and release this component (keep legacy behavior)
        locked = true;
        hero.getEvents().trigger("ui:weapon:locked");
        dispose();
        Gdx.app.log("HeroSkinSwitch", "locked by " + tag + ": level=" + newLevel);
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

    /* ========================== Form switching ========================== */

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

    private void switchToUpgradedBulletIfAvailable(int newLevel) {
        String upgraded = pickUpgradedBulletTexture(currentForm, newLevel);
        if (upgraded != null && !upgraded.isBlank()) {
            updateBulletTexture(upgraded);
            Gdx.app.log("HeroSkinSwitch", "bullet texture upgraded -> " + upgraded + " at level " + newLevel);
        }
    }

    private void updateBulletTexture(String bulletTexture) {
        if (hero == null) return;
        HeroTurretAttackComponent atc = hero.getComponent(HeroTurretAttackComponent.class);
        if (atc != null) atc.setBulletTexture(bulletTexture);
    }

    /* ========================== Sync form params to hero ========================== */

    /**
     * Form 1: sync attack params + sfx key/volume + base attack.
     */
    private void applyConfigToHero(HeroConfig cfg) {
        if (hero == null || cfg == null) return;
        HeroTurretAttackComponent atc = hero.getComponent(HeroTurretAttackComponent.class);
        CombatStatsComponent stats = hero.getComponent(CombatStatsComponent.class);
        if (atc != null) {
            atc.setCooldown(cfg.attackCooldown)
                    .setBulletParams(cfg.bulletSpeed, cfg.bulletLife)
                    .setBulletTexture(cfg.bulletTexture);
            if (cfg.shootSfx != null && !cfg.shootSfx.isBlank()) atc.setShootSfxKey(cfg.shootSfx);
            if (cfg.shootSfxVolume != null) atc.setShootSfxVolume(Math.max(0f, Math.min(1f, cfg.shootSfxVolume)));
        }
        if (stats != null) stats.setBaseAttack(cfg.baseAttack);
    }

    /**
     * Form 2: sync attack params + sfx key/volume + base attack.
     */
    private void applyConfigToHero2(HeroConfig2 cfg) {
        if (hero == null || cfg == null) return;
        HeroTurretAttackComponent atc = hero.getComponent(HeroTurretAttackComponent.class);
        CombatStatsComponent stats = hero.getComponent(CombatStatsComponent.class);
        if (atc != null) {
            atc.setCooldown(cfg.attackCooldown)
                    .setBulletParams(cfg.bulletSpeed, cfg.bulletLife)
                    .setBulletTexture(cfg.bulletTexture);
            if (cfg.shootSfx != null && !cfg.shootSfx.isBlank()) atc.setShootSfxKey(cfg.shootSfx);
            if (cfg.shootSfxVolume != null) atc.setShootSfxVolume(Math.max(0f, Math.min(1f, cfg.shootSfxVolume)));
        }
        if (stats != null) stats.setBaseAttack(cfg.baseAttack);
    }

    /**
     * Form 3: sync attack params + sfx key/volume + base attack.
     */
    private void applyConfigToHero3(HeroConfig3 cfg) {
        if (hero == null || cfg == null) return;
        HeroTurretAttackComponent atc = hero.getComponent(HeroTurretAttackComponent.class);
        CombatStatsComponent stats = hero.getComponent(CombatStatsComponent.class);
        if (atc != null) {
            atc.setCooldown(cfg.attackCooldown)
                    .setBulletParams(cfg.bulletSpeed, cfg.bulletLife)
                    .setBulletTexture(cfg.bulletTexture);
            if (cfg.shootSfx != null && !cfg.shootSfx.isBlank()) atc.setShootSfxKey(cfg.shootSfx);
            if (cfg.shootSfxVolume != null) atc.setShootSfxVolume(Math.max(0f, Math.min(1f, cfg.shootSfxVolume)));
        }
        if (stats != null) stats.setBaseAttack(cfg.baseAttack);
    }

    /* ========================== Rendering switch ========================== */

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

    /* ========================== Lifecycle ========================== */

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
        try {
            if (unsubSkinChanged != null) unsubSkinChanged.close();
        } catch (Exception ignore) {
        }
    }
}

