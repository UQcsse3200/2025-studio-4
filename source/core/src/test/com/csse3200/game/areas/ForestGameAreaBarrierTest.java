package com.csse3200.game.areas;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
class ForestGameAreaBarrierTest {

  @BeforeEach
  void setup() {
    ServiceLocator.registerEntityService(new EntityService());
    ServiceLocator.registerPhysicsService(new PhysicsService());
    ResourceService resourceService = mock(ResourceService.class);
    Texture mockTexture = mock(Texture.class);
    when(resourceService.getAsset(anyString(), eq(Texture.class))).thenReturn(mockTexture);
    ServiceLocator.registerResourceService(resourceService);
  }

  @Test
  void spawnBarrierAt_placesEntitiesAtGivenCells() throws Exception {
    // 使用半真实mock，避免构造函数内的复杂依赖
    ForestGameArea area = mock(ForestGameArea.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));

    // 反射直接调用私有方法，避免触发 create() 中的大量逻辑
    doNothing().when(area).spawnEntityAt(any(Entity.class), any(GridPoint2.class), anyBoolean(), anyBoolean());
    int[][] cells = new int[][] { {1,2}, {3,4} };
    var m = ForestGameArea.class.getDeclaredMethod("spawnBarrierAt", int[][].class);
    m.setAccessible(true);
    assertDoesNotThrow(() -> m.invoke(area, (Object) cells));
    verify(area, times(1)).spawnEntityAt(any(Entity.class), eq(new GridPoint2(1,2)), eq(true), eq(false));
    verify(area, times(1)).spawnEntityAt(any(Entity.class), eq(new GridPoint2(3,4)), eq(true), eq(false));
  }
}


