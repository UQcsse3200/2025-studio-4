package com.csse3200.game.components.hero.samurai;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.RotatingTextureRenderComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SwordJabPhysicsComponent.
 */
public class SwordJabPhysicsComponentTest {

    private Graphics mockGraphics;
    private Application mockApp;

    @BeforeEach
    void setupGdx() {
        mockGraphics = mock(Graphics.class);
        when(mockGraphics.getDeltaTime()).thenReturn(0.09f); // 90ms/frame as a default
        Gdx.graphics = mockGraphics;

        mockApp = mock(Application.class);
        doAnswer(inv -> { Runnable r = inv.getArgument(0); if (r!=null) r.run(); return null; })
                .when(mockApp).postRunnable(any(Runnable.class));
        Gdx.app = mockApp;
    }

    /** Attach a component to a host entity by setting the protected 'entity' field on Component. */
    private static void attachToEntity(Object component, Entity host) {
        try {
            Field f = Component.class.getDeclaredField("entity");
            f.setAccessible(true);
            f.set(component, host);
        } catch (Exception e) {
            throw new AssertionError("Failed to attach component to entity via reflection", e);
        }
    }

    /** Set a private/protected float field via reflection. */
    private static void setFloat(Object obj, String field, float value) {
        try {
            Field f = obj.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.setFloat(obj, value);
        } catch (Exception e) {
            throw new AssertionError("Failed to set float field '" + field + "'", e);
        }
    }

    /** Get a private/protected float field via reflection. */
    private static float getFloat(Object obj, String field) {
        try {
            Field f = obj.getClass().getDeclaredField(field);
            f.setAccessible(true);
            return f.getFloat(obj);
        } catch (Exception e) {
            throw new AssertionError("Failed to get float field '" + field + "'", e);
        }
    }

    /** Build a minimal sword entity: PhysicsComponent + RotatingTextureRenderComponent. */
    private static Entity buildSwordWithPhysics(Body body, RotatingTextureRenderComponent rot) {
        Entity sword = mock(Entity.class);
        PhysicsComponent phys = mock(PhysicsComponent.class);
        when(phys.getBody()).thenReturn(body);
        when(sword.getComponent(PhysicsComponent.class)).thenReturn(phys);
        when(sword.getComponent(RotatingTextureRenderComponent.class)).thenReturn(rot);
        return sword;
    }
    /** Get a private/protected boolean field via reflection. */
    private static boolean getBool(Object obj, String field) {
        try {
            Field f = obj.getClass().getDeclaredField(field);
            f.setAccessible(true);
            return f.getBoolean(obj);
        } catch (Exception e) {
            throw new AssertionError("Failed to get boolean field '" + field + "'", e);
        }
    }

    @Test
    void create_requiresPhysicsComponent() {
        // Owner with a center position
        Entity owner = mock(Entity.class);
        when(owner.getCenterPosition()).thenReturn(new Vector2(0f, 0f));

        // Sword entity WITHOUT physics component
        Entity sword = mock(Entity.class);
        when(sword.getComponent(PhysicsComponent.class)).thenReturn(null);

        SwordJabPhysicsComponent sut = new SwordJabPhysicsComponent(owner, /*restRadius*/1.0f);
        attachToEntity(sut, sword);

        assertThrows(IllegalStateException.class, sut::create, "PhysicsComponent is required");
    }

    @Test
    void jab_towards_updatesAngle_and_entersCooldown() {
        // Owner center fixed
        Entity owner = mock(Entity.class);
        when(owner.getCenterPosition()).thenReturn(new Vector2(0f, 0f));

        // Sword with physics and visual rot
        Body body = mock(Body.class);
        RotatingTextureRenderComponent rot = mock(RotatingTextureRenderComponent.class);
        Entity sword = buildSwordWithPhysics(body, rot);

        SwordJabPhysicsComponent sut = new SwordJabPhysicsComponent(owner, 1.0f)
                .setJabParams(0.18f, 0.8f)     // default
                .setJabCooldown(0.2f);         // introduce a cooldown
        attachToEntity(sut, sword);
        sut.create();

        // Trigger a jab toward +X (angle 0)
        sut.triggerJabTowards(new Vector2(10f, 0f));
        // It should be jabbing now -> update twice to cross duration (0.18s)
        sut.update(); // +0.09
        sut.update(); // +0.09  => reaches end

        // Finished jabbing -> expect cooldown active (cannot trigger immediately again)
        sut.triggerJabTowards(new Vector2(10f, 0f)); // should be ignored due to jabCdTimer
        // Verify setTransform was called at least once during the animation
        verify(body, atLeastOnce()).setTransform(anyFloat(), anyFloat(), anyFloat());
        // Visual rotation updated at least once
        verify(rot, atLeastOnce()).setRotation(anyFloat());

        // Advance time to clear cooldown
        when(Gdx.graphics.getDeltaTime()).thenReturn(0.21f);
        sut.update();

        // Now jab can be triggered again
        sut.triggerJabTowards(new Vector2(0f, 10f)); // aim up
        sut.update();
        verify(body, atLeast(2)).setTransform(anyFloat(), anyFloat(), anyFloat());
    }

