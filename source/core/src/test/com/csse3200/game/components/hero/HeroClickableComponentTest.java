package com.csse3200.game.components.hero;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for HeroClickableComponent.
 *
 * This component is "no-op" on click now; tests focus on debouncing logic and that no exceptions occur.
 */
public class HeroClickableComponentTest {

    private Graphics mockGraphics;
    private Input mockInput;

    @BeforeEach
    void setUpGdx() {
        // Mock Gdx.app to avoid NPE in any logging paths (none used here, but safe).
        Gdx.app = mock(Application.class);

        // Mock Gdx.graphics to control delta time.
        mockGraphics = mock(Graphics.class);
        when(mockGraphics.getDeltaTime()).thenReturn(0.1f); // 100ms/frame
        Gdx.graphics = mockGraphics;

        // Mock Gdx.input for button presses & clicks.
        mockInput = mock(Input.class);
        Gdx.input = mockInput;
    }

    /** Attach a component to entity via reflection (sets protected 'entity' field). */
    private static void attachToEntity(Component comp, Entity host) {
        try {
            Field f = Component.class.getDeclaredField("entity");
            f.setAccessible(true);
            f.set(comp, host);
        } catch (Exception e) {
            throw new AssertionError("Failed to attach component to entity", e);
        }
    }

    /** Read private/protected boolean field via reflection. */
    private static boolean getBool(Object obj, String field) {
        try {
            Field f = obj.getClass().getDeclaredField(field);
            f.setAccessible(true);
            return f.getBoolean(obj);
        } catch (Exception e) {
            throw new AssertionError("Failed to read boolean field: " + field, e);
        }
    }

    /** Read private/protected float field via reflection. */
    private static float getFloat(Object obj, String field) {
        try {
            Field f = obj.getClass().getDeclaredField(field);
            f.setAccessible(true);
            return f.getFloat(obj);
        } catch (Exception e) {
            throw new AssertionError("Failed to read float field: " + field, e);
        }
    }

    @Test
    void stillPressed_mouseDebounce_keepsWaiting_andNoDelayConsumed() {
        // Arrange
        HeroClickableComponent comp = new HeroClickableComponent(/*radius*/0.8f);
        Entity hero = new Entity();
        hero.setPosition(5f, 6f);
        attachToEntity(comp, hero);

        // On create, waitRelease should be true
        comp.create();

        // Simulate still pressing either left or right mouse button
        when(mockInput.isButtonPressed(Input.Buttons.LEFT)).thenReturn(true);
        when(mockInput.isButtonPressed(Input.Buttons.RIGHT)).thenReturn(false);

        float delayBefore = getFloat(comp, "armDelaySec");

        // Act
        comp.update();

        // Assert: still waiting for release, and no delay consumed
        assertTrue(getBool(comp, "waitRelease"), "Should still be waiting for release");
        assertEquals(delayBefore, getFloat(comp, "armDelaySec"), 1e-6, "armDelaySec should not decrement while waiting");
    }

    @Test
    void afterRelease_delayCountsDown_toZero_thenArmed() {
        // Arrange
        HeroClickableComponent comp = new HeroClickableComponent(0.8f);
        Entity hero = new Entity();
        hero.setPosition(0f, 0f);
        attachToEntity(comp, hero);
        comp.create();

        // Simulate mouse released now
        when(mockInput.isButtonPressed(Input.Buttons.LEFT)).thenReturn(false);
        when(mockInput.isButtonPressed(Input.Buttons.RIGHT)).thenReturn(false);

        // Act: First update should flip waitRelease to false, but still consume delay (0.2s)
        comp.update();
        assertFalse(getBool(comp, "waitRelease"), "After release, waitRelease should become false");

        // Still counting down delay (armDelaySec starts at 0.2, dt=0.1)
        float d1 = getFloat(comp, "armDelaySec");
        assertTrue(d1 < 0.2f && d1 > 0f, "armDelaySec should have decreased but not yet 0");

        // Next frame should bring delay to <=0
        comp.update();
        float d2 = getFloat(comp, "armDelaySec");
        assertTrue(d2 <= 0f, "armDelaySec should reach zero after enough frames");
    }

    @Test
    void justTouched_withNoCamera_doesNothing_andNoException() {
        // Arrange
        HeroClickableComponent comp = new HeroClickableComponent(1.0f);
        Entity hero = new Entity();
        hero.setPosition(10f, 10f);
        attachToEntity(comp, hero);
        comp.create();

        // Release first, then wait for delay to elapse
        when(mockInput.isButtonPressed(Input.Buttons.LEFT)).thenReturn(false);
        when(mockInput.isButtonPressed(Input.Buttons.RIGHT)).thenReturn(false);
        comp.update(); // waitRelease->false, armDelay ~0.1
        comp.update(); // armDelay -> 0

        // Simulate a click this frame
        when(mockInput.justTouched()).thenReturn(true);
        when(mockInput.getX()).thenReturn(100);
        when(mockInput.getY()).thenReturn(200);

        // Act & Assert: No exception expected even if no camera available inside getCamera()
        assertDoesNotThrow(comp::update, "Click with no camera should be a no-op without exceptions");
    }
}
