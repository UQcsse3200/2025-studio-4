package com.csse3200.game.components.mainmenu;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.csse3200.game.areas.ForestGameArea;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A ui component for displaying the Main menu.
 */
public class MainMenuDisplay extends UIComponent {
  private static final Logger logger = LoggerFactory.getLogger(MainMenuDisplay.class);
  private static final float Z_INDEX = 2f;
  private Table table;

  @Override
  public void create() {
    super.create();
    addActors();
    ForestGameArea.NUM_ENEMIES_DEFEATED = 0;
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

    // 创建自定义按钮样式
    TextButtonStyle customButtonStyle = createCustomButtonStyle();
    
    TextButton startBtn = new TextButton("New Game", customButtonStyle);
    TextButton loadBtn = new TextButton("Continue", customButtonStyle);
    TextButton settingsBtn = new TextButton("Settings", customButtonStyle);
    TextButton exitBtn = new TextButton("Exit", customButtonStyle);
    
    // 设置按钮大小
    float buttonWidth = 200f;
    float buttonHeight = 50f;
    
    startBtn.getLabel().setColor(Color.WHITE);
    loadBtn.getLabel().setColor(Color.WHITE);
    settingsBtn.getLabel().setColor(Color.WHITE);
    exitBtn.getLabel().setColor(Color.WHITE);

    // Triggers an event when the button is pressed
    startBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            logger.debug("Start button clicked");
            entity.getEvents().trigger("start");
          }
        });

    loadBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            logger.debug("Continue button clicked");
            entity.getEvents().trigger("continue");
          }
        });

    settingsBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            logger.debug("Settings button clicked");
            entity.getEvents().trigger("settings");
          }
        });

    exitBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {

            logger.debug("Exit button clicked");
            entity.getEvents().trigger("exit");
          }
        });

    
    table.add().expandY().row(); 
    table.add(startBtn).size(buttonWidth, buttonHeight).padTop(50f);
    table.row();
    table.add(loadBtn).size(buttonWidth, buttonHeight).padTop(20f);
    table.row();
    table.add(settingsBtn).size(buttonWidth, buttonHeight).padTop(20f);
    table.row();
    table.add(exitBtn).size(buttonWidth, buttonHeight).padTop(20f);
    table.row();
    table.add().expandY(); 

    stage.addActor(table);
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
