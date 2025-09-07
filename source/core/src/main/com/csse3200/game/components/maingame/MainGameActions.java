package com.csse3200.game.components.maingame;

import com.csse3200.game.GdxGame;
import com.csse3200.game.components.Component;
import com.csse3200.game.services.ServiceLocator;
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
    entity.getEvents().addListener("save", this::onSave);
    entity.getEvents().addListener("togglePause", this::onTogglePause);
    entity.getEvents().addListener("resume", this::onResume);
    entity.getEvents().addListener("openSettings", this::onOpenSettings);
    entity.getEvents().addListener("quitToMenu", this::onQuitToMenu);
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
    game.setScreen(GdxGame.ScreenType.MAIN_MENU);
  }

  /**
   * Swaps to the Main Menu screen.
   */
  private void onExit() {
    logger.info("Exiting main game screen");
    game.setScreen(GdxGame.ScreenType.MAIN_MENU);
  }
  
  private void onSave() {
    logger.info("Manual save requested");
    
    
    try {
     
      var entityService = ServiceLocator.getEntityService();
      if (entityService != null) {
      
        var saveService = new com.csse3200.game.services.SaveGameService(entityService);
        boolean success = saveService.saveGame();
        if (success) {
          logger.info("Manual save completed successfully");
          if (!isPaused) {
            onPause();
          }
          entity.getEvents().trigger("showSaveSuccess");
        } else {
          logger.warn("Manual save failed");
        }
      }
    } catch (Exception e) {
      logger.error("Error during manual save", e);
    }
  }
}
