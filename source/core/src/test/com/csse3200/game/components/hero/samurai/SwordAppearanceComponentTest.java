package com.csse3200.game.components.hero.samurai;

import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.SamuraiConfig;
import com.csse3200.game.rendering.RotatingTextureRenderComponent;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SwordAppearanceComponent.
 *
 * Verifies:
 *  - On create(): level-1 texture is applied and rotation is preserved.
 *  - applySwordTextureForLevel(): sets the expected texture, preserves rotation.
 *  - Out-of-range levels fall back to cfg.swordTexture.
 */
public class SwordAppearanceComponentTest {

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

    /** Invoke the private applySwordTextureForLevel(int) via reflection. */
    private static void callApplyLevel(SwordAppearanceComponent comp, int level) {
        try {
            Method m = SwordAppearanceComponent.class
                    .getDeclaredMethod("applySwordTextureForLevel", int.class);
            m.setAccessible(true);
            m.invoke(comp, level);
        } catch (Exception e) {
            throw new AssertionError("Failed to call applySwordTextureForLevel via reflection", e);
        }
    }

    @Test
    void create_appliesLevel1Texture_andPreservesRotation() {
        // --- Arrange ---
        SamuraiConfig cfg = new SamuraiConfig();
        cfg.swordTexture = "images/samurai/sword_default.png";
        cfg.swordLevelTextures = new String[] {
                "images/samurai/sword_lvl1.png",
                "images/samurai/sword_lvl2.png",
                "images/samurai/sword_lvl3.png"
        };

        // IMPORTANT: pass owner = null to avoid NPE from owner.getEvents()
        Entity owner = null;
        Entity swordEntity = mock(Entity.class);

        RotatingTextureRenderComponent rot = mock(RotatingTextureRenderComponent.class);
        when(rot.getRotation()).thenReturn(45f);
        when(swordEntity.getComponent(RotatingTextureRenderComponent.class)).thenReturn(rot);

        SwordAppearanceComponent comp = new SwordAppearanceComponent(owner, cfg);
        attachToEntity(comp, swordEntity);

        // --- Act ---
        comp.create();

        // --- Assert ---
        // Level-1 texture should be applied on create()
        verify(rot, times(1)).setTexture(eq("images/samurai/sword_lvl1.png"));
        // Rotation should be preserved (set back to 45f)
        verify(rot, times(1)).setRotation(eq(45f));
    }

    @Test
    void applySwordTextureForLevel_setsNthTexture_andPreservesRotation() {
        // --- Arrange ---
        SamuraiConfig cfg = new SamuraiConfig();
        cfg.swordTexture = "images/samurai/sword_default.png";
        cfg.swordLevelTextures = new String[] {
                "images/samurai/sword_lvl1.png",
                "images/samurai/sword_lvl2.png",
                "images/samurai/sword_lvl3.png",
                "images/samurai/sword_lvl4.png"
        };

        Entity owner = null; // avoid event bus NPE
        Entity swordEntity = mock(Entity.class);

        RotatingTextureRenderComponent rot = mock(RotatingTextureRenderComponent.class);
        // Return 20f for rotation when apply(level 3) is called
        when(rot.getRotation()).thenReturn(10f, 20f);
        when(swordEntity.getComponent(RotatingTextureRenderComponent.class)).thenReturn(rot);

        SwordAppearanceComponent comp = new SwordAppearanceComponent(owner, cfg);
        attachToEntity(comp, swordEntity);

        // Prime state by running create() (applies level-1; preserves 10f)
        comp.create();
        // Now apply level-3 explicitly (index 2; preserves 20f)
        callApplyLevel(comp, 3);

        // --- Assert ---
        verify(rot, times(1)).setTexture(eq("images/samurai/sword_lvl1.png"));
        verify(rot, times(1)).setRotation(eq(10f));

        verify(rot, times(1)).setTexture(eq("images/samurai/sword_lvl3.png"));
        verify(rot, times(1)).setRotation(eq(20f));
    }

    @Test
    void applySwordTextureForLevel_outOfRange_fallsBackToDefault() {
        // --- Arrange ---
        SamuraiConfig cfg = new SamuraiConfig();
        cfg.swordTexture = "images/samurai/sword_default.png";
        cfg.swordLevelTextures = new String[] {
                "images/samurai/sword_lvl1.png"
        };

        Entity owner = null; // avoid event bus NPE
        Entity swordEntity = mock(Entity.class);

        RotatingTextureRenderComponent rot = mock(RotatingTextureRenderComponent.class);
        // Both calls return 0f, so setRotation(0f) will be called twice.
        when(rot.getRotation()).thenReturn(0f, 0f);
        when(swordEntity.getComponent(RotatingTextureRenderComponent.class)).thenReturn(rot);

        SwordAppearanceComponent comp = new SwordAppearanceComponent(owner, cfg);
        attachToEntity(comp, swordEntity);

        // --- Act ---
        comp.create();           // level-1: uses sword_lvl1.png and preserves 0f
        callApplyLevel(comp, 5); // out-of-range: falls back to default, preserves 0f

        // --- Assert ---
        // Textures: exactly once for lvl1 and once for default
        verify(rot, times(1)).setTexture(eq("images/samurai/sword_lvl1.png"));
        verify(rot, times(1)).setTexture(eq("images/samurai/sword_default.png"));

        // Rotation: called twice with 0f (once per apply)
        verify(rot, times(2)).setRotation(eq(0f));
    }
}
