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
 * 默认 Hero 的一体化外观/形态/皮肤/升级控制中心：
 * - 三形态（1/2/3）切换（带冷却）
 * - 皮肤变更（热更新外观与子弹）
 * - 升级后：锁定武器栏 + 升级子弹贴图 + 按等级改“身体贴图”
 * - 负责预加载 heroTexture / bulletTexture / levelTextures[*]，避免升级瞬白
 */
public class HeroOneShotFormSwitchComponent extends InputComponent {
    private static final Logger logger = LoggerFactory.getLogger(HeroOneShotFormSwitchComponent.class);

    private static final float POLL_INTERVAL_SEC = 0.25f;
    private static final long SWITCH_COOLDOWN_MS = 3000L; // 3s 冷却

    private final HeroConfig  cfg1;   // 形态1
    private final HeroConfig2 cfg2;   // 形态2
    private final HeroConfig3 cfg3;   // 形态3

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

        // 预加载：三形态的 body / bullet / levelTextures[*]
        preloadFormTextures(cfg1, cfg2, cfg3);

        if (!tryFindHero()) {
            armHeroPolling();
        } else {
            hookUpgradeLock();
            bootToForm1();
        }

        // 监听 UI 武器切换（1/2/3），内置 3s 冷却
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

        // 皮肤变更（仅默认 Hero；工程师/武士在各自组件里监听）
        var gs = ServiceLocator.getGameStateService();
        if (gs != null) {
            unsubSkinChanged = gs.onSkinChanged((who, newSkin) -> {
                if (who != com.csse3200.game.services.GameStateService.HeroType.HERO) return;

                // 改三形态皮肤映射（含 levelTextures[0] 覆盖）
                applySkinToForm(cfg1, newSkin);
                applySkinToForm(cfg2, newSkin);
                applySkinToForm(cfg3, newSkin);

                // 预加载新贴图，避免热切瞬白
                preloadFormTextures(cfg1, cfg2, cfg3);

                // 正在使用的形态立即热刷新
                Object cur = switch (currentForm) {
                    case 1 -> cfg1;
                    case 2 -> cfg2;
                    case 3 -> cfg3;
                    default -> null;
                };
                if (cur != null) {
                    String bodyTex = pickBodyTextureForLevel(cur, 1); // 当前等级未知就按Lv1兜底
                    if (bodyTex != null && !bodyTex.isBlank()) {
                        switchTexture(bodyTex, "skin-changed");
                    }
                    String bullet = reflectGet(cur, "bulletTexture");
                    if (bullet != null && !bullet.isBlank()) {
                        updateBulletTexture(bullet);
                    }
                }
            });
        }
    }

    /* ========================== 皮肤/外观辅助 ========================== */

    /** 反射取 String 字段，失败返回 null */
    private static String reflectGet(Object bean, String field) {
        if (bean == null) return null;
        try {
            Object v = bean.getClass().getField(field).get(bean);
            return (v instanceof String s) ? s : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    /** 将皮肤 key 应用到单个形态配置（更新 heroTexture / bulletTexture / levelTextures[0]） */
    private void applySkinToForm(Object formCfg, String skinKey) {
        if (formCfg == null || skinKey == null || skinKey.isBlank()) return;
        try {
            // 本体
            var heroTexF = formCfg.getClass().getField("heroTexture");
            String body = com.csse3200.game.entities.configs.HeroSkinAtlas.body(
                    com.csse3200.game.services.GameStateService.HeroType.HERO, skinKey);
            heroTexF.set(formCfg, body);

            // 子弹（如果皮肤映射了子弹）
            try {
                var bulletTexF = formCfg.getClass().getField("bulletTexture");
                String bullet = com.csse3200.game.entities.configs.HeroSkinAtlas.bullet(
                        com.csse3200.game.services.GameStateService.HeroType.HERO, skinKey);
                if (bullet != null && !bullet.isBlank()) bulletTexF.set(formCfg, bullet);
            } catch (NoSuchFieldException ignore) {}

            // 防止 levelTextures[0] 把外观打回默认
            try {
                var levelsF = formCfg.getClass().getField("levelTextures");
                Object arr = levelsF.get(formCfg);
                if (arr instanceof String[] levels) {
                    if (levels.length > 0) levels[0] = body;
                    else levelsF.set(formCfg, new String[]{ body });
                } else {
                    levelsF.set(formCfg, new String[]{ body });
                }
            } catch (NoSuchFieldException ignore) {}
        } catch (Exception e) {
            Gdx.app.error("HeroSkinSwitch", "applySkinToForm failed", e);
        }
    }

    /** 预加载各形态：heroTexture / bulletTexture / levelTextures[*] */
    private void preloadFormTextures(Object... forms) {
        ResourceService rs = ServiceLocator.getResourceService();
        ArrayList<String> toLoad = new ArrayList<>();
        for (Object f : forms) {
            if (f == null) continue;
            // body
            String ht = reflectGet(f, "heroTexture");
            if (ht != null && !ht.isBlank()) toLoad.add(ht);
            // bullet（可能有单字段/数组/bulletTextureLv2）
            addBulletTextures(toLoad, f);
            // levelTextures[*]
            try {
                var levelsF = f.getClass().getField("levelTextures");
                Object arr = levelsF.get(f);
                if (arr instanceof String[] levels) {
                    for (String s : levels) if (s != null && !s.isBlank()) toLoad.add(s);
                }
            } catch (Exception ignore) {}
        }
        if (!toLoad.isEmpty()) {
            rs.loadTextures(toLoad.toArray(new String[0]));
            while (!rs.loadForMillis(10)) { /* wait */ }
        }
    }

    /** 收集子弹贴图（单值/数组/Lv2） */
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
            if (v instanceof String[] a) for (String s : a) if (s != null && !s.isBlank()) textures.add(s);
        } catch (Exception ignored) {}
        try {
            var lv2 = cfg.getClass().getField("bulletTextureLv2");
            Object v = lv2.get(cfg);
            if (v instanceof String s && !s.isBlank()) textures.add(s);
        } catch (Exception ignored) {}
    }

    /** 升级用：按等级挑选“身体贴图”（优先 levelTextures[level-1]，否则 heroTexture） */
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
        } catch (Exception ignore) {}
        // fallback: heroTexture
        return reflectGet(cfg, "heroTexture");
    }

    /** 升级用：按形态与等级选择子弹贴图（Lv2 > 数组[1] > 原始） */
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
        } catch (Exception ignored) {}

        try {
            var arrField = cfg.getClass().getField("bulletTextures");
            Object v = arrField.get(cfg);
            if (v instanceof String[] arr && arr.length > 1 && arr[1] != null && !arr[1].isBlank()) {
                return arr[1];
            }
        } catch (Exception ignored) {}

        return reflectGet(cfg, "bulletTexture");
    }

    /* ========================== 寻找/初始化 ========================== */

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

    /** 通过是否含 HeroTurretAttackComponent 识别默认 Hero，本地化 hero 引用 */
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

    /** 升级监听：升级后锁定 + 子弹升级 + 身体贴图升级 */
    private void hookUpgradeLock() {
        if (hero == null) return;

        // 监听直接等级广播
        hero.getEvents().addListener("hero.level", (EventListener1<Integer>) newLevel -> {
            if (!locked && newLevel != null && newLevel > 1) {
                onUpgraded(newLevel, "hero.level");
            }
        });

        // 监听通用 upgraded 事件（签名：Integer, Object, Integer）
        hero.getEvents().addListener("upgraded",
                (EventListener3<Integer, Object, Integer>) (newLevel, _costType, _cost) -> {
                    if (!locked && newLevel != null && newLevel > 1) {
                        onUpgraded(newLevel, "upgraded");
                    }
                });
    }

    /** 升级处理的集中入口 */
    private void onUpgraded(int newLevel, String tag) {
        // 1) 升级子弹贴图
        switchToUpgradedBulletIfAvailable(newLevel);

        // 2) 升级“身体贴图”（按当前形态）
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

        // 3) 锁定武器栏 + 释放自己（旧设计保持）
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

    /* ========================== 形态切换 ========================== */

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

    /* ========================== 形态参数同步 ========================== */

    /** 形态1：同步攻击参数 + 音效键/音量 + 基础攻击 */
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

    /** 形态2：同步 */
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

    /** 形态3：同步 */
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

    /* ========================== 渲染切换 ========================== */

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

    /* ========================== 生命周期 ========================== */

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
