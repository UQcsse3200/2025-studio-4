package com.csse3200.game.components.hero.engineer;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class FourWayCycleComponentTest {

    private GameTime gameTime;
    private final AtomicReference<Float> dt = new AtomicReference<>(0f);

    @BeforeEach
    void setUp() {
        // 可控 GameTime：getDeltaTime() 返回 dt.get()
        gameTime = mock(GameTime.class);
        when(gameTime.getDeltaTime()).thenAnswer((Answer<Float>) inv -> dt.get());
        ServiceLocator.registerTimeSource(gameTime);
    }

    @AfterEach
    void tearDown() {
        // 如项目有清理方法，可在这里调用
        // ServiceLocator.clear();
    }

    /** 把宿主 entity 注入组件并调用 create() */
    private static void attachAndCreate(FourWayCycleComponent comp, Entity host) throws Exception {
        Field f = comp.getClass().getSuperclass().getDeclaredField("entity");
        f.setAccessible(true);
        f.set(comp, host);
        comp.create();
    }

    private static void assertVecEquals(Vector2 actual, float x, float y) {
        assertNotNull(actual);
        assertEquals(x, actual.x, 1e-6);
        assertEquals(y, actual.y, 1e-6);
    }

    @Test
    void create_setsInitialDirection_toNearestCardinal() throws Exception {
        // initialDir 接近“上”(0,1)
        Vector2 initial = new Vector2(0.2f, 1f);
        FourWayCycleComponent comp = new FourWayCycleComponent(0.5f, initial);

        // mock turret attack
        TurretAttackComponent atk = mock(TurretAttackComponent.class);
        Entity host = mock(Entity.class);
        when(host.getComponent(TurretAttackComponent.class)).thenReturn(atk);

        attachAndCreate(comp, host);

        ArgumentCaptor<Vector2> cap = ArgumentCaptor.forClass(Vector2.class);
        verify(atk, times(1)).setDirection(cap.capture());
        assertVecEquals(cap.getValue(), 0f, 1f); // Up
    }

    @Test
    void update_doesNotSwitch_beforeInterval() throws Exception {
        FourWayCycleComponent comp = new FourWayCycleComponent(1.0f, new Vector2(1, 0)); // Right

        TurretAttackComponent atk = mock(TurretAttackComponent.class);
        Entity host = mock(Entity.class);
        when(host.getComponent(TurretAttackComponent.class)).thenReturn(atk);

        attachAndCreate(comp, host); // 第一次 setDirection -> Right

        // 未满间隔
        dt.set(0.4f);
        comp.update();
        dt.set(0.5f);
        comp.update(); // 累积 0.9 < 1.0

        // 仍应只有一次（create 阶段）
        verify(atk, times(1)).setDirection(any(Vector2.class));
    }

    @Test
    void update_switchesInOrder_onEachInterval() throws Exception {
        // 从 Up 开始（给一个接近 Up 的初始向量）
        FourWayCycleComponent comp = new FourWayCycleComponent(0.5f, new Vector2(0, 10));

        TurretAttackComponent atk = mock(TurretAttackComponent.class);
        Entity host = mock(Entity.class);
        when(host.getComponent(TurretAttackComponent.class)).thenReturn(atk);

        attachAndCreate(comp, host); // setDirection(Up)

        ArgumentCaptor<Vector2> cap = ArgumentCaptor.forClass(Vector2.class);

        // 第一次到达间隔 -> 应切到 Right
        dt.set(0.5f);
        comp.update();

        // 第二次到达间隔 -> 应切到 Down
        dt.set(0.5f);
        comp.update();

        // 第三次到达间隔 -> 应切到 Left
        dt.set(0.5f);
        comp.update();

        // 共应调用 1（create）+3（三次切换） = 4 次
        verify(atk, times(4)).setDirection(cap.capture());

        // 依次断言：Up, Right, Down, Left
        assertVecEquals(cap.getAllValues().get(0), 0f, 1f);   // Up on create
        assertVecEquals(cap.getAllValues().get(1), 1f, 0f);   // Right
        assertVecEquals(cap.getAllValues().get(2), 0f, -1f);  // Down
        assertVecEquals(cap.getAllValues().get(3), -1f, 0f);  // Left
    }

    @Test
    void nullInitialDir_defaultsToUp() throws Exception {
        FourWayCycleComponent comp = new FourWayCycleComponent(1.0f, null);

        TurretAttackComponent atk = mock(TurretAttackComponent.class);
        Entity host = mock(Entity.class);
        when(host.getComponent(TurretAttackComponent.class)).thenReturn(atk);

        attachAndCreate(comp, host);

        ArgumentCaptor<Vector2> cap = ArgumentCaptor.forClass(Vector2.class);
        verify(atk, times(1)).setDirection(cap.capture());
        assertVecEquals(cap.getValue(), 0f, 1f); // Up
    }

    @Test
    void accumulativeDt_triggersSwitch_whenSumReachesInterval() throws Exception {
        FourWayCycleComponent comp = new FourWayCycleComponent(1.0f, new Vector2(0, 1)); // Up

        TurretAttackComponent atk = mock(TurretAttackComponent.class);
        Entity host = mock(Entity.class);
        when(host.getComponent(TurretAttackComponent.class)).thenReturn(atk);

        attachAndCreate(comp, host); // Up

        // 0.6 + 0.5 = 1.1 >= 1.0 -> 应切到 Right
        dt.set(0.6f); comp.update();
        dt.set(0.5f); comp.update();

        ArgumentCaptor<Vector2> cap = ArgumentCaptor.forClass(Vector2.class);
        verify(atk, times(2)).setDirection(cap.capture());

        assertVecEquals(cap.getAllValues().get(0), 0f, 1f); // Up (create)
        assertVecEquals(cap.getAllValues().get(1), 1f, 0f); // Right (first switch)
    }
}
