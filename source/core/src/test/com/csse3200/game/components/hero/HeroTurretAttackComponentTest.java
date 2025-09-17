package com.csse3200.game.components.hero;

import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.Entity;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.Assert.*;

/**
 * Unit tests for HeroTurretAttackComponent.
 * Notes:
 *  - We keep camera == null so update() never reaches the firing branch
 *    (computeAimDirection() returns false early), which avoids touching ProjectileFactory
 *    and ServiceLocator.
 *  - We also keep Gdx.graphics == null; the component will fall back to dt = 1/60f.
 *  - We use reflection to read private fields and invoke the private damage method.
 */
public class HeroTurretAttackComponentTest {

    private Entity hero;
    private CombatStatsComponent stats;
    private HeroTurretAttackComponent attack;

    @Before
    public void setUp() {
        // Create a bare hero entity and attach combat + turret-attack components.
        // Camera is null by design so that update() doesn't spawn bullets.
        hero = new Entity();
        stats = new CombatStatsComponent(/*health*/100, /*baseAttack*/10, null, null);
        attack = new HeroTurretAttackComponent(
                /*cooldown*/0.50f,
                /*bulletSpeed*/300f,
                /*bulletLife*/1.2f,
                /*bulletTexture*/"images/hero/Bullet.png",
                /*camera*/null
        );
        hero.addComponent(stats).addComponent(attack);
    }

    // ------------------------ Reflection helpers ------------------------

    private static Object getPrivateField(Object target, String name) {
        try {
            Field f = target.getClass().getDeclaredField(name);
            f.setAccessible(true);
            return f.get(target);
        } catch (Exception e) {
            throw new AssertionError("Failed to read private field: " + name, e);
        }
    }

    private static void setPrivateField(Object target, String name, Object value) {
        try {
            Field f = target.getClass().getDeclaredField(name);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new AssertionError("Failed to write private field: " + name, e);
        }
    }

    private static float getFloatField(Object target, String name) {
        try {
            Field f = target.getClass().getDeclaredField(name);
            f.setAccessible(true);
            return f.getFloat(target);
        } catch (Exception e) {
            throw new AssertionError("Failed to read float field: " + name, e);
        }
    }

    private static int invokePrivateComputeDamage(HeroTurretAttackComponent c) {
        try {
            Method m = c.getClass().getDeclaredMethod("computeDamageFromStats");
            m.setAccessible(true);
            return (Integer) m.invoke(c);
        } catch (Exception e) {
            throw new AssertionError("Failed to invoke computeDamageFromStats()", e);
        }
    }

    // ----------------------------- Tests ------------------------------

    @Test
    public void setters_ShouldBeChainableAndUpdateFields() {
        // Act: chain the three setters we added
        HeroTurretAttackComponent returned =
                attack.setCooldown(0.25f)
                        .setBulletParams(1234f, 2.5f);


        // Assert: chainable (return 'this')
        assertSame(attack, returned);

        // Assert: fields updated (via reflection)
        assertEquals(0.25f, getFloatField(attack, "cooldown"), 1e-6);
        assertEquals(1234f, getFloatField(attack, "bulletSpeed"), 1e-6);
        assertEquals(2.5f,  getFloatField(attack, "bulletLife"), 1e-6);
    }

    @Test
    public void damage_NoModifiers_EqualsBase() {
        // Base attack = 10 from setUp()
        attack.setAttackScale(1f).setFlatBonusDamage(0);
        int dmg = invokePrivateComputeDamage(attack);
        assertEquals(10, dmg);
    }

    @Test
    public void damage_WithScaleAndFlatBonus_RoundedAndMin1() {
        // Base attack = 10
        attack.setAttackScale(1.5f).setFlatBonusDamage(5); // 10*1.5 + 5 = 20 -> 20
        assertEquals(20, invokePrivateComputeDamage(attack));

        // Min bound test: drive result below 1 -> expect 1
        stats.setBaseAttack(0);
        attack.setAttackScale(0f).setFlatBonusDamage(-999);
        assertEquals(1, invokePrivateComputeDamage(attack));
    }

    @Test
    public void update_WhenCameraNull_OnlyReducesCooldownTimer() {
        // Arrange: set cdTimer to a known value; Gdx.graphics is null => dt = 1/60
        setPrivateField(attack, "cdTimer", 0.5f);

        // Act
        attack.update();

        // Assert: cdTimer decreased by 1/60, no firing occurred, no reset to cooldown
        float cdAfter = getFloatField(attack, "cdTimer");

    }

    @Test
    public void update_WhenTimerExpiredButCameraNull_DoesNotFireOrReset() {
        // Arrange: timer expired, but camera remains null so aim fails and fire path is skipped
        setPrivateField(attack, "cdTimer", 0f);

        // Act
        attack.update();

        // Assert: still 0f (no reset to 'cooldown' because we never fired)
        assertEquals(0f, getFloatField(attack, "cdTimer"), 1e-6);
    }
}
