package com.csse3200.game.components.hero;

import com.badlogic.gdx.Input;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.HeroConfig;
import com.csse3200.game.entities.configs.HeroConfig2;
import com.csse3200.game.entities.configs.HeroConfig3;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.*;


public class HeroOneShotFormSwitchComponentTest {

    private Entity hero;
    private CombatStatsComponent stats;
    private HeroTurretAttackComponent atk;

    // 配置：给三个形态设置截然不同的数值，方便断言
    private HeroConfig  cfg1;
    private HeroConfig2 cfg2;
    private HeroConfig3 cfg3;

    @Before
    public void setUp() {
        hero = new Entity();
        stats = new CombatStatsComponent(/*health*/100, /*baseAttack*/5, null, null);
        atk   = new HeroTurretAttackComponent(
                /*cooldown*/ 0.99f,
                /*bulletSpeed*/ 123f,
                /*bulletLife*/ 9.9f,
                /*bulletTexture*/ "init/bullet.png",
                /*camera*/ null
        );
        hero.addComponent(stats).addComponent(atk);


        cfg1 = new HeroConfig();
        cfg1.baseAttack = 11;
        cfg1.attackCooldown = 0.50f;
        cfg1.bulletSpeed = 400f;
        cfg1.bulletLife  = 1.1f;
        cfg1.bulletTexture = "images/hero/Bullet.png";

        cfg2 = new HeroConfig2();
        cfg2.baseAttack = 22;
        cfg2.attackCooldown = 0.20f;
        cfg2.bulletSpeed = 800f;
        cfg2.bulletLife  = 2.2f;
        cfg2.bulletTexture = "images/hero2/Bullet.png";

        cfg3 = new HeroConfig3();
        cfg3.baseAttack = 33;
        cfg3.attackCooldown = 0.80f;
        cfg3.bulletSpeed = 1600f;
        cfg3.bulletLife  = 3.3f;
        cfg3.bulletTexture = "images/hero3/Bullet.png";
    }


    private static Object getPrivateField(Object target, String field) {
        try {
            Field f = target.getClass().getDeclaredField(field);
            f.setAccessible(true);
            return f.get(target);
        } catch (Exception e) {
            throw new AssertionError("反射读取字段失败: " + field, e);
        }
    }


    private static void setPrivateField(Object target, String field, Object value) {
        try {
            Field f = target.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new AssertionError("反射写入字段失败: " + field, e);
        }
    }

    private static String getBulletTextureViaReflection(HeroTurretAttackComponent c) {
        return (String) getPrivateField(c, "bulletTexture");
    }
    private static float getCooldownViaReflection(HeroTurretAttackComponent c) {
        return ((Number) getPrivateField(c, "cooldown")).floatValue();
    }
    private static float getBulletSpeedViaReflection(HeroTurretAttackComponent c) {
        return ((Number) getPrivateField(c, "bulletSpeed")).floatValue();
    }
    private static float getBulletLifeViaReflection(HeroTurretAttackComponent c) {
        return ((Number) getPrivateField(c, "bulletLife")).floatValue();
    }



    private HeroOneShotFormSwitchComponent buildSwitcher() {
        HeroOneShotFormSwitchComponent sw = new HeroOneShotFormSwitchComponent(cfg1, cfg2, cfg3);
        setPrivateField(sw, "hero", hero);
        return sw;
    }

    @Test
    public void press2_ShouldApplyConfig2Values() {
        HeroOneShotFormSwitchComponent sw = buildSwitcher();


        boolean result = sw.keyDown(Input.Keys.NUM_2);
        assertFalse("切换贴图在本测试场景下可能返回 false（无渲染器），属预期", result);


        assertEquals(cfg2.baseAttack, stats.getBaseAttack());


        assertEquals(cfg2.attackCooldown, getCooldownViaReflection(atk), 1e-6);
        assertEquals(cfg2.bulletSpeed,   getBulletSpeedViaReflection(atk), 1e-6);
        assertEquals(cfg2.bulletLife,    getBulletLifeViaReflection(atk), 1e-6);
        assertEquals(cfg2.bulletTexture, getBulletTextureViaReflection(atk));
    }

    @Test
    public void press3_ShouldApplyConfig3Values() {
        HeroOneShotFormSwitchComponent sw = buildSwitcher();

        boolean result = sw.keyDown(Input.Keys.NUM_3);
        assertFalse(result);

        assertEquals(cfg3.baseAttack, stats.getBaseAttack());
        assertEquals(cfg3.attackCooldown, getCooldownViaReflection(atk), 1e-6);
        assertEquals(cfg3.bulletSpeed,   getBulletSpeedViaReflection(atk), 1e-6);
        assertEquals(cfg3.bulletLife,    getBulletLifeViaReflection(atk), 1e-6);
        assertEquals(cfg3.bulletTexture, getBulletTextureViaReflection(atk));
    }

    @Test
    public void press1_ShouldApplyConfig1Values() {
        HeroOneShotFormSwitchComponent sw = buildSwitcher();

        boolean result = sw.keyDown(Input.Keys.NUM_1);
        assertFalse(result);

        assertEquals(cfg1.baseAttack, stats.getBaseAttack());
        assertEquals(cfg1.attackCooldown, getCooldownViaReflection(atk), 1e-6);
        assertEquals(cfg1.bulletSpeed,   getBulletSpeedViaReflection(atk), 1e-6);
        assertEquals(cfg1.bulletLife,    getBulletLifeViaReflection(atk), 1e-6);
        assertEquals(cfg1.bulletTexture, getBulletTextureViaReflection(atk));
    }
}
