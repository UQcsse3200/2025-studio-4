package com.csse3200.game.components.hero;

import com.badlogic.gdx.Gdx;
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
import com.csse3200.game.events.listeners.EventListener3;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.ArrayList;

public class HeroOneShotFormSwitchComponent extends InputComponent {
    private static final Logger logger = LoggerFactory.getLogger(HeroOneShotFormSwitchComponent.class);
    private static final float POLL_INTERVAL_SEC = 0.25f;

    private final HeroConfig  cfg1;   // Form 1
    private final HeroConfig2 cfg2;   // Form 2
    private final HeroConfig3 cfg3;   // Form 3

    private Entity hero;
    private boolean locked = false;
    private Timer.Task pollTask;
    private static final long SWITCH_COOLDOWN_MS = 3000L; // 3s CD
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

        // 预加载三形态贴图（声音推荐在 GameArea/HeroFactory 里统一 loadSounds）
        var textures = new ArrayList<String>();
        if (cfg1 != null && cfg1.heroTexture != null && !cfg1.heroTexture.isBlank()) textures.add(cfg1.heroTexture);
        if (cfg2 != null && cfg2.heroTexture != null && !cfg2.heroTexture.isBlank()) textures.add(cfg2.heroTexture);
        if (cfg3 != null && cfg3.heroTexture != null && !cfg3.heroTexture.isBlank()) textures.add(cfg3.heroTexture);

        addBulletTextures(textures, cfg1);
        addBulletTextures(textures, cfg2);
        addBulletTextures(textures, cfg3);

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

        // 监听 UI 切换（1/2/3），内置 3s 冷却
        this.getEntity().getEvents().addListener("ui:weapon:switch", (Integer form) -> {
            if (locked) return;
            if (hero == null && !tryFindHero()) return;

            long now = TimeUtils.millis();
            if (now < nextSwitchAtMs) {
                long remainMs = nextSwitchAtMs - now;
                hero.getEvents().trigger("ui:weapon:cooldown", remainMs);
                return;
            }

            int target = (form != null ? form : 0);
            if (target < 1 || target > 3) return;
            if (target == currentForm) return;

            boolean ok = applyForm(target, "ui-" + target);
            if (ok) {
                nextSwitchAtMs = now + SWITCH_COOLDOWN_MS;
                hero.getEvents().trigger("ui:weapon:cooldown:start", SWITCH_COOLDOWN_MS);
                Gdx.app.log("HeroSkinSwitch", "switched by UI to form " + target + " (cooldown 3s)");
            }
        });

