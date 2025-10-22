package com.csse3200.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.csse3200.game.GdxGame;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.services.AchievementService;
import com.csse3200.game.services.GameSessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for VictoryScreen
 * Tests the victory screen functionality including:
 * - Screen creation and initialization
 * - Scrolling text functionality
 * - Victory display with achievements
 * - Button interactions
 * - Map progression
 */
@ExtendWith(GameExtension.class)
class VictoryScreenTest {

    private GdxGame mockGame;
    private ResourceService resourceService;
    private AchievementService achievementService;
    private GameSessionManager sessionManager;
    
    @BeforeEach
    void setUp() {
        // Mock GdxGame
        mockGame = mock(GdxGame.class);
        
        // Mock Graphics
        Graphics mockGraphics = mock(Graphics.class);
        when(mockGraphics.getWidth()).thenReturn(1920);
        when(mockGraphics.getHeight()).thenReturn(1080);
        when(mockGraphics.getDeltaTime()).thenReturn(0.016f);
        Gdx.graphics = mockGraphics;
        
        // Mock Input
        Gdx.input = mock(com.badlogic.gdx.Input.class);
        when(Gdx.input.justTouched()).thenReturn(false);
        
        // Mock Files
        Gdx.files = mock(com.badlogic.gdx.Files.class);
        FileHandle mockFileHandle = mock(FileHandle.class);
        when(Gdx.files.internal(anyString())).thenReturn(mockFileHandle);
        when(mockFileHandle.exists()).thenReturn(false); // Force default skin creation
        
        // Setup services
        resourceService = mock(ResourceService.class);
        achievementService = mock(AchievementService.class);
        sessionManager = mock(GameSessionManager.class);
        
        // Mock textures
        Texture mockTexture = mock(Texture.class);
        when(resourceService.getAsset(anyString(), eq(Texture.class))).thenReturn(mockTexture);
        
        // Mock successful submission
        when(sessionManager.submitScoreIfNotSubmitted(anyBoolean())).thenReturn(true);
        
        ServiceLocator.registerResourceService(resourceService);
        ServiceLocator.registerEntityService(new EntityService());
        ServiceLocator.registerAchievementService(achievementService);
        ServiceLocator.registerGameSessionManager(sessionManager);
    }

    @Test
    void testVictoryScreenCreation() {
        VictoryScreen victoryScreen = new VictoryScreen(mockGame);
        assertNotNull(victoryScreen);
    }
    
    @Test
    void testVictoryScreenCreationWithMapId() {
        VictoryScreen victoryScreen = new VictoryScreen(mockGame, "MapTwo");
        assertNotNull(victoryScreen);
    }
    
    @Test
    void testInitializeServicesCreatesResourceServiceIfNull() {
        ServiceLocator.clear();
        ServiceLocator.registerGameSessionManager(sessionManager); // Still needed to avoid NPE
        
        VictoryScreen victoryScreen = new VictoryScreen(mockGame);
        assertNotNull(victoryScreen);
        
        assertNotNull(ServiceLocator.getResourceService());
        assertNotNull(ServiceLocator.getEntityService());
    }
    
    @Test
    void testShowSetsInputProcessor() {
        VictoryScreen victoryScreen = new VictoryScreen(mockGame);
        assertNotNull(victoryScreen);
        
        victoryScreen.show();
        
        // Verify that input processor was set (stage is set as input processor)
        verify(Gdx.input, atLeastOnce()).setInputProcessor(any());
    }
    
    @Test
    void testRenderUpdatesTimeElapsed() throws Exception {
        VictoryScreen victoryScreen = new VictoryScreen(mockGame);
        
        // Get initial timeElapsed
        Field timeElapsedField = VictoryScreen.class.getDeclaredField("timeElapsed");
        timeElapsedField.setAccessible(true);
        float initialTime = timeElapsedField.getFloat(victoryScreen);
        
        // Render one frame
        victoryScreen.render(0.016f);
        
        float updatedTime = timeElapsedField.getFloat(victoryScreen);
        assertEquals(initialTime + 0.016f, updatedTime, 0.001f);
    }
    
