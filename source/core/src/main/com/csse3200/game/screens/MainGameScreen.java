package com.csse3200.game.screens;
import com.csse3200.game.services.leaderboard.InMemoryLeaderboardService;
import com.csse3200.game.services.leaderboard.LeaderboardService; // 如果用到了接口

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.GdxGame;
import com.csse3200.game.areas.ForestGameArea;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.components.maingame.MainGameActions;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.factories.RenderFactory;
import com.csse3200.game.input.InputComponent;
import com.csse3200.game.input.InputDecorator;
import com.csse3200.game.input.InputService;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.rendering.Renderer;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.terminal.Terminal;
import com.csse3200.game.ui.terminal.TerminalDisplay;
import com.csse3200.game.components.maingame.MainGameExitDisplay;
import com.csse3200.game.components.maingame.MainGameOver;
import com.csse3200.game.components.maingame.MainGameWin;
import com.csse3200.game.components.gamearea.PerformanceDisplay;
import com.csse3200.game.services.SaveGameService;
import com.csse3200.game.components.maingame.PauseMenuDisplay;
import com.csse3200.game.components.maingame.PauseInputComponent;
import com.csse3200.game.components.settingsmenu.SettingsMenuDisplay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The game screen containing the main game.
 *
 * <p>Details on libGDX screens: https://happycoding.io/tutorials/libgdx/game-screens
 */

public class MainGameScreen extends ScreenAdapter {
  private static final Logger logger = LoggerFactory.getLogger(MainGameScreen.class);

  private static final String[] mainGameTextures = {
          "images/heart.png",
          "images/pause_button.png",
          "images/dim_bg.jpeg",
          "images/Main_Menu_Button_Background.png",
          "images/scrap.png"
  };

  private static final Vector2 CAMERA_POSITION = new Vector2(7.5f, 7.5f);

  private final GdxGame game;
  private final Renderer renderer;
  private final PhysicsEngine physicsEngine;
  public static Entity ui;
  private SaveGameService saveGameService;

  public MainGameScreen(GdxGame game) {
    this(game, false);
  }
  
  public MainGameScreen(GdxGame game, boolean isContinue) {
    this(game, isContinue, null);
  }

  public MainGameScreen(GdxGame game, boolean isContinue, String saveFileName) {
    this.game = game;
    ServiceLocator.registerGameService(game);
    ServiceLocator.registerLeaderboardService(
            new InMemoryLeaderboardService("player-001"));

    logger.debug("Initialising main game screen services (Continue: {}, Save: {})", isContinue, saveFileName);

    ServiceLocator.registerTimeSource(new GameTime());

    PhysicsService physicsService = new PhysicsService();
    ServiceLocator.registerPhysicsService(physicsService);
    physicsEngine = physicsService.getPhysics();

    ServiceLocator.registerInputService(new InputService());
    ServiceLocator.registerResourceService(new ResourceService());

    ServiceLocator.registerEntityService(new EntityService());
    ServiceLocator.registerRenderService(new RenderService());



      saveGameService = new SaveGameService(ServiceLocator.getEntityService());

    renderer = RenderFactory.createRenderer();
    renderer.getCamera().getEntity().setPosition(CAMERA_POSITION);
    renderer.getDebug().renderPhysicsWorld(physicsEngine.getWorld());

    loadAssets();
    ui = createUI();

    logger.debug("Initialising main game screen entities");
    
    // Handle save loading first
    boolean hasExistingPlayer = false;
    if (isContinue && saveFileName != null) {
      logger.info("Loading specific save file: {}", saveFileName);
      boolean success = saveGameService.loadGame(saveFileName);
      if (success) {
        logger.info("Save file loaded successfully");
        hasExistingPlayer = true;
      } else {
        logger.warn("Failed to load save file, starting new game");
      }
    } else if (isContinue && saveGameService.hasSaveFile()) {
      logger.info("Loading default saved game state for continue");
      boolean success = saveGameService.loadGame();
      if (success) {
        hasExistingPlayer = true;
      }
    } else if (!isContinue) {
      logger.info("Creating new player for new game");
    }
    
    // Create game area after loading save (or for new game)
    TerrainFactory terrainFactory = new TerrainFactory(renderer.getCamera());
    ForestGameArea forestGameArea = new ForestGameArea(terrainFactory);
    
    // Pass information about existing player to game area
    forestGameArea.setHasExistingPlayer(hasExistingPlayer);
    forestGameArea.create();
  }

  @Override
  public void render(float delta) {
    physicsEngine.update();
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
    logger.info("Game paused");
  }

  @Override
  public void resume() {
    logger.info("Game resumed");
  }

  @Override
  public void dispose() {
    logger.debug("Disposing main game screen");

    renderer.dispose();
    unloadAssets();

    ServiceLocator.getEntityService().dispose();
    ServiceLocator.getRenderService().dispose();
    ServiceLocator.getResourceService().dispose();

    ServiceLocator.clear();
  }

  private void loadAssets() {
    logger.debug("Loading assets");
    ResourceService resourceService = ServiceLocator.getResourceService();
    resourceService.loadTextures(mainGameTextures);
    ServiceLocator.getResourceService().loadAll();
  }

  private void unloadAssets() {
    logger.debug("Unloading assets");
    ResourceService resourceService = ServiceLocator.getResourceService();
    resourceService.unloadAssets(mainGameTextures);
  }

  /**
   * Creates the main game's ui including components for rendering ui elements to the screen and
   * capturing and handling ui input.
   */
  private Entity createUI() {
    logger.debug("Creating ui");
    Stage stage = ServiceLocator.getRenderService().getStage();
    InputComponent inputComponent =
        ServiceLocator.getInputService().getInputFactory().createForTerminal();

// AFTER
    Entity ui = new Entity();
    ui.addComponent(new InputDecorator(stage, 10))
            .addComponent(new PerformanceDisplay())
            .addComponent(new MainGameActions(this.game))
            .addComponent(new SettingsMenuDisplay(this.game, true))
            .addComponent(new PauseMenuDisplay(this.game))
            .addComponent(new PauseInputComponent())
            .addComponent(new MainGameExitDisplay())
            .addComponent(new Terminal())
            .addComponent(inputComponent)
            .addComponent(new TerminalDisplay());

    ServiceLocator.getEntityService().register(ui);
    ui.addComponent(new com.csse3200.game.ui.leaderboard.LeaderboardUI());
    return ui;
  }
}