        // ===== ★ 在这里：注册皮肤变更监听 =====
        var gs = ServiceLocator.getGameStateService();
        if (gs != null) {
            unsubSkinChanged = gs.onSkinChanged((who, newSkin) -> {
                // 仅主角：工程师/武士请在各自组件里监听
                if (who != com.csse3200.game.services.GameStateService.HeroType.HERO) return;

                // 1) 套新皮肤到三套表单
                applySkinToForm(cfg1, newSkin);
                applySkinToForm(cfg2, newSkin);
                applySkinToForm(cfg3, newSkin);

                // 2) 预加载新贴图/子弹，避免立即切换时报空
                preloadFormTextures(cfg1, cfg2, cfg3);

                // 3) 若当前正在使用某形态，立刻热刷新
                HeroConfig cur = switch (currentForm) {
                    case 1 -> cfg1;
                    case 2 -> cfg2;
                    case 3 -> cfg3;
                    default -> null;
                };
                if (cur != null) {
                    switchTexture(cur.heroTexture, "skin-changed");
                    updateBulletTexture(cur.bulletTexture);
                }
            });
        }

    }

    private void applySkinToForm(Object formCfg, String skinKey) {
        if (formCfg == null || skinKey == null || skinKey.isBlank()) return;

        // 你的三套配置类字段名一致（heroTexture / bulletTexture / levelTextures）
        // 所以用反射写一份即可复用（你也可以为 HeroConfig2/3 各写一个重载）
        try {
            // 本体纹理
            var heroTexF = formCfg.getClass().getField("heroTexture");
            String newHeroTex = com.csse3200.game.entities.configs.HeroSkinAtlas.body(
                    com.csse3200.game.services.GameStateService.HeroType.HERO, skinKey);
            heroTexF.set(formCfg, newHeroTex);

            // 子弹纹理（如果你的皮肤也映射了子弹）
            var bulletTexF = formCfg.getClass().getField("bulletTexture");
            String newBulletTex = com.csse3200.game.entities.configs.HeroSkinAtlas.bullet(
                    com.csse3200.game.services.GameStateService.HeroType.HERO, skinKey);
            bulletTexF.set(formCfg, newBulletTex);

            // levelTextures[0] 也同步，避免外观/升级组件把贴图打回默认
            try {
                var levelsF = formCfg.getClass().getField("levelTextures");
                Object arr = levelsF.get(formCfg);
                if (arr instanceof String[] levels) {
                    if (levels.length > 0) levels[0] = newHeroTex;
                    else levelsF.set(formCfg, new String[]{ newHeroTex });
                } else {
                    levelsF.set(formCfg, new String[]{ newHeroTex });
                }
            } catch (NoSuchFieldException ignore) {}
        } catch (Exception e) {
            Gdx.app.error("HeroSkinSwitch", "applySkinToForm failed", e);
        }
    }

    private void preloadFormTextures(Object... forms) {
        var rs = ServiceLocator.getResourceService();
        java.util.ArrayList<String> toLoad = new java.util.ArrayList<>();
        for (Object f : forms) {
            if (f == null) continue;
            try {
                var ht = (String) f.getClass().getField("heroTexture").get(f);
                if (ht != null && !ht.isBlank()) toLoad.add(ht);
            } catch (Exception ignore) {}
            try {
                var bt = (String) f.getClass().getField("bulletTexture").get(f);
                if (bt != null && !bt.isBlank()) toLoad.add(bt);
            } catch (Exception ignore) {}
        }
        if (!toLoad.isEmpty()) {
            rs.loadTextures(toLoad.toArray(new String[0]));
            while (!rs.loadForMillis(10)) { /* wait */ }
        }
    }


    private static void addBulletTextures(ArrayList<String> textures, Object cfg) {
        if (cfg == null) return;
        try {
            var single = cfg.getClass().getField("bulletTexture");
            Object v = single.get(cfg);
            if (v instanceof String s && !s.isBlank()) textures.add(s);
        } catch (Exception ignored) {}

        try {
            var arr = cfg.getClass().getField("bulletTextures");
            Object v = arr.get(cfg);
            if (v instanceof String[] a) {
                for (String s : a) if (s != null && !s.isBlank()) textures.add(s);
            }
        } catch (Exception ignored) {}

        // ★ 新增：升级后的单贴图
        try {
            var lv2 = cfg.getClass().getField("bulletTextureLv2");
            Object v = lv2.get(cfg);
            if (v instanceof String s && !s.isBlank()) textures.add(s);
        } catch (Exception ignored) {}
    }

    private String pickUpgradedBulletTexture(int form, int level) {
        if (level <= 1) return null;
        Object cfg = switch (form) {
            case 1 -> cfg1;
            case 2 -> cfg2;
            case 3 -> cfg3;
            default -> null;
        };
        if (cfg == null) return null;

        // 1) 优先用 bulletTextureLv2
        try {
            var lv2 = cfg.getClass().getField("bulletTextureLv2");
            Object v = lv2.get(cfg);
            if (v instanceof String s && s != null && !s.isBlank()) return s;
        } catch (Exception ignored) {}

        // 2) 其次用数组的第二张 bulletTextures[1]
        try {
            var arrField = cfg.getClass().getField("bulletTextures");
            Object v = arrField.get(cfg);
            if (v instanceof String[] arr && arr.length > 1 && arr[1] != null && !arr[1].isBlank()) {
                return arr[1];
            }
        } catch (Exception ignored) {}

        // 3) 最后兜底回到原 bulletTexture（如果你想强制升级必须换贴图，可以把这个兜底去掉）
        try {
            var singleField = cfg.getClass().getField("bulletTexture");
            Object v = singleField.get(cfg);
            if (v instanceof String s && s != null && !s.isBlank()) return s;
        } catch (Exception ignored) {}

        return null;
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
                switchToUpgradedBulletIfAvailable(newLevel);
                locked = true;
                hero.getEvents().trigger("ui:weapon:locked");
                dispose();
                Gdx.app.log("HeroSkinSwitch", "locked by levelUp: level=" + newLevel);
            }
        });

        hero.getEvents().addListener("upgraded",
                (EventListener3<Integer, Object, Integer>) (newLevel, _costType, _cost) -> {
                    if (!locked && newLevel != null && newLevel > 1) {
                        switchToUpgradedBulletIfAvailable(newLevel);
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
            applyConfigToHero(cfg1);   // ★ 会同步音效键/音量
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
            applyConfigToHero(cfg1);     // ★ 同步形态1参数 + 音效
        } else if (form == 2 && cfg2 != null) {
            updateBulletTexture(cfg2.bulletTexture);
            applyConfigToHero2(cfg2);    // ★ 同步形态2参数 + 音效
        } else if (form == 3 && cfg3 != null) {
            updateBulletTexture(cfg3.bulletTexture);
            applyConfigToHero3(cfg3);    // ★ 同步形态3参数 + 音效
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

    /** 形态1：同步攻击参数 + 音效键/音量 */
    private void applyConfigToHero(HeroConfig cfg) {
        if (hero == null || cfg == null) return;
        HeroTurretAttackComponent atc = hero.getComponent(HeroTurretAttackComponent.class);
        CombatStatsComponent stats = hero.getComponent(CombatStatsComponent.class);
        if (atc != null) {
            atc.setCooldown(cfg.attackCooldown)
                    .setBulletParams(cfg.bulletSpeed, cfg.bulletLife)
                    .setBulletTexture(cfg.bulletTexture);
            // ★ 音效（如果配置了就覆盖）
            if (cfg.shootSfx != null && !cfg.shootSfx.isBlank()) {
                atc.setShootSfxKey(cfg.shootSfx);
            }
            // shootSfxVolume 建议是 Float 包装类型；如果是 primitive，请去掉空判
            if (cfg.shootSfxVolume != null) {
                float vol = Math.max(0f, Math.min(1f, cfg.shootSfxVolume));
                atc.setShootSfxVolume(vol);
            }
        }
        if (stats != null) stats.setBaseAttack(cfg.baseAttack);
    }

    /** 形态2：同步攻击参数 + 音效键/音量 */
    private void applyConfigToHero2(HeroConfig2 cfg) {
        if (hero == null || cfg == null) return;
        HeroTurretAttackComponent atc = hero.getComponent(HeroTurretAttackComponent.class);
        CombatStatsComponent stats = hero.getComponent(CombatStatsComponent.class);
        if (atc != null) {
            atc.setCooldown(cfg.attackCooldown)
                    .setBulletParams(cfg.bulletSpeed, cfg.bulletLife)
                    .setBulletTexture(cfg.bulletTexture);
            if (cfg.shootSfx != null && !cfg.shootSfx.isBlank()) {
                atc.setShootSfxKey(cfg.shootSfx);
            }
            if (cfg.shootSfxVolume != null) {
                float vol = Math.max(0f, Math.min(1f, cfg.shootSfxVolume));
                atc.setShootSfxVolume(vol);
            }
        }
        if (stats != null) stats.setBaseAttack(cfg.baseAttack);
    }

    /** 形态3：同步攻击参数 + 音效键/音量 */
    private void applyConfigToHero3(HeroConfig3 cfg) {
        if (hero == null || cfg == null) return;
        HeroTurretAttackComponent atc = hero.getComponent(HeroTurretAttackComponent.class);
        CombatStatsComponent stats = hero.getComponent(CombatStatsComponent.class);
        if (atc != null) {
            atc.setCooldown(cfg.attackCooldown)
                    .setBulletParams(cfg.bulletSpeed, cfg.bulletLife)
                    .setBulletTexture(cfg.bulletTexture);
            if (cfg.shootSfx != null && !cfg.shootSfx.isBlank()) {
                atc.setShootSfxKey(cfg.shootSfx);
            }
            if (cfg.shootSfxVolume != null) {
                float vol = Math.max(0f, Math.min(1f, cfg.shootSfxVolume));
                atc.setShootSfxVolume(vol);
            }
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
        try { if (unsubSkinChanged != null) unsubSkinChanged.close(); } catch (Exception ignore) {}
    }
}
