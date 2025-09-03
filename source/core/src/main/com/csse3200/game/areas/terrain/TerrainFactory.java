package com.csse3200.game.areas.terrain;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapImageLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.areas.terrain.TerrainComponent.TerrainOrientation;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;

/** Factory for creating game terrains. */
public class TerrainFactory {
  private static final GridPoint2 MAP_SIZE = new GridPoint2(30, 30);

  private final OrthographicCamera camera;
  private final TerrainOrientation orientation;

  /**
   * Create a terrain factory with Orthogonal orientation
   *
   * @param cameraComponent Camera to render terrains to. Must be ortographic.
   */
  public TerrainFactory(CameraComponent cameraComponent) {
    this(cameraComponent, TerrainOrientation.ORTHOGONAL);
  }

  /**
   * Create a terrain factory
   *
   * @param cameraComponent Camera to render terrains to. Must be orthographic.
   * @param orientation orientation to render terrain at
   */
  public TerrainFactory(CameraComponent cameraComponent, TerrainOrientation orientation) {
    this.camera = (OrthographicCamera) cameraComponent.getCamera();
    this.orientation = orientation;
  }

  /**
   * Create a terrain of the given type, using the orientation of the factory.
   *
   * @param terrainType Terrain to create
   * @return Terrain component which renders the terrain
   */
  public TerrainComponent createTerrain(TerrainType terrainType) {
    switch (terrainType) {
      case FOREST_DEMO:
        // 使用简化的地图创建方式（只有 mmap 图层）
        return createForestDemoTerrain(0.5f);
      default:
        return null;
    }
  }

  // 简化的地形创建方法（只有 mmap 图层）
  private TerrainComponent createForestDemoTerrain(float tileWorldSize) {
    GridPoint2 tilePixelSize = new GridPoint2(32, 32);
    TiledMap tiledMap = createForestDemoTiles(tilePixelSize);
    TiledMapRenderer renderer = createRenderer(tiledMap, tileWorldSize / tilePixelSize.x);
    return new TerrainComponent(camera, tiledMap, renderer, orientation, tileWorldSize);
  }

  private TiledMapRenderer createRenderer(TiledMap tiledMap, float tileScale) {
    switch (orientation) {
      case ORTHOGONAL:
        return new OrthogonalTiledMapRenderer(tiledMap, tileScale);
      default:
        return null;
    }
  }

  // 创建只包含 mmap 图层的地图
  private TiledMap createForestDemoTiles(GridPoint2 tileSize) {
    TiledMap tiledMap = new TiledMap();
    TiledMapTileLayer dummyLayer =
        new TiledMapTileLayer(MAP_SIZE.x, MAP_SIZE.y, tileSize.x, tileSize.y);
    tiledMap.getLayers().add(dummyLayer);

    Texture mmapTex = ServiceLocator.getResourceService().getAsset("images/mmap.png", Texture.class);
    mmapTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    TiledMapImageLayer mmapLayer = new TiledMapImageLayer(new TextureRegion(mmapTex), 0, 0);
    mmapLayer.setName("mmap");
    tiledMap.getLayers().add(mmapLayer);

    return tiledMap;
  }

  /** 只保留简化版（mmap） */
  public enum TerrainType {
    FOREST_DEMO  // 仅 mmap 图层
  }
}
