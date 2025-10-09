package com.csse3200.game.components.hero.engineer;

import com.csse3200.game.components.currencysystem.CurrencyComponent.CurrencyType;
import com.csse3200.game.components.currencysystem.CurrencyManagerComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.*;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.Mockito.*;

/**
 * Tests for CurrencyGeneratorComponent using GameTime mock.
 */
public class CurrencyGeneratorComponentTest {

    private GameTime gameTime;                   // Mockito mock
    private final AtomicReference<Float> dt = new AtomicReference<>(0f); // 动态 dt

    @BeforeEach
    void setUp() {
        // 1) 创建 GameTime mock，并让 getDeltaTime() 返回可变的 dt
        gameTime = mock(GameTime.class);
        when(gameTime.getDeltaTime()).thenAnswer((Answer<Float>) inv -> dt.get());

        // 2) 注册到 ServiceLocator（符合你项目的方法签名）
        ServiceLocator.registerTimeSource(gameTime);
    }

    @AfterEach
    void tearDown() {
        // 如果你们有清理方法，建议在这里调用，避免污染其他测试
        // ServiceLocator.clear();
    }

    /** 工具：把 entity 注入 component，并调用 create() 完成初始化逻辑 */
    private static void attachAndCreate(CurrencyGeneratorComponent comp, Entity hostEntity) throws Exception {
        Field f = comp.getClass().getSuperclass().getDeclaredField("entity");
        f.setAccessible(true);
        f.set(comp, hostEntity);
        comp.create();
    }

    @Test
    void generatesOnlyAfterInterval_accumulatesDt() throws Exception {
        // Arrange
        Entity owner = mock(Entity.class);
        CurrencyManagerComponent cm = mock(CurrencyManagerComponent.class);
        when(owner.getComponent(CurrencyManagerComponent.class)).thenReturn(cm);
        when(cm.getCurrencyAmount(any())).thenReturn(0);

        CurrencyType type = CurrencyType.values()[0];
        CurrencyGeneratorComponent comp =
                new CurrencyGeneratorComponent(owner, type, /*amount*/5, /*intervalSec*/1.0f);

        Entity host = mock(Entity.class);
        attachAndCreate(comp, host);

        // Act + Assert
        dt.set(0.4f); comp.update();
        dt.set(0.4f); comp.update();
        verify(cm, never()).addCurrency(type, 5);

        dt.set(0.3f); comp.update();
        verify(cm, times(1)).addCurrency(type, 5);
        verify(cm, atLeast(1)).getCurrencyAmount(type);
    }

    @Test
    void resolvesOwnerFromOwnerComponent_whenOwnerNull() throws Exception {
        CurrencyType type = CurrencyType.values()[0];
        CurrencyGeneratorComponent comp =
                new CurrencyGeneratorComponent(/*owner*/null, type, 10, 0.5f);

        Entity host = mock(Entity.class);
        OwnerComponent oc = mock(OwnerComponent.class);
        when(host.getComponent(OwnerComponent.class)).thenReturn(oc);

        Entity owner = mock(Entity.class);
        when(oc.getOwner()).thenReturn(owner);

        CurrencyManagerComponent cm = mock(CurrencyManagerComponent.class);
        when(owner.getComponent(CurrencyManagerComponent.class)).thenReturn(cm);
        when(cm.getCurrencyAmount(type)).thenReturn(0);

        attachAndCreate(comp, host);

        dt.set(0.5f);
        comp.update();

        verify(cm, times(1)).addCurrency(type, 10);
    }

    @Test
    void skipWhenOwnerMissing() throws Exception {
        CurrencyType type = CurrencyType.values()[0];
        CurrencyGeneratorComponent comp =
                new CurrencyGeneratorComponent(/*owner*/null, type, 7, 0.2f);

        Entity host = mock(Entity.class);
        when(host.getComponent(OwnerComponent.class)).thenReturn(null);

        attachAndCreate(comp, host);

        dt.set(0.25f);
        comp.update();

        // 无 owner，不应抛异常；这里不对 cm 做 verify（因为没有 cm）
        Assertions.assertTrue(true);
    }

    @Test
    void skipWhenCurrencyManagerMissing() throws Exception {
        Entity owner = mock(Entity.class);
        when(owner.getComponent(CurrencyManagerComponent.class)).thenReturn(null);

        CurrencyType type = CurrencyType.values()[0];
        CurrencyGeneratorComponent comp =
                new CurrencyGeneratorComponent(owner, type, 3, 0.3f);

        Entity host = mock(Entity.class);
        attachAndCreate(comp, host);

        dt.set(0.3f);
        comp.update();

        // 无 cm，不会调用 addCurrency（无法拿到 cm 做 verify，这里确认不抛异常即可）
        Assertions.assertTrue(true);
    }

    @Test
    void generateMultipleCycles() throws Exception {
        Entity owner = mock(Entity.class);
        CurrencyManagerComponent cm = mock(CurrencyManagerComponent.class);
        when(owner.getComponent(CurrencyManagerComponent.class)).thenReturn(cm);

        CurrencyType type = CurrencyType.values()[0];
        CurrencyGeneratorComponent comp =
                new CurrencyGeneratorComponent(owner, type, 2, 1.0f);

        Entity host = mock(Entity.class);
        attachAndCreate(comp, host);

        // 连续 5 次 0.5s：应在第 2、4 次满 1.0s 时各触发一次，共 2 次
        for (int i = 0; i < 5; i++) {
            dt.set(0.5f);
            comp.update();
        }

        verify(cm, times(2)).addCurrency(type, 2);
    }
}
