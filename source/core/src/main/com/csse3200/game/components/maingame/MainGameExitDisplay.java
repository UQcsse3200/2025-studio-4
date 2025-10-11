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
import com.csse3200.game.files.UserSettings;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIStyleHelper;
import com.csse3200.game.ui.leaderboard.LeaderboardController;
import com.csse3200.game.ui.leaderboard.LeaderboardPopup;
import com.csse3200.game.ui.leaderboard.MinimalSkinFactory;
import com.csse3200.game.services.leaderboard.LeaderboardService;
import com.csse3200.game.services.leaderboard.InMemoryLeaderboardService;
import com.csse3200.game.ui.UIComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Displays buttons to control the game (save, exit, ranking, speed).
 */
public class MainGameExitDisplay extends UIComponent {
  private static final Logger logger = LoggerFactory.getLogger(MainGameExitDisplay.class);
  private static final float Z_INDEX = 2f;
  private static final float NORMAL_SPEED = 1.0f;
  private static final float DOUBLE_SPEED = 2.0f;
  
  private Table table;
  private TextButton speedButton;
  private boolean isDoubleSpeed = false;

  @Override
  public void create() {
    super.create();
    addActors();
  }

  private void addActors() {
    table = new Table();
    table.top().right();
    table.setFillParent(true);

    // Create custom button style
    TextButtonStyle customButtonStyle = UIStyleHelper.orangeButtonStyle();
    TextButton saveBtn = new TextButton("Save", customButtonStyle);
    TextButton mainMenuBtn = new TextButton("Exit", customButtonStyle);
    TextButton rankingBtn = new TextButton("Ranking", customButtonStyle);
    speedButton = new TextButton("Speed: 1x", customButtonStyle);

    // Set button size
    float buttonWidth = 120f;
    float buttonHeight = 40f;
    
    saveBtn.getLabel().setColor(Color.CYAN);
    mainMenuBtn.getLabel().setColor(Color.CYAN);
    rankingBtn.getLabel().setColor(Color.CYAN);
    speedButton.getLabel().setColor(Color.CYAN);
    
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
            logger.debug("Ranking button clicked");
            try {
                // Use the global leaderboard service (already registered in GdxGame)
                LeaderboardService leaderboardService = ServiceLocator.getLeaderboardService();
                
                if (leaderboardService == null) {
                    logger.error("Leaderboard service not available");
                    return;
                }
                LeaderboardController controller = new LeaderboardController(leaderboardService);
                LeaderboardPopup popup = new LeaderboardPopup(MinimalSkinFactory.create(), controller);
                popup.showOn(stage);
                
            } catch (Exception e) {
                logger.error("Error showing ranking", e);
            }
        }
    });

    speedButton.addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
            toggleSpeed();
        }
    });

    table.add(saveBtn).size(buttonWidth, buttonHeight).padTop(10f).padRight(10f);
    table.row();
    table.add(mainMenuBtn).size(buttonWidth, buttonHeight).padTop(5f).padRight(10f);
    table.row();
    table.add(rankingBtn).size(buttonWidth, buttonHeight).padTop(5f).padRight(10f);
    table.row();
    table.add(speedButton).size(buttonWidth, buttonHeight).padTop(5f).padRight(10f);

    stage.addActor(table);
    applyUiScale();
  }

  /**
   * Toggles the game speed between normal (1x) and double (2x).
   */
  private void toggleSpeed() {
      isDoubleSpeed = !isDoubleSpeed;

      if (isDoubleSpeed) {
          ServiceLocator.getTimeSource().setTimeScale(DOUBLE_SPEED);
          speedButton.setText("Speed: 2x");
          logger.info("Game speed set to 2x");
      } else {
          ServiceLocator.getTimeSource().setTimeScale(NORMAL_SPEED);
          speedButton.setText("Speed: 1x");
          logger.info("Game speed set to 1x");
      }
  }

  /**
   * Apply UI scale from user settings
   */
  private void applyUiScale() {
      UserSettings.Settings settings = UserSettings.get();
      if (table != null) {
          table.setTransform(true);

          // Force layout validation
          table.validate();

          // Set origin to top-right corner (where the table is anchored)
          table.setOrigin(table.getWidth(), table.getHeight());

          table.setScale(settings.uiScale);
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
    
    style.fontColor = Color.CYAN;
    style.downFontColor = new Color(0.0f, 0.6f, 0.8f, 1.0f);
    style.overFontColor = new Color(0.2f, 0.8f, 1.0f, 1.0f);
    
    return style;
  }

  @Override
  public void dispose() {
    table.clear();
    super.dispose();
  }
}