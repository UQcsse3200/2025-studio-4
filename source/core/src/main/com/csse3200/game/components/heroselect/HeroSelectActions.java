package com.csse3200.game.components.heroselect;

import com.csse3200.game.GdxGame;
import com.csse3200.game.components.Component;
import com.csse3200.game.services.SelectedHeroService;
import com.csse3200.game.services.ServiceLocator;

/**
 * 英雄选择界面的行为：把选择结果写入 SelectedHeroService 并切换场景。
 */
public class HeroSelectActions extends Component {
    private final GdxGame game;

    public HeroSelectActions(GdxGame game) {
        this.game = game;
    }

    @Override
    public void create() {
        entity.getEvents().addListener("pickHero", this::onPickHero);
        entity.getEvents().addListener("pickEngineer", this::onPickEngineer);
        entity.getEvents().addListener("pickSamurai", this::onPickSamurai); // ✅ 新增监听
        entity.getEvents().addListener("goBackToMenu", this::onBackToMenu);
    }

    private void onPickHero() {
        ServiceLocator.getSelectedHeroService()
                .setSelected(SelectedHeroService.HeroType.HERO);
        game.setScreen(GdxGame.ScreenType.MAIN_GAME);
    }

    private void onPickEngineer() {
        ServiceLocator.getSelectedHeroService()
                .setSelected(SelectedHeroService.HeroType.ENGINEER);
        game.setScreen(GdxGame.ScreenType.MAIN_GAME);
    }

    // ✅ 新增方法
    private void onPickSamurai() {
        ServiceLocator.getSelectedHeroService()
                .setSelected(SelectedHeroService.HeroType.SAMURAI);
        game.setScreen(GdxGame.ScreenType.MAIN_GAME);
    }

    private void onBackToMenu() {
        game.setScreen(GdxGame.ScreenType.MAIN_MENU);
    }
}
