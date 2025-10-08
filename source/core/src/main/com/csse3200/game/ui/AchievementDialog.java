package com.csse3200.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A dialog for displaying player achievements.
 * Shows achievement images in a scrollable grid layout.
 */
public class AchievementDialog extends Dialog {
    private static final Logger logger = LoggerFactory.getLogger(AchievementDialog.class);
    
    // Achievement image paths
    private static final String[] ACHIEVEMENT_IMAGES = {
        "images/tough survivor.jpg",
        "images/speed runner.jpg",
        "images/slayer.jpg",
        "images/perfect clear.jpg",
        "images/participation.jpg"
    };
    
    // Achievement names for display
    private static final String[] ACHIEVEMENT_NAMES = {
        "Tough Survivor",
        "Speed Runner",
        "Slayer",
        "Perfect Clear",
        "Participation"
    };
    
    // Achievement conditions/descriptions
    private static final String[] ACHIEVEMENT_CONDITIONS = {
        "Complete any wave in the game",
        "Defeat 5 enemies in a single game",
        "Defeat 20 enemies in a single game",
        "Win the game",
        "Play your first game!"
    };
    
    private TextButton closeButton;
    
    public AchievementDialog(String title, WindowStyle windowStyle) {
        super(title, windowStyle);
        
        // Set background image
        try {
            Texture bgTexture = ServiceLocator.getResourceService().getAsset(
                "images/name and leaderbooard background.png", Texture.class);
            setBackground(new TextureRegionDrawable(new TextureRegion(bgTexture)));
        } catch (Exception e) {
            // If background fails to load, continue without it
            logger.warn("Failed to load achievement dialog background", e);
        }
        
        createContent();
    }
    
    private void createContent() {
        // Create close button
        TextButton.TextButtonStyle buttonStyle = SimpleUI.buttonStyle();
        closeButton = new TextButton("Close", buttonStyle);
        
        // Add listener
        setupListeners();
        
        // Layout the dialog
        Table contentTable = getContentTable();
        contentTable.pad(15);
        
        // Title
        Label titleLabel = new Label("Achievements", SimpleUI.title());
        contentTable.add(titleLabel).padBottom(15);
        contentTable.row();
        
        // Create scroll pane for achievements
        Table achievementTable = createAchievementTable();
        ScrollPane scrollPane = new ScrollPane(achievementTable);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);
        
        contentTable.add(scrollPane).width(520).height(340).padBottom(15);
        contentTable.row();
        
        // Close button
        contentTable.add(closeButton).width(100).height(35);
        
        // Set dialog size and position
        setSize(560, 470);
    }
    
    private Table createAchievementTable() {
        Table table = new Table();
        table.defaults().pad(8);
        
        // Display achievements in a grid (2 columns)
        for (int i = 0; i < ACHIEVEMENT_IMAGES.length; i++) {
            Table achievementCard = createAchievementCard(
                ACHIEVEMENT_IMAGES[i], 
                ACHIEVEMENT_NAMES[i],
                ACHIEVEMENT_CONDITIONS[i],
                i
            );
            
            table.add(achievementCard).width(220).height(130);
            
            // Start new row after every 2 achievements
            if ((i + 1) % 2 == 0) {
                table.row();
            }
        }
        
        return table;
    }
    
    private Table createAchievementCard(String imagePath, String name, String condition, int index) {
        Table card = new Table();
        card.setBackground(SimpleUI.solid(new Color(0.2f, 0.2f, 0.2f, 0.8f)));
        card.pad(8);
        
        try {
            // Load achievement image
            Texture texture = ServiceLocator.getResourceService().getAsset(imagePath, Texture.class);
            Image achievementImage = new Image(texture);
            
            // Add image
            card.add(achievementImage).width(180).height(85).padBottom(4);
            card.row();
            
            // Add achievement name
            Label nameLabel = new Label(name, SimpleUI.label());
            nameLabel.setColor(Color.WHITE);
            card.add(nameLabel);
            
        } catch (Exception e) {
            logger.error("Failed to load achievement image: {}", imagePath, e);
            
            // Fallback: show text only
            Label errorLabel = new Label("Image not found", SimpleUI.label());
            errorLabel.setColor(Color.RED);
            card.add(errorLabel).padBottom(5);
            card.row();
            
            Label nameLabel = new Label(name, SimpleUI.label());
            nameLabel.setColor(Color.WHITE);
            card.add(nameLabel);
        }
        
        // Add click listener to show achievement condition
        card.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                showAchievementCondition(name, condition);
            }
        });
        
        return card;
    }
    
    private void setupListeners() {
        // Close button
        closeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                logger.debug("Achievement dialog closed");
                hide();
            }
        });
    }
    
    private void centerWindow() {
        Stage stage = getStage();
        if (stage != null) {
            setPosition(
                (stage.getWidth() - getWidth()) / 2,
                (stage.getHeight() - getHeight()) / 2
            );
        }
    }
    
    @Override
    public Dialog show(Stage stage) {
        super.show(stage);
        centerWindow();
        return this;
    }
    
    /**
     * Shows a popup with the achievement condition details
     */
    private void showAchievementCondition(String achievementName, String condition) {
        // Create condition dialog
        Dialog conditionDialog = new Dialog("Achievement Details", SimpleUI.windowStyle());
        
        Table contentTable = conditionDialog.getContentTable();
        contentTable.pad(20);
        
        // Achievement name
        Label nameLabel = new Label(achievementName, SimpleUI.title());
        nameLabel.setColor(Color.GOLD);
        contentTable.add(nameLabel).padBottom(15);
        contentTable.row();
        
        // Condition label
        Label conditionLabel = new Label("Condition:", SimpleUI.label());
        conditionLabel.setColor(Color.WHITE);
        contentTable.add(conditionLabel).left().padBottom(5);
        contentTable.row();
        
        // Condition text (wrapped for long text)
        Label conditionText = new Label(condition, SimpleUI.label());
        conditionText.setColor(Color.LIGHT_GRAY);
        conditionText.setWrap(true);
        contentTable.add(conditionText).width(300).padBottom(15);
        contentTable.row();
        
        // Close button
        TextButton okButton = new TextButton("OK", SimpleUI.buttonStyle());
        okButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                conditionDialog.hide();
            }
        });
        
        contentTable.add(okButton).width(100).height(30);
        
        // Set dialog size and show
        conditionDialog.setSize(350, 200);
        
        // Center the condition dialog
        Stage stage = getStage();
        if (stage != null) {
            conditionDialog.setPosition(
                (stage.getWidth() - conditionDialog.getWidth()) / 2,
                (stage.getHeight() - conditionDialog.getHeight()) / 2
            );
            stage.addActor(conditionDialog);
        }
        
        logger.debug("Showing condition for achievement: {}", achievementName);
    }
}