    @Test
    void testScrollingTextExists() throws Exception {
        VictoryScreen victoryScreen = new VictoryScreen(mockGame);
        
        Field scrollLabelField = VictoryScreen.class.getDeclaredField("scrollLabel");
        scrollLabelField.setAccessible(true);
        Label scrollLabel = (Label) scrollLabelField.get(victoryScreen);
        
        assertNotNull(scrollLabel);
        assertTrue(scrollLabel.getText().toString().contains("VICTORY"));
    }
    
    @Test
    void testScrollingTextForFirstMap() throws Exception {
        VictoryScreen victoryScreen = new VictoryScreen(mockGame, null);
        
        Field scrollLabelField = VictoryScreen.class.getDeclaredField("scrollLabel");
        scrollLabelField.setAccessible(true);
        Label scrollLabel = (Label) scrollLabelField.get(victoryScreen);
        
        String text = scrollLabel.getText().toString();
        assertTrue(text.contains("first line of defense"));
        assertTrue(text.contains("next battle awaits"));
    }
    
    @Test
    void testScrollingTextForSecondMap() throws Exception {
        VictoryScreen victoryScreen = new VictoryScreen(mockGame, "MapTwo");
        
        Field scrollLabelField = VictoryScreen.class.getDeclaredField("scrollLabel");
        scrollLabelField.setAccessible(true);
        Label scrollLabel = (Label) scrollLabelField.get(victoryScreen);
        
        String text = scrollLabel.getText().toString();
        assertTrue(text.contains("both strongholds"));
        assertTrue(text.contains("true hero"));
    }
    
    @Test
    void testResizeUpdatesStageViewport() {
        VictoryScreen victoryScreen = new VictoryScreen(mockGame);
        
        // Should not throw exception
        assertDoesNotThrow(() -> victoryScreen.resize(1024, 768));
    }
    
    @Test
    void testPauseAndResumeDoNotThrowExceptions() {
        VictoryScreen victoryScreen = new VictoryScreen(mockGame);
        
        assertDoesNotThrow(() -> victoryScreen.pause());
        assertDoesNotThrow(() -> victoryScreen.resume());
    }
    
    @Test
    void testHideDoesNotThrowException() {
        VictoryScreen victoryScreen = new VictoryScreen(mockGame);
        
        assertDoesNotThrow(() -> victoryScreen.hide());
    }
    
    @Test
    void testDispose() {
        VictoryScreen victoryScreen = new VictoryScreen(mockGame);
        
        assertDoesNotThrow(() -> victoryScreen.dispose());
    }
    
    @Test
    void testDisposeMultipleTimes() {
        VictoryScreen victoryScreen = new VictoryScreen(mockGame);
        
        // Should not throw exception when disposed multiple times
        assertDoesNotThrow(() -> {
            victoryScreen.dispose();
            victoryScreen.dispose();
        });
    }
    
    @Test
    void testResourcesAreLoaded() {
        VictoryScreen victoryScreen = new VictoryScreen(mockGame);
        assertNotNull(victoryScreen);
        
        // Verify textures were requested
        verify(resourceService, atLeastOnce()).loadTextures(any(String[].class));
        verify(resourceService, atLeastOnce()).loadAll();
    }
    
    @Test
    void testAchievementImagesLoaded() {
        VictoryScreen victoryScreen = new VictoryScreen(mockGame);
        assertNotNull(victoryScreen);
        
        verify(resourceService, times(1)).loadTextures(argThat(textures -> {
            boolean hasAchievements = false;
            for (String texture : textures) {
                if (texture.contains("tough survivor.jpg") ||
                    texture.contains("speed runner.jpg") ||
                    texture.contains("slayer.jpg") ||
                    texture.contains("perfect clear.jpg") ||
                    texture.contains("participation.jpg")) {
                    hasAchievements = true;
                    break;
                }
            }
            return hasAchievements;
        }));
    }
    
    @Test
    void testSubmitCurrentScoreOnCreation() {
        VictoryScreen victoryScreen = new VictoryScreen(mockGame);
        assertNotNull(victoryScreen);
        
        verify(sessionManager, atLeastOnce()).submitScoreIfNotSubmitted(true);
    }
    
    @Test
    void testSubmitCurrentScoreOnShow() {
        VictoryScreen victoryScreen = new VictoryScreen(mockGame);
        victoryScreen.show();
        
        // Should be called at least twice (once on creation, once on show)
        verify(sessionManager, atLeast(2)).submitScoreIfNotSubmitted(true);
    }
    
