package com.csse3200.game.components.projectile;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.areas.terrain.TerrainComponent;

/**
 * Handles projectile velocity, lifetime, and out-of-bounds cleanup.
 */
public class ProjectileComponent extends Component {
    private boolean dead = false;
    private final float vx, vy;
    private final float life;
    private float timer;
    private PhysicsComponent physics;

    // world bounds (lazy)
    private boolean boundsReady = false;
    private float minX = 0f, minY = 0f, maxX = Float.POSITIVE_INFINITY, maxY = Float.POSITIVE_INFINITY;
    private static final float EPSILON = 0.01f;

    public ProjectileComponent(float vx, float vy, float life) {
        this.vx = vx; this.vy = vy; this.life = life;
    }

    @Override
    public void create() {
        physics = entity.getComponent(PhysicsComponent.class);
        timer = life;
        if (physics != null && physics.getBody() != null) {
            physics.getBody().setLinearVelocity(vx, vy);
        }
    }

    @Override
    public void update() {
        ensureWorldBounds();
        if (boundsReady && physics != null && physics.getBody() != null) {
            Vector2 pos = physics.getBody().getPosition();
            if (pos.x < (minX - EPSILON) || pos.x > (maxX + EPSILON) ||
                    pos.y < (minY - EPSILON) || pos.y > (maxY + EPSILON)) {
                killProjectile();
                return;
            }
        }
        float dt = ServiceLocator.getTimeSource().getDeltaTime();
        timer -= dt;
        if (timer <= 0f) {
            killProjectile();
        }
    }

    private void killProjectile() {
        if (dead) return;
        dead = true;
        if (physics != null && physics.getBody() != null) {
            physics.getBody().setLinearVelocity(0, 0);
            physics.getBody().setActive(false);
        }
        Gdx.app.postRunnable(() -> {
            try { entity.dispose(); } catch (Exception ignored) {}
        });
    }

    private void ensureWorldBounds() {
        if (boundsReady) return;
        var es = ServiceLocator.getEntityService();
        if (es == null) return;
        try {
            Array<Entity> all = es.getEntities();
            for (int i = 0, n = all.size; i < n; i++) {
                Entity e = all.get(i);
                TerrainComponent terrain = e.getComponent(TerrainComponent.class);
                if (terrain != null) {
                    float tileSize = terrain.getTileSize();
                    GridPoint2 tb = terrain.getMapBounds(0);
                    minX = 0f; minY = 0f;
                    maxX = tb.x * tileSize;
                    maxY = tb.y * tileSize;
                    boundsReady = true;
                    break;
                }
            }
        } catch (Throwable ignored) {
        }
    }
}
