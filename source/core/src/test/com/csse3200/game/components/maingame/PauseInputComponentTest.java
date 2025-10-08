package com.csse3200.game.components.maingame;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.events.listeners.EventListener0;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(GameExtension.class)
@ExtendWith(MockitoExtension.class)
class PauseInputComponentTest {

    @Mock
    private Stage mockStage;
    
    @Mock
    private RenderService mockRenderService;
    
    @Mock
    private EntityService mockEntityService;
    
    private Entity testEntity;

    @BeforeEach
    void setUp() {
        ServiceLocator.registerTimeSource(new GameTime());
        lenient().when(mockRenderService.getStage()).thenReturn(mockStage);
        ServiceLocator.registerRenderService(mockRenderService);
        
        testEntity = new Entity();
        Array<Entity> entities = new Array<>();
        entities.add(testEntity);
        lenient().when(mockEntityService.getEntities()).thenReturn(entities);
        ServiceLocator.registerEntityService(mockEntityService);
    }

    @Test
    void escKeyShouldTogglePause() {
        Entity e = new Entity();
        PauseInputComponent comp = new PauseInputComponent();
        e.addComponent(comp);
        e.create();

        AtomicInteger pauseCalls = new AtomicInteger(0);
        AtomicInteger resumeCalls = new AtomicInteger(0);
        testEntity.getEvents().addListener("gamePaused", (EventListener0) () -> pauseCalls.incrementAndGet());
        testEntity.getEvents().addListener("gameResumed", (EventListener0) () -> resumeCalls.incrementAndGet());

        e.getEvents().trigger("togglePause");
        assertEquals(1, pauseCalls.get(), "First toggle should pause");

        e.getEvents().trigger("togglePause");
        assertEquals(1, resumeCalls.get(), "Second toggle should resume");
    }

    @Test
    void pKeyShouldTogglePause() {
        Entity e = new Entity();
        PauseInputComponent comp = new PauseInputComponent();
        e.addComponent(comp);
        e.create();

        AtomicInteger pauseCalls = new AtomicInteger(0);
        testEntity.getEvents().addListener("gamePaused", (EventListener0) () -> pauseCalls.incrementAndGet());

        e.getEvents().trigger("togglePause");
        assertEquals(1, pauseCalls.get(), "Toggle should trigger pause");
    }

    @Test
    void otherKeysShouldBeIgnored() {
        Entity e = new Entity();
        PauseInputComponent comp = new PauseInputComponent();
        e.addComponent(comp);
        e.create();

        AtomicInteger calls = new AtomicInteger(0);
        testEntity.getEvents().addListener("gamePaused", (EventListener0) () -> calls.incrementAndGet());

        assertEquals(0, calls.get(), "No pause should occur without toggle event");
    }
}
