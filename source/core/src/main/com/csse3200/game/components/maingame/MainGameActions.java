package com.csse3200.game.components.maingame;

import com.csse3200.game.GdxGame;
import com.csse3200.game.components.Component;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.services.leaderboard.LeaderboardService;
import com.csse3200.game.services.leaderboard.InMemoryLeaderboardService;
import com.csse3200.game.ui.leaderboard.LeaderboardController;
import com.csse3200.game.ui.leaderboard.LeaderboardPopup;
import com.csse3200.game.ui.leaderboard.MinimalSkinFactory;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class listens to events relevant to the Main Game Screen and does something when one of the
 * events is triggered.
 */
public class MainGameActions extends Component {
  private static final Logger logger = LoggerFactory.getLogger(MainGameActions.class);
  private GdxGame game;

  public MainGameActions(GdxGame game) {
    this.game = game;
  }

  @Override
  public void create() {
    entity.getEvents().addListener("exit", this::onExit);
    entity.getEvents().addListener("gameover", this::onExit);
    entity.getEvents().addListener("gamewin", this::onExit);
    entity.getEvents().addListener("restart", this::onRestart);
    entity.getEvents().addListener("save", this::onSave);
    entity.getEvents().addListener("togglePause", this::onTogglePause);
    entity.getEvents().addListener("resume", this::onResume);
    entity.getEvents().addListener("openSettings", this::onOpenSettings);
    entity.getEvents().addListener("quitToMenu", this::onQuitToMenu);
    entity.getEvents().addListener("showRanking", this::onShowRanking);
    entity.getEvents().addListener("awardStars", this::awardStars);
  }

  private boolean isPaused = false;

  private void onTogglePause() {
    if (isPaused) {
      onResume();
    } else {
      onPause();
    }
  }

  private void onPause() {
    ServiceLocator.getTimeSource().setTimeScale(0f);
    isPaused = true;
    entity.getEvents().trigger("showPauseUI"); // tells UI to show overlay
  }

  private void onResume() {
    ServiceLocator.getTimeSource().setTimeScale(1f);
    isPaused = false;
    entity.getEvents().trigger("hidePauseUI"); // tells UI to hide overlay
  }

  private void onOpenSettings() {
    // Opens the same Settings screen you use from main menu
    game.setScreen(GdxGame.ScreenType.SETTINGS);
  }

  private void onQuitToMenu() {
    game.setScreen(GdxGame.ScreenType.MAIN_MENU);
  }

  /**
   * Shows the ranking/leaderboard popup
   */
  private void onShowRanking() {
    logger.info("Showing ranking/leaderboard");
    
    try {
      Stage stage = ServiceLocator.getRenderService().getStage();
      Skin skin = MinimalSkinFactory.create();
      
      // Ensure leaderboard service is available
      if (ServiceLocator.getLeaderboardService() == null) {
        ServiceLocator.registerLeaderboardService(new InMemoryLeaderboardService("player-001"));
      }
      
      // Record current game pause state
      boolean wasGamePaused = isPaused;
      
      LeaderboardService leaderboardService = ServiceLocator.getLeaderboardService();
      LeaderboardController controller = new LeaderboardController(leaderboardService);
      LeaderboardPopup popup = new LeaderboardPopup(skin, controller);
      
      // Set close callback to restore game state
      popup.setOnCloseCallback(() -> {
        try {
          // Only resume if game wasn't already paused
          if (!wasGamePaused) {
            onResume();
            logger.info("Game resumed after closing leaderboard");
          }
        } catch (Exception e) {
          logger.error("Error resuming game after leaderboard close", e);
        }
      });
      
      // Pause game if it wasn't already paused
      if (!wasGamePaused) {
        onPause();
        logger.info("Game paused for leaderboard");
      }
      
      popup.showOn(stage);
      
    } catch (Exception e) {
      logger.error("Error showing ranking", e);
    }
  }

  /**
   * Restarts the game by creating a new MainGameScreen.
   */
  private void onRestart() {
    logger.info("Restarting game");
    game.setScreen(GdxGame.ScreenType.MAIN_GAME, false);
  }

  /**
   * Swaps to the Main Menu screen.
   */
  private void onExit() {
    logger.info("Exiting main game screen");
    game.setScreen(GdxGame.ScreenType.MAIN_MENU);
  }
  /**
   * Saves the current game state.
   */
  private void onSave() {
    logger.info("Manual save requested");
    
    
    try {
     
      var entityService = ServiceLocator.getEntityService();
      if (entityService != null) {
      
        var saveService = new com.csse3200.game.services.SaveGameService(entityService);
        boolean success = saveService.saveGame();
        if (success) {
          logger.info("Manual save completed successfully");
        } else {
          logger.warn("Manual save failed");
        }
      }
    } catch (Exception e) {
      logger.error("Error during manual save", e);
    }
  }

  /**
   * Awards stars when won
   */
  private void awardStars(int amount) {
    if ((ServiceLocator.getGameStateService()) == null) {
      logger.error("GameStateService is missing; register in Gdx.game");
      return;
    }
    ServiceLocator.getGameStateService().updateStars(amount);
    logger.info("Awarded {} star. Total = {}", amount, ServiceLocator.getGameStateService().getStars());
  }
}
