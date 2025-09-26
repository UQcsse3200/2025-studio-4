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

    // Create custom button style
    TextButtonStyle customButtonStyle = createCustomButtonStyle();

    TextButton saveBtn = new TextButton("Save", customButtonStyle);
    TextButton mainMenuBtn = new TextButton("Exit", customButtonStyle);
    TextButton rankingBtn = new TextButton("Ranking", customButtonStyle);

    // Set button size
    float buttonWidth = 120f;
    float buttonHeight = 40f;
    
    saveBtn.getLabel().setColor(Color.CYAN);
    mainMenuBtn.getLabel().setColor(Color.CYAN);
    rankingBtn.getLabel().setColor(Color.CYAN);
    
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
              // Use the new LeaderboardService instead of mock data
              com.csse3200.game.services.leaderboard.LeaderboardService leaderboard = ServiceLocator.getLeaderboardService();
              logger.debug("MainGameExitDisplay - LeaderboardService available: {}", leaderboard != null);
              
              // Force use of real leaderboard system - always try it first
              try {
                  // Ensure leaderboard service is available
                  if (leaderboard == null) {
                      logger.warn("MainGameExitDisplay - LeaderboardService is null, creating new one");
                      ServiceLocator.registerLeaderboardService(
                          new com.csse3200.game.services.leaderboard.PersistentLeaderboardService("player-001"));
                      leaderboard = ServiceLocator.getLeaderboardService();
                  }
                  
                  if (leaderboard != null) {
                      // Check if there are any entries
                      var entries = leaderboard.getEntries(new com.csse3200.game.services.leaderboard.LeaderboardService.LeaderboardQuery(0, 10, false));
                      logger.info("MainGameExitDisplay - Found {} leaderboard entries in real system", entries.size());
                      
                      var controller = new com.csse3200.game.ui.leaderboard.LeaderboardController(leaderboard);
                      com.badlogic.gdx.scenes.scene2d.ui.Skin leaderboardSkin = com.csse3200.game.ui.leaderboard.MinimalSkinFactory.create();
                      com.csse3200.game.ui.leaderboard.LeaderboardPopup popup = new com.csse3200.game.ui.leaderboard.LeaderboardPopup(leaderboardSkin, controller);
                      popup.showOn(stage);
                      logger.info("MainGameExitDisplay - Successfully showing real leaderboard with {} entries", entries.size());
                      return; // Success, don't show mock data
                  }
              } catch (Exception e) {
                  logger.error("MainGameExitDisplay - Error creating leaderboard popup: {}", e.getMessage(), e);
              }
              
              // Only show mock data if real leaderboard completely failed
              logger.warn("MainGameExitDisplay - Falling back to mock data because real leaderboard failed");
              List<PlayerRank> players = MockRanks.make(30);
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
