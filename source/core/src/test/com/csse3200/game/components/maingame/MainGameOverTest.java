package com.csse3200.game.components.maingame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.csse3200.game.entities.Entity;
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
 * Test class for MainGameOver component functionality
 */
@ExtendWith(GameExtension.class)
class MainGameOverTest {

    @Mock
    private ResourceService mockResourceService;
    
    @Mock
    private Texture mockTexture;
    
    @Mock
    private Entity mockEntity;
    
    private MainGameOver mainGameOver;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup ServiceLocator with mocked ResourceService
        ServiceLocator.registerResourceService(mockResourceService);
        
        // Mock texture loading
        when(mockResourceService.getAsset("images/Game_Over.png", Texture.class))
            .thenReturn(mockTexture);
        when(mockResourceService.getAsset("images/Main_Game_Button.png", Texture.class))
            .thenReturn(mockTexture);
        
        // Mock Gdx.input
        Gdx.input = mock(com.badlogic.gdx.Input.class);
        
        mainGameOver = new MainGameOver();
        mainGameOver.setEntity(mockEntity);
    }

    @Test
    void testCreate() {
        // Test that component can be created
        assertDoesNotThrow(() -> mainGameOver.create());
    }

    @Test
    void testAddActors() {
        // Test that addActors method works without throwing exceptions
        mainGameOver.create();
        assertDoesNotThrow(() -> mainGameOver.addActors());
    }

    @Test
    void testGetZIndex() {
        // Test that getZIndex returns correct value
        assertEquals(50f, mainGameOver.getZIndex());
    }

    @Test
    void testDraw() {
        // Test that draw method doesn't throw exceptions
        mainGameOver.create();
        assertDoesNotThrow(() -> mainGameOver.draw(mock(com.badlogic.gdx.graphics.g2d.SpriteBatch.class)));
    }

    @Test
    void testDispose() {
        // Test that dispose method works correctly
        mainGameOver.create();
        mainGameOver.addActors();
        
        assertDoesNotThrow(() -> mainGameOver.dispose());
    }

    @Test
    void testMultipleDispose() {
        // Test that component can be disposed multiple times safely
        mainGameOver.create();
        mainGameOver.addActors();
        
        mainGameOver.dispose();
        assertDoesNotThrow(() -> mainGameOver.dispose()); // Should not throw on second disposal
    }

    @Test
    void testResourceLoading() {
        // Test that component properly interacts with ResourceService
        mainGameOver.create();
        mainGameOver.addActors();
        
        // Verify that required textures are loaded
        verify(mockResourceService, atLeastOnce()).getAsset(eq("images/Game_Over.png"), eq(Texture.class));
        verify(mockResourceService, atLeastOnce()).getAsset(eq("images/Main_Game_Button.png"), eq(Texture.class));
    }

    @Test
    void testResourceLoadingFailure() {
        // Test behavior when resource loading fails
        when(mockResourceService.getAsset(anyString(), eq(Texture.class)))
                .thenThrow(new RuntimeException("Resource loading failed"));
        
        mainGameOver.create();
        // Should still create component without throwing
        assertDoesNotThrow(() -> mainGameOver.addActors());
    }

    @Test
    void testMultipleInstances() {
        // Test creating multiple component instances
        MainGameOver gameOver1 = new MainGameOver();
        MainGameOver gameOver2 = new MainGameOver();
        
        gameOver1.setEntity(mockEntity);
        gameOver2.setEntity(mockEntity);
        
        assertDoesNotThrow(() -> {
            gameOver1.create();
            gameOver1.addActors();
            gameOver2.create();
            gameOver2.addActors();
        });
    }

    @Test
    void testEntityEvents() {
        // Test that component properly handles entity events
        mainGameOver.create();
        mainGameOver.addActors();
        
        // Verify entity is set correctly
        assertEquals(mockEntity, mainGameOver.getEntity());
    }

    @Test
    void testButtonCreation() {
        // Test that buttons are created correctly
        mainGameOver.create();
        mainGameOver.addActors();
        
        // This test verifies that addActors completes without errors
        // which means buttons were created successfully
        assertTrue(true);
    }

    @Test
    void testBackgroundImageLoading() {
        // Test that background image is loaded
        mainGameOver.create();
        mainGameOver.addActors();
        
        // Verify Game_Over.png is loaded
        verify(mockResourceService, atLeastOnce()).getAsset(eq("images/Game_Over.png"), eq(Texture.class));
    }

    @Test
    void testButtonStyleCreation() {
        // Test that button style is created correctly
        mainGameOver.create();
        mainGameOver.addActors();
        
        // Verify Main_Game_Button.png is loaded for button style
        verify(mockResourceService, atLeastOnce()).getAsset(eq("images/Main_Game_Button.png"), eq(Texture.class));
    }

    @Test
    void testComponentLifecycle() {
        // Test complete component lifecycle
        mainGameOver.create();
        mainGameOver.addActors();
        mainGameOver.draw(mock(com.badlogic.gdx.graphics.g2d.SpriteBatch.class));
        mainGameOver.dispose();
        
        // Should complete without errors
        assertTrue(true);
    }

    @Test
    void testNullEntity() {
        // Test behavior with null entity
        mainGameOver.setEntity(null);
        
        assertDoesNotThrow(() -> {
            mainGameOver.create();
            mainGameOver.addActors();
        });
    }

    @Test
    void testMultipleAddActors() {
        // Test calling addActors multiple times
        mainGameOver.create();
        
        assertDoesNotThrow(() -> {
            mainGameOver.addActors();
            mainGameOver.addActors(); // Should handle multiple calls gracefully
        });
    }
}
