package com.csse3200.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.GdxGame;
import com.csse3200.game.components.book.*;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.input.InputDecorator;
import com.csse3200.game.input.InputService;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Screen that displays the in-game book UI.
 * <p>
 * Depending on the {@link BookPage} type, this screen shows either the main book menu
 * or a specific book page (Tower, Enemy, or Currency).
 * It initializes required ECS services, stage, and input handling.
 */
public class BookScreen extends ScreenAdapter {
    private static final Logger logger = LoggerFactory.getLogger(BookScreen.class);

    /** Reference to the main game class */
    private final GdxGame game;

    /** Stage used for rendering UI elements */
    private Stage stage;

    /** The type of book page to display */
    private final BookPage bookPage;

    /**
     * Constructs a BookScreen for a specific book page.
     *
     * @param game the main GdxGame instance
     * @param bookType the type of book page to display (TOWER_PAGE, ENEMY_PAGE, CURRENCY_PAGE, HERO_PAGE, NONE)
     */
    public BookScreen(GdxGame game, BookPage bookType) {
        this.game = game;
        this.bookPage = bookType;
        ServiceLocator.getAudioService().resumeMusic();
    }

    /**
     * Called when this screen becomes the current screen.
     * Initializes all required ECS services, preloads textures, sets up the stage,
     * registers the book UI entity, and binds input to the stage.
     */
    @Override
    public void show() {
        logger.debug("Initialising Book services");

        // Core services
        ServiceLocator.registerRenderService(new RenderService());
        ServiceLocator.registerEntityService(new EntityService());
        ServiceLocator.registerInputService(new InputService());
        ServiceLocator.registerResourceService(new ResourceService());

        // PRELOAD shared background (same as other menu screens)
        String[] textures = {"images/book/open_book_theme.png"};
        ServiceLocator.getResourceService().loadTextures(textures);
        ServiceLocator.getResourceService().loadAll();

        // Stage (no custom renderer/RenderFactory needed)
        stage = new Stage();
        ServiceLocator.getRenderService().setStage(stage);

        // UI entity (display + actions)
        Entity ui = new Entity();
        if (this.bookPage != BookPage.NONE) { // Tower/Enemy/Currency book page goes here
            ui
                    .addComponent(new BookDisplay(bookPage))
                    .addComponent(new BookDisplayActions(game));
        } else {
            ui
                    .addComponent(new MainBookDisplay())
                    .addComponent(new MainBookDisplayActions(game));
        }
        ServiceLocator.getEntityService().register(ui);

        // Route input to Stage via InputService (matches your project pattern)
        ServiceLocator.getInputService().register(new InputDecorator(stage, 10));
    }

    /**
     * Called every frame to update and render the screen.
     *
     * @param delta the time in seconds since the last render
     */
    @Override
    public void render(float delta) {
        // Update ECS + Stage, then draw
        ServiceLocator.getEntityService().update();
        stage.act(delta > 0 ? delta : Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    /**
     * Called when the screen is resized.
     * Updates the stage viewport to match new width and height.
     *
     * @param width  new width in pixels
     * @param height new height in pixels
     */
    @Override
    public void resize(int width, int height) {
        if (stage != null) {
            stage.getViewport().update(width, height, true);
        }
    }

    /**
     * Called when this screen is no longer the current screen.
     * Currently, no cleanup is done here; Stage remains alive between show/hide cycles.
     */
    @Override
    public void hide() {
        // optional: keep Stage alive between show/hide cycles if your screens reuse services
    }


    /**
     * Disposes of the screen and its resources.
     * Cleans up the stage and disposes ECS services in a safe order.
     */
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
    }
}
