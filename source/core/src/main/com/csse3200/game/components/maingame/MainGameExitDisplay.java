package com.csse3200.game.components.maingame;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.csse3200.game.files.UserSettings;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIStyleHelper;
import com.csse3200.game.ui.leaderboard.LeaderboardController;
import com.csse3200.game.ui.leaderboard.LeaderboardPopup;
import com.csse3200.game.ui.leaderboard.MinimalSkinFactory;
import com.csse3200.game.services.leaderboard.LeaderboardService;
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
  private TextButton startWaveButton;
  private boolean isDoubleSpeed = false;
  private boolean isWaveActive = false;
  private boolean allWavesComplete = false;

  @Override
  public void create() {
    super.create();
    addActors();
    
    // Listen for wave state changes
    entity.getEvents().addListener("waveStarted", this::onWaveStarted);
    entity.getEvents().addListener("waveComplete", this::onWaveComplete);
    entity.getEvents().addListener("allWavesComplete", this::onAllWavesComplete);
  }

  private void addActors() {
    table = new Table();
    table.top().right();
    table.setFillParent(true);

    // Create custom button style
    TextButtonStyle customButtonStyle = UIStyleHelper.mainGameMenuButtonStyle();
    // Note: Save button removed - now in pause menu
    TextButton mainMenuBtn = new TextButton("Exit", customButtonStyle);
    TextButton rankingBtn = new TextButton("Ranking", customButtonStyle);
    speedButton = new TextButton("Speed: 1x", customButtonStyle);
    startWaveButton = new TextButton("Start Wave", customButtonStyle);

    // Set button size
    float buttonWidth = 140f;
    float buttonHeight = 40f;
    
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

    startWaveButton.addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
            if (!isWaveActive && !allWavesComplete) {
                startWave();
            }
        }
    });

    // Save button removed - now in pause menu
    table.add(mainMenuBtn).size(buttonWidth, buttonHeight).padTop(10f).padRight(10f);
    table.row();
    table.add(rankingBtn).size(buttonWidth, buttonHeight).padTop(10f).padRight(10f);
    table.row();
    table.add(speedButton).size(buttonWidth, buttonHeight).padTop(10f).padRight(10f);
    table.row();
    table.add(startWaveButton).size(buttonWidth, buttonHeight).padTop(10f).padRight(10f);

    stage.addActor(table);
    applyUiScale();
  }

  private void startWave() {
    logger.debug("Start Wave button clicked");
    entity.getEvents().trigger("startWave");
  }

  /**
   * Called when a wave starts - disable the button
   */
  private void onWaveStarted() {
    isWaveActive = true;
    disableButton();
    logger.debug("Wave started - button disabled");
  }

  /**
   * Called when a wave completes - enable the button (unless all waves are complete)
   */
  private void onWaveComplete() {
    isWaveActive = false;
    if (!allWavesComplete) {
      enableButton();
      logger.debug("Wave complete - button enabled");
    }
  }

  /**
   * Called when all waves are complete - permanently disable the button
   */
  private void onAllWavesComplete() {
    allWavesComplete = true;
    isWaveActive = false;
    disableButton();
    logger.debug("All waves complete - button permanently disabled");
  }

  /**
   * Disable the Start Wave button (grey out entire button)
   */
  private void disableButton() {
    startWaveButton.setDisabled(true);
    startWaveButton.getLabel().setColor(Color.GRAY);
    // Grey out the entire button
    startWaveButton.setColor(0.5f, 0.5f, 0.5f, 1f);
  }

  /**
   * Enable the Start Wave button
   */
  private void enableButton() {
    startWaveButton.setDisabled(false);
    startWaveButton.getLabel().setColor(Color.WHITE);
    // Restore full color
    startWaveButton.setColor(Color.WHITE);
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

  @Override
  public void dispose() {
    table.clear();
    super.dispose();
  }
}