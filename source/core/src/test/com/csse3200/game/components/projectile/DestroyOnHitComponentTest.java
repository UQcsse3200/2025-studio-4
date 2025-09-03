package com.csse3200.game.components.projectile;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.InputMultiplexer;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.services.ServiceLocator;
import org.junit.*;
import org.mockito.ArgumentMatchers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DestroyOnHitComponent.
 *
 * We simulate collisions by triggering entity.getEvents().trigger("collisionStart", me, other).
 * The tests verify that deferred destruction happens, and only when colliding with the target layer.
 */
public class DestroyOnHitComponentTest {
  private static HeadlessApplication app;

  // Two test layers (bit masks), not relying on project constants
  private static final short LAYER_TARGET = 0x0002;     // target layer
  private static final short LAYER_OTHER  = 0x0004;     // non-target layer

  @BeforeClass
  public static void boot() {
    HeadlessApplicationConfiguration cfg = new HeadlessApplicationConfiguration();
    app = new HeadlessApplication(new ApplicationAdapter() {}, cfg);

    // Ensure Gdx.input has a multiplexer (some components may use it)
    if (!(Gdx.input.getInputProcessor() instanceof InputMultiplexer)) {
      Gdx.input.setInputProcessor(new InputMultiplexer());
    }
  }

  @AfterClass
  public static void shutdown() {
    if (app != null) app.exit();
  }

  @Before
  public void resetServices() {
    EntityService es = mock(EntityService.class);
    ServiceLocator.registerEntityService(es);
  }

  @After
  public void clearServices() {
    ServiceLocator.clear(); // Ignore if ServiceLocator does not have this method
  }

  /** Helper: wait briefly to let postRunnable tasks execute */
  private static void flushPosted() {
    try { Thread.sleep(25); } catch (InterruptedException ignored) {}
  }

  private static Fixture fixtureWithCategory(short categoryBits) {
    Fixture fx = mock(Fixture.class);
    Filter filter = new Filter();
    filter.categoryBits = categoryBits;
    when(fx.getFilterData()).thenReturn(filter);
    return fx;
  }

  private static Entity makeEntityWith(Component... comps) {
    Entity e = spy(new Entity()); // spy to verify dispose() invocation
    for (Component c : comps) e.addComponent(c);
    e.create();
    return e;
  }

  @Test
  public void destroysOnTargetLayer() {
    // Setup: a "me" fixture from this entity, and an "other" fixture in the target layer
    Fixture me = mock(Fixture.class);
    Fixture other = fixtureWithCategory(LAYER_TARGET);

    // Mock HitboxComponent: getFixture() returns me
    HitboxComponent hitbox = mock(HitboxComponent.class);
    when(hitbox.getFixture()).thenReturn(me);

    DestroyOnHitComponent comp = new DestroyOnHitComponent(LAYER_TARGET);

    // Create entity and attach component
    Entity entity = makeEntityWith(hitbox, comp);

    // Trigger collision
    entity.getEvents().trigger("collisionStart", me, other);

    // Wait for postRunnable to execute
    flushPosted();

    // Verify: entity is disposed
    verify(entity, atLeastOnce()).dispose();

    // Verify: entity is unregistered
    verify(ServiceLocator.getEntityService(), times(1)).unregister(entity);
  }

  @Test
  public void doesNothingOnNonTargetLayer() {
    Fixture me = mock(Fixture.class);
    Fixture other = fixtureWithCategory(LAYER_OTHER); // non-target layer

    HitboxComponent hitbox = mock(HitboxComponent.class);
    when(hitbox.getFixture()).thenReturn(me);

    DestroyOnHitComponent comp = new DestroyOnHitComponent(LAYER_TARGET);
    Entity entity = makeEntityWith(hitbox, comp);

    entity.getEvents().trigger("collisionStart", me, other);
    flushPosted();

    // Should not dispose or unregister
    verify(entity, never()).dispose();
    verify(ServiceLocator.getEntityService(), never()).unregister(ArgumentMatchers.any());
  }

  @Test
  public void ignoresIfNotOurFixture() {
    // The hitbox fixture stored in the component is not the same as the "me" fixture passed in
    Fixture myFixture = mock(Fixture.class);
    Fixture incomingMe = mock(Fixture.class); // fake "me"
    Fixture other = fixtureWithCategory(LAYER_TARGET);

    HitboxComponent hitbox = mock(HitboxComponent.class);
    when(hitbox.getFixture()).thenReturn(myFixture);

    DestroyOnHitComponent comp = new DestroyOnHitComponent(LAYER_TARGET);
    Entity entity = makeEntityWith(hitbox, comp);

    entity.getEvents().trigger("collisionStart", incomingMe, other);
    flushPosted();

    // Should not dispose or unregister
    verify(entity, never()).dispose();
    verify(ServiceLocator.getEntityService(), never()).unregister(ArgumentMatchers.any());
  }
}
