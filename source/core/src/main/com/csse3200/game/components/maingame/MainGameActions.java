package com.csse3200.game.components.maingame;

import com.csse3200.game.GdxGame;
import com.csse3200.game.areas.ForestGameArea;
import com.csse3200.game.components.Component;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.MockRanks;
import com.csse3200.game.ui.PlayerRank;
import com.csse3200.game.ui.RankingDialog;
import com.badlogic.gdx.scenes.scene2d.Stage;
import java.util.List;
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
    entity.getEvents().addListener("gamewin", this::onVictory);
    entity.getEvents().addListener("restart", this::onRestart);
    entity.getEvents().addListener("save", this::onSave);
    entity.getEvents().addListener("performSave", this::onPerformSave);
    entity.getEvents().addListener("performSaveAs", this::onPerformSaveAs);
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
    entity.getEvents().trigger("showPauseUI"); 
  }

  private void onResume() {
    ServiceLocator.getTimeSource().setTimeScale(1f);
    isPaused = false;
    entity.getEvents().trigger("hidePauseUI"); 
  }

  private void onOpenSettings() {
    game.setScreen(GdxGame.ScreenType.SETTINGS);
  }

  private void onQuitToMenu() {
    ForestGameArea.cleanupAllWaves();
    game.setScreen(GdxGame.ScreenType.MAIN_MENU);
  }

  /**
   * Shows the ranking/leaderboard popup
   */
  private void onShowRanking() {
    logger.info("Showing ranking/leaderboard");
    
    try {
      Stage stage = ServiceLocator.getRenderService().getStage();
      
      // Use the new LeaderboardService instead of mock data
      com.csse3200.game.services.leaderboard.LeaderboardService leaderboard = ServiceLocator.getLeaderboardService();
      logger.debug("LeaderboardService available: {}", leaderboard != null);
      
      // Force use of real leaderboard system - always try it first
      try {
          // Ensure leaderboard service is available
          if (leaderboard == null) {
              logger.warn("LeaderboardService is null, creating new one");
              ServiceLocator.registerLeaderboardService(
                  new com.csse3200.game.services.leaderboard.PersistentLeaderboardService("player-001"));
              leaderboard = ServiceLocator.getLeaderboardService();
          }
          
          if (leaderboard != null) {
              // Check if there are any entries
              var entries = leaderboard.getEntries(new com.csse3200.game.services.leaderboard.LeaderboardService.LeaderboardQuery(0, 10, false));
              logger.info("Found {} leaderboard entries in real system", entries.size());
              
              var controller = new com.csse3200.game.ui.leaderboard.LeaderboardController(leaderboard);
              com.badlogic.gdx.scenes.scene2d.ui.Skin leaderboardSkin = com.csse3200.game.ui.leaderboard.MinimalSkinFactory.create();
              com.csse3200.game.ui.leaderboard.LeaderboardPopup popup = new com.csse3200.game.ui.leaderboard.LeaderboardPopup(leaderboardSkin, controller);
              popup.showOn(stage);
              logger.info("Successfully showing real leaderboard with {} entries", entries.size());
              return; // Success, don't show mock data
          }
      } catch (Exception e) {
          logger.error("Error creating leaderboard popup: {}", e.getMessage(), e);
      }
      
      // Only show mock data if real leaderboard completely failed
      logger.warn("Falling back to mock data because real leaderboard failed");
      List<PlayerRank> players = MockRanks.make(30);
      RankingDialog rankingDialog = new RankingDialog("Leaderboard", players, 12);
      rankingDialog.show(stage);
      
    } catch (Exception e) {
      logger.error("Error showing ranking", e);
    }
  }

  /**
   * Restarts the game by creating a new MainGameScreen.
   */
  private void onRestart() {
    logger.info("Restarting game");
    ForestGameArea.NUM_ENEMIES_DEFEATED = 0;
    ForestGameArea.NUM_ENEMIES_TOTAL = 0;
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
   * Shows the victory screen using MainGameWin component.
   */
  private void onVictory() {
    logger.info("Game won, showing victory screen");
    // Use the MainGameWin component instead of switching to a separate screen
    com.csse3200.game.screens.MainGameScreen.ui.getComponent(com.csse3200.game.components.maingame.MainGameWin.class).addActors();
  }
  
  private void onSave() {
    logger.info("Manual save requested");
    // Show save menu instead of directly saving
    entity.getEvents().trigger("showSaveUI");
  }

  private void onPerformSave() {
    logger.info("Performing save operation (CI sync)");
    
    try {
      var entityService = ServiceLocator.getEntityService();
      if (entityService != null) {
        var saveService = new com.csse3200.game.services.SimpleSaveService(entityService);
        boolean success = saveService.save();
        if (success) {
          logger.info("Manual save completed successfully");
          entity.getEvents().trigger("showSaveSuccess");
          // auto-close save UI after success
          entity.getEvents().trigger("hideSaveUI");
        } else {
          logger.warn("Manual save failed");
          entity.getEvents().trigger("showSaveError");
        }
      }
    } catch (Exception e) {
      logger.error("Error during manual save", e);
      entity.getEvents().trigger("showSaveError");
    }
  }

  private void onPerformSaveAs() {
    logger.info("Save As requested");
    // TODO: Implement save as functionality
    entity.getEvents().trigger("showSaveError"); // For now, show error
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
