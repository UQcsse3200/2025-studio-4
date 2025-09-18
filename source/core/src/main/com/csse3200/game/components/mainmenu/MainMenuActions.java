package com.csse3200.game.components.mainmenu;

import com.csse3200.game.GdxGame;
import com.csse3200.game.components.Component;
<<<<<<< HEAD
import com.csse3200.game.services.PlayerNameService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.PlayerNameInputDialog;
=======
>>>>>>> origin/main
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
  }

  /**
   * Shows player name input dialog, then starts the game.
   */
  private void onStart() {
<<<<<<< HEAD
    logger.info("Start new game - requesting player name");
    showPlayerNameDialog();
  }
  
  /**
   * Shows the player name input dialog
   */
  private void showPlayerNameDialog() {
    PlayerNameInputDialog nameDialog = new PlayerNameInputDialog(new PlayerNameInputDialog.PlayerNameCallback() {
      @Override
      public void onNameConfirmed(String playerName) {
        logger.info("Player name confirmed: {}", playerName);
        // Register the player name service with the entered name
        ServiceLocator.registerPlayerNameService(new PlayerNameService(playerName));
        // Also store the name in the game instance for persistence across screen transitions
        if (game instanceof GdxGame) {
          ((GdxGame) game).setStoredPlayerName(playerName);
        }
        // Now start the game
        startGame();
      }
      
      @Override
      public void onNameCancelled() {
        logger.info("Player name input cancelled");
        // User cancelled, stay on main menu
      }
    });
    
    // Show the dialog
    nameDialog.show(ServiceLocator.getRenderService().getStage());
  }
  
  /**
   * Actually starts the game after name input
   */
  private void startGame() {
    logger.info("Starting game with player name: {}", 
      ServiceLocator.getPlayerNameService() != null ? 
      ServiceLocator.getPlayerNameService().getPlayerName() : "Unknown");
    game.setScreen(GdxGame.ScreenType.MAIN_GAME);
=======
    logger.info("Open Map Selection");
    game.setScreen(GdxGame.ScreenType.MAP_SELECTION);
>>>>>>> origin/main
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
}
