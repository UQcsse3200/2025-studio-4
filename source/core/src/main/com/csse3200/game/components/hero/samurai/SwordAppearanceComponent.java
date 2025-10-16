package com.csse3200.game.components.hero.samurai;

import com.csse3200.game.components.Component;
import com.csse3200.game.components.currencysystem.CurrencyComponent.CurrencyType;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.SamuraiConfig;
import com.csse3200.game.rendering.RotatingTextureRenderComponent;

public class SwordAppearanceComponent extends Component {
    private final Entity owner; // 武士本体
    private final SamuraiConfig cfg;

    public SwordAppearanceComponent(Entity owner, SamuraiConfig cfg) {
        this.owner = owner;
        this.cfg = cfg;
    }

    @Override
    public void create() {
        // 初始应用 1 级刀贴图
        applySwordTextureForLevel(1);

        // 升级时按等级切换
        if (owner != null) {
            owner.getEvents().addListener("upgraded",
                    (Integer level, CurrencyType t, Integer cost) -> applySwordTextureForLevel(level));
        }

        // ★ 监听“直接指定贴图”的事件（由 SamuraiSpinAttackComponent 转发）
        entity.getEvents().addListener("sword:skin:set", (String path) -> {
            if (path == null || path.isBlank()) return;
            RotatingTextureRenderComponent rot = entity.getComponent(RotatingTextureRenderComponent.class);
            if (rot != null) {
                float angle = rot.getRotation();
                rot.setTexture(path);
                rot.setRotation(angle);
            }
        });
    }

    private void applySwordTextureForLevel(int level) {
        String path = getSwordTextureForLevel(level);
        if (path == null || path.isBlank()) return;
        RotatingTextureRenderComponent rot = entity.getComponent(RotatingTextureRenderComponent.class);
        if (rot != null) {
            float angle = rot.getRotation();
            rot.setTexture(path);
            rot.setRotation(angle);
        }
    }

    private String getSwordTextureForLevel(int level) {
        if (cfg != null && cfg.swordLevelTextures != null) {
            int idx = level - 1;
            if (idx >= 0 && idx < cfg.swordLevelTextures.length) {
                String s = cfg.swordLevelTextures[idx];
                if (s != null && !s.isBlank()) return s;
            }
        }
        return (cfg != null) ? cfg.swordTexture : null;
    }
}
