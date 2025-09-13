package com.csse3200.game.screens;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.GdxGame;
import com.csse3200.game.components.mainmenu.SaveSelectionActions;
import com.csse3200.game.components.mainmenu.SaveSelectionDisplay;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.factories.RenderFactory;
import com.csse3200.game.input.InputDecorator;
import com.csse3200.game.input.InputService;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.rendering.Renderer;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The game screen containing the save selection interface.
 * Allows users to select, load, or delete save files.
 */
public class SaveSelectionScreen extends ScreenAdapter {
  private static final Logger logger = LoggerFactory.getLogger(SaveSelectionScreen.class);
  private static final String[] saveSelectionTextures = {
    "images/main_menu_background.png",
    "images/Main_Menu_Button_Background.png"
  };

  private final GdxGame game;
  private final Renderer renderer;

  public SaveSelectionScreen(GdxGame game) {
    this.game = game;

    logger.debug("Initialising save selection screen services");
    ServiceLocator.registerInputService(new InputService());
    ServiceLocator.registerResourceService(new ResourceService());
    ServiceLocator.registerEntityService(new EntityService());
    ServiceLocator.registerRenderService(new RenderService());

    renderer = RenderFactory.createRenderer();

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
    logger.trace("Resized renderer: ({} x {})", width, height);
  }

  @Override
  public void pause() {
    logger.info("Save selection screen paused");
  }

  @Override
  public void resume() {
    logger.info("Save selection screen resumed");
  }

  @Override
  public void dispose() {
    logger.debug("Disposing save selection screen");
    
    renderer.dispose();
    unloadAssets();
    ServiceLocator.getRenderService().dispose();
    ServiceLocator.getEntityService().dispose();

    ServiceLocator.clear();
  }

  private void loadAssets() {
    logger.debug("Loading assets");
    ResourceService resourceService = ServiceLocator.getResourceService();
    resourceService.loadTextures(saveSelectionTextures);
    ServiceLocator.getResourceService().loadAll();
  }

  private void unloadAssets() {
    logger.debug("Unloading assets");
    ResourceService resourceService = ServiceLocator.getResourceService();
    resourceService.unloadAssets(saveSelectionTextures);
  }

  /**
   * Creates the save selection screen's UI including components for rendering UI elements
   * and capturing and handling UI input.
   */
  private void createUI() {
    logger.debug("Creating UI");
    Stage stage = ServiceLocator.getRenderService().getStage();
    Entity ui = new Entity();
    ui.addComponent(new SaveSelectionDisplay())
        .addComponent(new SaveSelectionActions(game))
        .addComponent(new InputDecorator(stage, 10));
    ServiceLocator.getEntityService().register(ui);
  }
}