    @Test
    void sweep_direction_is_decided_by_delta_with_facing() {
        // Owner center fixed
        Entity owner = mock(Entity.class);
        when(owner.getCenterPosition()).thenReturn(new Vector2(0f, 0f));

        Body body = mock(Body.class);
        RotatingTextureRenderComponent rot = mock(RotatingTextureRenderComponent.class);
        Entity sword = buildSwordWithPhysics(body, rot);

        SwordJabPhysicsComponent sut = new SwordJabPhysicsComponent(owner, 1.0f)
                .setSweepParams(0.2f, 0.3f)
                .setSweepCooldown(0f)
                .setSweepArcDegrees(60f);
        attachToEntity(sut, sword);
        sut.create();

        // Set facing to 0 deg
        setFloat(sut, "facingDeg", 0f);

        // Aim at +Y (90 deg) => delta = +90 => sweepDir should be +1
        sut.triggerSweepToward(new Vector2(0f, 10f));
        assertTrue(getBool(sut, "sweeping"), "sweeping should be true after triggerSweepToward()");
        float dir1 = getFloat(sut, "sweepDir");
        assertEquals(+1f, dir1, 1e-4, "Sweep direction should be +1 when aim is ahead of facing");

        // Finish the sweep
        when(Gdx.graphics.getDeltaTime()).thenReturn(0.21f);
        sut.update();

        // Set facing to 170 deg, aim roughly -170 deg => delta negative -> sweepDir = -1
        setFloat(sut, "facingDeg", 170f);
        sut.triggerSweepToward(new Vector2(-10f, -1f)); // roughly pointing to ~ -174 deg
        assertTrue(getBool(sut, "sweeping"));
        float dir2 = getFloat(sut, "sweepDir");
    }


    @Test
    void spin_full_turn_updates_facing_at_end() {
        // Owner center fixed
        Entity owner = mock(Entity.class);
        when(owner.getCenterPosition()).thenReturn(new Vector2(0f, 0f));

        Body body = mock(Body.class);
        RotatingTextureRenderComponent rot = mock(RotatingTextureRenderComponent.class);
        Entity sword = buildSwordWithPhysics(body, rot);

        SwordJabPhysicsComponent sut = new SwordJabPhysicsComponent(owner, 1.0f)
                .setSpinParams(0.3f, 0.25f)     // duration=0.3s
                .setSpinCooldown(0f)
                .setSpinDirectionCCW(true)      // CCW
                .setSpinTurns(1f);              // 360 deg
        attachToEntity(sut, sword);
        sut.create();

        // Start with facing=10 deg
        setFloat(sut, "facingDeg", 10f);
        sut.triggerSpin(true); // CCW

        // Advance time to finish spin
        when(Gdx.graphics.getDeltaTime()).thenReturn(0.31f);
        sut.update();

        // After finishing, facing should be start + 360 (not normalized)
        float facingAfter = getFloat(sut, "facingDeg");
        assertEquals(370f, facingAfter, 1e-3, "Facing should advance by 360 degrees after 1 full turn");

        // Should have driven transforms & visual rotations at least once
        verify(body, atLeastOnce()).setTransform(anyFloat(), anyFloat(), anyFloat());
        verify(rot, atLeastOnce()).setRotation(anyFloat());
    }

    @Test
    void update_synchronizes_transform_and_visual_rotation() {
        // Owner center fixed
        Entity owner = mock(Entity.class);
        when(owner.getCenterPosition()).thenReturn(new Vector2(5f, 7f)); // arbitrary center

        Body body = mock(Body.class);
        RotatingTextureRenderComponent rot = mock(RotatingTextureRenderComponent.class);
        Entity sword = buildSwordWithPhysics(body, rot);

        SwordJabPhysicsComponent sut = new SwordJabPhysicsComponent(owner, 2.0f)
                .setSpriteForwardOffsetDeg(90f) // rotate sprite "up" for visibility change
                .setCenterToHandle(-0.25f);
        attachToEntity(sut, sword);
        sut.create();

        // One idle update should still set transform/rotation based on rest position
        sut.update();

        verify(body, atLeastOnce()).setTransform(anyFloat(), anyFloat(), anyFloat());
        verify(rot, atLeastOnce()).setRotation(anyFloat());
    }
}
