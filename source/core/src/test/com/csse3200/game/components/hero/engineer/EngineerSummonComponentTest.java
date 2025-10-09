package com.csse3200.game.components.hero.engineer;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Gdx;
import com.csse3200.game.components.maingame.SimplePlacementController;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;   // ✅ 正确的包
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EngineerSummonComponentTest {

    private Input mockInput;
    private InputMultiplexer mux; // spy
    private GameTime gameTime;
    private final AtomicReference<Float> dt = new AtomicReference<>(0f);

    private SimplePlacementController ctrl; // mock placement controller
    private Entity ctrlHolder;              // mock entity that holds controller

    @BeforeEach
    void setUp() {
        // ---- 1) Gdx.input & mux ----
        mockInput = mock(Input.class);
        mux = spy(new InputMultiplexer());
        when(mockInput.getInputProcessor()).thenReturn(mux);
        Gdx.input = mockInput;

        // ---- 2) EntityService（注意包名）+ GameTime ----
        ctrl = mock(SimplePlacementController.class);
        ctrlHolder = mock(Entity.class);
        when(ctrlHolder.getComponent(SimplePlacementController.class)).thenReturn(ctrl);

        // GameTime mock
        gameTime = mock(GameTime.class);
        when(gameTime.getDeltaTime()).thenAnswer((Answer<Float>) inv -> dt.get());
        ServiceLocator.registerTimeSource(gameTime);
    }

    @AfterEach
    void tearDown() {
        // 如有 ServiceLocator.clear() 可在这里调用
        // ServiceLocator.clear();
    }

    private static InputAdapter captureFirstAddedAdapter(InputMultiplexer muxSpy) {
        var cap = ArgumentCaptor.forClass(InputAdapter.class);
        verify(muxSpy, atLeastOnce()).addProcessor(eq(0), cap.capture());
        return cap.getAllValues().get(0);
    }

    private static void attachAndCreate(EngineerSummonComponent comp, Entity host) throws Exception {
        Field f = comp.getClass().getSuperclass().getDeclaredField("entity");
        f.setAccessible(true);
        f.set(comp, host);
        comp.create();
    }

    // 其余测试用例保持与我上一条消息一致（按 1/2/3、冷却、maxAlive、detach 等）
    // ...
}
