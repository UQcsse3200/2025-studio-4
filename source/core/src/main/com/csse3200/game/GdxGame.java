package com.csse3200.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.csse3200.game.files.UserSettings;
import com.csse3200.game.screens.MainGameScreen;
import com.csse3200.game.screens.MainMenuScreen;
import com.csse3200.game.screens.SettingsScreen;
import com.csse3200.game.screens.SaveSelectionScreen;
import com.csse3200.game.services.GameStateService;
import com.csse3200.game.services.PlayerNameService;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.badlogic.gdx.Gdx.app;

/**
 * Entry point of the non-platform-specific game logic. Controls which screen is currently running.
 * The current screen triggers transitions to other screens. This works similarly to a finite state
 * machine (See the State Pattern).
 */
public class GdxGame extends Game {
  private static final Logger logger = LoggerFactory.getLogger(GdxGame.class);
  private String storedPlayerName = null;

  @Override
  public void create() {
    logger.info("Creating game");
    loadSettings();

    // Sets background to light yellow
    Gdx.gl.glClearColor(248f/255f, 249/255f, 178/255f, 1);

    // instantiate game state
    ServiceLocator.registerGameStateService(new GameStateService());
    
    // instantiate player name service with default name
    ServiceLocator.registerPlayerNameService(new PlayerNameService());

    setScreen(ScreenType.MAIN_MENU);
  }

  /**
   * Loads the game's settings.
   */
  private void loadSettings() {
    logger.debug("Loading game settings");
    UserSettings.Settings settings = UserSettings.get();
    UserSettings.applySettings(settings);
  }

  /**
   * Sets the game's screen to a new screen of the provided type.
   * @param screenType screen type
   */
  public void setScreen(ScreenType screenType) {
    logger.info("Setting game screen to {}", screenType);
    Screen currentScreen = getScreen();
    if (currentScreen != null) {
      currentScreen.dispose();
    }
    setScreen(newScreen(screenType));
  }
  
  /**
   * Sets the game's screen to a new screen of the provided type with additional parameters.
   * @param screenType screen type
   * @param isContinue true if this is a continue operation, false for new game
   */
  public void setScreen(ScreenType screenType, boolean isContinue) {
    setScreen(screenType, isContinue, null);
  }

  /**
   * Sets the game's screen to a new screen of the provided type with additional parameters.
   * @param screenType screen type
   * @param isContinue true if this is a continue operation, false for new game
   * @param saveFileName specific save file to load (for continue operations)
   */
  public void setScreen(ScreenType screenType, boolean isContinue, String saveFileName) {
    logger.info("Setting game screen to {} (Continue: {}, Save: {})", screenType, isContinue, saveFileName);
    Screen currentScreen = getScreen();
    if (currentScreen != null) {
      currentScreen.dispose();
    }
    setScreen(newScreen(screenType, isContinue, saveFileName));
  }

  @Override
  public void dispose() {
    logger.debug("Disposing of current screen");
    getScreen().dispose();
  }

  /**
   * Create a new screen of the provided type.
   * @param screenType screen type
   * @return new screen
   */
  private Screen newScreen(ScreenType screenType) {
    return newScreen(screenType, false, null);
  }
   
  private Screen newScreen(ScreenType screenType, boolean isContinue) {
    return newScreen(screenType, isContinue, null);
  }

  private Screen newScreen(ScreenType screenType, boolean isContinue, String saveFileName) {
    switch (screenType) {
      case MAIN_MENU:
        return new MainMenuScreen(this);
      case MAIN_GAME:
        return new MainGameScreen(this, isContinue, saveFileName);
      case SETTINGS:
        return new SettingsScreen(this);
      case SAVE_SELECTION:
        return new SaveSelectionScreen(this);
      default:
        return null;
    }
  }

  public enum ScreenType {
    MAIN_MENU, MAIN_GAME, SETTINGS, SAVE_SELECTION
  }

  /**
   * Store the player name for use across screen transitions
   * @param playerName the player name to store
   */
  public void setStoredPlayerName(String playerName) {
    this.storedPlayerName = playerName;
  }

  /**
   * Get the stored player name
   * @return the stored player name, or null if none is stored
   */
  public String getStoredPlayerName() {
    return storedPlayerName;
  }

  /**
   * Exit the game.
   */
  public void exit() {
    app.exit();
  }
}
