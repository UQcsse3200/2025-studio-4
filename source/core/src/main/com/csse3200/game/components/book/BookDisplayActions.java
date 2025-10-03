package com.csse3200.game.components.book;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.GdxGame;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.deck.DeckComponent;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

import java.util.Map;

public class BookDisplayActions extends Component {
    private static final Logger logger = LoggerFactory.getLogger(BookDisplayActions.class);
    private GdxGame game;
    private final String closeBookSoundPath = "sounds/book_closing.mp3";

    public BookDisplayActions(GdxGame game) {
        this.game = game;
    }

    @Override
    public void create() {
        entity.getEvents().addListener("backToMain", this::onExit);
    }

    private void onExit() {
        game.setScreen(GdxGame.ScreenType.BOOK);
        playCurrencySound(closeBookSoundPath);
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
