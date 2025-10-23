package com.csse3200.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

/**
 * Centralized accessor for the Flat Earth UI skin.
 * Avoids duplicate Skin loading across screens.
 */
public class FlatEarthStyleHelper {
    private static Skin skin;

    public static Skin getSkin() {
        if (skin == null) {
            skin = new Skin(Gdx.files.internal("flat-earth/skin/flat-earth-ui.json"));
        }
        return skin;
    }
}
