package com.csse3200.game.components.hero.engineer;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService; // ✅ 注意包名
import com.csse3200.game.entities.factories.ProjectileFactory;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TurretAttackComponentTest {

    private Graphics mockGraphics;
    private Application mockApp;
    private EntityService entityService;

    @BeforeEach
    void setUp() {
        // 控制 delta time
        mockGraphics = mock(Graphics.class);
        when(mockGraphics.getDeltaTime()).thenReturn(1f / 60f);
        Gdx.graphics = mockGraphics;

        // 让 postRunnable 立刻执行
        mockApp = mock(Application.class);
        doAnswer(inv -> { Runnable r = inv.getArgument(0); if (r != null) r.run(); return null; })
                .when(mockApp).postRunnable(any());
        Gdx.app = mockApp;

        // 注册 EntityService
        entityService = mock(EntityService.class);
        ServiceLocator.registerEntityService(entityService);
    }

    @AfterEach
    void tearDown() {
        // 如有 reset/clear 可在此调用
        // ServiceLocator.clear();
    }

    // ---------- 小工具 ----------

    /** 反射把宿主 entity 注入到组件中（等价于游戏里 addComponent 后的效果） */
    private static void injectEntity(Object component, Entity host) throws Exception {
        Class<?> c = component.getClass();
        while (c != null && !"Component".equals(c.getSimpleName())) {
            c = c.getSuperclass();
        }
        if (c == null) throw new IllegalStateException("Cannot find Component superclass");
        Field f = c.getDeclaredField("entity");
        f.setAccessible(true);
        f.set(component, host);
    }

    /** 构造带 centerPosition 的宿主（含 CombatStats） */
    private static Entity hostWithCenter(Vector2 center, int baseAttack) {
        Entity host = mock(Entity.class);
        CombatStatsComponent stats = mock(CombatStatsComponent.class);
        when(stats.getBaseAttack()).thenReturn(baseAttack);
        when(host.getComponent(CombatStatsComponent.class)).thenReturn(stats);
        when(host.getCenterPosition()).thenReturn(center);
        return host;
    }

    /** 构造无 center、用 pos+scale*0.5 回退的宿主（含 CombatStats） */
    private static Entity hostWithPosScale(Vector2 pos, Vector2 scale, int baseAttack) {
        Entity host = mock(Entity.class);
        CombatStatsComponent stats = mock(CombatStatsComponent.class);
        when(stats.getBaseAttack()).thenReturn(baseAttack);
        when(host.getComponent(CombatStatsComponent.class)).thenReturn(stats);
        when(host.getCenterPosition()).thenReturn(null);
        when(host.getPosition()).thenReturn(pos);
        when(host.getScale()).thenReturn(scale);
        return host;
    }

    private static void assertVec2(Vector2 v, float x, float y, float eps) {
        assertNotNull(v);
        assertEquals(x, v.x, eps);
        assertEquals(y, v.y, eps);
    }

    // ---------- 测试用例 ----------

    @Test
    void firesOnce_usesCenterAndRegisters() throws Exception {
        TurretAttackComponent comp =
                new TurretAttackComponent(new Vector2(10, 0), /*cooldown*/0.1f,
                        /*speed*/20f, /*life*/2f, "bullet.png");

        Entity host = hostWithCenter(new Vector2(5f, 7f), /*atk*/3);
        injectEntity(comp, host);

        Entity bullet = mock(Entity.class);

        try (MockedStatic<ProjectileFactory> mocked = mockStatic(ProjectileFactory.class)) {
            mocked.when(() -> ProjectileFactory.createBullet(anyString(), any(Vector2.class),
                            anyFloat(), anyFloat(), anyFloat(), anyInt(), true))
                    .thenReturn(bullet);

            // 首次 update 应立即开火
            comp.update();

            // 捕获参数并断言
            ArgumentCaptor<String> texCap = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<Vector2> posCap = ArgumentCaptor.forClass(Vector2.class);
            ArgumentCaptor<Float> vxCap = ArgumentCaptor.forClass(Float.class);
            ArgumentCaptor<Float> vyCap = ArgumentCaptor.forClass(Float.class);
            ArgumentCaptor<Float> lifeCap = ArgumentCaptor.forClass(Float.class);
            ArgumentCaptor<Integer> dmgCap = ArgumentCaptor.forClass(Integer.class);

            mocked.verify(() -> ProjectileFactory.createBullet(
                    texCap.capture(), posCap.capture(), vxCap.capture(), vyCap.capture(),
                    lifeCap.capture(), dmgCap.capture(), true
            ), times(1));

            assertEquals("bullet.png", texCap.getValue());
            assertVec2(posCap.getValue(), 5f, 7f, 1e-6f);
            // direction (1,0) * speed 20
            assertEquals(20f, vxCap.getValue(), 1e-5f);
            assertEquals(0f,  vyCap.getValue(), 1e-5f);
            assertEquals(2f,  lifeCap.getValue(), 1e-6f);
            assertEquals(3,   dmgCap.getValue());

            // 注册到 EntityService（postRunnable 已立即执行）
            verify(entityService, times(1)).register(bullet);
        }
    }

    @Test
    void cooldown_blocksUntilNextFrame_thenFiresAgain() throws Exception {
        TurretAttackComponent comp =
                new TurretAttackComponent(new Vector2(0, 1), /*cooldown*/0.2f,
                        /*speed*/30f, /*life*/1.5f, "tex.png");

        Entity host = hostWithCenter(new Vector2(1f, 1f), /*atk*/2);
        injectEntity(comp, host);

        Entity bullet = mock(Entity.class);

        try (MockedStatic<ProjectileFactory> mocked = mockStatic(ProjectileFactory.class)) {
            mocked.when(() -> ProjectileFactory.createBullet(anyString(), any(), anyFloat(), anyFloat(), anyFloat(), anyInt(), true))
                    .thenReturn(bullet);

            // 第一次：立即开火
            comp.update();
            mocked.verify(() -> ProjectileFactory.createBullet(anyString(), any(), anyFloat(), anyFloat(), anyFloat(), anyInt(), true), times(1));
            verify(entityService, times(1)).register(bullet);

            // 用一帧“大 dt”穿越冷却：这一帧只会把 cd <= 0，然后 return，不会开火
            when(Gdx.graphics.getDeltaTime()).thenReturn(0.21f);
            comp.update();

            // 下一帧才真正开火
            when(Gdx.graphics.getDeltaTime()).thenReturn(1f / 60f);
            comp.update();

            mocked.verify(() -> ProjectileFactory.createBullet(anyString(), any(), anyFloat(), anyFloat(), anyFloat(), anyInt(), true), times(2));
            verify(entityService, times(2)).register(bullet);
        }
    }

    @Test
    void fallsBackToPosPlusHalfScale_whenCenterUnavailable() throws Exception {
        TurretAttackComponent comp =
                new TurretAttackComponent(new Vector2(0, -1), /*cooldown*/0.1f,
                        /*speed*/10f, /*life*/0.8f, "b.png");

        // 期望位置：pos(2,3) + scale(4,6)*0.5 = (4,6)
        Entity host = hostWithPosScale(new Vector2(2f, 3f), new Vector2(4f, 6f), /*atk*/1);
        injectEntity(comp, host);

        Entity bullet = mock(Entity.class);

        try (MockedStatic<ProjectileFactory> mocked = mockStatic(ProjectileFactory.class)) {
            mocked.when(() -> ProjectileFactory.createBullet(anyString(), any(), anyFloat(), anyFloat(), anyFloat(), anyInt(), true))
                    .thenReturn(bullet);

            comp.update();

            ArgumentCaptor<Vector2> posCap = ArgumentCaptor.forClass(Vector2.class);
            mocked.verify(() -> ProjectileFactory.createBullet(
                    eq("b.png"), posCap.capture(), anyFloat(), anyFloat(),
                    eq(0.8f), eq(1), true
            ), times(1));

            assertVec2(posCap.getValue(), 4f, 6f, 1e-6f);
            verify(entityService, times(1)).register(bullet);
        }
    }

    @Test
    void setDirection_normalizesAndAffectsNextShot() throws Exception {
        // 初始方向 (3,4) -> 归一化 (0.6,0.8)，速度 10 -> (6,8)
        TurretAttackComponent comp =
                new TurretAttackComponent(new Vector2(3, 4), /*cooldown*/0.05f,
                        /*speed*/10f, /*life*/1f, "c.png");

        Entity host = hostWithCenter(new Vector2(0, 0), /*atk*/1);
        injectEntity(comp, host);

        Entity bullet = mock(Entity.class);

        try (MockedStatic<ProjectileFactory> mocked = mockStatic(ProjectileFactory.class)) {
            mocked.when(() -> ProjectileFactory.createBullet(anyString(), any(), anyFloat(), anyFloat(), anyFloat(), anyInt(), true))
                    .thenReturn(bullet);

            // 第一次：以 (6,8) 速度开火
            comp.update();
            mocked.verify(() -> ProjectileFactory.createBullet(
                    eq("c.png"), any(), eq(6f), eq(8f), eq(1f), anyInt(), true
            ), times(1));

            // 改方向为 (0, -2) -> 归一化 (0,-1) -> 速度 (0,-10)
            comp.setDirection(new Vector2(0, -2));

            // 用大 dt 清冷却（本帧不会开火）
            when(Gdx.graphics.getDeltaTime()).thenReturn(0.2f);
            comp.update();

            // 下一帧才开火
            when(Gdx.graphics.getDeltaTime()).thenReturn(1f / 60f);
            comp.update();

            // 第二次：以 (0,-10) 速度开火（这里只验证出现一次该组参数）
            mocked.verify(() -> ProjectileFactory.createBullet(
                    eq("c.png"), any(), eq(0f), eq(-10f), eq(1f), anyInt(), true
            ), times(1));

            verify(entityService, times(2)).register(bullet);
        }
    }
}
