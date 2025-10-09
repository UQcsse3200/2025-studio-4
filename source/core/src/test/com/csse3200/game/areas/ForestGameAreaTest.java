package com.csse3200.game.areas;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.TerrainComponent;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.utils.Difficulty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class ForestGameTest {

    @BeforeEach
    void setUp() {
        // Mock ResourceService and register it
        ResourceService resourceService = mock(ResourceService.class);
        ServiceLocator.registerResourceService(resourceService);

        // Mock EntityService
        EntityService entityService = mock(EntityService.class);
        ServiceLocator.registerEntityService(entityService);

        // Mock PhysicsService
        PhysicsService physicsService = mock(PhysicsService.class);
        PhysicsEngine physicsEngine = mock(PhysicsEngine.class);
        when(physicsService.getPhysics()).thenReturn(physicsEngine);
        ServiceLocator.registerPhysicsService(physicsService);

        // Mock textures and music
        Texture mockTexture = mock(Texture.class);
        Music mockMusic = mock(Music.class);

        // Setup basic mock behavior
        when(resourceService.getAsset(anyString(), eq(Texture.class))).thenReturn(mockTexture);
        when(resourceService.getAsset(anyString(), eq(Music.class))).thenReturn(mockMusic);
        when(resourceService.loadForMillis(anyInt())).thenReturn(true);
        when(resourceService.getProgress()).thenReturn(100);
    }

    @Test
    void shouldCreateForestGameArea() {
        TerrainFactory terrainFactory = mock(TerrainFactory.class);
        ForestGameArea forestGameArea = new ForestGameArea(terrainFactory);
        assertNotNull(forestGameArea);
    }

    @Test
    void shouldCreateWithoutErrors() {
        TerrainFactory terrainFactory = mock(TerrainFactory.class);
        TerrainComponent terrainComponent = createMockTerrainComponent();
        when(terrainFactory.createTerrain(any())).thenReturn(terrainComponent);

        ForestGameArea forestGameArea = new ForestGameArea(terrainFactory);
        assertNotNull(forestGameArea);
    }

    @Test
    void shouldDisposeWithoutErrors() {
        TerrainFactory terrainFactory = mock(TerrainFactory.class);
        TerrainComponent terrainComponent = createMockTerrainComponent();
        when(terrainFactory.createTerrain(any())).thenReturn(terrainComponent);

        ForestGameArea forestGameArea = new ForestGameArea(terrainFactory);
        assertDoesNotThrow(forestGameArea::dispose);
    }

    @Test
    void shouldLoadAssets() {
        TerrainFactory terrainFactory = mock(TerrainFactory.class);
        TerrainComponent terrainComponent = createMockTerrainComponent();
        when(terrainFactory.createTerrain(any())).thenReturn(terrainComponent);

        ForestGameArea forestGameArea = new ForestGameArea(terrainFactory);
        assertNotNull(forestGameArea);
    }

    @Test
    void shouldSetAndRetainDifficulty() {
        TerrainFactory terrainFactory = mock(TerrainFactory.class);
        TerrainComponent terrainComponent = createMockTerrainComponent();
        when(terrainFactory.createTerrain(any())).thenReturn(terrainComponent);

        ForestGameArea forestGameArea = new ForestGameArea(terrainFactory);
        ForestGameArea.gameDifficulty = Difficulty.HARD;

        assertEquals(Difficulty.HARD, ForestGameArea.gameDifficulty,
                "ForestGameArea should correctly store the selected difficulty");

        ForestGameArea.gameDifficulty = Difficulty.EASY;
        assertEquals(Difficulty.EASY, ForestGameArea.gameDifficulty,
                "ForestGameArea should update the difficulty when reassigned");
    }

    private TerrainComponent createMockTerrainComponent() {
        TerrainComponent terrainComponent = mock(TerrainComponent.class);
        TiledMap tiledMap = mock(TiledMap.class);
        MapLayers mapLayers = mock(MapLayers.class);
        TiledMapTileLayer baseLayer = mock(TiledMapTileLayer.class);

        when(terrainComponent.getMap()).thenReturn(tiledMap);
        when(tiledMap.getLayers()).thenReturn(mapLayers);
        when(mapLayers.get(0)).thenReturn(baseLayer);
        when(mapLayers.getCount()).thenReturn(1);
        when(baseLayer.getWidth()).thenReturn(30);
        when(baseLayer.getHeight()).thenReturn(30);
        when(baseLayer.getTileWidth()).thenReturn(32);
        when(baseLayer.getTileHeight()).thenReturn(32);

        when(terrainComponent.getTileSize()).thenReturn(32f);
        when(terrainComponent.getMapBounds(0)).thenReturn(new GridPoint2(30, 30));
        when(terrainComponent.tileToWorldPosition(any(GridPoint2.class)))
                .thenReturn(new Vector2(64f, 64f));

        return terrainComponent;
    }
}
