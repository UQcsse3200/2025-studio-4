package com.csse3200.game.entities.factories;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.components.projectile.ProjectileComponent;
import com.csse3200.game.components.TouchAttackComponent;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.projectile.DestroyOnHitComponent;
import com.csse3200.game.physics.PhysicsLayer;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockConstruction;

/**
 * Tests for {@link ProjectileFactory}.
 * We use Mockito constructor-mocking to avoid touching libGDX/asset loading during component construction.
 */
class ProjectileFactoryTest {

    @Test
    void createBullet_withCustomDamage_buildsExpectedComponents() {
        String texture = "bullet.png";
        Vector2 start = new Vector2(10f, 20f);
        float vx = 300f, vy = 0f, life = 2.0f;
        int damage = 42;

        try (MockedConstruction<TextureRenderComponent> mockTex =
                     mockConstruction(TextureRenderComponent.class);
             MockedConstruction<ProjectileComponent> mockProj =
                     mockConstruction(ProjectileComponent.class);
             MockedConstruction<TouchAttackComponent> mockTouch =
                     mockConstruction(TouchAttackComponent.class);
             MockedConstruction<DestroyOnHitComponent> mockDestroy =
                     mockConstruction(DestroyOnHitComponent.class)) {

            Entity bullet = ProjectileFactory.createBullet(texture, start, vx, vy, life, damage);

            assertNotNull(bullet, "Factory should return a non-null bullet");

            // Physics + hitbox
            assertNotNull(bullet.getComponent(PhysicsComponent.class), "PhysicsComponent missing");
            HitboxComponent hitbox = bullet.getComponent(HitboxComponent.class);
            assertNotNull(hitbox, "HitboxComponent missing");
            assertEquals(PhysicsLayer.PROJECTILE, hitbox.getLayer(), "Hitbox layer should be PROJECTILE");

            // Render, projectile movement/lifetime, attack, destroy-on-hit (都被构造器 mock 拦截)
            assertNotNull(bullet.getComponent(TextureRenderComponent.class), "TextureRenderComponent missing");
            assertNotNull(bullet.getComponent(ProjectileComponent.class), "ProjectileComponent missing");
            assertNotNull(bullet.getComponent(TouchAttackComponent.class), "TouchAttackComponent missing");
            assertNotNull(bullet.getComponent(DestroyOnHitComponent.class), "DestroyOnHitComponent missing");

            // Combat stats: health=1, damage=damage
            CombatStatsComponent stats = bullet.getComponent(CombatStatsComponent.class);
            assertNotNull(stats, "CombatStatsComponent missing");
            assertEquals(1, stats.getHealth());
            assertEquals(damage, stats.getBaseAttack());

            // Spawn position should be set
            assertEquals(start.x, bullet.getPosition().x, 1e-4);
            assertEquals(start.y, bullet.getPosition().y, 1e-4);

            // （可选）确认“重”组件构造器被调用
            assertEquals(1, mockTex.constructed().size());
            assertEquals(1, mockProj.constructed().size());
            assertEquals(1, mockTouch.constructed().size());
            assertEquals(1, mockDestroy.constructed().size());
        }
    }

    @Test
    void createBullet_withDefaultDamage_usesFactoryDefaultAndBuildsComponents() {
        String texture = "bullet.png";
        Vector2 start = new Vector2(0f, 0f);
        float vx = 100f, vy = 50f, life = 1.5f;

        try (MockedConstruction<TextureRenderComponent> mockTex =
                     mockConstruction(TextureRenderComponent.class);
             MockedConstruction<ProjectileComponent> mockProj =
                     mockConstruction(ProjectileComponent.class);
             MockedConstruction<TouchAttackComponent> mockTouch =
                     mockConstruction(TouchAttackComponent.class);
             MockedConstruction<DestroyOnHitComponent> mockDestroy =
                     mockConstruction(DestroyOnHitComponent.class)) {

            Entity bullet = ProjectileFactory.createBullet(texture, start, vx, vy, life);
            assertNotNull(bullet);

            assertNotNull(bullet.getComponent(PhysicsComponent.class));
            assertNotNull(bullet.getComponent(HitboxComponent.class));
            assertNotNull(bullet.getComponent(TextureRenderComponent.class));
            assertNotNull(bullet.getComponent(ProjectileComponent.class));
            assertNotNull(bullet.getComponent(TouchAttackComponent.class));
            assertNotNull(bullet.getComponent(DestroyOnHitComponent.class));

            CombatStatsComponent stats = bullet.getComponent(CombatStatsComponent.class);
            assertNotNull(stats);
            assertEquals(1, stats.getHealth());
            assertEquals(25, stats.getBaseAttack()); // defaultDamage
        }
    }
}
