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
 * 一次性“英雄换肤”组件（轮询发现英雄 + 5 秒自动默认第一形态 + 无 removeComponent 兜底）。
 */
public class HeroOneShotFormSwitchComponent extends InputComponent {
    private static final Logger logger = LoggerFactory.getLogger(HeroOneShotFormSwitchComponent.class);

    private static final float POLL_INTERVAL_SEC = 0.25f; // 轮询查找英雄
    private static final float AUTO_DEFAULT_DELAY_SEC = 5f; // 找到英雄后 5 秒默认第一形态

    private final HeroConfig  cfg1; // 1（默认）
    private final HeroConfig2 cfg2; // 2
    private final HeroConfig3 cfg3; // 3

    private Entity hero;
    private boolean locked = false;

    private Timer.Task pollTask;        // 轮询查找英雄
    private Timer.Task autoDefaultTask; // 5 秒自动默认

    public HeroOneShotFormSwitchComponent(HeroConfig cfg1, HeroConfig2 cfg2, HeroConfig3 cfg3) {
        super(1000); // 高优先级，保证拿到 1/2/3
        this.cfg1 = cfg1;
        this.cfg2 = cfg2;
        this.cfg3 = cfg3;
    }

    @Override
    public void create() {
        super.create();
        // 预加载三套贴图，避免切换时资源缺失
        ResourceService rs = ServiceLocator.getResourceService();
        rs.loadTextures(new String[]{
                cfg1 != null ? cfg1.heroTexture : null,
                cfg2 != null ? cfg2.heroTexture : null,
                cfg3 != null ? cfg3.heroTexture : null
        });
        while (!rs.loadForMillis(5)) { /* spin a bit */ }

        // 立即跑一次，找到了就不用轮询
        if (!tryFindHero()) {
            // 英雄还没生成：启动轮询
            armHeroPolling();
        } else {
            // 英雄已存在：直接启动 5 秒自动默认
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
                    cancel();            // 停止轮询
                    pollTask = null;
                    armAutoDefaultTimer();
                }
            }
        }, 0f, POLL_INTERVAL_SEC); // 立刻开始，每 0.25s 查一次
        logger.info("[HeroSkinSwitch] Polling for hero every {}s...", POLL_INTERVAL_SEC);
    }

    /** 尝试查找“正式英雄”（含 HeroTurretAttackComponent），找到返回 true */
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
        if (hero == null && !tryFindHero()) return false; // 若还没英雄，忽略输入

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
            this.entity.dispose(); // 一次性：注销自身（会从 InputService 注销）
        }
        return ok;
    }

    /**
     * 先尝试反射 setTexture(String)；若无，则隐藏旧渲染(透明 + 尽量 dispose)，再添加新渲染组件。
     */
    private boolean applyTextureToHero(String texturePath) {
        try {
            RotatingTextureRenderComponent rot = hero.getComponent(RotatingTextureRenderComponent.class);
            if (rot == null) {
                logger.warn("[HeroSkinSwitch] Hero has no RotatingTextureRenderComponent.");
                return false;
            }

            // A) 反射调用 setTexture(String)
            try {
                Method m = rot.getClass().getMethod("setTexture", String.class);
                m.invoke(rot, texturePath);
                return true;
            } catch (NoSuchMethodException noSetter) {
                // B) 兜底：隐藏旧渲染（透明）+ 尽量 dispose + 新增一个渲染组件
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
