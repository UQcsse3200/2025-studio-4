package com.csse3200.game.areas;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.TerrainComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class MapEditorTest {

    @BeforeEach
    void setUp() {
        // Mock ResourceService for texture loading
        ResourceService resourceService = mock(ResourceService.class);
        ServiceLocator.registerResourceService(resourceService);

        // Mock EntityService
        EntityService entityService = mock(EntityService.class);
        ServiceLocator.registerEntityService(entityService);

        // Mock textures
        Texture mockTexture = mock(Texture.class);
        doNothing().when(mockTexture).setFilter(any(), any());
        when(mockTexture.getWidth()).thenReturn(32);
        when(mockTexture.getHeight()).thenReturn(32);

        // Return mock textures for all texture requests
        when(resourceService.getAsset(anyString(), eq(Texture.class))).thenReturn(mockTexture);
    }

    @Test
    void shouldCreateMapEditor() {
        TerrainComponent terrain = createMockTerrain();
        Entity player = mock(Entity.class);

        MapEditor mapEditor = new MapEditor(terrain, player);

        assertNotNull(mapEditor);
    }

    @Test
    void shouldHandleKeyInput() {
        TerrainComponent terrain = createMockTerrain();
        Entity player = mock(Entity.class);
        when(player.getPosition()).thenReturn(new Vector2(64f, 64f));

        MapEditor mapEditor = new MapEditor(terrain, player);

        // Test Q key returns true (handled)
        boolean resultQ = mapEditor.keyDown(Input.Keys.Q);
        assertTrue(resultQ);

        // Test other key returns false (not handled)
        boolean resultA = mapEditor.keyDown(Input.Keys.A);
        assertFalse(resultA);
    }

    @Test
    void shouldGenerateEnemyPath() {
        TerrainComponent terrain = createMockTerrain();
        Entity player = mock(Entity.class);

        MapEditor mapEditor = new MapEditor(terrain, player);

        // Should not throw exception when generating path
        assertDoesNotThrow(() -> mapEditor.generateEnemyPath());
    }

    @Test
    void shouldGeneratePlaceableAreas() {
        TerrainComponent terrain = createMockTerrain();
        Entity player = mock(Entity.class);

        MapEditor mapEditor = new MapEditor(terrain, player);

        // Should not throw exception when generating placeable areas
        assertDoesNotThrow(() -> mapEditor.generatePlaceableAreas());
    }

    @Test
    void shouldCleanupWithoutErrors() {
        TerrainComponent terrain = createMockTerrain();
        Entity player = mock(Entity.class);

        MapEditor mapEditor = new MapEditor(terrain, player);

        // Should not throw exception when cleaning up
        assertDoesNotThrow(() -> mapEditor.cleanup());
    }

    @Test
    void shouldEnableEditor() {
        TerrainComponent terrain = createMockTerrain();
        Entity player = mock(Entity.class);

        MapEditor mapEditor = new MapEditor(terrain, player);

        // Should not throw exception when enabling editor
        assertDoesNotThrow(() -> mapEditor.enableEditor());
    }

    @Test
    void shouldHandleNullTerrain() {
        Entity player = mock(Entity.class);

        MapEditor mapEditor = new MapEditor(null, player);

        assertNotNull(mapEditor);
        assertDoesNotThrow(() -> mapEditor.generateEnemyPath());
        assertDoesNotThrow(() -> mapEditor.generatePlaceableAreas());
    }

    @Test
    void shouldHandleNullPlayer() {
        TerrainComponent terrain = createMockTerrain();

        MapEditor mapEditor = new MapEditor(terrain, null);

        assertNotNull(mapEditor);
        boolean result = mapEditor.keyDown(Input.Keys.Q);
        assertTrue(result); // Still handles the key but doesn't crash
    }

    private TerrainComponent createMockTerrain() {
        TerrainComponent terrain = mock(TerrainComponent.class);
        TiledMap tiledMap = mock(TiledMap.class);
        MapLayers mapLayers = mock(MapLayers.class);
        TiledMapTileLayer baseLayer = mock(TiledMapTileLayer.class);

        when(terrain.getMap()).thenReturn(tiledMap);
        when(tiledMap.getLayers()).thenReturn(mapLayers);
        when(mapLayers.get(0)).thenReturn(baseLayer);
        when(mapLayers.getCount()).thenReturn(1);
        when(baseLayer.getWidth()).thenReturn(30);
        when(baseLayer.getHeight()).thenReturn(30);
        when(baseLayer.getTileWidth()).thenReturn(32);
        when(baseLayer.getTileHeight()).thenReturn(32);
        when(baseLayer.getName()).thenReturn("base-layer");
        when(terrain.getTileSize()).thenReturn(32f);
        when(terrain.getMapBounds(0)).thenReturn(new GridPoint2(30, 30));
        when(terrain.tileToWorldPosition(any(GridPoint2.class))).thenReturn(new Vector2(64f, 64f));

        return terrain;
    }
}