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
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.services.leaderboard.LeaderboardService;
import com.csse3200.game.ui.UIComponent;
import com.csse3200.game.components.PlayerScoreComponent;
import com.csse3200.game.entities.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Displays a button to exit the Main Game screen to the Main Menu screen.
 */
public class MainGameWin extends UIComponent {
  private static final Logger logger = LoggerFactory.getLogger(MainGameExitDisplay.class);
  private static final float Z_INDEX = 50f;
  private Table table;

  @Override
  public void create() {
    super.create();
  }

  public void addActors() {
    try {
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

      // Collect player game data safely
      int finalScore = 0;
      int level = 1;
      int enemiesKilled = 0;
      long gameDuration = 0;
      int wavesSurvived = 0;
      
      try {
        // Safely access entities to avoid iterator conflicts
        com.badlogic.gdx.utils.Array<Entity> entityArray = ServiceLocator.getEntityService().getEntities();
        for (int i = 0; i < entityArray.size; i++) {
          Entity entityItem = entityArray.get(i);
          PlayerScoreComponent scoreComponent = entityItem.getComponent(PlayerScoreComponent.class);
          if (scoreComponent != null) {
            finalScore = scoreComponent.getTotalScore();
            level = scoreComponent.getLevel();
            enemiesKilled = scoreComponent.getEnemiesKilled();
            gameDuration = scoreComponent.getGameDuration();
            wavesSurvived = scoreComponent.getWavesSurvived();
            scoreComponent.updateGameDuration(); // Update final duration
            break;
          }
        }
      } catch (Exception e) {
        logger.warn("Could not collect player score data: {}", e.getMessage());
        // Use default values if data collection fails
      }

      // Add victory background image
      Image victoryBackground = new Image(ServiceLocator.getResourceService()
              .getAsset("images/Game_Victory.png", Texture.class));
      victoryBackground.setFillParent(true);
      stage.addActor(victoryBackground);
      
      table = new Table();
      table.setFillParent(true);

      // Create content container
      Table contentTable = new Table();
      contentTable.center();

      // Get player name from service
      String currentPlayerName = "Player";
      if (ServiceLocator.getPlayerNameService() != null) {
        String serviceName = ServiceLocator.getPlayerNameService().getPlayerName();
        if (serviceName != null && !serviceName.trim().isEmpty()) {
          currentPlayerName = serviceName.trim();
        }
      }

      // Display victory message
      Label victoryLabel = new Label("VICTORY!", skin, "large");
      victoryLabel.setColor(Color.GOLD);
      contentTable.add(victoryLabel).padBottom(20f);
      contentTable.row();

      // Display player name and final score
      Label playerNameLabel = new Label("Player: " + currentPlayerName, skin, "large");
      playerNameLabel.setColor(Color.GOLD);
      contentTable.add(playerNameLabel).padBottom(10f);
      contentTable.row();
      
      Label finalScoreLabel = new Label("Final Score: " + finalScore, skin, "large");
      finalScoreLabel.setColor(Color.WHITE);
      contentTable.add(finalScoreLabel).padBottom(20f);
      contentTable.row();

      // Create custom button style
      TextButtonStyle customButtonStyle = createCustomButtonStyle();

      // Continue to main menu button
      TextButton mainMenuBtn = new TextButton("Continue to Main Menu", customButtonStyle);
      final String playerName = currentPlayerName;
      final int finalScore1 = finalScore;
      final int finalLevel1 = level;
      final int finalEnemiesKilled1 = enemiesKilled;
      final long finalGameDuration1 = gameDuration;
      final int finalWavesSurvived1 = wavesSurvived;

      // Triggers an event when the button is pressed.
      mainMenuBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            logger.debug("Victory button clicked");
            
            // Save victory data to leaderboard
            saveScore(playerName, finalScore1, finalLevel1, finalEnemiesKilled1, finalGameDuration1, finalWavesSurvived1);
            
            entity.getEvents().trigger("awardStars", 1);
            // Directly trigger exit to main menu instead of gamewin
            entity.getEvents().trigger("exit");
          }
        });

      mainMenuBtn.getLabel().setColor(Color.BLUE);
      contentTable.add(mainMenuBtn).size(300f, 60f);

      table.add(contentTable);
      stage.addActor(table);
      
      logger.info("Victory screen displayed successfully");
    } catch (Exception e) {
      logger.error("Error displaying Victory screen: {}", e.getMessage());
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

  public void saveScore(String playerName, int finalScore, int level, int enemiesKilled, long gameDuration, int wavesSurvived) {
    // Handle empty or null player name
    if (playerName == null || playerName.trim().isEmpty()) {
      playerName = "Player";
    }
    
    LeaderboardService leaderboard = ServiceLocator.getLeaderboardService();

    if (leaderboard != null) {
      // Add entry to leaderboard with extended game data
      leaderboard.addEntry(playerName, finalScore, level, enemiesKilled, gameDuration, wavesSurvived);

      logger.info("Victory data submitted and saved for player '{}': Score={}, Level={}, Kills={}, Duration={}ms, Waves={}", 
                 playerName, finalScore, level, enemiesKilled, gameDuration, wavesSurvived);
    } else {
      logger.warn("LeaderboardService not available, victory data not saved.");
    }
  }

  /**
   * Creates custom button style using button background image
   */
  private TextButtonStyle createCustomButtonStyle() {
    TextButtonStyle style = new TextButtonStyle();
    
    // Use skin font with fallback
    try {
      style.font = skin.getFont("font");
    } catch (Exception e) {
      // Fallback to default font if font is not available
      style.font = skin.getFont("default");
      if (style.font == null) {
        style.font = new com.badlogic.gdx.graphics.g2d.BitmapFont();
      }
    }
    
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

  @Override
  public void dispose() {
    if (table != null) {
      table.clear(); 
    }
    super.dispose();
  }
}
