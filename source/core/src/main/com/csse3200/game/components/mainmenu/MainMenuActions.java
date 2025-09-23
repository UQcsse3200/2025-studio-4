package com.csse3200.game.components.mainmenu;

import com.csse3200.game.GdxGame;
import com.csse3200.game.components.Component;
import com.csse3200.game.services.SaveGameService;
import com.csse3200.game.services.SelectedHeroService;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class listens to events relevant to the Main Menu Screen and does something when one of the
 * events is triggered.
 */
public class MainMenuActions extends Component {
    private static final Logger logger = LoggerFactory.getLogger(MainMenuActions.class);
    private GdxGame game;

    public MainMenuActions(GdxGame game) {
        this.game = game;
    }

    @Override
    public void create() {
        entity.getEvents().addListener("start", this::onStart);
        entity.getEvents().addListener("continue", this::onContinue);
        entity.getEvents().addListener("exit", this::onExit);
        entity.getEvents().addListener("settings", this::onSettings);
        entity.getEvents().addListener("openHeroSelect", this::onOpenHeroSelect);
    }

    /**
     * Swaps to the Main Game screen.
     */
    private void onStart() {
        logger.info("Start game");
        game.setScreen(GdxGame.ScreenType.MAIN_GAME);
    }

    /**
     * Opens the save selection interface.
     * Users can choose which save file to load or delete.
     */
    private void onContinue() {
        logger.info("Opening save selection interface");
        game.setScreen(GdxGame.ScreenType.SAVE_SELECTION);
    }

    /**
     * Exits the game.
     */
    private void onExit() {
        logger.info("Exit game");
        game.exit();
    }

    /**
     * Swaps to the Settings screen.
     */
    private void onSettings() {
        logger.info("Launching settings screen");
        game.setScreen(GdxGame.ScreenType.SETTINGS);
    }

    private void onOpenHeroSelect() {
        game.setScreen(GdxGame.ScreenType.HERO_SELECT);
    }

}
