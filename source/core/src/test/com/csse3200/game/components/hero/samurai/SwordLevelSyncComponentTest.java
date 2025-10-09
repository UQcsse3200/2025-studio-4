package com.csse3200.game.components.hero.samurai;

import com.csse3200.game.components.Component;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.SamuraiConfig;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.mockito.Mockito.*;

/**
 * Unit tests for SwordLevelSyncComponent.
 *
 * Verifies:
 *  - On create(): applies level-1 damage from config to CombatStatsComponent
 *  - If config array is empty/null, falls back to stats.getBaseAttack()
 *  - applyForLevel(level) (invoked reflectively) selects correct array value and clamps out-of-range to last
 *  - No crash / no interactions when CombatStatsComponent is absent
 */
public class SwordLevelSyncComponentTest {

    /** Attach a component to a host entity by setting the protected 'entity' field on Component via reflection. */
    private static void attachToEntity(Object component, Entity host) {
        try {
            Field f = Component.class.getDeclaredField("entity");
            f.setAccessible(true);
            f.set(component, host);
        } catch (Exception e) {
            throw new AssertionError("Failed to attach component to entity via reflection", e);
        }
    }

    /** Invoke the private applyForLevel(int) via reflection. */
    private static void callApplyLevel(SwordLevelSyncComponent comp, int level) {
        try {
            Method m = SwordLevelSyncComponent.class.getDeclaredMethod("applyForLevel", int.class);
            m.setAccessible(true);
            m.invoke(comp, level);
        } catch (Exception e) {
            throw new AssertionError("Failed to call applyForLevel via reflection", e);
        }
    }

    @Test
    void create_appliesLevel1_fromConfig() {
        // Config with damages per level
        SamuraiConfig cfg = new SamuraiConfig();
        cfg.swordDamageByLevel = new int[] { 5, 10, 20 };

        // Owner not needed for this test; pass null to avoid event registration
        Entity owner = null;

        // Host (sword) entity and its CombatStatsComponent
        Entity sword = mock(Entity.class);
        CombatStatsComponent stats = mock(CombatStatsComponent.class);
        when(sword.getComponent(CombatStatsComponent.class)).thenReturn(stats);

        SwordLevelSyncComponent comp = new SwordLevelSyncComponent(owner, cfg);
        attachToEntity(comp, sword);

        // Act
        comp.create();

        // Assert: level-1 => index 0 => 5
        verify(stats, times(1)).setBaseAttack(5);
    }

    @Test
    void create_usesFallback_whenArrayEmpty() {
        SamuraiConfig cfg = new SamuraiConfig();
        cfg.swordDamageByLevel = null; // empty/absent array

        Entity owner = null;

        Entity sword = mock(Entity.class);
        CombatStatsComponent stats = mock(CombatStatsComponent.class);
        when(sword.getComponent(CombatStatsComponent.class)).thenReturn(stats);
        // fallback = stats.getBaseAttack()
        when(stats.getBaseAttack()).thenReturn(7);

        SwordLevelSyncComponent comp = new SwordLevelSyncComponent(owner, cfg);
        attachToEntity(comp, sword);

        comp.create();

        // Should set to fallback value (7)
        verify(stats, times(1)).setBaseAttack(7);
    }

    @Test
    void applyForLevel_setsCorrectValue_andClampsOutOfRange() {
        SamuraiConfig cfg = new SamuraiConfig();
        cfg.swordDamageByLevel = new int[] { 3, 6, 9 }; // lv1->3, lv2->6, lv3->9

        Entity owner = null;

        Entity sword = mock(Entity.class);
        CombatStatsComponent stats = mock(CombatStatsComponent.class);
        when(sword.getComponent(CombatStatsComponent.class)).thenReturn(stats);
        when(stats.getBaseAttack()).thenReturn(1); // not used if array present

        SwordLevelSyncComponent comp = new SwordLevelSyncComponent(owner, cfg);
        attachToEntity(comp, sword);

        // Prime create() (applies level-1)
        comp.create();
        verify(stats, times(1)).setBaseAttack(3);

        // Level 2 -> 6
        callApplyLevel(comp, 2);
        verify(stats, times(1)).setBaseAttack(6);

        // Level 3 -> 9
        callApplyLevel(comp, 3);
        verify(stats, times(1)).setBaseAttack(9);

        // Out of range level 10 -> clamp to last (9)
        callApplyLevel(comp, 10);
        verify(stats, times(2)).setBaseAttack(9);
    }

    @Test
    void noStats_componentDoesNothing() {
        SamuraiConfig cfg = new SamuraiConfig();
        cfg.swordDamageByLevel = new int[] { 8, 16 };

        Entity owner = null;

        Entity sword = mock(Entity.class);
        // No CombatStatsComponent on the sword
        when(sword.getComponent(CombatStatsComponent.class)).thenReturn(null);

        SwordLevelSyncComponent comp = new SwordLevelSyncComponent(owner, cfg);
        attachToEntity(comp, sword);

        // Should not throw
        comp.create();

        // Nothing to verify (no stats), but we can ensure no unexpected interactions happened on a null mock by… no-op
        // (i.e., if there was a call, tests would have failed earlier with NPE)
    }
}
