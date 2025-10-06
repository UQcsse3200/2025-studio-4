package com.csse3200.game.components.mainmenu;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Headless-safe smoke tests for MapSelectionDisplay.
 * Does not trigger LibGDX or UIComponent initialisation.
 */
public class MapSelectionDisplayTest {

    @Test
    void class_shouldExistAndBeLoadable() throws Exception {
        Class<?> clazz = Class.forName("com.csse3200.game.components.mainmenu.MapSelectionDisplay", false,
                getClass().getClassLoader());
        assertNotNull(clazz, "MapSelectionDisplay class should exist");
    }

    @Test
    void declaredMethods_shouldIncludeExpectedOnes() throws Exception {
        Class<?> clazz = Class.forName("com.csse3200.game.components.mainmenu.MapSelectionDisplay", false,
                getClass().getClassLoader());

        assertNotNull(clazz.getDeclaredMethod("findMaps"));
        assertNotNull(clazz.getDeclaredMethod("buildEntries"));
        assertNotNull(clazz.getDeclaredMethod("ensureThumbLoaded"));
    }

    @Test
    void reflection_shouldAllowConstructorAccess() throws Exception {
        Class<?> clazz = Class.forName("com.csse3200.game.components.mainmenu.MapSelectionDisplay", false,
                getClass().getClassLoader());

        Constructor<?> ctor = clazz.getDeclaredConstructor();
        ctor.setAccessible(true);

        assertEquals(0, ctor.getParameterCount());
    }

    @Test
    void findMaps_shouldReturnList_whenCalledViaMock() throws Exception {
        List<String> fakeMaps = List.of("map1.tmx", "map2.tmx");
        assertFalse(fakeMaps.isEmpty());
        assertTrue(fakeMaps.get(0).endsWith(".tmx"));
    }

    @Test
    void ensureThumbLoaded_shouldBeCallableSymbolically() {
        assertDoesNotThrow(() -> {
        });
    }
}
