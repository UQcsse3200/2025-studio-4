package com.csse3200.game.components.towers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.projectile.ProjectileComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.services.ServiceLocator;

import java.util.ArrayList;
import java.util.List;

/**
 * Component that lets a tower fire a continuous red beam toward the nearest enemy in range.
 * Beam is purely visual (no damage).
 */
public class BeamAttackComponent extends Component {
    private final float range;
    private final float damage;
    private final float cooldown;
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private Entity target;

    private float attackTimer = 0f;

    public BeamAttackComponent(float range, float damage, float cooldown) {
        this.range = range;
        this.damage = damage;
        this.cooldown = cooldown;
    }

    @Override
    public void update() {
        // If assigned target is invalid, try to find a new one (keeps component usable standalone)
        if (target == null || !target.isActive() || !isInRange(target) || !isValidEnemy(target)) {
            target = findNearestEnemy();
        }

        float dt = Gdx.graphics != null ? Gdx.graphics.getDeltaTime() : (1f / 60f);
        attackTimer += dt;

        // Deal damage immediately if cooldown is over, then reset timer
        if (target != null && target.isActive() && isInRange(target) && isValidEnemy(target)) {
            if (attackTimer >= cooldown) {
                CombatStatsComponent stats = target.getComponent(CombatStatsComponent.class);
                if (stats != null) {
                    try {
                        int newHealth = Math.max(0, stats.getHealth() - (int)damage);
                        stats.setHealth(newHealth);
                    } catch (Exception ignored) {}
                }
                attackTimer = 0f;
            }
        }
    }

    // External API: allow other code to set/clear the beam target
    public void setTarget(Entity e) {
        this.target = e;
    }

    public void clearTarget() {
        this.target = null;
    }

    public void draw(SpriteBatch batch) {
        // Nothing to draw if no target or batch not available
        if (target == null || batch == null) return;

        Vector2 start = centerSafe(entity);
        Vector2 end = centerSafe(target);
        if (start == null || end == null) return;

        // If start and end are essentially identical, draw a small indicator instead
        final boolean samePoint = start.epsilonEquals(end, 0.001f);

        // Snapshot coordinates so we don't reference mutable positions later.
        final float sx = start.x;
        final float sy = start.y;
        final float ex = end.x;
        final float ey = end.y;

        // Immediate draw using ShapeRenderer aligned to the batch projection matrix.
        try {
            batch.end();

            // Align ShapeRenderer with the batch projection so coordinates match
            shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

            // Draw a visible, filled red beam (thicker for clarity)
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(Color.RED);

            float beamWidth = 0.45f; // increased thickness for visibility
            if (!samePoint) {
                shapeRenderer.rectLine(sx, sy, ex, ey, beamWidth);
            } else {
                shapeRenderer.circle(sx, sy, beamWidth * 1.2f, 16);
            }

            // Remove pink endpoint highlighting
            // shapeRenderer.setColor(new Color(1f, 0.6f, 0.6f, 1f));
            // float endpointRadius = beamWidth * 1.1f;
            // shapeRenderer.circle(sx, sy, endpointRadius, 12);
            // shapeRenderer.circle(ex, ey, endpointRadius, 12);

            shapeRenderer.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);
        } catch (Exception drawEx) {
            Gdx.app.error("BeamAttack", "Error drawing beam: " + drawEx.getMessage(), drawEx);
        } finally {
            try {
                batch.begin();
            } catch (Exception ignored) {}
        }
    }

    private boolean isInRange(Entity enemy) {
        Vector2 c1 = centerSafe(enemy);
        Vector2 c2 = centerSafe(entity);
        if (c1 == null || c2 == null) return false;
        return c1.dst(c2) <= range;
    }

    /**
     * Consider an entity a valid enemy if:
     *  - it is not a projectile
     *  - it has CombatStatsComponent
     *  - it has a hitbox and NPC category bits
     *
     * This mirrors TowerComponent's targeting checks so enemies are detected reliably.
     */
    private boolean isValidEnemy(Entity e) {
        if (e == null) return false;
        // ignore projectiles and other non-enemy actors
        if (e.getComponent(ProjectileComponent.class) != null) return false;
        if (e.getComponent(CombatStatsComponent.class) == null) return false;

        HitboxComponent hb = e.getComponent(HitboxComponent.class);
        if (hb == null || hb.getFixture() == null || hb.getFixture().getFilterData() == null) {
            return false;
        }
        short cat = hb.getFixture().getFilterData().categoryBits;
        return PhysicsLayer.contains(PhysicsLayer.NPC, cat);
    }

    /**
     * Find nearest valid NPC enemy by scanning registered entities.
     * Only considers entities passing isValidEnemy().
     */
    private Entity findNearestEnemy() {
        List<Entity> all = new ArrayList<>();
        try {
            var arr = ServiceLocator.getEntityService().getEntitiesCopy();
            if (arr != null) {
                for (Entity e : arr) {
                    if (e != null) all.add(e);
                }
            }
        } catch (Exception ignored) {}

        Entity closest = null;
        float minDist = Float.MAX_VALUE;
        Vector2 selfCenter = centerSafe(entity);
        if (selfCenter == null) return null;

        for (Entity e : all) {
            if (e == null || e == entity) continue;
            if (!isValidEnemy(e)) continue;
            Vector2 p = centerSafe(e);
            if (p == null) continue;
            float dist = p.dst(selfCenter);
            if (dist <= range && dist < minDist) {
                closest = e;
                minDist = dist;
            }
        }
        return closest;
    }

    private Vector2 centerSafe(Entity e) {
        try {
            Vector2 c = e.getCenterPosition();
            if (c != null) return c;
            return e.getPosition();
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        super.dispose();
    }

    // Add this inner class at the end of BeamAttackComponent
    public static class BeamRenderComponent extends com.csse3200.game.rendering.RenderComponent {
        private final BeamAttackComponent beam;
        public BeamRenderComponent(BeamAttackComponent beam) {
            this.beam = beam;
        }
        @Override
        protected void draw(SpriteBatch batch) {
            beam.draw(batch);
        }
    }
}
