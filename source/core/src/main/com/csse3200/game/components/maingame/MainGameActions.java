package com.csse3200.game.components.maingame;

import com.csse3200.game.GdxGame;
import com.csse3200.game.areas.ForestGameArea;
import com.csse3200.game.areas2.MapTwo.ForestGameArea2;
import com.csse3200.game.components.Component;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.services.leaderboard.LeaderboardService;
import com.csse3200.game.components.book.BookOnMainGame;
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
    entity.getEvents().addListener("gameover", this::onGameOver);
    entity.getEvents().addListener("gamewin", this::onVictory);
    entity.getEvents().addListener("restart", this::onRestart);
    entity.getEvents().addListener("save", this::onSave);
    entity.getEvents().addListener("performSave", this::onPerformSave);
    // Save As removed
    entity.getEvents().addListener("openSettings", this::onOpenSettings);
    entity.getEvents().addListener("quitToMenu", this::onQuitToMenu);
    entity.getEvents().addListener("showRanking", this::onShowRanking);
    entity.getEvents().addListener("showBook", this::onShowBook);
  }

  private void onOpenSettings() {
    game.setScreen(GdxGame.ScreenType.SETTINGS);
  }

  private void onShowBook() {
    logger.info("Showing Book in main game");

    try {
      Stage stage = ServiceLocator.getRenderService().getStage();
      Skin skin = MinimalSkinFactory.create();

      BookOnMainGame popup = new BookOnMainGame(skin);
      popup.showOn(stage);

    } catch (Exception e) {
      logger.error("Error showing ranking", e);
    }
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
      Skin skin = MinimalSkinFactory.create();
      
      // Use the global leaderboard service (already registered in GdxGame)
      LeaderboardService leaderboardService = ServiceLocator.getLeaderboardService();
      
      if (leaderboardService == null) {
        logger.error("Leaderboard service not available");
        return;
      }
      LeaderboardController controller = new LeaderboardController(leaderboardService);
      LeaderboardPopup popup = new LeaderboardPopup(skin, controller);
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
    ForestGameArea.NUM_ENEMIES_DEFEATED = 0;
    ForestGameArea.NUM_ENEMIES_TOTAL = 0;
    game.setScreen(GdxGame.ScreenType.MAIN_GAME, false);
  }

  /**
   * Swaps to the Main Menu screen.
   */
  private void onExit() {
    logger.info("Exiting main game screen");
    ForestGameArea.cleanupAllWaves();
    ForestGameArea2.cleanupAllWaves();
    game.setScreen(GdxGame.ScreenType.MAIN_MENU);
  }
  
  /**
   * Handles game over (defeat) scenario.
   */
  private void onGameOver() {
    logger.info("Game over, submitting defeat score");
    
    // Calculate and save the final score before switching screens
    try {
      var sessionManager = ServiceLocator.getGameSessionManager();
      if (sessionManager != null) {
        
          //Submission failure score (this is calculated while the entity still exists)
        sessionManager.submitScoreIfNotSubmitted(false);
      }
    } catch (Exception e) {
      logger.error("Error submitting defeat score", e);
    }
    
    // 切换到游戏结束屏幕或主菜单
    game.setScreen(GdxGame.ScreenType.MAIN_MENU);
  }
  
  /**
   * Swaps to the Victory screen.
   */
  private void onVictory() {
    logger.info("Game won, showing victory screen");
    
    // Unlock achievements
    unlockAchievementsOnVictory();
    
    // 在切换屏幕之前计算并保存最终得分
    try {
      var sessionManager = ServiceLocator.getGameSessionManager();
      if (sessionManager != null) {
        // 提交胜利得分（这会在实体还存在时计算得分）
        sessionManager.submitScoreIfNotSubmitted(true);
      }
    } catch (Exception e) {
      logger.error("Error submitting victory score", e);
    }
    
    game.setScreen(GdxGame.ScreenType.VICTORY);
  }
  
  /**
   * Unlocks achievements when the player wins the game
   */
  private void unlockAchievementsOnVictory() {
    try {
      com.csse3200.game.services.AchievementService achievementService = 
        ServiceLocator.getAchievementService();
      
      if (achievementService != null) {
        // Unlock "Perfect Clear" for winning the game
        achievementService.unlockAchievement(
          com.csse3200.game.services.AchievementService.PERFECT_CLEAR);
        logger.info("Unlocked Perfect Clear achievement");
        
        // Check enemy defeat count for other achievements
        int enemiesDefeated = com.csse3200.game.areas.ForestGameArea.NUM_ENEMIES_DEFEATED;
        
        // Unlock "Speed Runner" for defeating 5+ enemies
        if (enemiesDefeated >= 5) {
          achievementService.unlockAchievement(
            com.csse3200.game.services.AchievementService.SPEED_RUNNER);
          logger.info("Unlocked Speed Runner achievement");
        }
        
        // Unlock "Slayer" for defeating 20+ enemies
        if (enemiesDefeated >= 20) {
          achievementService.unlockAchievement(
            com.csse3200.game.services.AchievementService.SLAYER);
          logger.info("Unlocked Slayer achievement");
        }
        
        // Unlock "Tough Survivor" for completing the game
        achievementService.unlockAchievement(
          com.csse3200.game.services.AchievementService.TOUGH_SURVIVOR);
        logger.info("Unlocked Tough Survivor achievement");
        
        // Unlock "Participation" for playing
        achievementService.unlockAchievement(
          com.csse3200.game.services.AchievementService.PARTICIPATION);
        logger.info("Unlocked Participation achievement");
      }
    } catch (Exception e) {
      logger.error("Error unlocking achievements", e);
    }
  }
  
  private void onSave() {
    logger.info("Manual save requested");
    // Show save menu instead of directly saving
    entity.getEvents().trigger("showSaveUI");
  }

  private void onPerformSave() {
    logger.info("Performing save operation with naming dialog");
    try {
      // Hide the save menu to prevent it from blocking the dialog
      entity.getEvents().trigger("hideSaveMenuOnly");
      
      Stage stage = ServiceLocator.getRenderService().getStage();
      com.csse3200.game.ui.SaveNameDialog dialog = new com.csse3200.game.ui.SaveNameDialog(
          "Save Game", com.csse3200.game.ui.SimpleUI.windowStyle(), new com.csse3200.game.ui.SaveNameDialog.Callback() {
            @Override public void onConfirmed(String name) {
              try {
                var entityService = ServiceLocator.getEntityService();
                if (entityService == null) {
                  entity.getEvents().trigger("showSaveError");
                  return;
                }
                var saveService = new com.csse3200.game.services.SimpleSaveService(entityService);
                boolean success = saveService.saveAs(name);
                if (success) {
                  logger.info("Saved as '{}' successfully", name);
                  entity.getEvents().trigger("showSaveSuccess");
                } else {
                  entity.getEvents().trigger("showSaveError");
                }
              } catch (Exception ex) {
                logger.error("Error during named save", ex);
                entity.getEvents().trigger("showSaveError");
              }
            }
            @Override public void onCancelled() { 
              // Dialog cancelled, no action needed
            }
          }
      );
      dialog.show(stage);
    } catch (Exception e) {
      logger.error("Error opening SaveNameDialog", e);
      entity.getEvents().trigger("showSaveError");
    }
  }

  // Save As removed

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

