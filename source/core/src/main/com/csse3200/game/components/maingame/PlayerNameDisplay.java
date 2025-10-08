package com.csse3200.game.components.maingame;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.services.PlayerNameService;
import com.csse3200.game.ui.UIComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * UI component that displays the player's name in the top-left corner of the game screen.
 * Shows "Player: [name]" where [name] is the current player's name from PlayerNameService.
 */
public class PlayerNameDisplay extends UIComponent {
    private static final Logger logger = LoggerFactory.getLogger(PlayerNameDisplay.class);
    private static final float Z_INDEX = 1f;
    
    private Table table;
    private Label playerNameLabel;
    private PlayerNameService playerNameService;
    
    @Override
    public void create() {
        super.create();
        playerNameService = ServiceLocator.getPlayerNameService();
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
        
        // Get player name from service
        String playerName = playerNameService != null ? playerNameService.getPlayerName() : "Player";
        
        // Create player name label
        playerNameLabel = new Label("Player: " + playerName, skin, "large");
        playerNameLabel.setColor(Color.WHITE);
        
        // Add padding and positioning
        table.pad(20f, 20f, 0f, 20f);
        table.add(playerNameLabel);
        
        stage.addActor(table);
        logger.debug("Player name display added: Player: {}", playerName);
    }
    
    @Override
    public void draw(com.badlogic.gdx.graphics.g2d.SpriteBatch batch) {
        // draw is handled by the stage
    }
    
    @Override
    public float getZIndex() {
        return Z_INDEX;
    }
    
    /**
     * Updates the displayed player name
     */
    public void updatePlayerName() {
        if (playerNameLabel != null && playerNameService != null) {
            String playerName = playerNameService.getPlayerName();
            playerNameLabel.setText("Player: " + playerName);
            logger.debug("Player name updated to: {}", playerName);
        }
    }
}
