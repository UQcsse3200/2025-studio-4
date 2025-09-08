package com.csse3200.game.components.hero;

import com.badlogic.gdx.Gdx;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.currencysystem.CurrencyComponent.CurrencyType;
import com.csse3200.game.entities.configs.HeroConfig;
import com.csse3200.game.rendering.RotatingTextureRenderComponent;
import com.csse3200.game.rendering.TextureRenderComponent;

/**
 * 升级换装（支持 RotatingTextureRenderComponent / TextureRenderComponent）。
 * 由于贴图字段为 final，采用“移除旧组件 + 新建新贴图组件”的方式，并尽量保留原有旋转角等状态。
 */
public class HeroAppearanceComponent extends Component {
    private final HeroConfig cfg;

    public HeroAppearanceComponent(HeroConfig cfg) {
        this.cfg = cfg;
    }

    @Override
    public void create() {
        // 进场先套 1 级外观（可选）
        applyTextureForLevel(1);

        // 升级后切图
        entity.getEvents().addListener("upgraded", (Integer level, CurrencyType t, Integer cost) -> {
            applyTextureForLevel(level);
        });
    }

    private void applyTextureForLevel(int level) {
        final String path = getTextureForLevel(level);
        if (path == null || path.isBlank()) return;

        // 如果是可旋转渲染组件（英雄主体）
        RotatingTextureRenderComponent rot = entity.getComponent(RotatingTextureRenderComponent.class);
        if (rot != null) {
            float angle = rot.getRotation();
            rot.setTexture(path);   // ✅ 直接切贴图
            rot.setRotation(angle); // 恢复角度（可选）
            return;
        }


        // 如果是普通贴图组件（ghost 英雄）
        TextureRenderComponent tex = entity.getComponent(TextureRenderComponent.class);
        if (tex != null) {
            final float rotDeg = tex.getRotation();
            Gdx.app.postRunnable(() -> {
                TextureRenderComponent newTex = new TextureRenderComponent(path);
                entity.addComponent(newTex);
                newTex.setRotation(rotDeg);
            });
        }
    }


    // level 从 1 开始；有配就用 levelTextures[level-1]，否则回退 heroTexture
    private String getTextureForLevel(int level) {
        if (cfg.levelTextures != null) {
            int idx = level - 1;
            if (idx >= 0 && idx < cfg.levelTextures.length) {
                String s = cfg.levelTextures[idx];
                if (s != null && !s.isBlank()) return s;
            }
        }
        return cfg.heroTexture;
    }
}



