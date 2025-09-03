package com.csse3200.game.entities.factories;

import com.badlogic.gdx.graphics.Camera;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.hero.HeroTurretAttackComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.HeroConfig;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.RotatingTextureRenderComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link HeroFactory}.
 * Uses Mockito "constructor mocking" to avoid touching libGDX/asset loading.
 */
class HeroFactoryTest {

    private HeroConfig cfg;
    private Camera mockCamera;

    @BeforeEach
    void setUp() {
        cfg = new HeroConfig();
        // strings (paths) only — HeroFactory uses them to build components
        cfg.heroTexture = "hero.png";
        cfg.health = 100;
        cfg.baseAttack = 20;
        cfg.attackCooldown = 1.5f;
        cfg.bulletSpeed = 300f;
        cfg.bulletLife = 2f;
        cfg.bulletTexture = "bullet.png";

        mockCamera = mock(Camera.class);
    }

    @Test
    void createHero_buildsEntityWithExpectedComponents() {
        // Intercept new RotatingTextureRenderComponent(String) and new HeroTurretAttackComponent(...)
        try (MockedConstruction<RotatingTextureRenderComponent> mockedRender =
                     mockConstruction(RotatingTextureRenderComponent.class);
             MockedConstruction<HeroTurretAttackComponent> mockedAttack =
                     mockConstruction(HeroTurretAttackComponent.class)) {

            Entity hero = HeroFactory.createHero(cfg, mockCamera);
            assertNotNull(hero, "Factory should return a non-null entity");

            // physics bits
            assertNotNull(hero.getComponent(PhysicsComponent.class), "PhysicsComponent missing");
            assertNotNull(hero.getComponent(ColliderComponent.class), "ColliderComponent missing");
            HitboxComponent hitbox = hero.getComponent(HitboxComponent.class);
            assertNotNull(hitbox, "HitboxComponent missing");
            assertEquals(PhysicsLayer.PLAYER, hitbox.getLayer(), "Hitbox layer should be PLAYER");

            // render (constructor was mocked to avoid asset loading)
            assertNotNull(hero.getComponent(RotatingTextureRenderComponent.class),
                    "RotatingTextureRenderComponent missing");

            // combat stats use values from cfg
            CombatStatsComponent combat = hero.getComponent(CombatStatsComponent.class);
            assertNotNull(combat, "CombatStatsComponent missing");
            assertEquals(100, combat.getHealth());
            assertEquals(20, combat.getBaseAttack());

            // attack component present (also mocked)
            assertNotNull(hero.getComponent(HeroTurretAttackComponent.class),
                    "HeroTurretAttackComponent missing");

            // default scale set by factory
            assertEquals(1f, hero.getScale().x, 1e-3);
            assertEquals(1f, hero.getScale().y, 1e-3);

            // (可选) 断言构造器确实被调用过一次
            assertEquals(1, mockedRender.constructed().size(),
                    "RotatingTextureRenderComponent should be constructed exactly once");
            assertEquals(1, mockedAttack.constructed().size(),
                    "HeroTurretAttackComponent should be constructed exactly once");
        }
    }
}
