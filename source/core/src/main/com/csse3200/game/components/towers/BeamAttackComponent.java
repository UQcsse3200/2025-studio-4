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
                        // Play laser sound effect when attacking
                        ServiceLocator.getAudioService().playSound("laser");
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
        if (target == null || batch == null) return;

        Vector2 start = centerSafe(entity);
        Vector2 end = centerSafe(target);
        if (start == null || end == null) return;

        // --- Start beam closer to the center of the tower ---
        Vector2 dir = new Vector2(end).sub(start);
        if (dir.len() > 0.01f) {
            dir.nor();
            // Tweak these values to move the beam start further away from the center:
            float offset = Math.min(range * 0.15f, 0.25f); // Increase multiplier or max for further away
            start.add(dir.scl(offset));
        }

        float sx = start.x, sy = start.y, ex = end.x, ey = end.y;
        boolean samePoint = start.epsilonEquals(end, 0.001f);

        batch.end();
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Fixed beam width for focused look
        float beamWidth = 0.2f;

        // Layer 1: Core white (minimized)
        Color coreWhite = new Color(1f, 1f, 1f, 0.5f);

        // Layer 2: Mid red-white
        Color midRedWhite = new Color(1f, 0.5f, 0.5f, 0.7f);

        // Layer 3: Bright, opaque red (dominant)
        Color brightRed = new Color(1f, 0f, 0f, 1f);

        // Layer 4: Outer red glow (slightly translucent for edge)
        Color outerRed = new Color(1f, 0.1f, 0.1f, 0.7f);

        // Draw from outer to inner for best fade effect
        if (!samePoint) {
            // Outer glow
            shapeRenderer.setColor(outerRed);
            shapeRenderer.rectLine(sx, sy, ex, ey, beamWidth * 2.0f);

            // Bright red (opaque, dominant)
            shapeRenderer.setColor(brightRed);
            shapeRenderer.rectLine(sx, sy, ex, ey, beamWidth * 1.2f);

            // Mid red-white
            shapeRenderer.setColor(midRedWhite);
            shapeRenderer.rectLine(sx, sy, ex, ey, beamWidth * 0.7f);

            // Core white (minimized)
            shapeRenderer.setColor(coreWhite);
            shapeRenderer.rectLine(sx, sy, ex, ey, beamWidth * 0.3f);
        } else {
            // Outer glow
            shapeRenderer.setColor(outerRed);
            shapeRenderer.circle(sx, sy, beamWidth * 2.0f, 16);

            // Bright red (opaque, dominant)
            shapeRenderer.setColor(brightRed);
            shapeRenderer.circle(sx, sy, beamWidth * 1.2f, 16);

            // Mid red-white
            shapeRenderer.setColor(midRedWhite);
            shapeRenderer.circle(sx, sy, beamWidth * 0.7f, 16);

            // Core white (minimized)
            shapeRenderer.setColor(coreWhite);
            shapeRenderer.circle(sx, sy, beamWidth * 0.3f, 16);
        }

        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
        batch.begin();
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
        public int getLayer() {
            // Return a very high layer so the beam is rendered on top of all other components
            return 9999;
        }
        @Override
        protected void draw(SpriteBatch batch) {
            beam.draw(batch);
        }
    }
}
