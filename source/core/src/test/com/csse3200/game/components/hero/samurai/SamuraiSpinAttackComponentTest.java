package com.csse3200.game.components.hero.samurai;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.SamuraiConfig;
import com.csse3200.game.entities.factories.SwordFactory;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SamuraiSpinAttackComponent.
 */
public class SamuraiSpinAttackComponentTest {

    private Graphics mockGraphics;
    private Application mockApp;
    private Input mockInput;
    private AtomicReference<Object> inputProcessorRef;

    @BeforeEach
    void setupGdx() {
        // Stable delta time
        mockGraphics = mock(Graphics.class);
        when(mockGraphics.getDeltaTime()).thenReturn(1f / 60f);
        Gdx.graphics = mockGraphics;

        // Run postRunnable immediately to make tests synchronous
        mockApp = mock(Application.class);
        doAnswer(inv -> { Runnable r = inv.getArgument(0); if (r!=null) r.run(); return null; })
                .when(mockApp).postRunnable(any(Runnable.class));
        Gdx.app = mockApp;

        // Mock Gdx.input so we can capture/get the InputProcessor (an InputMultiplexer)
        mockInput = mock(Input.class);
        inputProcessorRef = new AtomicReference<>();
        // setInputProcessor stores the processor into the ref
        doAnswer(inv -> { inputProcessorRef.set(inv.getArgument(0)); return null; })
                .when(mockInput).setInputProcessor(any());
        // getInputProcessor reads from the ref
        when(mockInput.getInputProcessor()).thenAnswer(inv -> inputProcessorRef.get());
        // Provide some screen coordinates for the camera.unproject path
        when(mockInput.getX()).thenReturn(100);
        when(mockInput.getY()).thenReturn(200);
        Gdx.input = mockInput;
    }

    @Test
    void create_registersSword_andInstallsAdapter_andKeyBindingsWork() {
        // Mocks
        Entity host = mock(Entity.class);
        SamuraiConfig cfg = new SamuraiConfig();
        Camera camera = mock(Camera.class);
        // Let unproject pass-through (screen 100,200 -> world 100,200)
        doAnswer(inv -> inv.getArgument(0)).when(camera).unproject(any(Vector3.class));

        // Mock sword entity and its control component
        Entity sword = mock(Entity.class);
        SwordJabPhysicsComponent ctrl = mock(SwordJabPhysicsComponent.class);
        when(sword.getComponent(SwordJabPhysicsComponent.class)).thenReturn(ctrl);

        // Mock EntityService and SwordFactory
        var entityService = mock(com.csse3200.game.entities.EntityService.class);
        try (MockedStatic<ServiceLocator> svc = mockStatic(ServiceLocator.class);
             MockedStatic<SwordFactory> swordFactory = mockStatic(SwordFactory.class)) {

            svc.when(ServiceLocator::getEntityService).thenReturn(entityService);
            // Match the call: createSword(this.entity, cfg, swordTexture, restRadius, 0f)
            swordFactory.when(() -> SwordFactory.createSword(eq(host), eq(cfg), anyString(), anyFloat(), anyFloat()))
                    .thenReturn(sword);

            SamuraiSpinAttackComponent sut = new SamuraiSpinAttackComponent(
                    /*restRadius*/ 1.5f,
                    /*swordTexture*/ "images/samurai/sword.png",
                    cfg,
                    camera
            );

            // Attach component to entity via reflection and create
            attachToEntity(sut, host);
            sut.create();

            // Sword should be registered (postRunnable executed immediately)
            verify(entityService, times(1)).register(sword);

            // An InputMultiplexer should now be installed
            Object proc = inputProcessorRef.get();
            assertNotNull(proc, "InputProcessor should be installed");
            assertTrue(proc instanceof InputMultiplexer, "InputProcessor should be an InputMultiplexer");
            InputMultiplexer mux = (InputMultiplexer) proc;

            // Key bindings:
            // NUM_1 -> triggerJabTowards(worldMouse)
            boolean c1 = mux.keyDown(Input.Keys.NUM_1);
            assertTrue(c1, "NUM_1 should be consumed");
            verify(ctrl, times(1)).triggerJabTowards(argThat(v -> approxEquals(v, new Vector2(100f, 200f))));

            // NUM_2 -> triggerSweepToward(worldMouse)
            boolean c2 = mux.keyDown(Input.Keys.NUM_2);
            assertTrue(c2, "NUM_2 should be consumed");
            verify(ctrl, times(1)).triggerSweepToward(argThat(v -> approxEquals(v, new Vector2(100f, 200f))));

            // NUM_3 -> triggerSpin(true)
            boolean c3 = mux.keyDown(Input.Keys.NUM_3);
            assertTrue(c3, "NUM_3 should be consumed");
            verify(ctrl, times(1)).triggerSpin(eq(true));
        }
    }

    @Test
    void dispose_removesAdapter_andDisposesSword() {
        // Mocks
        Entity host = mock(Entity.class);
        SamuraiConfig cfg = new SamuraiConfig();
        Camera camera = mock(Camera.class);

        Entity sword = mock(Entity.class);
        var entityService = mock(com.csse3200.game.entities.EntityService.class);

        try (MockedStatic<ServiceLocator> svc = mockStatic(ServiceLocator.class);
             MockedStatic<SwordFactory> swordFactory = mockStatic(SwordFactory.class)) {

            svc.when(ServiceLocator::getEntityService).thenReturn(entityService);
            swordFactory.when(() -> SwordFactory.createSword(eq(host), eq(cfg), anyString(), anyFloat(), anyFloat()))
                    .thenReturn(sword);

            SamuraiSpinAttackComponent sut = new SamuraiSpinAttackComponent(
                    1.5f, "images/samurai/sword.png", cfg, camera
            );
            attachToEntity(sut, host);
            sut.create();

            assertTrue(inputProcessorRef.get() instanceof InputMultiplexer);
            InputMultiplexer mux = (InputMultiplexer) inputProcessorRef.get();

            sut.dispose();

            // After dispose, the adapter should be gone and keyDown not consumed
            boolean consumed = mux.keyDown(Input.Keys.NUM_1);
            assertFalse(consumed, "After dispose, adapter should be removed and not consume keys.");

            // Sword entity should be disposed
            verify(sword, times(1)).dispose();
        }
    }

    // ----- helpers -----

    private static boolean approxEquals(Vector2 actual, Vector2 expected) {
        return actual != null && actual.epsilonEquals(expected, 1e-4f);
    }

    /**
     * Attach a component to an entity by setting the protected 'entity' field
     * on com.csse3200.game.components.Component via reflection.
     */
    private static void attachToEntity(Object component, Entity host) {
        try {
            Class<?> base = Class.forName("com.csse3200.game.components.Component");
            java.lang.reflect.Field f = base.getDeclaredField("entity");
            f.setAccessible(true);
            f.set(component, host);
        } catch (Exception e) {
            throw new AssertionError("Failed to attach to entity via reflection", e);
        }
    }
}
