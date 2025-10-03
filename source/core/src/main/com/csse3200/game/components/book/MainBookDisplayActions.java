package com.csse3200.game.components.book;

import com.badlogic.gdx.audio.Sound;
import com.csse3200.game.GdxGame;
import com.csse3200.game.components.Component;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainBookDisplayActions extends Component {
    private static final Logger logger = LoggerFactory.getLogger(MainBookDisplayActions.class);
    private GdxGame game;
    private final String openBookSoundPath = "sounds/book_opening.mp3";
    public MainBookDisplayActions(GdxGame game) {
        this.game = game;
    }

    @Override
    public void create() {
        entity.getEvents().addListener("backToMain", this::backToMain);
        entity.getEvents().addListener("goToCurrency", this::goToCurrency);
        entity.getEvents().addListener("goToEnemy", this::goToEnemy);
        entity.getEvents().addListener("goToTower", this::goToTower);
    }

    private void backToMain() {
        logger.info("Returning to main menu");
        game.setScreen(GdxGame.ScreenType.MAIN_MENU);
    }

    private void goToCurrency() {
        logger.info("Go to currency page");
        game.setScreen(GdxGame.ScreenType.CURRENCY_BOOK);
        playCurrencySound(openBookSoundPath);
    }

    private void goToEnemy() {
        logger.info("Go to enemy page");
        game.setScreen(GdxGame.ScreenType.ENEMY_BOOK);
        playCurrencySound(openBookSoundPath);
    }

    private void goToTower() {
        logger.info("Go to tower page");
        game.setScreen(GdxGame.ScreenType.TOWER_BOOK);
        playCurrencySound(openBookSoundPath);
    }
    /**
     * Plays the collection sound associated with the given currency type.
     *
     * @param soundPath the sound should be played
     */
    private void playCurrencySound(String soundPath) {
        Sound sound = ServiceLocator.getResourceService().getAsset(soundPath, Sound.class);
        if (sound != null) {
            sound.play(1.0f);
        } else {
            System.out.println("Sound not found: " + soundPath);
        }
    }

}
