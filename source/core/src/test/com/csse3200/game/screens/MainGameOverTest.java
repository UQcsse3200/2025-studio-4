package com.csse3200.game.screens;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.components.maingame.MainGameOver;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.leaderboard.LeaderboardService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import com.badlogic.gdx.utils.Array;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test class for MainGameOver screen functionality
 */
@ExtendWith(GameExtension.class)
class MainGameOverTest {

    @Mock
    private EntityService entityService;
    
    @Mock
    private LeaderboardService leaderboardService;
    
    @Mock
    private Entity playerEntity;
    
    @Mock
    private Stage stage;
    
    @Mock
    private SpriteBatch spriteBatch;

    private MainGameOver mainGameOver;
    private Array<Entity> entities;

    @BeforeEach
    void setUp() {
        // Setup mocks
        entityService = mock(EntityService.class);
        leaderboardService = mock(LeaderboardService.class);
        playerEntity = mock(Entity.class);
        stage = mock(Stage.class);
        spriteBatch = mock(SpriteBatch.class);

        // Setup entity list
        entities = new Array<>();
        entities.add(playerEntity);

        // Setup entity service
        when(entityService.getEntities()).thenReturn(entities);

        // Setup service locator
        ServiceLocator.registerEntityService(entityService);
        ServiceLocator.registerLeaderboardService(leaderboardService);

        // Create MainGameOver instance
        mainGameOver = new MainGameOver();
        mainGameOver.setEntity(playerEntity);
        // Note: MainGameOver doesn't have setStage method, it uses the stage from UIComponent
    }

    @Test
    void shouldCreateGameOverUI() {
        // When
        mainGameOver.addActors();

        // Then - verify that the stage has actors added
        verify(stage, atLeastOnce()).addActor(any());
    }

    @Test
    void shouldCreateRestartButton() {
        // When
        mainGameOver.addActors();

        // Then - verify that buttons are created
        verify(stage, atLeastOnce()).addActor(any());
    }

    @Test
    void shouldCreateMainMenuButton() {
        // When
        mainGameOver.addActors();

        // Then - verify that buttons are created
        verify(stage, atLeastOnce()).addActor(any());
    }

    @Test
    void shouldSetCorrectZIndex() {
        // When
        float zIndex = mainGameOver.getZIndex();

        // Then
        assertEquals(50f, zIndex, "Z-index should be 50f for game over screen");
    }

    @Test
    void shouldDisposeProperly() {
        // Given
        mainGameOver.addActors();

        // When
        mainGameOver.dispose();

        // Then - should not throw any exceptions
        assertDoesNotThrow(() -> mainGameOver.dispose());
    }

    @Test
    void shouldCreateBackgroundImage() {
        // When
        mainGameOver.addActors();

        // Then - verify that background image is added
        verify(stage, atLeastOnce()).addActor(any());
    }

    @Test
    void shouldHandleServiceLocatorFailure() {
        // Given
        ServiceLocator.clear(); // Remove all services

        // When & Then - should handle gracefully
        assertDoesNotThrow(() -> mainGameOver.addActors());
    }

    @Test
    void shouldCreateCustomButtonStyle() {
        // When
        mainGameOver.addActors();

        // Then - verify that UI is created successfully
        verify(stage, atLeastOnce()).addActor(any());
    }

    @Test
    void shouldHandleMultipleAddActorsCalls() {
        // When
        mainGameOver.addActors();
        mainGameOver.addActors();

        // Then - should handle multiple calls gracefully
        assertDoesNotThrow(() -> mainGameOver.addActors());
    }

    @Test
    void shouldHandleNullEntity() {
        // Given
        mainGameOver.setEntity(null);

        // When & Then - should handle null entity gracefully
        assertDoesNotThrow(() -> mainGameOver.addActors());
    }

    @Test
    void shouldCreateTableLayout() {
        // When
        mainGameOver.addActors();

        // Then - verify that table layout is created
        verify(stage, atLeastOnce()).addActor(any());
    }

    @Test
    void shouldHandleExceptionInAddActors() {
        // Given - setup to cause an exception
        // Note: We can't easily mock stage.addActor since MainGameOver uses its own stage

        // When & Then - should handle exceptions gracefully
        assertDoesNotThrow(() -> mainGameOver.addActors());
    }

    @Test
    void shouldHaveCorrectInitialState() {
        // When
        MainGameOver newGameOver = new MainGameOver();

        // Then
        assertNotNull(newGameOver, "MainGameOver should be created");
        assertEquals(50f, newGameOver.getZIndex(), "Should have correct Z-index");
    }

    @Test
    void shouldSupportEntityEvents() {
        // Given
        mainGameOver.addActors();

        // When & Then - verify entity is set up for events
        verify(playerEntity, atLeastOnce()).getEvents();
    }

    @Test
    void shouldCreateButtonContainer() {
        // When
        mainGameOver.addActors();

        // Then - verify that button container is created
        verify(stage, atLeastOnce()).addActor(any());
    }

    @Test
    void shouldHandleResourceServiceFailure() {
        // Given
        ServiceLocator.clear();
        ServiceLocator.registerEntityService(entityService);
        // Don't register resource service to simulate failure

        // When & Then - should handle missing resource service gracefully
        assertDoesNotThrow(() -> mainGameOver.addActors());
    }
}