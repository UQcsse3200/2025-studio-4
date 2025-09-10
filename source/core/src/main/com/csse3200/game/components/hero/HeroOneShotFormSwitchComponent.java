package com.csse3200.game.components.hero;

import com.badlogic.gdx.Input;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.HeroConfig;
import com.csse3200.game.entities.configs.HeroConfig2;
import com.csse3200.game.entities.configs.HeroConfig3;
import com.csse3200.game.rendering.RotatingTextureRenderComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.input.InputComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * 一次性“英雄换肤”组件（无 removeComponent 版本）：
 * - 如未生成英雄，按键时会懒查找；
 * - 切换成功后锁定并自注销；
 * - 优先 setTexture(String)；若无，则隐藏旧渲染(+尽量 dispose)，再添加新渲染组件。
 */
public class HeroOneShotFormSwitchComponent extends InputComponent {
    private static final Logger logger = LoggerFactory.getLogger(HeroOneShotFormSwitchComponent.class);

    private final HeroConfig  cfg1; // 1
    private final HeroConfig2 cfg2; // 2
    private final HeroConfig3 cfg3; // 3

    private Entity hero;
    private boolean locked = false;

    public HeroOneShotFormSwitchComponent(HeroConfig cfg1, HeroConfig2 cfg2, HeroConfig3 cfg3) {
        super(1000);
        this.cfg1 = cfg1;
        this.cfg2 = cfg2;
        this.cfg3 = cfg3;
    }

    @Override
    public void create() {
        super.create();
        // 预加载
        ResourceService rs = ServiceLocator.getResourceService();
        rs.loadTextures(new String[]{
                (cfg1 != null ? cfg1.heroTexture : null),
                (cfg2 != null ? cfg2.heroTexture : null),
                (cfg3 != null ? cfg3.heroTexture : null)
        });
        while (!rs.loadForMillis(5)) { /* spin */ }

        ensureHeroFound(false);
    }

    private void ensureHeroFound(boolean verbose) {
        if (hero != null) return;
        for (Entity e : ServiceLocator.getEntityService().getEntities()) {
            if (e.getComponent(HeroTurretAttackComponent.class) != null) {
                hero = e;
                if (verbose) logger.info("[HeroSkinSwitch] Found hero on demand.");
                return;
            }
        }
        if (verbose) logger.info("[HeroSkinSwitch] Still no hero on map.");
    }

    @Override
    public boolean keyDown(int keycode) {
        if (locked) return false;
        if (hero == null) ensureHeroFound(true);
        if (hero == null) return false;

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
        if (hero == null) ensureHeroFound(true);
        if (hero == null) return false;

        if (character == '1') return switchOnce(cfg1 != null ? cfg1.heroTexture : null, "1");
        if (character == '2') return switchOnce(cfg2 != null ? cfg2.heroTexture : null, "2");
        if (character == '3') return switchOnce(cfg3 != null ? cfg3.heroTexture : null, "3");
        return false;
    }

    private boolean switchOnce(String texturePath, String keyTag) {
        if (texturePath == null || texturePath.isBlank()) {
            logger.warn("[HeroSkinSwitch] Texture path for key {} is empty. Ignored.", keyTag);
            return false;
        }
        boolean ok = applyTextureToHero(texturePath);
        if (ok) {
            locked = true;
            logger.info("[HeroSkinSwitch] Applied texture via key {} -> {}. Locked.", keyTag, texturePath);
            this.entity.dispose(); // 一次性
        }
        return ok;
    }

    /**
     * 先试 setTexture(String)；不行则隐藏旧渲染(并尝试 dispose)后，再添加一个新的渲染组件。
     */
    private boolean applyTextureToHero(String texturePath) {
        try {
            RotatingTextureRenderComponent rot = hero.getComponent(RotatingTextureRenderComponent.class);
            if (rot == null) {
                logger.warn("[HeroSkinSwitch] Hero has no RotatingTextureRenderComponent.");
                return false;
            }

            // A) 反射尝试 setTexture(String)
            try {
                Method m = rot.getClass().getMethod("setTexture", String.class);
                m.invoke(rot, texturePath);
                return true;
            } catch (NoSuchMethodException noSetter) {
                // B) 兜底：隐藏旧的 + 尽量 dispose + 新增一个渲染组件
                try {
                    // 尝试隐藏旧渲染（透明）
                    Method setColor = rot.getClass().getMethod("setColor", float.class, float.class, float.class, float.class);
                    setColor.invoke(rot, 1f, 1f, 1f, 0f);
                } catch (Throwable ignore) {
                    // 如果没有 setColor 方法，忽略
                }
                try {
                    // 若组件实现了 dispose()，尽量释放资源（注意：并不一定移除组件引用）
                    Method dispose = rot.getClass().getMethod("dispose");
                    dispose.invoke(rot);
                } catch (Throwable ignore) {
                    // 没有 dispose 也没关系
                }

                // 添加一个新的渲染组件（使用新的贴图路径）
                hero.addComponent(new RotatingTextureRenderComponent(texturePath));
                return true;
            }

        } catch (Exception e) {
            logger.error("[HeroSkinSwitch] Failed to switch texture to {}: {}", texturePath, e.getMessage());
            return false;
        }
    }
}
