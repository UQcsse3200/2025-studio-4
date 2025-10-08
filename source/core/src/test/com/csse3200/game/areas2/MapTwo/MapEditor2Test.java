package com.csse3200.game.areas2.MapTwo;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas2.terrainTwo.TerrainComponent2;
import com.csse3200.game.components.enemy.SpeedWaypointComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class MapEditor2Test {

    @BeforeEach
    void setUp() {
        ServiceLocator.clear();

        ResourceService resourceService = mock(ResourceService.class);
        ServiceLocator.registerResourceService(resourceService);

        EntityService entityService = mock(EntityService.class);
        ServiceLocator.registerEntityService(entityService);

        Texture texture = mock(Texture.class);
        doNothing().when(texture).setFilter(any(), any());
        when(texture.getWidth()).thenReturn(32);
        when(texture.getHeight()).thenReturn(32);

        when(resourceService.getAsset(anyString(), eq(Texture.class))).thenReturn(texture);
    }

    @AfterEach
    void tearDown() {
        ServiceLocator.clear();
    }

    @Test
    void shouldConfigureSpeedWaypointsSeparateFromKeypoints() {
        TerrainComponent2 terrain = createMockTerrain();
        Entity player = mock(Entity.class);

        MapEditor2 editor = new MapEditor2(terrain, player);
        editor.generateEnemyPath();

        java.util.Map<String, Entity> waypointLookup = editor.waypointList.stream()
                .collect(Collectors.toMap(this::asTileKey, e -> e, (existing, replacement) -> existing));

        String[] speedTiles = {"5,2", "5,3", "5,4", "5,5", "7,10", "8,10", "9,10", "10,10"};
        String[] primaryKeyTiles = {"5,0", "5,10", "10,10", "15,14", "15,25", "5,25", "5,32"};
        String[] secondaryKeyTiles = {"28,6", "33,12", "33,21", "28,27", "18,27", "15,25", "5,25", "5,32"};

        java.util.Set<String> expectedTiles = new java.util.HashSet<>();
        java.util.Collections.addAll(expectedTiles, speedTiles);
        java.util.Collections.addAll(expectedTiles, primaryKeyTiles);
        java.util.Collections.addAll(expectedTiles, secondaryKeyTiles);

        assertEquals(expectedTiles, waypointLookup.keySet(),
                "Waypoint coordinates should exactly match configured key and speed tiles");

        for (String tile : speedTiles) {
            Entity waypoint = waypointLookup.get(tile);
            assertNotNull(waypoint, "Missing speed waypoint at tile " + tile);
            assertNotNull(waypoint.getComponent(SpeedWaypointComponent.class),
                    "Expected SpeedWaypointComponent at tile " + tile);
        }

        java.util.Set<String> speedTileSet = java.util.Set.of(speedTiles);

        for (String tile : primaryKeyTiles) {
            if (speedTileSet.contains(tile)) {
                continue;
            }
            Entity waypoint = waypointLookup.get(tile);
            assertNotNull(waypoint, "Missing primary key waypoint at tile " + tile);
            assertNull(waypoint.getComponent(SpeedWaypointComponent.class),
                    "Primary key waypoint should not have speed component at tile " + tile);
        }

        for (String tile : secondaryKeyTiles) {
            if (speedTileSet.contains(tile)) {
                continue;
            }
            Entity waypoint = waypointLookup.get(tile);
            assertNotNull(waypoint, "Missing secondary key waypoint at tile " + tile);
            assertNull(waypoint.getComponent(SpeedWaypointComponent.class),
                    "Secondary key waypoint should not have speed component at tile " + tile);
        }
    }

    private String asTileKey(Entity entity) {
        Vector2 position = entity.getPosition();
        int x = Math.round(position.x * 2);
        int y = Math.round(position.y * 2);
        return x + "," + y;
    }

    private TerrainComponent2 createMockTerrain() {
        TerrainComponent2 terrain = mock(TerrainComponent2.class);
        TiledMap tiledMap = mock(TiledMap.class);
        MapLayers mapLayers = mock(MapLayers.class);
        TiledMapTileLayer baseLayer = mock(TiledMapTileLayer.class);

        when(terrain.getMap()).thenReturn(tiledMap);
        when(tiledMap.getLayers()).thenReturn(mapLayers);
        when(mapLayers.get(0)).thenReturn(baseLayer);
        when(mapLayers.getCount()).thenReturn(1);
        when(baseLayer.getWidth()).thenReturn(40);
        when(baseLayer.getHeight()).thenReturn(40);
        when(baseLayer.getTileWidth()).thenReturn(32);
        when(baseLayer.getTileHeight()).thenReturn(32);
        when(baseLayer.getName()).thenReturn("base-layer");
        when(terrain.getTileSize()).thenReturn(32f);
        when(terrain.getMapBounds(0)).thenReturn(new GridPoint2(40, 40));
        when(terrain.tileToWorldPosition(any(GridPoint2.class))).thenReturn(new Vector2(64f, 64f));

        return terrain;
    }
}