    @Test
    void testScoreSubmissionHandlesNullSessionManager() {
        ServiceLocator.clear();
        ServiceLocator.registerResourceService(resourceService);
        ServiceLocator.registerEntityService(new EntityService());
        // Don't register session manager
        
        // Should not throw exception
        assertDoesNotThrow(() -> new VictoryScreen(mockGame));
    }
    
    @Test
    void testScrollSpeedIsPositive() throws Exception {
        VictoryScreen victoryScreen = new VictoryScreen(mockGame);
        
        Field scrollSpeedField = VictoryScreen.class.getDeclaredField("scrollSpeed");
        scrollSpeedField.setAccessible(true);
        float scrollSpeed = scrollSpeedField.getFloat(victoryScreen);
        
        assertTrue(scrollSpeed > 0, "Scroll speed should be positive");
    }
    
    @Test
    void testClickCountInitiallyZero() throws Exception {
        VictoryScreen victoryScreen = new VictoryScreen(mockGame);
        
        Field clickCountField = VictoryScreen.class.getDeclaredField("clickCount");
        clickCountField.setAccessible(true);
        int clickCount = clickCountField.getInt(victoryScreen);
        
        assertEquals(0, clickCount);
    }
    
    @Test
    void testCurrentStageInitiallyScrollingText() throws Exception {
        VictoryScreen victoryScreen = new VictoryScreen(mockGame);
        
        Field currentStageField = VictoryScreen.class.getDeclaredField("currentStage");
        currentStageField.setAccessible(true);
        Object currentStage = currentStageField.get(victoryScreen);
        
        assertEquals("SCROLLING_TEXT", currentStage.toString());
    }
    
    @Test
    void testButtonsNotShownInitially() throws Exception {
        VictoryScreen victoryScreen = new VictoryScreen(mockGame);
        
        Field buttonsShownField = VictoryScreen.class.getDeclaredField("buttonsShown");
        buttonsShownField.setAccessible(true);
        boolean buttonsShown = buttonsShownField.getBoolean(victoryScreen);
        
        assertFalse(buttonsShown);
    }
    
    @Test
    void testMapNameForNullMapId() throws Exception {
        VictoryScreen victoryScreen = new VictoryScreen(mockGame, null);
        
        java.lang.reflect.Method getMapName = VictoryScreen.class.getDeclaredMethod("getMapName");
        getMapName.setAccessible(true);
        String mapName = (String) getMapName.invoke(victoryScreen);
        
        assertEquals("Forest Demo Sector", mapName);
    }
    
    @Test
    void testMapNameForMapTwo() throws Exception {
        VictoryScreen victoryScreen = new VictoryScreen(mockGame, "MapTwo");
        
        java.lang.reflect.Method getMapName = VictoryScreen.class.getDeclaredMethod("getMapName");
        getMapName.setAccessible(true);
        String mapName = (String) getMapName.invoke(victoryScreen);
        
        assertEquals("Map Two Sector", mapName);
    }
    
    @Test
    void testMapNameForUnknownMap() throws Exception {
        VictoryScreen victoryScreen = new VictoryScreen(mockGame, "UnknownMap");
        
        java.lang.reflect.Method getMapName = VictoryScreen.class.getDeclaredMethod("getMapName");
        getMapName.setAccessible(true);
        String mapName = (String) getMapName.invoke(victoryScreen);
        
        assertEquals("Unknown Sector", mapName);
    }
    
    @Test
    void testScrollTextNotFinishedInitially() throws Exception {
        VictoryScreen victoryScreen = new VictoryScreen(mockGame);
        
        Field scrollTextFinishedField = VictoryScreen.class.getDeclaredField("scrollTextFinished");
        scrollTextFinishedField.setAccessible(true);
        boolean scrollTextFinished = scrollTextFinishedField.getBoolean(victoryScreen);
        
        assertFalse(scrollTextFinished);
    }
    
    @Test
    void testScrollTextNotCenteredInitially() throws Exception {
        VictoryScreen victoryScreen = new VictoryScreen(mockGame);
        
        Field scrollTextCenteredField = VictoryScreen.class.getDeclaredField("scrollTextCentered");
        scrollTextCenteredField.setAccessible(true);
        boolean scrollTextCentered = scrollTextCenteredField.getBoolean(victoryScreen);
        
        assertFalse(scrollTextCentered);
    }
}
