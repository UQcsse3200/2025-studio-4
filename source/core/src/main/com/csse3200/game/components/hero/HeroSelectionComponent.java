package com.csse3200.game.components.hero;

import com.csse3200.game.components.Component;
import com.csse3200.game.services.GameStateService;
import com.csse3200.game.services.ServiceLocator;
import com.badlogic.gdx.Gdx;

/**
 * After entering the level, the corresponding hero placement device will be automatically installed according to the main menu selection.
 *No longer monitors keyboard/buttons to avoid cross-screen input coupling.
 *
 */
public class HeroSelectionComponent extends Component {
    private final Runnable installHeroPlacement;      // Executed when a Hero is selected
    private final Runnable installEngineerPlacement;  // Execute when Engineer is selected
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
        // Read the selection written to the main menu
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

        //You can remove yourself after selecting (optional)
        if (entity != null) {
            entity.dispose();
        }
    }

    @Override
    public void dispose() {
        // No need to remove input handlers, keyboard listeners are no longer used
    }
}



