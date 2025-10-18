package com.csse3200.game.components.maingame;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.files.UserSettings;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.services.PlayerNameService;
import com.csse3200.game.services.PlayerAvatarService;
import com.csse3200.game.ui.UIComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * UI component that displays the player's name and avatar in the top-left corner of the game screen.
 * Shows the player's avatar image alongside their name.
 */
public class PlayerNameDisplay extends UIComponent {
    private static final Logger logger = LoggerFactory.getLogger(PlayerNameDisplay.class);
    private static final float Z_INDEX = 1f;
    
    private Table table;
    private Label playerNameLabel;
    private Image avatarImage;
    private PlayerNameService playerNameService;
    private PlayerAvatarService playerAvatarService;
    
    @Override
    public void create() {
        super.create();
        playerNameService = ServiceLocator.getPlayerNameService();
        playerAvatarService = ServiceLocator.getPlayerAvatarService();
        addActors();
    }
    
    private void addActors() {
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
        table.top().left();
        table.setFillParent(true);
        
        // Get player info from services
        String playerName = playerNameService != null ? playerNameService.getPlayerName() : "Player";
        String avatarId = playerAvatarService != null ? playerAvatarService.getPlayerAvatar() : "avatar_1";
        
        // Create avatar image
        avatarImage = createAvatarImage(avatarId);
        
        // Create player name label
        Label.LabelStyle labelStyle = new Label.LabelStyle(skin.get(Label.LabelStyle.class));
        labelStyle.fontColor = Color.WHITE;
        playerNameLabel = new Label(playerName, labelStyle);
        
        // Add padding and positioning
        table.pad(20f, 20f, 0f, 20f);
        
        // Add avatar and name horizontally
        if (avatarImage != null) {
            table.add(avatarImage).size(40, 40).padRight(10);
        }
        table.add(playerNameLabel);
        
        stage.addActor(table);
        logger.debug("Player name display added: Player: {}", playerName);

        applyUiScale();
    }

    /**
     * Apply UI scale from user settings
     */
    private void applyUiScale() {
        UserSettings.Settings settings = UserSettings.get();
        if (table != null) {
            table.setTransform(true);
            table.validate();
            table.setOrigin(0f, table.getHeight());
            table.setScale(settings.uiScale);
        }
    }

    @Override
    public void draw(com.badlogic.gdx.graphics.g2d.SpriteBatch batch) {
        // draw is handled by the stage
    }
    
    @Override
    public float getZIndex() {
        return Z_INDEX;
    }
    
    private Image createAvatarImage(String avatarId) {
        if (playerAvatarService == null) {
            return null;
        }
        
        try {
            String imagePath = playerAvatarService.getAvatarImagePath(avatarId);
            Texture texture = ServiceLocator.getResourceService().getAsset(imagePath, Texture.class);
            TextureRegion region = new TextureRegion(texture);
            TextureRegionDrawable drawable = new TextureRegionDrawable(region);
            return new Image(drawable);
        } catch (Exception e) {
            logger.warn("Failed to load avatar image for {}: {}", avatarId, e.getMessage());
            return null;
        }
    }
    
    /**
     * Updates the displayed player name and avatar
     */
    public void updatePlayerName() {
        if (playerNameLabel != null && playerNameService != null) {
            String playerName = playerNameService.getPlayerName();
            playerNameLabel.setText(playerName);
            logger.debug("Player name updated to: {}", playerName);
        }
    }
    
    /**
     * Updates the displayed avatar
     */
    public void updateAvatar() {
        if (playerAvatarService != null) {
            // Recreate the entire UI to update avatar
            addActors();
            logger.debug("Player avatar updated");
        }
    }
    
    /**
     * Updates both name and avatar
     */
    public void updatePlayerInfo() {
        addActors();
        logger.debug("Player info updated");
    }
}
