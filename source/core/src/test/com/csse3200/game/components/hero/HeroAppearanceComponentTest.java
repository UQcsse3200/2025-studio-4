package com.csse3200.game.components.hero;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.ComponentPriority;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.HeroConfig;
import com.csse3200.game.rendering.RotatingTextureRenderComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for HeroAppearanceComponent.
 */
public class HeroAppearanceComponentTest {

    @BeforeEach
    void setupGdxApp() {
        // Make Gdx.app non-null; DO NOT execute runnables to avoid hitting graphics/files deps.
        Application app = mock(Application.class);
        doNothing().when(app).postRunnable(any(Runnable.class));
        Gdx.app = app;
    }

    @Test
    void create_appliesLevel1_onRotatingComponent_andPreservesRotation() {
        // Config with per-level textures and default
        HeroConfig cfg = new HeroConfig();
        cfg.heroTexture = "images/hero/default.png";
        cfg.levelTextures = new String[]{
                "images/hero/level1.png", // level 1
                "images/hero/level2.png"
        };

        Entity hero = new Entity();
        RotatingTextureRenderComponent rot = mock(RotatingTextureRenderComponent.class);
        when(rot.getRotation()).thenReturn(45f);
        when(rot.getPrio()).thenReturn(ComponentPriority.LOW); // ADD THIS LINE

        HeroAppearanceComponent comp = new HeroAppearanceComponent(cfg);
        hero.addComponent(rot);
        hero.addComponent(comp);
        hero.create();

        // Level-1 should be applied on create(), and rotation preserved
        verify(rot, times(1)).setTexture(eq("images/hero/level1.png"));
        verify(rot, times(1)).setRotation(eq(45f));
        // postRunnable should NOT be needed for RotatingTexture path
        verify(Gdx.app, never()).postRunnable(any());
    }

    @Test
    void upgraded_event_appliesLevel_andFallbackToDefaultWhenOutOfRange() {
        HeroConfig cfg = new HeroConfig();
        cfg.heroTexture = "images/hero/default.png";
        cfg.levelTextures = new String[]{
                "images/hero/level1.png",
                "images/hero/level2.png",
                "images/hero/level3.png"
        };

        Entity hero = new Entity();
        RotatingTextureRenderComponent rot = mock(RotatingTextureRenderComponent.class);
        when(rot.getRotation()).thenReturn(10f, 20f, 30f); // successive calls to preserve
        when(rot.getPrio()).thenReturn(ComponentPriority.LOW); // ADD THIS LINE

        HeroAppearanceComponent comp = new HeroAppearanceComponent(cfg);
        hero.addComponent(rot);
        hero.addComponent(comp);
        hero.create(); // applies level 1

        // Trigger an upgrade to level 3
        hero.getEvents().trigger("upgraded", 3, /*type*/null, /*cost*/0);
        verify(rot, times(1)).setTexture(eq("images/hero/level3.png"));
        verify(rot, times(1)).setRotation(eq(20f)); // second getRotation() result

        // Trigger an upgrade to an out-of-range level (e.g., 10) -> fallback to default
        hero.getEvents().trigger("upgraded", 10, null, 0);
        verify(rot, times(1)).setTexture(eq("images/hero/default.png"));
        verify(rot, times(1)).setRotation(eq(30f)); // third getRotation() result

        verify(Gdx.app, never()).postRunnable(any()); // still no async path used
    }

    @Test
    void whenOnlyTextureRender_present_schedulesAddNewTextureComponent_andPreservesRotationReading() {
        HeroConfig cfg = new HeroConfig();
        cfg.heroTexture = "images/hero/default.png";
        cfg.levelTextures = new String[]{
                "images/hero/level1.png",
                "images/hero/level2.png"
        };

        Entity hero = new Entity();

        // Old standard texture render component (mocked)
        TextureRenderComponent texOld = mock(TextureRenderComponent.class);
        when(texOld.getRotation()).thenReturn(33f);
        when(texOld.getPrio()).thenReturn(ComponentPriority.LOW); // ADD THIS LINE

        HeroAppearanceComponent comp = new HeroAppearanceComponent(cfg);
        hero.addComponent(texOld);
        hero.addComponent(comp);
        hero.create();

        // For the TextureRenderComponent path, component should:
        // 1) read old rotation immediately
        // 2) schedule (via postRunnable) adding a new TextureRenderComponent and set its rotation
        verify(texOld, atLeastOnce()).getRotation();

        ArgumentCaptor<Runnable> runCap = ArgumentCaptor.forClass(Runnable.class);
        verify(Gdx.app, times(1)).postRunnable(runCap.capture());
        assertNotNull(runCap.getValue(), "A runnable must be scheduled to add the new TextureRenderComponent");

        // We deliberately DO NOT run the runnable here, to avoid hitting real graphics/files deps.
        // The purpose of this test is to ensure scheduling + rotation reading happened.
    }
}