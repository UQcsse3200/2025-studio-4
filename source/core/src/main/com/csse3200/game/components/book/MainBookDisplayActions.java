package com.csse3200.game.components.book;

import com.badlogic.gdx.audio.Sound;
import com.csse3200.game.GdxGame;
import com.csse3200.game.components.Component;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles main book UI navigation actions. This component listens for events
 * triggered by the book UI and updates the game's screen accordingly. It also
 * plays sound effects when navigating to specific pages.
 *
 * <p>Supported events:</p>
 * <ul>
 *     <li>{@code backToMain} - returns to the main menu</li>
 *     <li>{@code goToCurrency} - navigates to the currency book page</li>
 *     <li>{@code goToEnemy} - navigates to the enemy book page</li>
 *     <li>{@code goToTower} - navigates to the tower book page</li>
 * </ul>
 */
public class MainBookDisplayActions extends Component {
    private static final Logger logger = LoggerFactory.getLogger(MainBookDisplayActions.class);
    /** Reference to the main game instance to switch screens. */
    private GdxGame game;
    /** Path to the sound effect played when opening the book. */
    private static final String openBookSoundPath = "sounds/book_opening.mp3";

    /**
     * Constructs a new {@code MainBookDisplayActions} with a reference to the game.
     *
     * @param game the main {@link GdxGame} instance
     */
    public MainBookDisplayActions(GdxGame game) {
        this.game = game;
    }

    /**
     * Registers event listeners for book UI actions.
     * This method is called when the component is created.
     */
    @Override
    public void create() {
        entity.getEvents().addListener("backToMain", this::backToMain);
        entity.getEvents().addListener("goToCurrency", this::goToCurrency);
        entity.getEvents().addListener("goToEnemy", this::goToEnemy);
        entity.getEvents().addListener("goToTower", this::goToTower);
    }

    /**
     * Event handler for returning to the main menu screen.
     */
    private void backToMain() {
        logger.info("Returning to main menu");
        game.setScreen(GdxGame.ScreenType.MAIN_MENU);
    }

    /**
     * Event handler for navigating to the currency book page and
     * playing the open book sound effect.
     */
    private void goToCurrency() {
        logger.info("Go to currency page");
        game.setScreen(GdxGame.ScreenType.CURRENCY_BOOK);
        playCurrencySound(openBookSoundPath);
    }

    /**
     * Event handler for navigating to the enemy book page and
     * playing the open book sound effect.
     */
    private void goToEnemy() {
        logger.info("Go to enemy page");
        game.setScreen(GdxGame.ScreenType.ENEMY_BOOK);
        playCurrencySound(openBookSoundPath);
    }

    /**
     * Event handler for navigating to the tower book page and
     * playing the open book sound effect.
     */
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
            logger.info("Sound not found: " + soundPath);
        }
    }

}
