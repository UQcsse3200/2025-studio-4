package com.csse3200.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.csse3200.game.files.UserSettings;
import com.csse3200.game.screens.*;
// NEW: Map selection screen

import com.csse3200.game.services.GameStateService;
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

  /** Used by MainMenuScreen to avoid restarting BGM repeatedly (0 = not started, 1 = playing). */
  public static int musicON = 0;

  @Override
  public void create() {
    logger.info("Creating game");
    loadSettings();

    // Sets background to light yellow
    Gdx.gl.glClearColor(248f/255f, 249/255f, 178/255f, 1);

    // instantiate game state
    ServiceLocator.registerGameStateService(new GameStateService());

    setScreen(ScreenType.OPENING_CUTSCENE);
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
   * @param saveFileName specific save file to load (for continue operations); for new game,
   *                     this string may be used to pass a selected map id.
   */
  public void setScreen(ScreenType screenType, boolean isContinue, String saveFileName) {
    logger.info("Setting game screen to {} (Continue: {}, Save/Arg: {})", screenType, isContinue, saveFileName);
    Screen currentScreen = getScreen();
    if (currentScreen != null) {
      currentScreen.dispose();
    }
    setScreen(newScreen(screenType, isContinue, saveFileName));
  }

  @Override
  public void dispose() {
    logger.debug("Disposing of current screen");
    Screen current = getScreen();
    if (current != null) {
      current.dispose();
    }
  }

  /**
   * Create a new screen of the provided type.
   * @param screenType screen type
   * @return new screen
   */
  private Screen newScreen(ScreenType screenType) {
    return newScreen(screenType, false, null);
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
      case OPENING_CUTSCENE:
        return new OpeningCutsceneScreen(this);
      case VICTORY:
        return new VictoryScreen(this);
      case MAP_SELECTION: // NEW
        return new MapSelectionScreen(this);
      case BOOK:
        return new BookScreen(this);
      default:
        return null;
    }
  }

  public enum ScreenType {
    MAIN_MENU, MAIN_GAME, SETTINGS, SAVE_SELECTION, OPENING_CUTSCENE, VICTORY,
    MAP_SELECTION, BOOK
  }

  /**
   * 设置带有指定背景的开场动画
   * @param backgroundIndex 背景索引 (0-4)
   */
  public void setOpeningCutsceneWithBackground(int backgroundIndex) {
    logger.info("Setting opening cutscene with background index: {}", backgroundIndex);
    Screen currentScreen = getScreen();
    if (currentScreen != null) {
      currentScreen.dispose();
    }
    setScreen(OpeningCutsceneScreen.withBackground(this, backgroundIndex));
  }

  /**
   * Exit the game.
   */
  public void exit() {
    app.exit();
  }
}
