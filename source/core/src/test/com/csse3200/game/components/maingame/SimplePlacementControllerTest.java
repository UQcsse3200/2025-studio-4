package com.csse3200.game.components.maingame;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.csse3200.game.entities.Entity;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.*;

public class SimplePlacementControllerTest {

    @Test
    public void update_earlyReturns_whenInactive_orCameraNull() throws Exception {
        SimplePlacementController ctrl = new SimplePlacementController();

        // camera present but inactive -> no-op
        set(ctrl, "camera", new OrthographicCamera());
        set(ctrl, "placementActive", false);
        ctrl.update();
        assertFalse(getBool(ctrl, "placementActive"));

        // active but camera null -> no-op (stays active)
        set(ctrl, "camera", null);
        set(ctrl, "placementActive", true);
        ctrl.update();
        assertTrue(getBool(ctrl, "placementActive"));
    }

    // --- tiny reflection helpers ---
    private static void set(Object target, String field, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(field);
        f.setAccessible(true);
        f.set(target, value);
    }
    private static boolean getBool(Object target, String field) throws Exception {
        Field f = target.getClass().getDeclaredField(field);
        f.setAccessible(true);
        return (boolean) f.get(target);
    }
    private static String getString(Object target) throws Exception {
        Field f = target.getClass().getDeclaredField("pendingType");
        f.setAccessible(true);
        return (String) f.get(target);
    }
}
