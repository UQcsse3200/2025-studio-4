package com.csse3200.game.services;

/**
 * 用来在“主菜单”与“游戏关卡（ForestGameArea/MainGameScreen）”之间
 * 传递玩家选择的英雄类型的简单服务。
 */
public class SelectedHeroService {
    /** 先做两个选项；后续要扩展可以继续加 */
    public enum HeroType {
        HERO,       // 普通英雄
        ENGINEER    // 工程师
    }

    /** 默认选 HERO（防止未设置时为 null） */
    private HeroType selected = HeroType.HERO;

    /** 设定当前选择 */
    public void setSelected(HeroType type) {
        if (type == null) return;
        this.selected = type;
    }

    /** 读取当前选择 */
    public HeroType getSelected() {
        return selected;
    }
}
