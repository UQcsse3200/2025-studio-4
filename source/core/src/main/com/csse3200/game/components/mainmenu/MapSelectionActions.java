package com.csse3200.game.components.mainmenu;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.csse3200.game.GdxGame;
import com.csse3200.game.areas.ForestGameArea;
import com.csse3200.game.areas2.MapTwo.ForestGameArea2;
import com.csse3200.game.ui.UIComponent;
import com.csse3200.game.utils.Difficulty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Handles mapSelected/back events from MapSelectionDisplay. */
public class MapSelectionActions extends UIComponent {
    private static final Logger logger = LoggerFactory.getLogger(MapSelectionActions.class);
    private final GdxGame game;

    public MapSelectionActions(GdxGame game) { this.game = game; }

    @Override
    public void create() {
        super.create();
        entity.getEvents().addListener("mapSelected", this::onMapSelected);
        entity.getEvents().addListener("backToMainMenu", this::onBack);
    }

    @Override
    protected void draw(SpriteBatch batch) { /* no-op */ }

    private void onMapSelected(String mapId, Difficulty difficulty) {
        logger.info("Map selected: {}", mapId);
        // NEW GAME with mapId passed as the 3rd arg:
        ForestGameArea.gameDifficulty = difficulty;
        ForestGameArea2.gameDifficulty = difficulty;
        game.setScreen(GdxGame.ScreenType.MAIN_GAME, false, mapId);
    }

    private void onBack() {
        logger.debug("Back to main menu");
        game.setScreen(GdxGame.ScreenType.MAIN_MENU);
    }
}
