package com.csse3200.game.components.mainmenu;

import com.csse3200.game.GdxGame;
import com.csse3200.game.components.Component;
import com.csse3200.game.services.SaveGameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;


public class SaveSelectionActions extends Component {
  private static final Logger logger = LoggerFactory.getLogger(SaveSelectionActions.class);
  private GdxGame game;
  private SaveGameService saveGameService;

  public SaveSelectionActions(GdxGame game) {
    this.game = game;
    this.saveGameService = new SaveGameService(null);
  }

  @Override
  public void create() {
    entity.getEvents().addListener("backToMain", this::onBackToMain);
    entity.getEvents().addListener("loadSave", this::onLoadSave);
    entity.getEvents().addListener("deleteSave", this::onDeleteSave);
    entity.getEvents().addListener("newSave", this::onNewSave);
  }

  
  private void onBackToMain() {
    logger.info("Returning to main menu");
    game.setScreen(GdxGame.ScreenType.MAIN_MENU);
  }

  
  private void onLoadSave(String saveFileName) {
    logger.info("Loading save file: {}", saveFileName);
    
    try {
      String saveName = saveFileName.replace(".json", "");
      
      game.setScreen(GdxGame.ScreenType.MAIN_GAME, true, saveName);
      logger.info("Starting game to load save: {}", saveName);
    } catch (Exception e) {
      logger.error("Failed to start game for save loading: {}", saveFileName, e);
    }
  }

  
  private void onDeleteSave(String saveFileName) {
    logger.info("Deleting save file: {}", saveFileName);
    
    try {
      File saveFile = new File("saves/" + saveFileName);
      if (saveFile.exists()) {
        boolean deleted = saveFile.delete();
        if (deleted) {
          logger.info("Save file deleted successfully: {}", saveFileName);
          entity.getEvents().trigger("refreshSaveList");
        } else {
          logger.error("Failed to delete save file: {}", saveFileName);
        }
      } else {
        logger.warn("Save file not found: {}", saveFileName);
      }
    } catch (Exception e) {
      logger.error("Failed to delete save file: {}", saveFileName, e);
    }
  }

  
  private void onNewSave() {
    logger.info("Starting new game for save creation");
    
    try {
      
      game.setScreen(GdxGame.ScreenType.MAIN_GAME, false);
    } catch (Exception e) {
      logger.error("Error starting new game for save", e);
    }
  }
}
