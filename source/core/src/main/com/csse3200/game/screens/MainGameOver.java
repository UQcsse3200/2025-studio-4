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
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.csse3200.game.components.maingame.MainGameExitDisplay;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.services.leaderboard.LeaderboardService;
import com.csse3200.game.services.PlayerNameService;
import com.csse3200.game.ui.UIComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.csse3200.game.components.PlayerScoreComponent;
import com.csse3200.game.entities.Entity;

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

            // Get player name from service (already entered at game start)
            String currentPlayerName = "Player";
            if (ServiceLocator.getPlayerNameService() != null) {
                String serviceName = ServiceLocator.getPlayerNameService().getPlayerName();
                if (serviceName != null && !serviceName.trim().isEmpty()) {
                    currentPlayerName = serviceName.trim();
                }
            }

            // Display player name and final score
            Label playerNameLabel = new Label("Player: " + currentPlayerName, skin, "large");
            playerNameLabel.setColor(Color.GOLD);
            buttonTable.add(playerNameLabel).pad(10f);
            buttonTable.row();
            
            Label finalScoreLabel = new Label("Final Score: " + finalScore, skin, "large");
            finalScoreLabel.setColor(Color.WHITE);
            buttonTable.add(finalScoreLabel).pad(10f);
            buttonTable.row();

            // Create custom button style
            TextButtonStyle customButtonStyle = createCustomButtonStyle();

            // Restart button
            TextButton restartBtn = new TextButton("Restart Game", customButtonStyle);
            final String playerName = currentPlayerName;
            int finalScore1 = finalScore;
            int finalLevel1 = level;
            int finalEnemiesKilled1 = enemiesKilled;
            long finalGameDuration1 = gameDuration;
            int finalWavesSurvived1 = wavesSurvived;
            restartBtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent changeEvent, Actor actor) {
                    logger.debug("Restart button clicked");
                    // Automatically save with the player name from game start
                    saveScore(playerName, finalScore1, finalLevel1, finalEnemiesKilled1, finalGameDuration1, finalWavesSurvived1);
                    entity.getEvents().trigger("restart");
                }
            });

            // Main menu button
            TextButton mainMenuBtn = new TextButton("Main Menu", customButtonStyle);
            int finalScore2 = finalScore;
            int finalLevel2 = level;
            int finalEnemiesKilled2 = enemiesKilled;
            long finalGameDuration2 = gameDuration;
            int finalWavesSurvived2 = wavesSurvived;
            mainMenuBtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent changeEvent, Actor actor) {
                    logger.debug("Main Menu button clicked");
                    // Automatically save with the player name from game start
                    saveScore(playerName, finalScore2, finalLevel2, finalEnemiesKilled2, finalGameDuration2, finalWavesSurvived2);
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

    public void saveScore(String playerName, int finalScore, int level, int enemiesKilled, long gameDuration, int wavesSurvived) {
        // Handle empty or null player name
        if (playerName == null || playerName.trim().isEmpty()) {
            playerName = "Player";
        }
        
        // Update the PlayerNameService with the entered name
        PlayerNameService playerNameService = ServiceLocator.getPlayerNameService();
        if (playerNameService != null) {
            playerNameService.setPlayerName(playerName);
        }
        
        LeaderboardService leaderboard = ServiceLocator.getLeaderboardService();

        if (leaderboard != null) {
            // Add entry to leaderboard with extended game data
            leaderboard.addEntry(playerName, finalScore, level, enemiesKilled, gameDuration, wavesSurvived);

            logger.info("Game data submitted and saved for player '{}': Score={}, Level={}, Kills={}, Duration={}ms, Waves={}", 
                       playerName, finalScore, level, enemiesKilled, gameDuration, wavesSurvived);
        } else {
            logger.warn("LeaderboardService not available, score not saved.");
        }
    }

    // Keep backward compatibility method
    public void saveScore(String playerName, int finalScore) {
        saveScore(playerName, finalScore, 1, 0, 0, 0);
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
