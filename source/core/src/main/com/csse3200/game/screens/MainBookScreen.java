package com.csse3200.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.GdxGame;
import com.csse3200.game.components.book.MainBookDisplay;
import com.csse3200.game.components.book.MainBookDisplayActions;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.input.InputDecorator;
import com.csse3200.game.input.InputService;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainBookScreen extends ScreenAdapter {

    private static final Logger logger = LoggerFactory.getLogger(MainBookScreen.class);

    private final GdxGame game;
    private Stage stage;
    private static boolean hasRegisteredMusic = false;
    private static boolean isPlayingMusic = false;

    public MainBookScreen(GdxGame game) {
        this.game = game;
        if (isPlayingMusic == false && hasRegisteredMusic == false) {
            ServiceLocator.getAudioService().registerMusic("book_bgm", "sounds/book_theme.mp3");
            ServiceLocator.getAudioService().playMusic("book_bgm", true);
            isPlayingMusic = true;
            hasRegisteredMusic = true;
        } else if (isPlayingMusic == false && GdxGame.musicON == 0) {
            ServiceLocator.getAudioService().resumeMusic();
            isPlayingMusic = true;
        } else if (isPlayingMusic == false && GdxGame.musicON == 1) {
            ServiceLocator.getAudioService().playMusic("book_bgm", true);
            isPlayingMusic = true;
            GdxGame.musicON = 0;
        }
    }

    @Override
    public void show() {
        logger.debug("Initialising Book services");

        // Core services
        ServiceLocator.registerRenderService(new RenderService());
        ServiceLocator.registerEntityService(new EntityService());
        ServiceLocator.registerInputService(new InputService());
        ServiceLocator.registerResourceService(new ResourceService());

        // PRELOAD shared background and button images
        String[] textures = {
            "images/book/encyclopedia_theme.png",
            "images/book/enemies_book.png",
            "images/book/currencies_book.png",
            "images/book/towers_book.png",
            "images/score_trophy.png",
            "images/book/hologram.png"
        };
        ServiceLocator.getResourceService().loadTextures(textures);
        ServiceLocator.getResourceService().loadAll();

        // Stage (no custom renderer/RenderFactory needed)
        stage = new Stage();
        ServiceLocator.getRenderService().setStage(stage);

        // UI entity (display + actions)
        Entity ui = new Entity()
                .addComponent(new MainBookDisplay())
                .addComponent(new MainBookDisplayActions(game));
        ServiceLocator.getEntityService().register(ui);

        // Route input to Stage via InputService (matches your project pattern)
        ServiceLocator.getInputService().register(new InputDecorator(stage, 10));
    }

    @Override
    public void render(float delta) {
        // Update ECS + Stage, then draw
        ServiceLocator.getEntityService().update();
        stage.act(delta > 0 ? delta : Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        if (stage != null) {
            stage.getViewport().update(width, height, true);
        }
    }

    @Override
    public void hide() {
        // optional: keep Stage alive between show/hide cycles if your screens reuse services
    }

    @Override
    public void dispose() {
        logger.debug("Disposing MapSelectionScreen");
        // Dispose in a safe order
        if (stage != null) {
            stage.dispose();
            stage = null;
        }
        ServiceLocator.getRenderService().dispose();
        ServiceLocator.getEntityService().dispose();
        GdxGame.musicON = 0;
        isPlayingMusic = false;
        // NOTE: your InputService likely has no dispose(); do not call it
        //ServiceLocator.getResourceService().dispose();
        //ServiceLocator.clear();
    }
}
