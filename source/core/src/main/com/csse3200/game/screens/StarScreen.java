package com.csse3200.game.screens;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.GdxGame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StarScreen extends ScreenAdapter {
    private static final Logger logger = LoggerFactory.getLogger(StarScreen.class);

    private final GdxGame game;
    private Stage stage;

    public StarScreen(GdxGame game) { this.game = game; }

    @Override
    public void show() {
        stage = new Stage();
    }
}
