package com.csse3200.game.components.hero.engineer;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.csse3200.game.components.hero.engineer.AutoDespawnOnDeathComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.events.EventHandler;
import org.junit.jupiter.api.*;
import org.mockito.stubbing.Answer;
import static org.mockito.Mockito.*;
import java.lang.reflect.Field;

import static org.mockito.Mockito.*;

/**
 * Tests for AutoDespawnOnDeathComponent.
 */
public class AutoDespawnOnDeathComponentTest {

    private Application originalApp;

    @BeforeEach
    void setUpGdxAppMock() {
        originalApp = Gdx.app;

        Application mockApp = mock(Application.class);
        doAnswer((Answer<Void>) inv -> {
            Runnable r = inv.getArgument(0);
            if (r != null) r.run();
            return null;
        }).when(mockApp).postRunnable(any(Runnable.class));

        Gdx.app = mockApp;
    }

    @AfterEach
    void tearDownGdxAppMock() {
        Gdx.app = originalApp;
    }

    /**
     * 帮助方法：把 entity 注入到 component 的受保护字段里，并调用 create()。
     */
    private static void attach(AutoDespawnOnDeathComponent component, Entity entity) throws Exception {
        Field f = component.getClass().getSuperclass().getDeclaredField("entity");
        f.setAccessible(true);
        f.set(component, entity);
        component.create();
    }

    @Test
    void testDisposeCalledOnceOnDeath() throws Exception {
        AutoDespawnOnDeathComponent component = new AutoDespawnOnDeathComponent();

        EventHandler events = new EventHandler();

        Entity mockEntity = mock(Entity.class);
        when(mockEntity.getEvents()).thenReturn(events);

        attach(component, mockEntity);

        events.trigger("death");

        verify(mockEntity, times(1)).dispose();
    }

    @Test
    void testDisposeCalledOnceWhenMultipleDeathSignals() throws Exception {
        AutoDespawnOnDeathComponent component = new AutoDespawnOnDeathComponent();
        EventHandler events = new EventHandler();
        Entity mockEntity = mock(Entity.class);
        when(mockEntity.getEvents()).thenReturn(events);
        attach(component, mockEntity);


        events.trigger("entityDeath");
        events.trigger("death");
        events.trigger("setDead", true);

        verify(mockEntity, times(1)).dispose();

        events.trigger("setDead", false);
        verifyNoMoreInteractions(ignoreStubs(mockEntity));
    }

    @Test
    void testNoDisposeWhenSetDeadFalse() throws Exception {
        AutoDespawnOnDeathComponent component = new AutoDespawnOnDeathComponent();
        EventHandler events = new EventHandler();
        Entity mockEntity = mock(Entity.class);
        when(mockEntity.getEvents()).thenReturn(events);
        attach(component, mockEntity);

        // 仅 setDead(false) 不应 dispose
        events.trigger("setDead", false);

        verify(mockEntity, never()).dispose();
    }
}
