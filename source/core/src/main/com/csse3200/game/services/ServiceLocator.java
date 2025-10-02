package com.csse3200.game.services;

import com.csse3200.game.GdxGame;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.input.InputService;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.rendering.RenderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simplified implementation of the Service Locator pattern:
 * https://martinfowler.com/articles/injection.html#UsingAServiceLocator
 *
 * <p>Allows global access to a few core game services.
 * Warning: global access is a trap and should be used <i>extremely</i> sparingly.
 * Read the wiki for details (https://github.com/UQcsse3200/game-engine/wiki/Service-Locator).
 */
public class ServiceLocator {
  private static final Logger logger = LoggerFactory.getLogger(ServiceLocator.class);

  private static EntityService entityService;
  private static RenderService renderService;
  private static PhysicsService physicsService;
  private static GameTime timeSource;
  private static InputService inputService;
  private static ResourceService resourceService;
  private static GdxGame gameService;
  private static GameStateService gameStateService;private static SelectedHeroService selectedHeroService;

    // NEW: centralised audio service
  private static AudioService audioService;

  // Leaderboard
  private static com.csse3200.game.services.leaderboard.LeaderboardService leaderboardService;

    // --- Getters ---
  public static EntityService getEntityService() {
    return entityService;
  }

    public static RenderService getRenderService() {
        return renderService;
    }

    public static PhysicsService getPhysicsService() {
        return physicsService;
    }

    public static GameTime getTimeSource() {
        return timeSource;
    }

    public static InputService getInputService() {
        return inputService;
    }

    public static ResourceService getResourceService() {
        return resourceService;
    }

    public static GdxGame getGameService() {
        return gameService;
    }

  public static GameStateService getGameStateService() {
    return gameStateService;
  }

  // NEW: Audio getter
  public static AudioService getAudioService() {
    return audioService;
  }

    public static com.csse3200.game.services.leaderboard.LeaderboardService getLeaderboardService() {
    return leaderboardService;
  }

  // --- Registrations ---
  public static void registerEntityService(EntityService service) {
    logger.debug("Registering entity service {}", service);
    entityService = service;
  }

  public static void registerRenderService(RenderService service) {
    logger.debug("Registering render service {}", service);
    renderService = service;
  }

    public static void registerPhysicsService(PhysicsService service) {
        logger.debug("Registering physics service {}", service);
        physicsService = service;
    }

    public static void registerTimeSource(GameTime source) {
        logger.debug("Registering time source {}", source);
        timeSource = source;
    }

    public static void registerInputService(InputService source) {
        logger.debug("Registering input service {}", source);
        inputService = source;
    }

    public static void registerResourceService(ResourceService source) {
        logger.debug("Registering resource service {}", source);
        resourceService = source;
    }

    public static void registerGameService(GdxGame source) {
        logger.debug("Registering game service {}", source);
        gameService = source;
    }

    public static void registerGameStateService(GameStateService source) {
        logger.debug("Registering game state service {}", source);
        gameStateService = source;
    }

  // NEW: Audio registration
  public static void registerAudioService(AudioService source) {
    logger.debug("Registering audio service {}", source);
    audioService = source;
  }

  public static void registerLeaderboardService(
          com.csse3200.game.services.leaderboard.LeaderboardService service) {
    logger.debug("Registering leaderboard service {}", service);
    leaderboardService = service;
  }

  // --- Teardown ---
  public static void clear() {
    entityService = null;
    renderService = null;
    physicsService = null;
    timeSource = null;
    inputService = null;
    resourceService = null;
    gameService = null;
    audioService = null;     // ensure audio is cleared
    leaderboardService = null;
  }
    // === NEW: 注册和获取 SelectedHeroService ===
    public static void registerSelectedHeroService(SelectedHeroService service) {
        logger.debug("Registering SelectedHeroService {}", service);
        selectedHeroService = service;
    }

    public static SelectedHeroService getSelectedHeroService() {
        return selectedHeroService;
    }

    private ServiceLocator() {
        throw new IllegalStateException("Instantiating static util class");
    }
}
