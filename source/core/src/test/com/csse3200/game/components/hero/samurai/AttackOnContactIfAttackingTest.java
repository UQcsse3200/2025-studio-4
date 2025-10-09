package com.csse3200.game.components.hero.samurai;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.HitboxComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AttackOnContactIfAttacking.
 */
public class AttackOnContactIfAttackingTest {

    private Graphics mockGraphics;
    private Application mockApp;

    @BeforeEach
    void setupGdx() {
        // Provide a deterministic delta time for update()
        mockGraphics = mock(Graphics.class);
        when(mockGraphics.getDeltaTime()).thenReturn(1f / 60f);
        Gdx.graphics = mockGraphics;

        // Run postRunnable immediately so assertions can be synchronous
        mockApp = mock(Application.class);
        doAnswer(inv -> {
            Runnable r = inv.getArgument(0);
            if (r != null) r.run();
            return null;
        }).when(mockApp).postRunnable(ArgumentMatchers.any(Runnable.class));
        Gdx.app = mockApp;
    }

    // --------- Helpers ---------

    /** Injects the protected 'entity' field on the Component base class. */
    private static void injectEntity(Object component, Entity host) throws Exception {
        Field f = Component.class.getDeclaredField("entity");
        f.setAccessible(true);
        f.set(component, host);
    }

    /** Sets a (possibly private) field by walking up the inheritance tree. */
    private static void setPrivate(Object obj, String fieldName, Object value) throws Exception {
        Class<?> c = obj.getClass();
        while (c != null) {
            try {
                Field f = c.getDeclaredField(fieldName);
                f.setAccessible(true);
                f.set(obj, value);
                return;
            } catch (NoSuchFieldException ignored) {
                c = c.getSuperclass();
            }
        }
        throw new NoSuchFieldException("Field '" + fieldName + "' not found on " + obj.getClass());
    }

    /** Invokes the private onCollisionStart(Fixture, Fixture). */
    private static void fireCollision(AttackOnContactIfAttacking comp, Fixture me, Fixture other) throws Exception {
        Method m = AttackOnContactIfAttacking.class.getDeclaredMethod("onCollisionStart", Fixture.class, Fixture.class);
        m.setAccessible(true);
        m.invoke(comp, me, other);
    }

    /** Builds a mock fixture with the given categoryBits and userData. */
    private static Fixture makeFixture(short categoryBits, Object userData) {
        Fixture fx = mock(Fixture.class);
        Filter filter = new Filter();
        filter.categoryBits = categoryBits;
        when(fx.getFilterData()).thenReturn(filter);
        when(fx.getUserData()).thenReturn(userData);
        return fx;
    }

    // --------- Tests ---------

    @Test
    void noDamage_whenNotAttacking() throws Exception {
        final short TARGET_LAYER = 0x0002; // choose a single bit, keep same on both fixtures

        // System under test: cooldown 0.2s
        AttackOnContactIfAttacking comp = new AttackOnContactIfAttacking(
                /*attackerOwner*/ null, /*damage*/ 10, TARGET_LAYER, /*perTargetCooldownSec*/ 0.2f);

        // Sword entity hosting the component
        Entity sword = mock(Entity.class);

        // Dependencies on sword
        HitboxComponent hitbox = mock(HitboxComponent.class);
        SwordJabPhysicsComponent jab = mock(SwordJabPhysicsComponent.class);
        when(jab.isAttacking()).thenReturn(false); // not attacking

        // Our own fixture ("me"), must match hitbox.getFixture()
        Fixture me = makeFixture(TARGET_LAYER, sword);
        when(hitbox.getFixture()).thenReturn(me);

        // Target entity + its combat stats
        Entity target = mock(Entity.class);
        CombatStatsComponent targetStats = mock(CombatStatsComponent.class);
        when(target.getComponent(CombatStatsComponent.class)).thenReturn(targetStats);

        // "Other" fixture carries the target entity at the same category bit
        Fixture other = makeFixture(TARGET_LAYER, target);

        // Wire into the component
        injectEntity(comp, sword);
        setPrivate(comp, "hitbox", hitbox);
        setPrivate(comp, "jabCtrl", jab);
        setPrivate(comp, "spawnMuteTimer", 0f); // bypass the 50ms spawn mute

        // Trigger collision
        fireCollision(comp, me, other);

        // Not attacking => no damage should be applied
        verify(targetStats, never()).hit(any(CombatStatsComponent.class));
        // Do not verify int overload: your CombatStatsComponent does not define hit(int)
    }

    @Test
    void dealsDamage_onlyWhenAttacking_andRespectsCooldown() throws Exception {
        final short TARGET_LAYER = 0x0004;

        // Provide an explicit attacker owner (so hit(attackerStats) path is used)
        Entity attackerOwner = mock(Entity.class);
        AttackOnContactIfAttacking comp = new AttackOnContactIfAttacking(
                attackerOwner, /*damage*/ 12, TARGET_LAYER, /*perTargetCooldownSec*/ 0.2f);

        // Sword entity hosting the component
        Entity sword = mock(Entity.class);

        // Attacker stats live on the owner
        CombatStatsComponent attackerStats = mock(CombatStatsComponent.class);
        when(attackerOwner.getComponent(CombatStatsComponent.class)).thenReturn(attackerStats);

        // Target entity + stats
        Entity target = mock(Entity.class);
        CombatStatsComponent targetStats = mock(CombatStatsComponent.class);
        when(target.getComponent(CombatStatsComponent.class)).thenReturn(targetStats);

        // Dependencies on sword
        HitboxComponent hitbox = mock(HitboxComponent.class);
        SwordJabPhysicsComponent jab = mock(SwordJabPhysicsComponent.class);
        when(jab.isAttacking()).thenReturn(true); // attacking now

        // Fixtures
        Fixture me = makeFixture(TARGET_LAYER, sword);
        when(hitbox.getFixture()).thenReturn(me);
        Fixture other = makeFixture(TARGET_LAYER, target);

        // Wire into the component
        injectEntity(comp, sword);
        setPrivate(comp, "hitbox", hitbox);
        setPrivate(comp, "jabCtrl", jab);
        setPrivate(comp, "spawnMuteTimer", 0f); // bypass the spawn mute

        // 1) First collision => should deal damage once (via hit(attackerStats))
        fireCollision(comp, me, other);
        verify(targetStats, times(1)).hit(any(CombatStatsComponent.class));

        // 2) Immediate second collision (still within per-target cooldown) => no extra call
        fireCollision(comp, me, other);
        verify(targetStats, times(1)).hit(any(CombatStatsComponent.class));

        // 3) Advance the per-target cooldown via update()
        when(Gdx.graphics.getDeltaTime()).thenReturn(0.21f); // > 0.2s cooldown
        comp.update();

        // 4) Collision again after cooldown => should deal damage again
        fireCollision(comp, me, other);
        verify(targetStats, times(2)).hit(any(CombatStatsComponent.class));
    }
}
