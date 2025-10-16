package com.csse3200.game.components.hero;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector3;
import com.csse3200.game.areas.MapEditor;
import com.csse3200.game.areas.terrain.TerrainComponent;
import com.csse3200.game.rendering.Renderer;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;

/**
 * Unit tests for HeroPlacementRules.
 *
 * Notes:
 * - Uses mockito-inline for static mocking of Renderer.getCurrentRenderer().
 * - Uses RETURNS_DEEP_STUBS to stub the chain renderer.getCamera().getCamera().
 * - Camera.unproject(Vector3) is stubbed to map "screen" coordinates to desired "world" coordinates.
 */
public class HeroPlacementRulesTest {

    @Test
    void screenToGridNoClamp_returns_null_when_renderer_or_camera_missing() {
        TerrainComponent terrain = mock(TerrainComponent.class);

        try (MockedStatic<Renderer> rendererStatic = mockStatic(Renderer.class)) {
            // Case 1: Renderer is null
            rendererStatic.when(Renderer::getCurrentRenderer).thenReturn(null);
            assertNull(HeroPlacementRules.screenToGridNoClamp(10, 10, terrain));

            // Case 2: Renderer present, but camera holder is null
            Renderer renderer = mock(Renderer.class, RETURNS_DEEP_STUBS);
            rendererStatic.when(Renderer::getCurrentRenderer).thenReturn(renderer);
            when(renderer.getCamera()).thenReturn(null);
            assertNull(HeroPlacementRules.screenToGridNoClamp(10, 10, terrain));
        }
    }

    @Test
    void screenToGridNoClamp_maps_inside_bounds_and_clamps_outside_to_null() {
        TerrainComponent terrain = mock(TerrainComponent.class);
        when(terrain.getTileSize()).thenReturn(1f);
        // Map bounds: width=10, height=8 (valid gx: 0..9, gy: 0..7)
        when(terrain.getMapBounds(0)).thenReturn(new GridPoint2(10, 8));

        Renderer renderer = mock(Renderer.class, RETURNS_DEEP_STUBS);
        Camera camera = mock(Camera.class);

        // Stub Camera.unproject to convert screen -> world in two scenarios:
        // 1) First call -> (3.2, 5.9) -> gx=3, gy=5  (inside)
        // 2) Second call -> (12.0, 1.0) -> gx=12, gy=1 (outside on x -> null)
        final float[][] worldSeq = new float[][]{
                {3.2f, 5.9f},
                {12.0f, 1.0f}
        };
        final int[] idx = {0};
        doAnswer(inv -> {
            Vector3 v = inv.getArgument(0);
            v.x = worldSeq[idx[0]][0];
            v.y = worldSeq[idx[0]][1];
            if (idx[0] < worldSeq.length - 1) idx[0]++;
            return null;
        }).when(camera).unproject(any(Vector3.class));

        when(renderer.getCamera().getCamera()).thenReturn(camera);

        try (MockedStatic<Renderer> rendererStatic = mockStatic(Renderer.class)) {
            rendererStatic.when(Renderer::getCurrentRenderer).thenReturn(renderer);

            // Inside bounds
            GridPoint2 cell1 = HeroPlacementRules.screenToGridNoClamp(100, 200, terrain);
            assertNotNull(cell1);
            assertEquals(new GridPoint2(3, 5), cell1);

            // Outside (gx=12 >= 10) -> null
            GridPoint2 cell2 = HeroPlacementRules.screenToGridNoClamp(100, 200, terrain);
            assertNull(cell2);
        }
    }

    @Test
    void isBlockedCell_handles_null_editor_empty_map_and_present_key() {
        // Case 1: null mapEditor should be treated as NOT blocked
        assertFalse(HeroPlacementRules.isBlockedCell(2, 3, null));

        // Case 2: mapEditor present but invalid map is null or empty -> not blocked
        MapEditor editor = mock(MapEditor.class);
        when(editor.getInvalidTiles()).thenReturn(null);
        assertFalse(HeroPlacementRules.isBlockedCell(1, 1, editor));

        when(editor.getInvalidTiles()).thenReturn(new HashMap<>());
        assertFalse(HeroPlacementRules.isBlockedCell(1, 1, editor));

        // Case 3: map contains the "x,y" key -> blocked
        Map<String, GridPoint2> invalid = new HashMap<>();
        invalid.put("5,7", new GridPoint2(5, 7));
        when(editor.getInvalidTiles()).thenReturn(invalid);

        assertTrue(HeroPlacementRules.isBlockedCell(5, 7, editor));
        assertFalse(HeroPlacementRules.isBlockedCell(5, 6, editor));
        assertFalse(HeroPlacementRules.isBlockedCell(6, 7, editor));
    }
}
