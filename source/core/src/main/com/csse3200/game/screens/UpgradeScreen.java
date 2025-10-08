package com.csse3200.game.screens;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.GdxGame;
import com.csse3200.game.components.currencysystem.UpgradeMenuDisplay;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.factories.RenderFactory;
import com.csse3200.game.input.InputDecorator;
import com.csse3200.game.input.InputService;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.rendering.Renderer;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpgradeScreen extends ScreenAdapter {
    private static final Logger logger = LoggerFactory.getLogger(UpgradeScreen.class);
    private static final String[] starScreenTextures = {
            "images/main_menu_background.png",
            "images/Main_Menu_Button_Background.png"
    };

    private final GdxGame game;
    private final Renderer renderer;

    public UpgradeScreen(GdxGame game) {
        this.game = game;

        logger.debug("Initialising star screen services");
        ServiceLocator.registerInputService(new InputService());
        ServiceLocator.registerResourceService(new ResourceService());
        ServiceLocator.registerEntityService(new EntityService());
        ServiceLocator.registerRenderService(new RenderService());
        ServiceLocator.registerTimeSource(new GameTime());

        renderer = RenderFactory.createRenderer();
        renderer.getCamera().getEntity().setPosition(5f, 5f);

        loadAssets();
        createUI();
    }

    @Override
    public void render(float delta) {
        ServiceLocator.getEntityService().update();
        renderer.render();
    }

    @Override
    public void resize(int width, int height) {
        renderer.resize(width, height);
    }

    @Override
    public void dispose() {
        logger.debug("Disposing upgrade screen");

        renderer.dispose();
        unloadAssets();
        ServiceLocator.getRenderService().dispose();
        ServiceLocator.getEntityService().dispose();
    }

    private void loadAssets() {
        logger.debug("Loading assets");
        ResourceService resourceService = ServiceLocator.getResourceService();
        resourceService.loadTextures(starScreenTextures);
        ServiceLocator.getResourceService().loadAll();
    }

    private void unloadAssets() {
        logger.debug("Unloading assets");
        ResourceService resourceService = ServiceLocator.getResourceService();
        resourceService.unloadAssets(starScreenTextures);
    }

    /**
     * Creates the setting screen's ui including components for rendering ui elements to the screen
     * and capturing and handling ui input.
     */
    private void createUI() {
        logger.debug("Creating ui");
        Stage stage = ServiceLocator.getRenderService().getStage();
        Entity ui = new Entity();
        ui.addComponent(new UpgradeMenuDisplay(game)).addComponent(new InputDecorator(stage, 10));
        ServiceLocator.getEntityService().register(ui);
    }
}
