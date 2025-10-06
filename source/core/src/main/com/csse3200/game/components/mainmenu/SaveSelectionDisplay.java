package com.csse3200.game.components.mainmenu;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.services.ServiceLocator;

import com.csse3200.game.ui.UIComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;


public class SaveSelectionDisplay extends UIComponent {
  private static final Logger logger = LoggerFactory.getLogger(SaveSelectionDisplay.class);
  private static final float Z_INDEX = 2f;
  private Table table;

  @Override
  public void create() {
    super.create();
    addActors();
    
    entity.getEvents().addListener("refreshSaveList", this::refreshSaveList);
  }

  private void addActors() {
    table = new Table();
    table.setFillParent(true);
    
    Image backgroundImage =
        new Image(
            ServiceLocator.getResourceService()
                .getAsset("images/main_menu_background.png", Texture.class));
    backgroundImage.setFillParent(true);
    stage.addActor(backgroundImage);

    Label title = new Label("Select Save File", skin, "title");
    title.setColor(Color.WHITE);
    
    // 创建自定义按钮样式
    TextButtonStyle customButtonStyle = createCustomButtonStyle();
    
    TextButton newSaveBtn = new TextButton("New Save", customButtonStyle);
    newSaveBtn.getLabel().setColor(Color.WHITE);
    
    // 设置按钮大小
    float buttonWidth = 200f;
    float buttonHeight = 50f;
    
    newSaveBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            logger.debug("New save button clicked");
            entity.getEvents().trigger("newSave");
          }
        });
    
    Table saveListTable = createSaveFileList();
    
    TextButton backBtn = new TextButton("Back to Main Menu", customButtonStyle);
    backBtn.getLabel().setColor(Color.WHITE);
    backBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            logger.debug("Back button clicked");
            entity.getEvents().trigger("backToMain");
          }
        });

    table.add(title).expandX().top().padTop(30f);
    table.row().padTop(20f);
    table.add(newSaveBtn).size(buttonWidth, buttonHeight).padBottom(20f);
    table.row();
    table.add(saveListTable).expandX().expandY();
    table.row();
    table.add(backBtn).size(buttonWidth, buttonHeight).padBottom(30f);

    stage.addActor(table);
  }

  private Table createSaveFileList() {
    Table saveTable = new Table();
    
    File savesDir = new File("saves");
    if (!savesDir.exists() || savesDir.listFiles() == null || savesDir.listFiles().length == 0) {
      Label noSavesLabel = new Label("No save files found", skin);
      noSavesLabel.setColor(Color.WHITE);
      saveTable.add(noSavesLabel).padTop(50f);
      return saveTable;
    }

    Label headerLabel = new Label("Available Saves:", skin);
    headerLabel.setColor(Color.WHITE);
    saveTable.add(headerLabel).padBottom(20f);
    saveTable.row();

    
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
    
    Label nameLabel = new Label(saveFile.getName().replace(".json", ""), skin);
    nameLabel.setColor(Color.WHITE);
    
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    String dateStr = sdf.format(new Date(saveFile.lastModified()));
    Label dateLabel = new Label(dateStr, skin);
    dateLabel.setColor(Color.WHITE);

    TextButtonStyle customButtonStyle = createCustomButtonStyle();
    
    TextButton loadBtn = new TextButton("Load", customButtonStyle);
    loadBtn.getLabel().setColor(Color.WHITE);
    loadBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            logger.debug("Load save file: {}", saveFile.getName());
            entity.getEvents().trigger("loadSave", saveFile.getName());
          }
        });
    TextButton deleteBtn = new TextButton("Delete", customButtonStyle);
    deleteBtn.getLabel().setColor(Color.WHITE);
    deleteBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            logger.debug("Delete save file: {}", saveFile.getName());
            entity.getEvents().trigger("deleteSave", saveFile.getName());
          }
        });

    // Show save file name exactly as created (without .json)
    rowTable.add(nameLabel).width(260f).left();
    rowTable.add(dateLabel).width(150f).left();
    rowTable.add(loadBtn).size(80f, 40f).padLeft(20f);
    rowTable.add(deleteBtn).size(80f, 40f).padLeft(10f);

    return rowTable;
  }

  @Override
  public void draw(SpriteBatch batch) {
  
  }

  @Override
  public float getZIndex() {
    return Z_INDEX;
  }


  private void refreshSaveList() {
    logger.debug("Refreshing save file list");
    table.clear();
    addActors();
  }

  /**
   * 创建自定义按钮样式，使用按钮背景图片
   */
  private TextButtonStyle createCustomButtonStyle() {
    TextButtonStyle style = new TextButtonStyle();
    
    // 使用Segoe UI字体
    style.font = skin.getFont("segoe_ui");
    
    // 加载按钮背景图片
    Texture buttonTexture = ServiceLocator.getResourceService()
        .getAsset("images/Main_Menu_Button_Background.png", Texture.class);
    TextureRegion buttonRegion = new TextureRegion(buttonTexture);
    
    // 创建NinePatch用于可缩放的按钮背景
    NinePatch buttonPatch = new NinePatch(buttonRegion, 10, 10, 10, 10);
    
    // 创建按下状态的NinePatch（稍微变暗）
    NinePatch pressedPatch = new NinePatch(buttonRegion, 10, 10, 10, 10);
    pressedPatch.setColor(new Color(0.8f, 0.8f, 0.8f, 1f));
    
    // 创建悬停状态的NinePatch（稍微变亮）
    NinePatch hoverPatch = new NinePatch(buttonRegion, 10, 10, 10, 10);
    hoverPatch.setColor(new Color(1.1f, 1.1f, 1.1f, 1f));
    
    // 设置按钮状态
    style.up = new NinePatchDrawable(buttonPatch);
    style.down = new NinePatchDrawable(pressedPatch);
    style.over = new NinePatchDrawable(hoverPatch);
    
    // 设置字体颜色
    style.fontColor = Color.WHITE;
    style.downFontColor = Color.LIGHT_GRAY;
    style.overFontColor = Color.WHITE;
    
    return style;
  }

  @Override
  public void dispose() {
    table.clear();
    super.dispose();
  }
}
