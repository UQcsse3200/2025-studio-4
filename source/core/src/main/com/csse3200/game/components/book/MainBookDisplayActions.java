package com.csse3200.game.components.book;

import com.csse3200.game.GdxGame;
import com.csse3200.game.components.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainBookDisplayActions extends Component {
    private static final Logger logger = LoggerFactory.getLogger(MainBookDisplayActions.class);
    private GdxGame game;

    public MainBookDisplayActions(GdxGame game) {
        this.game = game;
    }

    @Override
    public void create() {
        entity.getEvents().addListener("backToMain", this::backToMain);
        entity.getEvents().addListener("goToCurrency", this::goToCurrency);
    }

    private void backToMain() {
        logger.info("Returning to main menu");
        game.setScreen(GdxGame.ScreenType.MAIN_MENU);
    }

    private void goToCurrency() {
        logger.info("Go to currency");
        game.setScreen(GdxGame.ScreenType.CURRENCY_BOOK);
    }
}
