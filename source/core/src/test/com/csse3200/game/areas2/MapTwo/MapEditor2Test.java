package com.csse3200.game.areas2.MapTwo;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.areas2.terrainTwo.TerrainComponent2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class MapEditor2Test {

  @BeforeEach
  void setUp() {
    ResourceService resourceService = mock(ResourceService.class);
    ServiceLocator.registerResourceService(resourceService);

    Texture mockTexture = mock(Texture.class);
    doNothing().when(mockTexture).setFilter(any(), any());
    when(mockTexture.getWidth()).thenReturn(32);
    when(mockTexture.getHeight()).thenReturn(32);
    when(resourceService.getAsset(anyString(), eq(Texture.class))).thenReturn(mockTexture);
  }

  @Test
  void generateEnemyPath_createsSecondPath() {
    TerrainComponent2 terrain = mock(TerrainComponent2.class);
    TiledMap tiledMap = new TiledMap();
    TiledMapTileLayer baseLayer = new TiledMapTileLayer(40, 40, 32, 32);
    baseLayer.setName("base-layer");
    tiledMap.getLayers().add(baseLayer);

    when(terrain.getMap()).thenReturn(tiledMap);

    Entity player = mock(Entity.class);
    MapEditor2 mapEditor2 = new MapEditor2(terrain, player);

    assertDoesNotThrow(mapEditor2::generateEnemyPath);

    Map<String, GridPoint2> invalidTiles = mapEditor2.getInvalidTiles();

    GridPoint2 verticalSegment = invalidTiles.get("33,15");
    assertNotNull(verticalSegment, "Second path vertical segment should be created");
    assertEquals(33, verticalSegment.x);
    assertEquals(15, verticalSegment.y);

    GridPoint2 horizontalSegment = invalidTiles.get("22,27");
    assertNotNull(horizontalSegment, "Second path horizontal segment should be created");
    assertEquals(22, horizontalSegment.x);
    assertEquals(27, horizontalSegment.y);
  }
}
