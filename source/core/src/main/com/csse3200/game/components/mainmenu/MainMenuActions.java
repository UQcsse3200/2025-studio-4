package com.csse3200.game.components.mainmenu;

import com.csse3200.game.GdxGame;
import com.csse3200.game.components.Component;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.services.PlayerNameServiceImpl;
import com.csse3200.game.ui.NameInputDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class listens to events relevant to the Main Menu Screen and does something when one of the
 * events is triggered.
 */
public class MainMenuActions extends Component {
  private static final Logger logger = LoggerFactory.getLogger(MainMenuActions.class);
  private GdxGame game;

  public MainMenuActions(GdxGame game) {
    this.game = game;
  }

  @Override
  public void create() {
    entity.getEvents().addListener("start", this::onStart);
    entity.getEvents().addListener("continue", this::onContinue);
    entity.getEvents().addListener("exit", this::onExit);
    entity.getEvents().addListener("settings", this::onSettings);
    entity.getEvents().addListener("ranking", this::onRanking);
  }

  /**
   * Shows name input dialog before starting new game.
   */
  private void onStart() {
    logger.info("New Game clicked, showing name input dialog");
    showNameInputDialog();
  }
  
  /**
   * Shows the name input dialog for new game.
   */
  private void showNameInputDialog() {
    // Register PlayerNameService if not already registered
    if (ServiceLocator.getPlayerNameService() == null) {
      ServiceLocator.registerPlayerNameService(new PlayerNameServiceImpl());
    }
    
    // Create and show name input dialog with callback
    NameInputDialog nameDialog = new NameInputDialog("Player Name", com.csse3200.game.ui.SimpleUI.windowStyle(), 
        new NameInputDialog.NameInputCallback() {
          @Override
          public void onNameConfirmed(String name) {
            handleNameConfirmed(name);
          }
          
          @Override
          public void onNameCancelled() {
            handleNameCancelled();
          }
        });
    
    // Get the current stage from the render service
    if (ServiceLocator.getRenderService() != null && ServiceLocator.getRenderService().getStage() != null) {
      ServiceLocator.getRenderService().getStage().addActor(nameDialog);
      nameDialog.show(ServiceLocator.getRenderService().getStage());
    } else {
      logger.warn("No stage available for name input dialog");
      // Fallback: proceed to map selection without name input
      game.setScreen(GdxGame.ScreenType.MAP_SELECTION);
    }
  }
  
  /**
   * Handles when player confirms their name.
   */
  private void handleNameConfirmed(String name) {
    logger.info("Player name confirmed: {}", name);
    // Proceed to map selection
    game.setScreen(GdxGame.ScreenType.MAP_SELECTION);
  }
  
  /**
   * Handles when player cancels name input.
   */
  private void handleNameCancelled() {
    logger.info("Name input cancelled, proceeding to map selection with default name");
    // Proceed to map selection with default name
    game.setScreen(GdxGame.ScreenType.MAP_SELECTION);
  }


  /**
   * Opens the save selection interface.
   * Users can choose which save file to load or delete.
   */
  private void onContinue() {
    logger.info("Opening save selection interface");
    game.setScreen(GdxGame.ScreenType.SAVE_SELECTION);
  }

  /**
   * Exits the game.
   */
  private void onExit() {
    logger.info("Exit game");
    game.exit();
  }

  /**
   * Swaps to the Settings screen.
   */
  private void onSettings() {
    logger.info("Launching settings screen");
    game.setScreen(GdxGame.ScreenType.SETTINGS);
  }

  /**
   * Shows the leaderboard/ranking screen.
   */
  private void onRanking() {
    logger.info("Launching ranking screen");
    // For now, we'll show a simple message or could navigate to a dedicated ranking screen
    // Since we have LeaderboardUI component, we can trigger it directly
    showLeaderboard();
  }

  /**
   * Shows the leaderboard popup.
   */
  private void showLeaderboard() {
    try {
      // Use the global leaderboard service (already registered in GdxGame)
      com.csse3200.game.services.leaderboard.LeaderboardService leaderboardService = 
        ServiceLocator.getLeaderboardService();
      
      if (leaderboardService == null) {
        logger.error("Leaderboard service not available");
        return;
      }
      
      // Create and show leaderboard popup
      com.csse3200.game.ui.leaderboard.LeaderboardController controller = 
        new com.csse3200.game.ui.leaderboard.LeaderboardController(leaderboardService);
      
      com.csse3200.game.ui.leaderboard.LeaderboardPopup popup = 
        new com.csse3200.game.ui.leaderboard.LeaderboardPopup(
          com.csse3200.game.ui.leaderboard.MinimalSkinFactory.create(), controller);
      
      popup.showOn(ServiceLocator.getRenderService().getStage());
      
    } catch (Exception e) {
      logger.error("Failed to show leaderboard", e);
    }
  }
}
