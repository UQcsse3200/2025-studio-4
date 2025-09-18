package com.csse3200.game.screens;
import com.csse3200.game.services.leaderboard.InMemoryLeaderboardService;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

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
import com.csse3200.game.services.GameStateService;
import com.csse3200.game.services.PlayerNameService;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.terminal.Terminal;
import com.csse3200.game.ui.terminal.TerminalDisplay;
import com.csse3200.game.components.maingame.MainGameExitDisplay;
import com.csse3200.game.screens.GameOverScreen;
import com.csse3200.game.components.maingame.MainGameWin;
import com.csse3200.game.components.gamearea.PerformanceDisplay;

import com.csse3200.game.components.maingame.PauseMenuDisplay;
import com.csse3200.game.components.maingame.PauseInputComponent;
import com.csse3200.game.components.maingame.SaveMenuDisplay;
import com.csse3200.game.components.settingsmenu.SettingsMenuDisplay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

public class MainGameScreen extends ScreenAdapter {
  private static final Logger logger = LoggerFactory.getLogger(MainGameScreen.class);

  private static final String[] mainGameTextures = {
          "images/heart.png",
          "images/pause_button.png",
          "images/dim_bg.jpeg",
          "images/Main_Menu_Button_Background.png",
          "images/Main_Game_Button.png",
          "images/scrap.png",
          "images/Game_Over.png",
<<<<<<< HEAD
          "images/score_trophy.png"
=======
          "images/Game_Victory.png"
>>>>>>> origin/main
  };

  private static final Vector2 CAMERA_POSITION = new Vector2(7.5f, 7.5f);

  private final GdxGame game;
  private final Renderer renderer;
  private final PhysicsEngine physicsEngine;
  public static Entity ui;
  // private SaveGameService saveGameService;

  // NEW: carry the intent and arg generically (save name for Continue, mapId for New Game)
  private final boolean isContinue;
  private final String startupArg;

  public MainGameScreen(GdxGame game) {
    this(game, false);
  }

  public MainGameScreen(GdxGame game, boolean isContinue) {
    this(game, isContinue, null);
  }

