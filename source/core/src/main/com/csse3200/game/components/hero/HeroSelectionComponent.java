package com.csse3200.game.components.hero;

import com.csse3200.game.components.Component;
import com.csse3200.game.services.GameStateService;
import com.csse3200.game.services.ServiceLocator;
import com.badlogic.gdx.Gdx;

/**
 * 进入关卡后自动根据主菜单选择，安装对应的英雄放置器。
 * 不再监听键盘/按钮，避免跨 Screen 的输入耦合。
 */
public class HeroSelectionComponent extends Component {
    private final Runnable installHeroPlacement;      // 选择 Hero 时执行
    private final Runnable installEngineerPlacement;  // 选择 Engineer 时执行
    private final Runnable installSamuraiPlacement;
    private boolean applied = false;

    public HeroSelectionComponent(Runnable installHeroPlacement,
                                  Runnable installEngineerPlacement,Runnable installSamuraiPlacement) {
        this.installHeroPlacement = installHeroPlacement;
        this.installEngineerPlacement = installEngineerPlacement;
        this.installSamuraiPlacement=installSamuraiPlacement;
    }

    @Override
    public void create() {
        // 读取主菜单写入的选择
        var gameState = ServiceLocator.getGameStateService();
        GameStateService.HeroType chosen =
                (gameState != null) ? gameState.getSelectedHero() : GameStateService.HeroType.HERO;

        switch (chosen) {
            case ENGINEER -> {
                installEngineerPlacement.run();
                Gdx.app.log("HeroSelection", "Applied selection from menu: ENGINEER");
            }
            case HERO -> {
                installHeroPlacement.run();
                Gdx.app.log("HeroSelection", "Applied selection from menu: HERO");
            }
            case SAMURAI-> {
                installSamuraiPlacement.run();
                Gdx.app.log("HeroSelection", "Applied selection from menu: SAMURAI");
            }
        }

        applied = true;
        // 选完就可以把自己移除（可选）
        if (entity != null) {
            entity.dispose();
        }
    }

    @Override
    public void dispose() {
        // 无需移除输入处理器，已不再使用键盘监听
    }
}



