package com.csse3200.game.components.hero;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector3;
import com.csse3200.game.areas.terrain.TerrainComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.factories.HeroFactory;
import com.csse3200.game.rendering.Renderer;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;

/**
 * Unit tests for HeroGhostPreview.
 *
 * Notes:
 * - Uses Mockito static mocking (mockito-inline) for HeroFactory, ServiceLocator, Renderer.
 * - Uses a TestEntity to track dispose() calls (without calling super.dispose() to avoid engine deps).
 * - Uses RETURNS_DEEP_STUBS to stub Renderer#getCamera().getCamera().
 */
public class HeroGhostPreviewTest {

    /** Simple entity that records whether dispose() was called. */
    static class TestEntity extends Entity {
        boolean disposed = false;

        @Override
        public void dispose() {
            // Mark disposed but DO NOT call super.dispose() to avoid engine-side dependencies in tests.
            disposed = true;
        }
    }

    @Test
    void spawnAt_registers_and_places_entity_correctly() {
        // Arrange terrain with 2x2 world-unit tiles
        TerrainComponent terrain = mock(TerrainComponent.class);
        when(terrain.getTileSize()).thenReturn(2f);

        // Prepare a controllable ghost entity
        TestEntity ghost = new TestEntity();

        try (MockedStatic<HeroFactory> heroFactoryMock = mockStatic(HeroFactory.class);
             MockedStatic<ServiceLocator> slMock = mockStatic(ServiceLocator.class)) {

            // HeroFactory should return our ghost instance
            heroFactoryMock.when(() -> HeroFactory.createHeroGhost(anyFloat())).thenReturn(ghost);

            // EntityService should be available and register() should be invoked
            EntityService es = mock(EntityService.class);
            slMock.when(ServiceLocator::getEntityService).thenReturn(es);

            HeroGhostPreview preview = new HeroGhostPreview(terrain, 0.5f);

            // Act: spawn ghost at grid (3,4) -> world position (6,8), scale (2,2)
            GridPoint2 cell = new GridPoint2(3, 4);
            preview.spawnAt(cell);

            // Assert
            verify(es, times(1)).register(ghost);
            assertTrue(preview.hasGhost(), "Ghost should exist after spawn");
            assertEquals(cell, preview.getCell(), "Stored grid cell should match the spawn cell");

            assertEquals(2f, ghost.getScale().x, 1e-6);
            assertEquals(2f, ghost.getScale().y, 1e-6);
            assertEquals(3 * 2f, ghost.getPosition().x, 1e-6);
            assertEquals(4 * 2f, ghost.getPosition().y, 1e-6);
        }
    }

    @Test
    void remove_disposes_and_clears_state() {
        // Arrange terrain with 1x1 tiles
        TerrainComponent terrain = mock(TerrainComponent.class);
        when(terrain.getTileSize()).thenReturn(1f);

        TestEntity ghost = new TestEntity();

        try (MockedStatic<HeroFactory> heroFactoryMock = mockStatic(HeroFactory.class);
             MockedStatic<ServiceLocator> slMock = mockStatic(ServiceLocator.class)) {

            heroFactoryMock.when(() -> HeroFactory.createHeroGhost(anyFloat())).thenReturn(ghost);
            EntityService es = mock(EntityService.class);
            slMock.when(ServiceLocator::getEntityService).thenReturn(es);

            HeroGhostPreview preview = new HeroGhostPreview(terrain, 0.7f);
            preview.spawnAt(new GridPoint2(1, 2));

            // Act
            preview.remove();

            // Assert
            assertTrue(ghost.disposed, "Ghost entity should be disposed on remove()");
            assertFalse(preview.hasGhost(), "Ghost flag should be cleared");
            assertNull(preview.getCell(), "Stored cell should be cleared");
        }
    }

    @Test
    void hitByScreen_returns_true_inside_cell_and_false_outside_or_when_missing_context() {
        // Arrange terrain with 1x1 tiles
        TerrainComponent terrain = mock(TerrainComponent.class);
        when(terrain.getTileSize()).thenReturn(1f);

        TestEntity ghost = new TestEntity();

        // Prepare a Renderer mock with deep stubs so we can stub getCamera().getCamera()
        Renderer rendererMock = mock(Renderer.class, RETURNS_DEEP_STUBS);
        Camera cam = mock(Camera.class);

        // First unproject -> world (5.5, 7.5): inside cell [5..6] x [7..8]
        // Second unproject -> world (4.9, 7.5): outside on X
        final float[][] worldSeq = new float[][]{
                {5.5f, 7.5f}, // inside
                {4.9f, 7.5f}  // outside
        };
        final int[] idx = {0};
        doAnswer(inv -> {
            Vector3 v = inv.getArgument(0);
            v.x = worldSeq[idx[0]][0];
            v.y = worldSeq[idx[0]][1];
            if (idx[0] < worldSeq.length - 1) idx[0]++;
            return null;
        }).when(cam).unproject(any(Vector3.class));

        // Deep-stub chain: renderer.getCamera().getCamera() -> cam
        when(rendererMock.getCamera().getCamera()).thenReturn(cam);

        try (MockedStatic<HeroFactory> heroFactoryMock = mockStatic(HeroFactory.class);
             MockedStatic<ServiceLocator> slMock = mockStatic(ServiceLocator.class);
             MockedStatic<Renderer> rendererStatic = mockStatic(Renderer.class)) {

            heroFactoryMock.when(() -> HeroFactory.createHeroGhost(anyFloat())).thenReturn(ghost);
            EntityService es = mock(EntityService.class);
            slMock.when(ServiceLocator::getEntityService).thenReturn(es);

            // Renderer.getCurrentRenderer() should return our deep-stubbed renderer
            rendererStatic.when(Renderer::getCurrentRenderer).thenReturn(rendererMock);

            HeroGhostPreview preview = new HeroGhostPreview(terrain, 0.5f);
            preview.spawnAt(new GridPoint2(5, 7)); // cell bounds x:[5..6], y:[7..8]

            // Act & Assert: first click maps inside, second maps outside
            assertTrue(preview.hitByScreen(100, 200), "First mapped point should be inside the cell");
            assertFalse(preview.hitByScreen(100, 200), "Second mapped point should be outside the cell");

            // No ghost -> always false
            preview.remove();
            assertFalse(preview.hitByScreen(100, 200), "No ghost => false");

            // No renderer/camera -> false
            preview.spawnAt(new GridPoint2(5, 7));
            rendererStatic.when(Renderer::getCurrentRenderer).thenReturn(null);
            assertFalse(preview.hitByScreen(100, 200), "No renderer => false");
        }
    }
}
