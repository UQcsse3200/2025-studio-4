package com.csse3200.game.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.csse3200.game.components.maingame.MainGameExitDisplay;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Displays a button to exit the Main Game screen to the Main Menu screen.
 */
public class GameOverScreen extends UIComponent {
  private static final Logger logger = LoggerFactory.getLogger(MainGameExitDisplay.class);
  private static final float Z_INDEX = 50f;
  private Table table;

  @Override
  public void create() {
    super.create();
  }

  public void addActors() {
    // Ensure stage is initialized
    if (stage == null) {
      stage = ServiceLocator.getRenderService().getStage();
      if (stage == null) {
        logger.warn("Stage not available, cannot add actors");
        return;
      }
    }
    
    // Remove existing UI if present
    if (table != null && table.getStage() != null) {
      table.remove();
    }
    
    table = new Table();
    table.center();
    table.setFillParent(true);

    BitmapFont font = new BitmapFont();
    font.getData().setScale(3.0f);
    
    Label.LabelStyle labelStyle = new Label.LabelStyle();
    labelStyle.font = font;
    labelStyle.fontColor = Color.RED;
    
    Label failLabel = new Label("You Failed!", labelStyle);
    failLabel.setAlignment(Align.center);
    failLabel.setColor(1f, 1f, 1f, 0f);
    
    table.add(failLabel).center();
    stage.addActor(table);
    
    failLabel.addAction(Actions.sequence(
      Actions.fadeIn(0.5f),
      Actions.delay(1.5f),
      Actions.run(() -> {
        logger.info("Auto-triggering defeat screen");
        submitCurrentScore();
        entity.getEvents().trigger("gameover");
      })
    ));
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
   * Creates custom button style using button background image
   */
  private TextButtonStyle createCustomButtonStyle() {
    TextButtonStyle style = new TextButtonStyle();
    
    // Use Segoe UI font
    style.font = skin.getFont("segoe_ui");
    
        // Load button background image
        Texture buttonTexture = ServiceLocator.getResourceService()
            .getAsset("images/Main_Game_Button.png", Texture.class);
    TextureRegion buttonRegion = new TextureRegion(buttonTexture);
    
    // Create NinePatch for scalable button background
    NinePatch buttonPatch = new NinePatch(buttonRegion, 10, 10, 10, 10);
    
    // Create pressed state NinePatch (slightly darker)
    NinePatch pressedPatch = new NinePatch(buttonRegion, 10, 10, 10, 10);
    pressedPatch.setColor(new Color(0.8f, 0.8f, 0.8f, 1f));
    
    // Create hover state NinePatch (slightly brighter)
    NinePatch hoverPatch = new NinePatch(buttonRegion, 10, 10, 10, 10);
    hoverPatch.setColor(new Color(1.1f, 1.1f, 1.1f, 1f));
    
    // Set button states
    style.up = new NinePatchDrawable(buttonPatch);
    style.down = new NinePatchDrawable(pressedPatch);
    style.over = new NinePatchDrawable(hoverPatch);
    
    // Set font colors
    style.fontColor = Color.BLUE; // Normal blue
    style.downFontColor = new Color(0.0f, 0.0f, 0.8f, 1.0f); // Dark blue
    style.overFontColor = new Color(0.2f, 0.2f, 1.0f, 1.0f); // Light blue
    
    return style;
  }

  /**
   * 提交当前游戏得分到排行榜（通过会话管理器防止重复提交）
   */
  private void submitCurrentScore() {
    try {
      // 使用会话管理器防止重复提交
      com.csse3200.game.services.GameSessionManager sessionManager = 
        ServiceLocator.getGameSessionManager();
      
      if (sessionManager == null) {
        logger.error("Game session manager not available");
        return;
      }
      
      // 尝试提交得分（如果本次会话还未提交）
      // 现在使用GameScoreService获取真实得分
      boolean submitted = sessionManager.submitScoreIfNotSubmitted(false); // false = 游戏失败
      
      if (submitted) {
        logger.info("Successfully submitted game over score to leaderboard");
      } else {
        logger.info("Score already submitted for this session, skipping duplicate submission");
      }
      
    } catch (Exception e) {
      logger.error("Failed to submit game over score to leaderboard", e);
    }
  }
  

  @Override
  public void dispose() {
    if (table != null) {
      table.clear(); 
    }
    super.dispose();
  }
}
