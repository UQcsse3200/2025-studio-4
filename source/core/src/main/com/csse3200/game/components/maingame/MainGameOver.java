package com.csse3200.game.components.maingame;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Displays a button to exit the Main Game screen to the Main Menu screen.
 */
public class MainGameOver extends UIComponent {
  private static final Logger logger = LoggerFactory.getLogger(MainGameExitDisplay.class);
  private static final float Z_INDEX = 50f;
  private Table table;

  @Override
  public void create() {
    super.create();
  }

  public void addActors() {
    try {
      // 如果界面已经存在，先移除
      if (table != null && table.getStage() != null) {
        table.remove();
      }

      // 首先添加背景图片到舞台
      Image gameOverBackground = new Image(ServiceLocator.getResourceService()
          .getAsset("images/Game_Over.png", Texture.class));
      gameOverBackground.setFillParent(true);
      stage.addActor(gameOverBackground);

      // 创建主表格用于内容布局
      table = new Table();
      table.setFillParent(true);

      // 创建按钮容器
      Table buttonTable = new Table();
      buttonTable.center();

      // 创建自定义按钮样式
      TextButtonStyle customButtonStyle = createCustomButtonStyle();

      // 重新开始按钮
      TextButton restartBtn = new TextButton("Restart Game", customButtonStyle);
      restartBtn.addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent changeEvent, Actor actor) {
          logger.debug("Restart button clicked");
          entity.getEvents().trigger("restart");
        }
      });

      // 返回主菜单按钮
      TextButton mainMenuBtn = new TextButton("Main Menu", customButtonStyle);
      mainMenuBtn.addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent changeEvent, Actor actor) {
          logger.debug("Main Menu button clicked");
          entity.getEvents().trigger("gameover");
        }
      });

      // 设置按钮样式
      restartBtn.getLabel().setColor(Color.BLUE); // 正常蓝色
      mainMenuBtn.getLabel().setColor(Color.BLUE); // 正常蓝色

      // 将按钮添加到按钮表格
      buttonTable.add(restartBtn).size(250f, 60f).pad(15f);
      buttonTable.row();
      buttonTable.add(mainMenuBtn).size(250f, 60f).pad(15f);

      // 添加空白空间到顶部
      table.add().expand();
      table.row();
      
      // 将按钮添加到主表格底部
      table.add(buttonTable).bottom().padBottom(150f);

      stage.addActor(table);
      logger.info("Game Over screen with background image displayed successfully");
    } catch (Exception e) {
      logger.error("Error displaying Game Over screen: {}", e.getMessage());
    }
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
            .getAsset("images/Main_Game_Button.png", Texture.class);
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
    style.fontColor = Color.BLUE; // 正常蓝色
    style.downFontColor = new Color(0.0f, 0.0f, 0.8f, 1.0f); // 深蓝色
    style.overFontColor = new Color(0.2f, 0.2f, 1.0f, 1.0f); // 亮蓝色
    
    return style;
  }

  @Override
  public void dispose() {
    if (table != null) {
      table.clear(); 
    }
    super.dispose();
  }
}
