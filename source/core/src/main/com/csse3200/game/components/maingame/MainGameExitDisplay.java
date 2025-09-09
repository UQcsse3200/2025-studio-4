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
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.MockRanks;
import com.csse3200.game.ui.PlayerRank;
import com.csse3200.game.ui.RankingDialog;
import com.csse3200.game.ui.UIComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Displays a button to exit the Main Game screen to the Main Menu screen.
 */
public class MainGameExitDisplay extends UIComponent {
  private static final Logger logger = LoggerFactory.getLogger(MainGameExitDisplay.class);
  private static final float Z_INDEX = 2f;
  private Table table;

  @Override
  public void create() {
    super.create();
    addActors();
  }

  private void addActors() {
    table = new Table();
    table.top().right();
    table.setFillParent(true);

    // 创建自定义按钮样式
    TextButtonStyle customButtonStyle = createCustomButtonStyle();

    TextButton saveBtn = new TextButton("Save", customButtonStyle);
    TextButton mainMenuBtn = new TextButton("Exit", customButtonStyle);
    TextButton rankingBtn = new TextButton("Ranking", customButtonStyle);

    // 设置按钮大小
    float buttonWidth = 120f;
    float buttonHeight = 40f;
    
    saveBtn.getLabel().setColor(Color.BLUE); // 正常蓝色
    mainMenuBtn.getLabel().setColor(Color.BLUE); // 正常蓝色
    rankingBtn.getLabel().setColor(Color.BLUE); // 正常蓝色
    
    saveBtn.addListener(
      new ChangeListener() {
        @Override
        public void changed(ChangeEvent changeEvent, Actor actor) {
          logger.debug("Save button clicked");
          entity.getEvents().trigger("save");
        }
      });

    
    mainMenuBtn.addListener(
      new ChangeListener() {
        @Override
        public void changed(ChangeEvent changeEvent, Actor actor) {
          logger.debug("Exit button clicked");
          entity.getEvents().trigger("exit");
        }
      });

      rankingBtn.addListener(new ChangeListener() {
          @Override public void changed(ChangeEvent event, Actor actor) {
              List<PlayerRank> players = MockRanks.make(30);     // 一次生成 30 条
              new RankingDialog("Leaderboard", players, 12).show(stage);
          }
      });


      table.add(saveBtn).size(buttonWidth, buttonHeight).padTop(10f).padRight(10f);
    table.row();
    table.add(mainMenuBtn).size(buttonWidth, buttonHeight).padTop(5f).padRight(10f);
    table.row();
    table.add(rankingBtn).size(buttonWidth, buttonHeight).padTop(5f).padRight(10f);

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
    table.clear();
    super.dispose();
  }
}
