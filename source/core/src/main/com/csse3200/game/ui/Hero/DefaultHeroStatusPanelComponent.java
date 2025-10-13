package com.csse3200.game.ui.Hero;

import com.badlogic.gdx.graphics.Color;
import com.csse3200.game.ui.Hero.BaseHeroStatusPanelComponent;

/**
 * 普通英雄状态栏（无召唤容量/冷却区），可用于 Hero1/2/3 等。
 * 配色：蓝青强调；面板高度 0.24f
 */
public class DefaultHeroStatusPanelComponent extends BaseHeroStatusPanelComponent {

    public DefaultHeroStatusPanelComponent(com.csse3200.game.entities.Entity hero, String heroName) {
        super(
                hero,
                heroName != null ? heroName : "Hero",
                new Color(0.15f, 0.15f, 0.18f, 0.90f), // 背景
                Color.WHITE,                           // 文字
                new Color(0.35f, 0.75f, 1.00f, 1f),    // 强调：蓝青
                0.32f                                  // 高度
        );
    }

    // 无额外区块，无需覆写 buildExtraSections()/bindExtraListeners()
}
