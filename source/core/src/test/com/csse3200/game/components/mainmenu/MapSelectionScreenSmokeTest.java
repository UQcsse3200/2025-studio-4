package com.csse3200.game.components.mainmenu;

import com.badlogic.gdx.Screen;
import com.csse3200.game.GdxGame;
import com.csse3200.game.screens.MapSelectionScreen;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Headless-safe smoke tests for the new MapSelectionScreen.
 * These avoid LibGDX static initialisation by never calling Game.setScreen(...)
 * and never touching UIComponent-based classes.
 */
public class MapSelectionScreenSmokeTest {

    /** Minimal stub so we can construct GdxGame without booting LibGDX. */
    static class DummyGame extends GdxGame {
        @Override public void create() { /* no-op */ }
    }

    @Test
    void constructor_shouldCreateWithoutThrowing() {
        DummyGame game = new DummyGame();
        MapSelectionScreen screen = new MapSelectionScreen(game);
        assertNotNull(screen);
        assertTrue(screen instanceof Screen);
    }

    @Test
    void newScreen_reflection_returnsMapSelectionScreen() throws Exception {
        DummyGame game = new DummyGame();

        // Call GdxGame.newScreen(ScreenType, boolean, String) via reflection to avoid Game.setScreen(...)
        Method m = GdxGame.class.getDeclaredMethod(
                "newScreen", GdxGame.ScreenType.class, boolean.class, String.class);
        m.setAccessible(true);

        Object result = m.invoke(game, GdxGame.ScreenType.MAP_SELECTION, false, null);
        assertNotNull(result);
        assertTrue(result instanceof MapSelectionScreen,
                "newScreen(MAP_SELECTION) should return a MapSelectionScreen");
    }
}
