package com.csse3200.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

/**
 * Centralized accessor for the shared "book-style" UI skin.
 * Ensures consistency across menus like Book, Pause, and Map Select.
 */
public class BookStyleHelper {
    private static Skin skin;

    public static Skin getSkin() {
        if (skin == null) {
            // use same skin path as BookMenu
            skin = new Skin(Gdx.files.internal("flat-earth/skin/flat-earth-ui.json"));
        }
        return skin;
    }
}
