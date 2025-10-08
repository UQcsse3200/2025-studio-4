package com.csse3200.game.components.book;

import com.badlogic.gdx.audio.Sound;
import com.csse3200.game.GdxGame;
import com.csse3200.game.components.Component;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Component responsible for handling actions in the book display UI.
 * <p>
 * This includes listening for events such as exiting the book and playing
 * associated sounds.
 */
public class BookDisplayActions extends Component {
    private static final Logger logger = LoggerFactory.getLogger(BookDisplayActions.class);

    /** Reference to the main game to switch screens. */
    private final GdxGame game;

    /**
     * Constructs a BookDisplayActions component.
     *
     * @param game the main game instance
     */
    public BookDisplayActions(GdxGame game) {
        this.game = game;
    }

    /**
     * Called when the component is created. Registers event listeners
     * for book actions.
     */
    @Override
    public void create() {
        entity.getEvents().addListener("backToMain", this::onExit);
    }

    /**
     * Handles the "backToMain" event. Switches the screen to the book
     * and plays the book closing sound.
     */
    private void onExit() {
        logger.debug("Exit book");
        game.setScreen(GdxGame.ScreenType.BOOK);
        String closeBookSoundPATH = "sounds/book_closing.mp3";
        playSound(closeBookSoundPATH);
    }

    /**
     * Plays the sound effect.
     *
     * @param soundPath the sound should be played
     */
    private void playSound(String soundPath) {
        Sound sound = ServiceLocator.getResourceService().getAsset(soundPath, Sound.class);
        if (sound != null) {
            sound.play(1.0f);
        } else {
            System.out.println("Sound not found: " + soundPath);
        }
    }
}