  public MainGameScreen(GdxGame game, boolean isContinue, String saveFileName) {
    this.game = game;
    this.isContinue = isContinue;
    this.startupArg = saveFileName; // mapId when new game, save name when continue

    ServiceLocator.registerGameService(game);
    ServiceLocator.registerLeaderboardService(
            new InMemoryLeaderboardService("player-001"));

<<<<<<< HEAD
    // Re-register GameStateService since it was cleared by previous screen disposal
    if (ServiceLocator.getGameStateService() == null) {
      ServiceLocator.registerGameStateService(new GameStateService());
    }
    
    // Re-register PlayerNameService, but preserve any existing name from the game instance
    if (ServiceLocator.getPlayerNameService() == null) {
      // Try to get the player name from the game instance if it was stored there
      String playerName = (game instanceof GdxGame) ? ((GdxGame) game).getStoredPlayerName() : null;
      if (playerName != null && !playerName.isEmpty()) {
        ServiceLocator.registerPlayerNameService(new PlayerNameService(playerName));
      } else {
        ServiceLocator.registerPlayerNameService(new PlayerNameService());
      }
    }

    logger.debug("Initialising main game screen services (Continue: {}, Save: {})", isContinue, saveFileName);
=======
    logger.debug("Initialising main game screen services (Continue: {}, Save/Arg: {})", isContinue, saveFileName);
>>>>>>> origin/main

    ServiceLocator.registerTimeSource(new GameTime());

    PhysicsService physicsService = new PhysicsService();
    ServiceLocator.registerPhysicsService(physicsService);
    physicsEngine = physicsService.getPhysics();

    ServiceLocator.registerInputService(new InputService());
    ServiceLocator.registerResourceService(new ResourceService());

    ServiceLocator.registerEntityService(new EntityService());
    ServiceLocator.registerRenderService(new RenderService());

    // Switched to SimpleSaveService
    var simpleSave = new com.csse3200.game.services.SimpleSaveService(ServiceLocator.getEntityService());

    renderer = RenderFactory.createRenderer();
    renderer.getCamera().getEntity().setPosition(CAMERA_POSITION);
    renderer.getDebug().renderPhysicsWorld(physicsEngine.getWorld());
<<<<<<< HEAD
    // Display collision volume
    //renderer.getDebug().setActive(true);

=======
    // renderer.getDebug().setActive(true); // collision debug
>>>>>>> origin/main

    loadAssets();
    ui = createUI();

    logger.debug("Initialising main game screen entities");

    // Handle save loading first
    boolean hasExistingPlayer = false;
    if (isContinue && startupArg != null) {
      logger.info("Loading specific save file: {}", startupArg);
      boolean success = simpleSave.loadToPending();
      if (success) {
        logger.info("Save file loaded successfully");
        hasExistingPlayer = true;
      } else {
        logger.warn("Failed to load save file, starting new game");
      }
    } else if (isContinue) {
      logger.info("Loading default saved game state for continue");
      boolean success = simpleSave.loadToPending();
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

    // NEW: If this is a NEW game and we have a selected map id, try to forward it to the area.
    // We support either setMapPath(String) or setMapId(String) if your area exposes them.
    if (!isContinue) {
      String chosenMapPath = resolveNewGameMapPath();
      if (chosenMapPath != null) {
        boolean applied = tryApplyMapToArea(forestGameArea, chosenMapPath);
        if (applied) {
          logger.info("Starting NEW game on map: {}", chosenMapPath);
        } else {
          logger.info("Area has no map setter; using its default map. (Wanted: {})", chosenMapPath);
        }
      }
    }

    // If continuing from a save, avoid auto-starting waves to prevent duplicate enemies.
    forestGameArea.setAutoStartWaves(!hasExistingPlayer);
    forestGameArea.create();

    // After game area and assets are ready, apply pending save restoration if any
    if (hasExistingPlayer) {
      // Use area waypoints when restoring to avoid any initial drift
      java.util.List<com.csse3200.game.entities.Entity> canonical = forestGameArea.getWaypointList();
      boolean applied = (canonical != null && !canonical.isEmpty())
              ? simpleSave.applyPendingRestoreWithWaypoints(canonical)
              : simpleSave.applyPendingRestore();
      if (applied) {
        logger.info("Applied pending save restoration after game area creation");
        // No longer need extra rebind pass; restore already bound with canonical waypoints
      } else {
        logger.warn("No pending restoration to apply or failed to apply");
      }
    }
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

    //ServiceLocator.clear();
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
    // Ensure music stops when leaving the main game to avoid overlap on re-entry
    if (ServiceLocator.getAudioService() != null) {
      ServiceLocator.getAudioService().stopMusic();
    }
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

    Entity ui = new Entity();
    ui.addComponent(new InputDecorator(stage, 10))
            .addComponent(new PerformanceDisplay())
            .addComponent(new MainGameActions(this.game))
            .addComponent(new SettingsMenuDisplay(this.game, true))
            .addComponent(new PauseMenuDisplay(this.game))
            .addComponent(new SaveMenuDisplay(this.game))
            .addComponent(new PauseInputComponent())
            .addComponent(new MainGameExitDisplay())
            .addComponent(new GameOverScreen())
            .addComponent(new MainGameWin())
            .addComponent(new Terminal())
            .addComponent(inputComponent)
            .addComponent(new TerminalDisplay());

    ServiceLocator.getEntityService().register(ui);
    return ui;
  }

  // -----------------------
  // NEW: tiny helpers below
  // -----------------------

  /**
   * Resolve a TMX path for a NEW game using the selected map id (filename w/o extension).
   * Falls back to the first file in assets/maps, then to a safe default.
   */
  private String resolveNewGameMapPath() {
    // When continuing, we don't select a new map
    if (isContinue) return null;

    // Prefer the selected mapId (e.g., "forest_01")
    if (startupArg != null && !startupArg.isBlank()) {
      String candidate = "maps/" + startupArg + ".tmx";
      if (Gdx.files.internal(candidate).exists()) {
        return candidate;
      }
      logger.warn("Selected map '{}' not found at {}", startupArg, candidate);
    }

    // Fallback: first .tmx under assets/maps/
    FileHandle dir = Gdx.files.internal("maps");
    if (dir.exists() && dir.isDirectory()) {
      for (FileHandle f : dir.list()) {
        if ("tmx".equalsIgnoreCase(f.extension())) {
          return f.path();
        }
      }
    }

    // Last resort: keep your current default (change if your project uses another)
    return "maps/forest.tmx";
  }

  /**
   * Try to set the map on the area via reflection, so we don't require code changes elsewhere.
   * Supports either setMapPath(String) or setMapId(String). Returns true if applied.
   */
  private boolean tryApplyMapToArea(Object area, String mapPath) {
    try {
      // Preferred: setMapPath(String)
      Method m = area.getClass().getMethod("setMapPath", String.class);
      m.invoke(area, mapPath);
      return true;
    } catch (ReflectiveOperationException ignored) {
      // Try alternative: setMapId(String) with filename w/o extension
      try {
        String fileName = mapPath;
        int slash = fileName.lastIndexOf('/');
        if (slash >= 0) fileName = fileName.substring(slash + 1);
        int dot = fileName.lastIndexOf('.');
        String mapId = (dot > 0) ? fileName.substring(0, dot) : fileName;

        Method m2 = area.getClass().getMethod("setMapId", String.class);
        m2.invoke(area, mapId);
        return true;
      } catch (ReflectiveOperationException ignoredToo) {
        return false;
      }
    }
  }
}
