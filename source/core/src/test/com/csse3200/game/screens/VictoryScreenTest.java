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
 * Test class for VictoryScreen functionality
 */
@ExtendWith(GameExtension.class)
class VictoryScreenTest {

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
    void testVictoryScreenCreation() {
        // Test that screen can be created
        assertDoesNotThrow(() -> {
            VictoryScreen screen = new VictoryScreen(mockGame);
            assertNotNull(screen);
        });
    }

    @Test
    void testScreenLifecycle() {
        // Test screen lifecycle methods
        VictoryScreen screen = new VictoryScreen(mockGame);
        
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
        VictoryScreen screen = new VictoryScreen(mockGame);
        
        assertDoesNotThrow(() -> {
            screen.render(0.016f); // 60 FPS delta time
        });
    }

    @Test
    void testResourceServiceInteraction() {
        // Test that screen properly interacts with ResourceService
        new VictoryScreen(mockGame);
        
        // Verify that loadTextures was called with correct assets
        verify(mockResourceService, atLeastOnce()).loadTextures(any(String[].class));
        verify(mockResourceService, atLeastOnce()).loadAll();
        
        // Verify specific texture loading
        verify(mockResourceService, atLeastOnce()).getAsset(eq("images/Game_Victory.png"), eq(Texture.class));
        verify(mockResourceService, atLeastOnce()).getAsset(eq("images/Main_Menu_Button_Background.png"), eq(Texture.class));
        verify(mockResourceService, atLeastOnce()).getAsset(eq("images/Main_Game_Button.png"), eq(Texture.class));
    }

    @Test
    void testMultipleScreenInstances() {
        // Test creating multiple screen instances
        VictoryScreen screen1 = new VictoryScreen(mockGame);
        VictoryScreen screen2 = new VictoryScreen(mockGame);
        
        assertNotNull(screen1);
        assertNotNull(screen2);
        assertNotSame(screen1, screen2);
    }

    @Test
    void testScreenDisposal() {
        // Test that screen can be disposed multiple times safely
        VictoryScreen screen = new VictoryScreen(mockGame);
        
        assertDoesNotThrow(() -> screen.dispose());
        assertDoesNotThrow(() -> screen.dispose()); // Should not throw on second disposal
    }

    @Test
    void testAnimationTiming() {
        // Test that animation timing constants are reasonable
        VictoryScreen screen = new VictoryScreen(mockGame);
        
        // Test render with different delta times
        assertDoesNotThrow(() -> {
            screen.render(0.0f);   // Zero delta
            screen.render(0.016f); // 60 FPS
            screen.render(0.033f); // 30 FPS
            screen.render(1.0f);   // Very large delta
        });
    }

    @Test
    void testScreenWithNullGame() {
        // Test behavior with null game parameter
        assertThrows(NullPointerException.class, () -> {
            new VictoryScreen(null);
        });
    }

    @Test
    void testResourceLoadingFailure() {
        // Test behavior when resource loading fails
        when(mockResourceService.getAsset(anyString(), eq(Texture.class)))
                .thenThrow(new RuntimeException("Resource loading failed"));
        
        // Should still create screen without throwing
        assertDoesNotThrow(() -> {
            VictoryScreen screen = new VictoryScreen(mockGame);
            assertNotNull(screen);
        });
    }

    @Test
    void testScreenResizeDifferentAspectRatios() {
        // Test screen resize with different aspect ratios
        VictoryScreen screen = new VictoryScreen(mockGame);
        
        assertDoesNotThrow(() -> {
            screen.resize(1920, 1080); // 16:9
            screen.resize(1024, 768);  // 4:3
            screen.resize(800, 600);   // 4:3
            screen.resize(1280, 720);  // 16:9
        });
    }

    @Test
    void testScreenWithZeroDimensions() {
        // Test screen resize with zero dimensions
        VictoryScreen screen = new VictoryScreen(mockGame);
        
        assertDoesNotThrow(() -> {
            screen.resize(0, 0);
            screen.resize(100, 0);
            screen.resize(0, 100);
        });
    }

    @Test
    void testScreenLifecycleOrder() {
        // Test proper screen lifecycle order
        VictoryScreen screen = new VictoryScreen(mockGame);
        
        assertDoesNotThrow(() -> {
            screen.show();
            screen.resize(800, 600);
            screen.render(0.016f);
            screen.pause();
            screen.resume();
            screen.render(0.016f);
            screen.hide();
            screen.dispose();
        });
    }

    @Test
    void testScreenMultipleShowHide() {
        // Test multiple show/hide cycles
        VictoryScreen screen = new VictoryScreen(mockGame);
        
        assertDoesNotThrow(() -> {
            screen.show();
            screen.hide();
            screen.show();
            screen.hide();
            screen.show();
        });
    }

    @Test
    void testScreenMultiplePauseResume() {
        // Test multiple pause/resume cycles
        VictoryScreen screen = new VictoryScreen(mockGame);
        
        assertDoesNotThrow(() -> {
            screen.pause();
            screen.resume();
            screen.pause();
            screen.resume();
            screen.pause();
        });
    }
}
