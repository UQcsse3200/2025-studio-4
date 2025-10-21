package com.csse3200.game.areas.terrain;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.maps.tiled.TiledMap;

public interface ITerrainComponent {
    Vector2 tileToWorldPosition(GridPoint2 tilePos);
    Vector2 tileToWorldPosition(int x, int y);
    float getTileSize();
    GridPoint2 getMapBounds(int layer);
    TiledMap getMap();

}

