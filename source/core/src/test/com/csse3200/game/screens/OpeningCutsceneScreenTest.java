package com.csse3200.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.csse3200.game.GdxGame;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for OpeningCutsceneScreen functionality
 */
@ExtendWith(GameExtension.class)
class OpeningCutsceneScreenTest {

    @Mock
    private GdxGame mockGame;
    
    @Mock
    private ResourceService mockResourceService;
    
    @Mock
    private Texture mockTexture;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup ServiceLocator with mocked ResourceService
        ServiceLocator.registerResourceService(mockResourceService);
        
        // Mock texture loading
        when(mockResourceService.getAsset(anyString(), eq(Texture.class)))
                .thenReturn(mockTexture);
        
        // Mock Gdx.input
        Gdx.input = mock(com.badlogic.gdx.Input.class);
    }

    @Test
    void testOpeningCutsceneScreenCreation() {
        // Test that screen can be created with default constructor
        assertDoesNotThrow(() -> {
            OpeningCutsceneScreen screen = new OpeningCutsceneScreen(mockGame);
            assertNotNull(screen);
        });
    }

    @Test
    void testOpeningCutsceneScreenWithBackground() {
        // Test that screen can be created with specific background
        String testBackground = "images/test_background.png";
        
        assertDoesNotThrow(() -> {
            OpeningCutsceneScreen screen = new OpeningCutsceneScreen(mockGame, testBackground);
            assertNotNull(screen);
        });
    }

    @Test
    void testWithBackgroundStaticMethod() {
        // Test the static withBackground method
        assertDoesNotThrow(() -> {
            OpeningCutsceneScreen screen = OpeningCutsceneScreen.withBackground(mockGame, 0);
            assertNotNull(screen);
        });
    }

    @Test
    void testWithBackgroundInvalidIndex() {
        // Test withBackground with invalid index (should default to 0)
        assertDoesNotThrow(() -> {
            OpeningCutsceneScreen screen = OpeningCutsceneScreen.withBackground(mockGame, -1);
            assertNotNull(screen);
        });
        
        assertDoesNotThrow(() -> {
            OpeningCutsceneScreen screen = OpeningCutsceneScreen.withBackground(mockGame, 999);
            assertNotNull(screen);
        });
    }

    @Test
    void testGetAvailableBackgrounds() {
        // Test getting available backgrounds
        String[] backgrounds = OpeningCutsceneScreen.getAvailableBackgrounds();
        assertNotNull(backgrounds);
        assertTrue(backgrounds.length > 0);
        
        // Verify it returns a copy (not the original array)
        String[] backgrounds2 = OpeningCutsceneScreen.getAvailableBackgrounds();
        assertNotSame(backgrounds, backgrounds2);
    }

    @Test
    void testScreenLifecycle() {
        // Test screen lifecycle methods
        OpeningCutsceneScreen screen = new OpeningCutsceneScreen(mockGame);
        
        // Test show method
        assertDoesNotThrow(() -> screen.show());
        
        // Test resize method
        assertDoesNotThrow(() -> screen.resize(800, 600));
        
        // Test pause method
        assertDoesNotThrow(() -> screen.pause());
        
        // Test resume method
        assertDoesNotThrow(() -> screen.resume());
        
        // Test hide method
        assertDoesNotThrow(() -> screen.hide());
        
        // Test dispose method
        assertDoesNotThrow(() -> screen.dispose());
    }

    @Test
    void testRenderMethod() {
        // Test render method doesn't throw exceptions
        OpeningCutsceneScreen screen = new OpeningCutsceneScreen(mockGame);
        
        assertDoesNotThrow(() -> {
            screen.render(0.016f); // 60 FPS delta time
        });
    }

    @Test
    void testBackgroundOptions() {
        // Test that all background options are valid
        String[] backgrounds = OpeningCutsceneScreen.getAvailableBackgrounds();
        
        for (String background : backgrounds) {
            assertNotNull(background);
            assertFalse(background.isEmpty());
            assertTrue(background.startsWith("images/"));
        }
    }

    @Test
    void testMultipleScreenInstances() {
        // Test creating multiple screen instances
        OpeningCutsceneScreen screen1 = new OpeningCutsceneScreen(mockGame);
        OpeningCutsceneScreen screen2 = new OpeningCutsceneScreen(mockGame, "images/desert.png");
        
        assertNotNull(screen1);
        assertNotNull(screen2);
        assertNotSame(screen1, screen2);
    }

    @Test
    void testResourceServiceInteraction() {
        // Test that screen properly interacts with ResourceService
        new OpeningCutsceneScreen(mockGame);
        
        // Verify that loadTextures was called
        verify(mockResourceService, atLeastOnce()).loadTextures(any(String[].class));
        verify(mockResourceService, atLeastOnce()).loadAll();
    }

    @Test
    void testScreenDisposal() {
        // Test that screen can be disposed multiple times safely
        OpeningCutsceneScreen screen = new OpeningCutsceneScreen(mockGame);
        
        assertDoesNotThrow(() -> screen.dispose());
        assertDoesNotThrow(() -> screen.dispose()); // Should not throw on second disposal
    }
}
