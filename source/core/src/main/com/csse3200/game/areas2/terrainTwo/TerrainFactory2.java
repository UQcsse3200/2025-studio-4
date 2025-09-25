package com.csse3200.game.areas2.terrainTwo;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapImageLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.areas2.terrainTwo.TerrainComponent2.TerrainOrientation;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.services.ServiceLocator;

/** Factory for creating game terrains. */
public class TerrainFactory2 {
  private static final GridPoint2 MAP_SIZE = new GridPoint2(32, 32);

  private final OrthographicCamera camera;
  private final TerrainOrientation orientation;

  /**
   * Create a terrain factory with Orthogonal orientation
   *
   * @param cameraComponent Camera to render terrains to. Must be ortographic.
   */
  public TerrainFactory2(CameraComponent cameraComponent) {
    this(cameraComponent, TerrainOrientation.ORTHOGONAL);
  }

  /**
   * Create a terrain factory
   *
   * @param cameraComponent Camera to render terrains to. Must be orthographic.
   * @param orientation orientation to render terrain at
   */
  public TerrainFactory2(CameraComponent cameraComponent, TerrainOrientation orientation) {
    this.camera = (OrthographicCamera) cameraComponent.getCamera();
    this.orientation = orientation;
  }

  /**
   * Create a terrain of the given type, using the orientation of the factory.
   *
   * @param terrainType Terrain to create
   * @return Terrain component which renders the terrain
   */
  public TerrainComponent2 createTerrain(TerrainType terrainType) {
    switch (terrainType) {
      case FOREST_DEMO:
        // Use a simplified map creation method (with only mmap layers)使用简化的地图创建方式（只有 mmap 图层）
        return createForestDemoTerrain(0.5f);
      case MAP_TWO:
        return createMapTwoTerrain(0.5f);
      default:
        return null;
        
    }
  }

  // A simplified terrain creation method (with only mmap layers)简化的地形创建方法（只有 mmap 图层）
  private TerrainComponent2 createForestDemoTerrain(float tileWorldSize) {
    // 根据mmap原始尺寸与MAP_SIZE计算每格像素尺寸，确保贴图严格覆盖31x30格
    Texture mmapTex = ServiceLocator.getResourceService().getAsset("images/mmap.png", Texture.class);
    int tilePixelW = Math.max(1, Math.round((float) mmapTex.getWidth() / MAP_SIZE.x));
    int tilePixelH = Math.max(1, Math.round((float) mmapTex.getHeight() / MAP_SIZE.y));
    GridPoint2 tilePixelSize = new GridPoint2(tilePixelW, tilePixelH);

    TiledMap tiledMap = createForestDemoTiles(tilePixelSize, mmapTex);
    TiledMapRenderer renderer = createRenderer(tiledMap, tileWorldSize / tilePixelSize.x);
    return new TerrainComponent2(camera, tiledMap, renderer, orientation, tileWorldSize);
  }

  private TiledMapRenderer createRenderer(TiledMap tiledMap, float tileScale) {
    switch (orientation) {
      case ORTHOGONAL:
        return new OrthogonalTiledMapRenderer(tiledMap, tileScale);
      default:
        return null;
    }
  }

  // Create a map with only mmap layers只包含 mmap 图层的地图
  private TiledMap createForestDemoTiles(GridPoint2 tileSize, Texture mmapTex) {
    TiledMap tiledMap = new TiledMap();
    TiledMapTileLayer dummyLayer =
        new TiledMapTileLayer(MAP_SIZE.x, MAP_SIZE.y, tileSize.x, tileSize.y);
    tiledMap.getLayers().add(dummyLayer);

    mmapTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    TiledMapImageLayer mmapLayer = new TiledMapImageLayer(new TextureRegion(mmapTex), 0, 0);
    mmapLayer.setName("mmap");
    tiledMap.getLayers().add(mmapLayer);

    return tiledMap;
  }

  private TerrainComponent2 createMapTwoTerrain(float tileWorldSize) {
    Texture mmapTex = ServiceLocator.getResourceService().getAsset("images/mmap.png", Texture.class);
    int tilePixelW = Math.max(1, Math.round((float) mmapTex.getWidth() / MAP_SIZE.x));
    int tilePixelH = Math.max(1, Math.round((float) mmapTex.getHeight() / MAP_SIZE.y));
    GridPoint2 tilePixelSize = new GridPoint2(tilePixelW, tilePixelH);

    TiledMap tiledMap = createMapTwoTiles(tilePixelSize, mmapTex);
    TiledMapRenderer renderer = createRenderer(tiledMap, tileWorldSize / tilePixelSize.x);
    return new TerrainComponent2(camera, tiledMap, renderer, orientation, tileWorldSize);
  }

  private TiledMap createMapTwoTiles(GridPoint2 tileSize, Texture mmapTex) {
    TiledMap tiledMap = new TiledMap();
    TiledMapTileLayer dummyLayer =
        new TiledMapTileLayer(MAP_SIZE.x, MAP_SIZE.y, tileSize.x, tileSize.y);
    tiledMap.getLayers().add(dummyLayer);

    mmapTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    TiledMapImageLayer mmapLayer = new TiledMapImageLayer(new TextureRegion(mmapTex), 0, 0);
    mmapLayer.setName("mmap");
    tiledMap.getLayers().add(mmapLayer);

    return tiledMap;
  }

  /** Only keep the simplified version (mmap)只保留简化版（mmap） */
  public enum TerrainType {
    FOREST_DEMO,  // Only mmap layers仅 mmap 图层
    MAP_TWO
  }
}