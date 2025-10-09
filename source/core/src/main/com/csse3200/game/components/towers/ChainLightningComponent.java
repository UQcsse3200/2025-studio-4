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

import java.util.*;

/**
 * Chain Lightning component for the Village Shaman tower.
 * Draws white lightning bolts chaining between multiple enemies.
 */
public class ChainLightningComponent extends Component {
    private final float range;         // Max range from tower
    private final float chainRange;    // Range between chained targets
    private final float damage;
    private final int maxChains;
    private final float cooldown;

    private float timer = 0f;
    private final ShapeRenderer renderer = new ShapeRenderer();

    private final List<Vector2> lightningPoints = new ArrayList<>();
    private float lightningTimer = 0f;
    private boolean lightningActive = false;

    public ChainLightningComponent(float range, float damage, float cooldown, int maxChains, float chainRange) {
        this.range = range;
        this.damage = damage;
        this.cooldown = cooldown;
        this.maxChains = 20; // Increased to 20
        this.chainRange = chainRange;
    }

    /**
     * Updates the chain lightning logic, finds targets, applies damage, and handles rendering.
     */
    @Override
    public void update() {
        float dt = Gdx.graphics.getDeltaTime();
        timer += dt;

        if (lightningActive) {
            lightningTimer += dt;
            if (lightningTimer > 0.15f) { // lightning lasts briefly
                lightningActive = false;
                lightningPoints.clear();
            }
        }

        if (timer >= cooldown) {
            List<Entity> chain = findChainTargets();
            if (!chain.isEmpty()) {
                dealDamage(chain);
                createLightningPoints(chain);
                lightningActive = true;
                lightningTimer = 0f;
                ServiceLocator.getAudioService().playSound("lightning_zap");
            }
            timer = 0f;
        }
    }

    /**
     * Deals damage to each entity in the chain, with diminishing returns.
     * @param chain The list of entities in the chain.
     */
    private void dealDamage(List<Entity> chain) {
        float dmg = damage;
        for (Entity e : chain) {
            CombatStatsComponent stats = e.getComponent(CombatStatsComponent.class);
            if (stats != null) {
                stats.setHealth(Math.max(0, stats.getHealth() - (int) dmg));
            }
            dmg *= 0.8f; // Each jump deals less
        }
    }

    /**
     * Creates the points for rendering the lightning chain.
     * @param chain The list of entities in the chain.
     */
    private void createLightningPoints(List<Entity> chain) {
        lightningPoints.clear();
        Vector2 start = entity.getCenterPosition();
        lightningPoints.add(new Vector2(start));

        for (Entity e : chain) {
            lightningPoints.add(new Vector2(e.getCenterPosition()));
        }
    }

    /**
     * Finds the chain of targets for the lightning effect.
     * @return List of entities in the chain.
     */
    private List<Entity> findChainTargets() {
        List<Entity> result = new ArrayList<>();
        Entity current = findNearestEnemy(entity.getCenterPosition(), range, null);
        if (current == null) return result;
        result.add(current);

        for (int i = 1; i < maxChains; i++) {
            Entity next = findNearestEnemy(current.getCenterPosition(), chainRange, result);
            if (next == null) break;
            result.add(next);
            current = next;
        }
        return result;
    }

    /**
     * Finds the nearest valid enemy from a given position, excluding specified entities.
     * @param from The starting position.
     * @param searchRange The search range.
     * @param exclude List of entities to exclude.
     * @return The nearest enemy entity, or null if none found.
     */
    private Entity findNearestEnemy(Vector2 from, float searchRange, List<Entity> exclude) {
        Entity closest = null;
        float minDist = Float.MAX_VALUE;

        for (Entity e : ServiceLocator.getEntityService().getEntitiesCopy()) {
            if (!isValidEnemy(e)) continue;
            if (exclude != null && exclude.contains(e)) continue;

            Vector2 pos = e.getCenterPosition();
            float d = pos.dst(from);
            if (d < searchRange && d < minDist) {
                minDist = d;
                closest = e;
            }
        }
        return closest;
    }

    /**
     * Checks if an entity is a valid enemy for chain lightning.
     * @param e The entity to check.
     * @return True if valid enemy, false otherwise.
     */
    private boolean isValidEnemy(Entity e) {
        if (e == null || e == entity) return false;
        if (e.getComponent(ProjectileComponent.class) != null) return false;
        if (e.getComponent(CombatStatsComponent.class) == null) return false;

        HitboxComponent hb = e.getComponent(HitboxComponent.class);
        if (hb == null || hb.getFixture() == null || hb.getFixture().getFilterData() == null) return false;
        short cat = hb.getFixture().getFilterData().categoryBits;
        return PhysicsLayer.contains(PhysicsLayer.NPC, cat);
    }

    /**
     * Draws the lightning chain between targets.
     * @param batch The SpriteBatch used for rendering.
     */
    public void draw(SpriteBatch batch) {
        if (!lightningActive || lightningPoints.size() < 2) return;

        batch.end();
        renderer.setProjectionMatrix(batch.getProjectionMatrix());
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        renderer.begin(ShapeRenderer.ShapeType.Line);
        renderer.setColor(Color.WHITE);

        for (int i = 0; i < lightningPoints.size() - 1; i++) {
            Vector2 start = lightningPoints.get(i);
            Vector2 end = lightningPoints.get(i + 1);
            drawJaggedLine(start, end, 6, 0.15f);
        }

        renderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
        batch.begin();
    }

    /**
     * Draws a jagged line between two points for the lightning effect.
     * @param start The start position.
     * @param end The end position.
     * @param segments Number of segments in the line.
     * @param jitterAmount Amount of jitter for the jagged effect.
     */
    private void drawJaggedLine(Vector2 start, Vector2 end, int segments, float jitterAmount) {
        Vector2 dir = new Vector2(end).sub(start);
        float segmentLength = dir.len() / segments;
        dir.nor();

        Vector2 prev = new Vector2(start);
        for (int i = 1; i <= segments; i++) {
            Vector2 next = new Vector2(dir).scl(i * segmentLength).add(start);
            next.add((float) (Math.random() - 0.5f) * jitterAmount, (float) (Math.random() - 0.5f) * jitterAmount);
            renderer.line(prev, next);
            prev.set(next);
        }
    }

    /**
     * Disposes of resources used by the chain lightning component.
     */
    @Override
    public void dispose() {
        renderer.dispose();
    }

    /**
     * RenderComponent for drawing the chain lightning on top of all other components.
     */
    public static class ChainLightningRenderComponent extends com.csse3200.game.rendering.RenderComponent {
        private final ChainLightningComponent lightning;
        /**
         * Constructs a ChainLightningRenderComponent for the given ChainLightningComponent.
         * @param lightning The ChainLightningComponent to render.
         */
        public ChainLightningRenderComponent(ChainLightningComponent lightning) {
            this.lightning = lightning;
        }
        /**
         * Gets the rendering layer for the lightning.
         * @return The layer value.
         */
        @Override
        public int getLayer() {
            return 9999;
        }
        /**
         * Draws the lightning using the provided SpriteBatch.
         * @param batch The SpriteBatch used for rendering.
         */
        @Override
        protected void draw(SpriteBatch batch) {
            lightning.draw(batch);
        }
    }
}
