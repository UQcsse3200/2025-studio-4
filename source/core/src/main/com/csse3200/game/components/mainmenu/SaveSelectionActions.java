package com.csse3200.game.components.mainmenu;

import com.csse3200.game.GdxGame;
import com.csse3200.game.components.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;


public class SaveSelectionActions extends Component {
  private static final Logger logger = LoggerFactory.getLogger(SaveSelectionActions.class);
  private GdxGame game;

  public SaveSelectionActions(GdxGame game) {
    this.game = game;
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
    logger.info("Creating new save (out-of-game)");
    try {
      com.badlogic.gdx.scenes.scene2d.Stage stage = com.csse3200.game.services.ServiceLocator.getRenderService().getStage();
      com.csse3200.game.ui.SaveNameDialog dialog = new com.csse3200.game.ui.SaveNameDialog(
          "New Save", com.csse3200.game.ui.SimpleUI.windowStyle(), new com.csse3200.game.ui.SaveNameDialog.Callback() {
            @Override public void onConfirmed(String name) {
              // Directly write a minimal template saves/<name>.json for out-of-game creation
              writeInitialSaveTemplate(name);
              entity.getEvents().trigger("refreshSaveList");
            }
            @Override public void onCancelled() { /* no-op */ }
          }
      );
      dialog.show(stage);
    } catch (Exception e) {
      logger.error("Error creating out-of-game save", e);
    }
  }


  /**
   * Write a minimal usable save template.
   */
  private void writeInitialSaveTemplate(String name) {
    try {
      String safe = sanitize(name);
      java.io.File dir = new java.io.File("saves");
      if (!dir.exists()) dir.mkdirs();
      java.io.File f = new java.io.File(dir, safe + ".json");
      String json = "{\n" +
          "  \"version\":1,\n" +
          "  \"timestamp\":\"" + new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new java.util.Date()) + "\",\n" +
          "  \"mapId\":null,\n" +
          "  \"difficulty\":\"EASY\",\n" +
          "  \"player\":{\"pos\":{\"x\":7.5,\"y\":7.5},\"hp\":100,\"gold\":100,\"metalScrap\":100,\"titaniumCore\":0,\"neurochip\":0},\n" +
          "  \"towers\":[],\n" +
          "  \"enemies\":[]\n" +
          "}";
      try (java.io.FileOutputStream out = new java.io.FileOutputStream(f)) {
        out.write(json.getBytes(java.nio.charset.StandardCharsets.UTF_8));
      }
      logger.info("Created new save template: {}", safe);
    } catch (Exception e) {
      logger.error("Write initial save template failed", e);
    }
  }

  private String sanitize(String name) {
    return name.replaceAll("[^a-zA-Z0-9 _-]", "_");
  }
}
