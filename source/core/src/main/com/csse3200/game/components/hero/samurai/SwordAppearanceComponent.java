package com.csse3200.game.components.hero.samurai;

import com.badlogic.gdx.Gdx;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.currencysystem.CurrencyComponent.CurrencyType;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.SamuraiConfig;
import com.csse3200.game.rendering.RotatingTextureRenderComponent;
import com.csse3200.game.rendering.TextureRenderComponent;

public class SwordAppearanceComponent extends Component {
    private final Entity owner;          // 武士本体
    private final SamuraiConfig cfg;

    public SwordAppearanceComponent(Entity owner, SamuraiConfig cfg) {
        this.owner = owner;
        this.cfg = cfg;
    }

    @Override
    public void create() {
        // 初始应用 1 级刀贴图
        applySwordTextureForLevel(1);

        // 监听武士本体的升级事件
        if (owner != null) {
            owner.getEvents().addListener("upgraded",
                    (Integer level, CurrencyType t, Integer cost) -> applySwordTextureForLevel(level));
        }
    }

    private void applySwordTextureForLevel(int level) {
        String path = getSwordTextureForLevel(level);
        if (path == null || path.isBlank()) return;

        // 优先支持旋转贴图组件（你的刀一般是这个）
        RotatingTextureRenderComponent rot = entity.getComponent(RotatingTextureRenderComponent.class);
        if (rot != null) {
            float angle = rot.getRotation();
            rot.setTexture(path);     // 直接替换贴图
            rot.setRotation(angle);   // 保持当前旋转
            return;
        }
    }

    private String getSwordTextureForLevel(int level) {
        if (cfg.swordLevelTextures != null) {
            int idx = level - 1;
            if (idx >= 0 && idx < cfg.swordLevelTextures.length) {
                String s = cfg.swordLevelTextures[idx];
                if (s != null && !s.isBlank()) return s;
            }
        }
        // 兜底：用初始刀贴图
        return cfg.swordTexture;
    }
}
