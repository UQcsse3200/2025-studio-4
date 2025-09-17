package com.csse3200.game.screens;

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
import com.csse3200.game.components.maingame.MainGameExitDisplay;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.services.leaderboard.LeaderboardService;
import com.csse3200.game.ui.UIComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.csse3200.game.components.PlayerScoreComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.SaveGameService;


import java.util.List;

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
            // Remove existing UI if present
            if (table != null && table.getStage() != null) {
                table.remove();
            }

            int finalScore = 0;
            for (Entity entity : ServiceLocator.getEntityService().getEntities()) {
                PlayerScoreComponent scoreComponent = entity.getComponent(PlayerScoreComponent.class);
                if (scoreComponent != null) {
                    finalScore = scoreComponent.getTotalScore();
                    break;
                }
            }

            // Add background image to stage
            Image gameOverBackground = new Image(ServiceLocator.getResourceService()
                    .getAsset("images/Game_Over.png", Texture.class));
            gameOverBackground.setFillParent(true);
            stage.addActor(gameOverBackground);

            // Create main table for content layout
            table = new Table();
            table.setFillParent(true);

            // Create button container
            Table buttonTable = new Table();
            buttonTable.center();

            // Create text field for name entry
            TextField nameField = new TextField("", skin);
            nameField.setMessageText("Enter your name");
            nameField.setMaxLength(12);
            nameField.setWidth(250f);
            buttonTable.add(nameField).size(250f, 60f).pad(10f);
            buttonTable.row();

            // Create custom button style
            TextButtonStyle customButtonStyle = createCustomButtonStyle();

            // Restart button
            TextButton restartBtn = new TextButton("Restart Game", customButtonStyle);
            int finalScore1 = finalScore;
            restartBtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent changeEvent, Actor actor) {
                    logger.debug("Restart button clicked");
                    String playerName = nameField.getText().isEmpty() ? "Player" : nameField.getText();
                    saveScore(playerName, finalScore1);
                    entity.getEvents().trigger("restart");
                }
            });

            // Main menu button
            TextButton mainMenuBtn = new TextButton("Main Menu", customButtonStyle);
            int finalScore2 = finalScore;
            mainMenuBtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent changeEvent, Actor actor) {
                    logger.debug("Main Menu button clicked");
                    String playerName = nameField.getText().isEmpty() ? "Player" : nameField.getText();
                    saveScore(playerName, finalScore2);
                    entity.getEvents().trigger("gameover");
                }
            });

            // Set button colors
            restartBtn.getLabel().setColor(Color.BLUE); // Normal blue
            mainMenuBtn.getLabel().setColor(Color.BLUE); // Normal blue

            // Add buttons to button table
            buttonTable.add(restartBtn).size(250f, 60f).pad(15f);
            buttonTable.row();
            buttonTable.add(mainMenuBtn).size(250f, 60f).pad(15f);

            // Add space at top
            table.add().expand();
            table.row();

            // Add buttons to main table bottom
            table.add(buttonTable).bottom().padBottom(150f);

            stage.addActor(table);
            logger.info("Game Over screen with background image displayed successfully");
        } catch (Exception e) {
            logger.error("Error displaying Game Over screen: {}", e.getMessage());
        }
    }

    private void saveScore(String playerName, int finalScore) {
        LeaderboardService leaderboard = ServiceLocator.getLeaderboardService();
        SaveGameService saveGameService = ServiceLocator.getSaveGameService();

        if (leaderboard != null && saveGameService != null) {
            leaderboard.addEntry(playerName, finalScore);

            LeaderboardService.LeaderboardQuery query = new LeaderboardService.LeaderboardQuery(0, 100, false);
            List<LeaderboardService.LeaderboardEntry> entries = leaderboard.getEntries(query);

            // 保存排行榜
            saveGameService.save("leaderboard", entries);

            logger.info("Score {} submitted and saved for player '{}'", finalScore, playerName);
        } else {
            logger.warn("Leaderboard or SaveGameService not available, score not saved.");
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

    @Override
    public void dispose() {
        if (table != null) {
            table.clear();
        }
        super.dispose();
    }
}
