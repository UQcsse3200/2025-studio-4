package com.csse3200.game.components.mainmenu;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.services.SaveGameService;
import com.csse3200.game.ui.UIComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A UI component for displaying the save selection interface.
 * Shows available save files and allows users to select or delete them.
 */
public class SaveSelectionDisplay extends UIComponent {
  private static final Logger logger = LoggerFactory.getLogger(SaveSelectionDisplay.class);
  private static final float Z_INDEX = 2f;
  private Table table;
  private SaveGameService saveGameService;

  @Override
  public void create() {
    super.create();
    saveGameService = new SaveGameService(null);
    addActors();
    
    // Listen for refresh events
    entity.getEvents().addListener("refreshSaveList", this::refreshSaveList);
  }

  private void addActors() {
    table = new Table();
    table.setFillParent(true);
    
    // Add background image (same as main menu)
    Image backgroundImage =
        new Image(
            ServiceLocator.getResourceService()
                .getAsset("images/main_menu_background.png", Texture.class));
    backgroundImage.setFillParent(true);
    stage.addActor(backgroundImage);

    // Create title
    Label title = new Label("Select Save File", skin, "title");
    title.setColor(Color.WHITE);
    
    // Create new save button
    TextButton newSaveBtn = new TextButton("New Save", skin);
    newSaveBtn.getLabel().setColor(Color.WHITE);
    newSaveBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            logger.debug("New save button clicked");
            entity.getEvents().trigger("newSave");
          }
        });
    
    // Create save file list
    Table saveListTable = createSaveFileList();
    
    // Create back button
    TextButton backBtn = new TextButton("Back to Main Menu", skin);
    backBtn.getLabel().setColor(Color.WHITE);
    backBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            logger.debug("Back button clicked");
            entity.getEvents().trigger("backToMain");
          }
        });

    // Layout
    table.add(title).expandX().top().padTop(30f);
    table.row().padTop(20f);
    table.add(newSaveBtn).padBottom(20f);
    table.row();
    table.add(saveListTable).expandX().expandY();
    table.row();
    table.add(backBtn).padBottom(30f);

    stage.addActor(table);
  }

  private Table createSaveFileList() {
    Table saveTable = new Table();
    
    // Check if saves directory exists and has save files
    File savesDir = new File("saves");
    if (!savesDir.exists() || savesDir.listFiles() == null || savesDir.listFiles().length == 0) {
      Label noSavesLabel = new Label("No save files found", skin);
      noSavesLabel.setColor(Color.WHITE);
      saveTable.add(noSavesLabel).padTop(50f);
      return saveTable;
    }

    // Add header
    Label headerLabel = new Label("Available Saves:", skin);
    headerLabel.setColor(Color.WHITE);
    saveTable.add(headerLabel).padBottom(20f);
    saveTable.row();

    // List save files
    File[] saveFiles = savesDir.listFiles((dir, name) -> name.endsWith(".json"));
    if (saveFiles != null) {
      for (File saveFile : saveFiles) {
        saveTable.add(createSaveFileRow(saveFile)).padBottom(10f);
        saveTable.row();
      }
    }

    return saveTable;
  }

  private Table createSaveFileRow(File saveFile) {
    Table rowTable = new Table();
    
    // Save file name
    Label nameLabel = new Label(saveFile.getName().replace(".json", ""), skin);
    nameLabel.setColor(Color.WHITE);
    
    // Last modified date
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    String dateStr = sdf.format(new Date(saveFile.lastModified()));
    Label dateLabel = new Label(dateStr, skin);
    Label nameLabel1 = new Label("Save point:", skin);
    dateLabel.setColor(Color.WHITE);
    nameLabel1.setColor(Color.RED);
    // Load button
    TextButton loadBtn = new TextButton("Load", skin);
    loadBtn.getLabel().setColor(Color.WHITE);
    loadBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            logger.debug("Load save file: {}", saveFile.getName());
            entity.getEvents().trigger("loadSave", saveFile.getName());
          }
        });

    // Delete button
    TextButton deleteBtn = new TextButton("Delete", skin);
    deleteBtn.getLabel().setColor(Color.WHITE);
    deleteBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            logger.debug("Delete save file: {}", saveFile.getName());
            entity.getEvents().trigger("deleteSave", saveFile.getName());
          }
        });

    // Layout
    rowTable.add(nameLabel1).width(200f).left();
    rowTable.add(dateLabel).width(150f).left();
    rowTable.add(loadBtn).width(80f).padLeft(20f);
    rowTable.add(deleteBtn).width(80f).padLeft(10f);

    return rowTable;
  }

  @Override
  public void draw(SpriteBatch batch) {
    // draw is handled by the stage
  }

  @Override
  public float getZIndex() {
    return Z_INDEX;
  }

  /**
   * Refreshes the save file list by recreating the UI.
   */
  private void refreshSaveList() {
    logger.debug("Refreshing save file list");
    // Clear current table and recreate
    table.clear();
    addActors();
  }

  @Override
  public void dispose() {
    table.clear();
    super.dispose();
  }
}
