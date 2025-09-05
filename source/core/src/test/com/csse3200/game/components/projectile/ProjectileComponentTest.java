package com.csse3200.game.components.projectile;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.*;
import org.mockito.MockedStatic;

import static org.mockito.Mockito.*;

/**
 * Tests for ProjectileComponent.
 * - Velocity setting
 * - Expiration destruction (postRunnable -> execute immediately)
 * - Safe behavior without Physics/Body
 */
public class ProjectileComponentTest {
  private static MockedStatic<ServiceLocator> serviceStatic;

  @BeforeClass
  public static void boot() {
    // 1) Use Mockito to mock Gdx.app, and make postRunnable execute immediately
    Application appMock = mock(Application.class);
    doAnswer(inv -> {
      Runnable r = inv.getArgument(0);
      r.run(); // Key: run the runnable synchronously to avoid depending on render loop
      return null;
    }).when(appMock).postRunnable(any());
    Gdx.app = appMock;

    // 2) Allow static ServiceLocator stubbing
    serviceStatic = mockStatic(ServiceLocator.class);
  }

  @AfterClass
  public static void shutdown() {
    if (serviceStatic != null) serviceStatic.close();
  }

  private Entity mkEntityWith(Component... comps) {
    Entity e = spy(new Entity());  // spy to verify dispose()
    for (Component c : comps) e.addComponent(c);
    e.create();
    return e;
  }

  @Test
  public void create_appliesVelocityToBody() {
    // Given
    float vx = 3.5f, vy = -1.2f, life = 5f;

    Body body = mock(Body.class);
    PhysicsComponent phys = mock(PhysicsComponent.class);
    when(phys.getBody()).thenReturn(body);

    // GameTime (not directly needed in this case, but stubbed in case the component accesses it)
    GameTime ts = mock(GameTime.class);
    when(ts.getDeltaTime()).thenReturn(0f);
    serviceStatic.when(ServiceLocator::getTimeSource).thenReturn(ts);

    // When
    ProjectileComponent proj = new ProjectileComponent(vx, vy, life);
    Entity e = mkEntityWith(phys, proj);

    // Then
    verify(body, times(1)).setLinearVelocity(vx, vy);
    verify(body, never()).setActive(false);

    e.dispose();
  }

  @Test
  public void expires_triggersStopPhysicsAndDestroy() {
    // Given: life=0.1s, delta=0.2s → expires in one update
    float vx = 10f, vy = 0f, life = 0.1f;

    Body body = mock(Body.class);
    PhysicsComponent phys = mock(PhysicsComponent.class);
    when(phys.getBody()).thenReturn(body);

    GameTime ts = mock(GameTime.class);
    when(ts.getDeltaTime()).thenReturn(0.2f);
    EntityService es = mock(EntityService.class);

    serviceStatic.when(ServiceLocator::getTimeSource).thenReturn(ts);
    serviceStatic.when(ServiceLocator::getEntityService).thenReturn(es);

    ProjectileComponent proj = new ProjectileComponent(vx, vy, life);
    Entity e = mkEntityWith(phys, proj);

    // When: one update reaches expiration (postRunnable already executes synchronously)
    proj.update();

    // Then: physics stopped in the same frame
    verify(body, times(1)).setLinearVelocity(0f, 0f);
    verify(body, times(1)).setActive(false);

    // Disposal and unregistration executed synchronously
    verify(e, atLeastOnce()).dispose();
    verify(es, atLeastOnce()).unregister(e);

    // On further updates, unregistration should not repeat
    clearInvocations(es);
    proj.update();
    verify(es, never()).unregister(e);
  }

  @Test
  public void expires_withoutPhysics_isSafe() {
    // Given: no Physics or Body attached
    float life = 0.05f;

    GameTime ts = mock(GameTime.class);
    when(ts.getDeltaTime()).thenReturn(0.06f);
    EntityService es = mock(EntityService.class);

    serviceStatic.when(ServiceLocator::getTimeSource).thenReturn(ts);
    serviceStatic.when(ServiceLocator::getEntityService).thenReturn(es);

    ProjectileComponent proj = new ProjectileComponent(0, 0, life);
    Entity e = mkEntityWith(proj);

    // When: one update expires (postRunnable executes synchronously)
    proj.update();

    // Then: no crash, entity is disposed and unregistered
    verify(e, atLeastOnce()).dispose();
    verify(es, atLeastOnce()).unregister(e);
  }
}
