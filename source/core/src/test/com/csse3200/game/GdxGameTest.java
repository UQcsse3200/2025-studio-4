package com.csse3200.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GdxGame class focusing on main function functionality.
 */
@ExtendWith(GameExtension.class)
class GdxGameTest {
    
    private GdxGame game;
    
    @BeforeEach
    void setUp() {
        game = new GdxGame();
        ServiceLocator.clear();
    }
    
    @Test
    void testGameInitialization() {
        // Test that game can be created
        assertNotNull(game);
        assertTrue(game instanceof Game);
    }
    
    @Test
    void testScreenTypeEnum() {
        // Test that all expected screen types exist
        GdxGame.ScreenType[] screenTypes = GdxGame.ScreenType.values();
        
        assertTrue(screenTypes.length >= 7);
        
        // Check for expected screen types
        boolean hasMainMenu = false;
        boolean hasMainGame = false;
        boolean hasSettings = false;
        boolean hasSaveSelection = false;
        boolean hasOpeningCutscene = false;
        boolean hasVictory = false;
        boolean hasMapSelection = false;
        
        for (GdxGame.ScreenType type : screenTypes) {
            switch (type) {
                case MAIN_MENU -> hasMainMenu = true;
                case MAIN_GAME -> hasMainGame = true;
                case SETTINGS -> hasSettings = true;
                case SAVE_SELECTION -> hasSaveSelection = true;
                case OPENING_CUTSCENE -> hasOpeningCutscene = true;
                case VICTORY -> hasVictory = true;
                case MAP_SELECTION -> hasMapSelection = true;
            }
        }
        
        assertTrue(hasMainMenu, "MAIN_MENU screen type should exist");
        assertTrue(hasMainGame, "MAIN_GAME screen type should exist");
        assertTrue(hasSettings, "SETTINGS screen type should exist");
        assertTrue(hasSaveSelection, "SAVE_SELECTION screen type should exist");
        assertTrue(hasOpeningCutscene, "OPENING_CUTSCENE screen type should exist");
        assertTrue(hasVictory, "VICTORY screen type should exist");
        assertTrue(hasMapSelection, "MAP_SELECTION screen type should exist");
    }
    
    @Test
    void testSetScreenBasic() {
        // Test basic screen setting
        game.setScreen(GdxGame.ScreenType.MAIN_MENU);
        
        Screen currentScreen = game.getScreen();
        assertNotNull(currentScreen);
    }
    
    @Test
    void testSetScreenWithContinue() {
        // Test screen setting with continue parameter
        game.setScreen(GdxGame.ScreenType.MAIN_GAME, true);
        
        Screen currentScreen = game.getScreen();
        assertNotNull(currentScreen);
    }
    
    @Test
    void testSetScreenWithSaveFileName() {
        // Test screen setting with save file name
        game.setScreen(GdxGame.ScreenType.MAIN_GAME, true, "test_save.json");
        
        Screen currentScreen = game.getScreen();
        assertNotNull(currentScreen);
    }
    
    @Test
    void testSetOpeningCutsceneWithBackground() {
        // Test setting opening cutscene with specific background
        game.setOpeningCutsceneWithBackground(2);
        
        Screen currentScreen = game.getScreen();
        assertNotNull(currentScreen);
    }
    
    @Test
    void testMusicONStaticVariable() {
        // Test that musicON variable exists and can be accessed
        int initialValue = GdxGame.musicON;
        assertTrue(initialValue >= 0);
        assertTrue(initialValue <= 1);
    }
    
    @Test
    void testScreenTransition() {
        // Test transitioning between different screens
        game.setScreen(GdxGame.ScreenType.MAIN_MENU);
        Screen menuScreen = game.getScreen();
        assertNotNull(menuScreen);
        
        game.setScreen(GdxGame.ScreenType.SETTINGS);
        Screen settingsScreen = game.getScreen();
        assertNotNull(settingsScreen);
        assertNotSame(menuScreen, settingsScreen);
    }
    
    @Test
    void testAllScreenTypesCanBeSet() {
        // Test that all screen types can be set without throwing exceptions
        for (GdxGame.ScreenType screenType : GdxGame.ScreenType.values()) {
            try {
                game.setScreen(screenType);
                // Some screens might be null (like default case), which is acceptable
                // The important thing is that no exception is thrown
            } catch (Exception e) {
                fail("Setting screen type " + screenType + " should not throw exception: " + e.getMessage());
            }
        }
    }
}
