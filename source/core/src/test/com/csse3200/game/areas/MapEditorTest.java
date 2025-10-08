package com.csse3200.game.areas;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

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
import com.csse3200.game.components.enemy.SpeedWaypointComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class MapEditorTest {

    @BeforeEach
    void setUp() {
        ServiceLocator.clear();
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
    void shouldGenerateEnemyPath() {
        TerrainComponent terrain = createMockTerrain();
        Entity player = mock(Entity.class);

        MapEditor mapEditor = new MapEditor(terrain, player);

        // Should not throw exception when generating path
        assertDoesNotThrow(() -> mapEditor.generateEnemyPath());
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
    void shouldHandleNullTerrain() {
        Entity player = mock(Entity.class);

        MapEditor mapEditor = new MapEditor(null, player);

        assertNotNull(mapEditor);
        assertDoesNotThrow(() -> mapEditor.generateEnemyPath());
        //assertDoesNotThrow(() -> mapEditor.generatePlaceableAreas());
    }

    @Test
    void shouldCreateSpeedWaypointsAtConfiguredLocations() {
        TerrainComponent terrain = createMockTerrain();
        Entity player = mock(Entity.class);

        MapEditor mapEditor = new MapEditor(terrain, player);
        mapEditor.generateEnemyPath();

        assertEquals(16, mapEditor.waypointList.size(), "Expected 8 keypoints and 8 speed points");

        java.util.Map<String, Entity> waypointLookup = mapEditor.waypointList.stream()
                .collect(java.util.stream.Collectors.toMap(
                        e -> asTileKey(e.getPosition()),
                        e -> e,
                        (existing, replacement) -> existing));

        String[] speedTiles = {"6,6", "7,6", "8,6", "9,6", "18,12", "19,12", "20,12", "21,12"};
        for (String tile : speedTiles) {
            Entity waypoint = waypointLookup.get(tile);
            assertNotNull(waypoint, "Missing speed waypoint at tile " + tile);
            assertNotNull(waypoint.getComponent(SpeedWaypointComponent.class),
                    "Expected SpeedWaypointComponent at tile " + tile);
        }

        String[] keyTiles = {"0,10", "5,10", "5,6", "12,6", "12,12", "25,12", "25,6", "28,6"};
        for (String tile : keyTiles) {
            Entity waypoint = waypointLookup.get(tile);
            assertNotNull(waypoint, "Missing key waypoint at tile " + tile);
            assertNull(waypoint.getComponent(SpeedWaypointComponent.class),
                    "Key waypoint should not have speed component at tile " + tile);
        }
    }

    private String asTileKey(Vector2 position) {
        int x = Math.round(position.x * 2);
        int y = Math.round(position.y * 2);
        return x + "," + y;
    }

    @AfterEach
    void tearDown() {
        ServiceLocator.clear();
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
