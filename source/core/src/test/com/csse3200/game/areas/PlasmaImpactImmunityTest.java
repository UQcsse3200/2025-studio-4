package com.csse3200.game.areas;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.ForestGameArea;
import com.csse3200.game.areas2.MapTwo.ForestGameArea2;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.enemy.EnemyTypeComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.services.ServiceLocator;
import java.lang.reflect.Method;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

class PlasmaImpactImmunityTest {

  private Application previousApp;
  private Graphics previousGraphics;

  @BeforeEach
  void setUp() {
    previousApp = Gdx.app;
    previousGraphics = Gdx.graphics;

    Application mockApp = mock(Application.class);
    doAnswer(invocation -> {
      Runnable runnable = invocation.getArgument(0);
      runnable.run();
      return null;
    }).when(mockApp).postRunnable(any(Runnable.class));
    Gdx.app = mockApp;

    Graphics mockGraphics = mock(Graphics.class);
    doAnswer(invocation -> 0.016f).when(mockGraphics).getDeltaTime();
    Gdx.graphics = mockGraphics;

    ServiceLocator.clear();
    ServiceLocator.registerEntityService(new EntityService());
  }

  @AfterEach
  void tearDown() {
    ServiceLocator.clear();
    Gdx.app = previousApp;
    Gdx.graphics = previousGraphics;
  }

  @Test
  void plasmaImpactEliminatesStandardEnemy() {
    ForestGameArea area = new ForestGameArea(null);
    Vector2 impact = new Vector2(2f, 2f);
    Entity grunt = registerEnemy("grunt", impact);
    CombatStatsComponent stats = grunt.getComponent(CombatStatsComponent.class);
    assertNotNull(stats);
    assertEquals(100, stats.getHealth());

    invokeHandlePlasmaImpact(area, impact);

    assertEquals(0, stats.getHealth());
  }

  @Test
  void plasmaImpactSkipsTankEnemy() {
    ForestGameArea area = new ForestGameArea(null);
    Vector2 impact = new Vector2(3f, 3f);
    Entity tank = registerEnemy("tank", impact);
    CombatStatsComponent stats = tank.getComponent(CombatStatsComponent.class);
    assertNotNull(stats);

    invokeHandlePlasmaImpact(area, impact);

    assertEquals(100, stats.getHealth());
  }

  @Test
  void plasmaImpactSkipsBossEnemyOnMapTwo() {
    ForestGameArea2 area = new ForestGameArea2(null);
    Vector2 impact = new Vector2(4f, 4f);
    Entity boss = registerEnemy("boss", impact);
    CombatStatsComponent stats = boss.getComponent(CombatStatsComponent.class);
    assertNotNull(stats);

    invokeHandlePlasmaImpact(area, impact);

    assertEquals(100, stats.getHealth());
  }

  private Entity registerEnemy(String type, Vector2 position) {
    Entity enemy =
        new Entity()
            .addComponent(new EnemyTypeComponent(type))
            .addComponent(new CombatStatsComponent(100, 0, null, null));
    enemy.setPosition(position);
    ServiceLocator.getEntityService().register(enemy);
    return enemy;
  }

  private void invokeHandlePlasmaImpact(Object area, Vector2 impact) {
    try {
      Method method = area.getClass().getDeclaredMethod("handlePlasmaImpact", Vector2.class);
      method.setAccessible(true);
      method.invoke(area, impact);
    } catch (Exception e) {
      throw new AssertionError("Failed to invoke handlePlasmaImpact", e);
    }
  }
}
