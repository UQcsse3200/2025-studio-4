package com.csse3200.game.components.hero;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.GameStateService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for HeroSelectionComponent.
 *
 * Verifies that:
 * - The correct installer Runnable is invoked based on GameStateService.HeroType.
 * - The component disposes its host entity at the end of create().
 * - When GameStateService is null, it defaults to HERO branch.
 *
 * Notes:
 * - We mock Gdx.app to avoid NPE in Gdx.app.log.
 * - We mock ServiceLocator.getGameStateService() via mockito-inline static mocking.
 */
public class HeroSelectionComponentTest {

    /** Minimal entity that records whether dispose() was called, without touching engine internals. */
    static class TestEntity extends Entity {
        boolean disposed = false;

        @Override
        public void dispose() {
            // DO NOT call super.dispose() to avoid engine-side dependencies in tests.
            disposed = true;
        }
    }

    @BeforeEach
    void setupGdx() {
        Application mockApp = mock(Application.class);
        // We don't need to execute anything, just avoid NPE on log
        doNothing().when(mockApp).log(anyString(), anyString());
        Gdx.app = mockApp;
    }

    private static HeroSelectionComponent makeComponent(Runnable hero, Runnable eng, Runnable sam) {
        return new HeroSelectionComponent(hero, eng, sam);
    }

    private static TestEntity attachToNewEntity(Component c) {
        TestEntity e = new TestEntity();
        e.addComponent(c);
        return e;
    }

    @Test
    void whenSelectedHero_callsHeroInstaller_andDisposesEntity() {
        // Arrange selection = HERO
        GameStateService gameState = mock(GameStateService.class);
        when(gameState.getSelectedHero()).thenReturn(GameStateService.HeroType.HERO);

        Runnable hero = mock(Runnable.class);
        Runnable eng  = mock(Runnable.class);
        Runnable sam  = mock(Runnable.class);

        HeroSelectionComponent comp = makeComponent(hero, eng, sam);
        TestEntity entity = attachToNewEntity(comp);

        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class)) {
            sl.when(ServiceLocator::getGameStateService).thenReturn(gameState);

            // Act
            entity.create();

            // Assert
            verify(hero, times(1)).run();
            verify(eng,  never()).run();
            verify(sam,  never()).run();

            assertTrue(entity.disposed, "Entity should be disposed after selection is applied");
        }
    }

    @Test
    void whenSelectedEngineer_callsEngineerInstaller_andDisposesEntity() {
        // Arrange selection = ENGINEER
        GameStateService gameState = mock(GameStateService.class);
        when(gameState.getSelectedHero()).thenReturn(GameStateService.HeroType.ENGINEER);

        Runnable hero = mock(Runnable.class);
        Runnable eng  = mock(Runnable.class);
        Runnable sam  = mock(Runnable.class);

        HeroSelectionComponent comp = makeComponent(hero, eng, sam);
        TestEntity entity = attachToNewEntity(comp);

        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class)) {
            sl.when(ServiceLocator::getGameStateService).thenReturn(gameState);

            // Act
            entity.create();

            // Assert
            verify(eng,  times(1)).run();
            verify(hero, never()).run();
            verify(sam, never()).run();

            assertTrue(entity.disposed, "Entity should be disposed after selection is applied");
        }
    }

    @Test
    void whenSelectedSamurai_callsSamuraiInstaller_andDisposesEntity() {
        // Arrange selection = SAMURAI
        GameStateService gameState = mock(GameStateService.class);
        when(gameState.getSelectedHero()).thenReturn(GameStateService.HeroType.SAMURAI);

        Runnable hero = mock(Runnable.class);
        Runnable eng  = mock(Runnable.class);
        Runnable sam  = mock(Runnable.class);

        HeroSelectionComponent comp = makeComponent(hero, eng, sam);
        TestEntity entity = attachToNewEntity(comp);

        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class)) {
            sl.when(ServiceLocator::getGameStateService).thenReturn(gameState);

            // Act
            entity.create();

            // Assert
            verify(sam,  times(1)).run();
            verify(hero, never()).run();
            verify(eng,  never()).run();

            assertTrue(entity.disposed, "Entity should be disposed after selection is applied");
        }
    }

    @Test
    void whenGameStateIsNull_defaultsToHero_andDisposesEntity() {
        // Arrange: ServiceLocator returns null -> default HERO branch
        Runnable hero = mock(Runnable.class);
        Runnable eng  = mock(Runnable.class);
        Runnable sam  = mock(Runnable.class);

        HeroSelectionComponent comp = makeComponent(hero, eng, sam);
        TestEntity entity = attachToNewEntity(comp);

        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class)) {
            sl.when(ServiceLocator::getGameStateService).thenReturn(null);

            // Act
            entity.create();

            // Assert
            verify(hero, times(1)).run();
            verify(eng,  never()).run();
            verify(sam,  never()).run();

            assertTrue(entity.disposed, "Entity should be disposed after selection is applied");
        }
    }
}
