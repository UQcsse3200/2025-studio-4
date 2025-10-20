package com.csse3200.game.components.towers;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.currencysystem.CurrencyComponent.CurrencyType;
import com.csse3200.game.components.currencysystem.CurrencyManagerComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.CurrencyFactory;
import com.csse3200.game.rendering.Renderer;
import com.csse3200.game.services.ServiceLocator;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.ITerrainComponent;
import com.csse3200.game.areas.terrain.TerrainComponent;
import com.csse3200.game.areas2.terrainTwo.TerrainComponent2;

/**
 * Component that generates currency for the player at regular intervals.
 * Used by the Bank Tower to provide passive income by spawning collectible currency entities.
 *
 * Path A (level_A) controls the spawn interval:
 *  - 1 -> 5.0s, 2 -> 4.75s, 3 -> 4.5s, 4 -> 4.25s, 5 -> 4.0s
 *
 * Path B (level_B) controls what currencies are spawned:
 *  - 1 -> METAL_SCRAP only
 *  - 2 -> METAL_SCRAP + TITANIUM_CORE
 *  - 3+ -> METAL_SCRAP + TITANIUM_CORE + NEUROCHIP
 */
public class CurrencyGeneratorComponent extends Component {
    // Legacy defaults (not used in dynamic logic but kept for compatibility)
    private CurrencyType currencyType;
    private int currencyAmount;
    private float generationInterval; // legacy default, dynamic interval is derived from Path A
    private float timer = 0f;

    private static final float TTL_SECONDS = 10f;
    private static final int SCRAP_AMOUNT = 50;
    private static final int TITANIUM_AMOUNT = 1;
    private static final int NEUROCHIP_AMOUNT = 1;

    // New: push-out animation tuning
    private static final float PUSH_DURATION = 0.25f;      // seconds to push out from center
    private static final float TOP_LAYER_Y_NUDGE = 0.02f;  // smaller y draws on top of base

    // Cache of world map bounds rectangle
    private Rectangle mapWorldBounds;

    public CurrencyGeneratorComponent(CurrencyType currencyType, int currencyAmount, float generationInterval) {
        this.currencyType = currencyType;
        this.currencyAmount = currencyAmount;
        this.generationInterval = generationInterval;
    }

    @Override
    public void update() {
        // Read dynamic levels from tower stats
        TowerStatsComponent stats = entity != null ? entity.getComponent(TowerStatsComponent.class) : null;
        if (stats == null) {
            return;
        }

        // Derive interval from Path A (level_A)
        float interval = intervalForLevel(stats.getLevel_A());

        // Tick timer
        float dt = ServiceLocator.getTimeSource() != null ? ServiceLocator.getTimeSource().getDeltaTime() : 0f;
        timer += dt;
        if (timer < interval) {
            return;
        }
        timer = 0f;

        // Decide which currencies to spawn from Path B (level_B)
        int levelB = stats.getLevel_B();
        boolean spawnScrap = true;
        boolean spawnTitanium = levelB >= 2;
        boolean spawnNeurochip = levelB >= 3;

        // Spawn at least Metal Scrap (50)
        if (spawnScrap) {
            spawnOne(CurrencyType.METAL_SCRAP, SCRAP_AMOUNT, stats);
        }
        if (spawnTitanium) {
            spawnOne(CurrencyType.TITANIUM_CORE, TITANIUM_AMOUNT, stats);
        }
        if (spawnNeurochip) {
            spawnOne(CurrencyType.NEUROCHIP, NEUROCHIP_AMOUNT, stats);
        }
    }

    private static float intervalForLevel(int levelA) {
        // 1 -> 5.0, 2 -> 4.75, 3 -> 4.5, 4 -> 4.25, 5+ -> 4.0
        switch (Math.max(1, Math.min(5, levelA))) {
            case 2: return 4.75f;
            case 3: return 4.50f;
            case 4: return 4.25f;
            case 5:
            default: return 4.00f;
            case 1: // keep last to satisfy switch fall-through lints
                return 5.00f;
        }
    }

    private void spawnOne(CurrencyType type, int amount, TowerStatsComponent stats) {
        // Find player + currency manager
        Entity player = findPlayer();
        if (player == null) return;
        CurrencyManagerComponent cm = player.getComponent(CurrencyManagerComponent.class);
        if (cm == null) return;

        // Random direction
        float theta = MathUtils.random(0f, MathUtils.PI2);
        float dirX = MathUtils.cos(theta);
        float dirY = MathUtils.sin(theta);

        // Base radius: use tower range if present, else small fallback
        float range = stats.getRange();
        float baseRadius = range > 0f ? range : 0.75f;

        // Ensure we land outside the bank footprint (4x4 -> half-size 2)
        float minSpawnRadius = 0.6f;
        TowerComponent tc = entity.getComponent(TowerComponent.class);
        if (tc != null) {
            float halfW = tc.getWidth() * 0.5f;
            float halfH = tc.getHeight() * 0.5f;
            minSpawnRadius = Math.max(halfW, halfH) + 0.1f; // just outside footprint
        }

        // Choose final distance: just outside footprint, keep it very close
        float extraMin = 0.05f;
        float extraMax = 0.20f;
        float targetDistance = minSpawnRadius + MathUtils.random(extraMin, extraMax);

        // Start at the center of the bank (slightly nudged forward in y so it renders above)
        float centerX = entity.getCenterPosition().x;
        float centerY = entity.getCenterPosition().y;
        float startX = centerX;
        float startY = centerY - TOP_LAYER_Y_NUDGE;

        // Intended destination before clamping
        float endX = centerX + dirX * targetDistance;
        float endY = centerY + dirY * targetDistance - TOP_LAYER_Y_NUDGE;

        // Reflect against actual map world bounds (not camera view)
        Rectangle worldBounds = getMapWorldBounds();
        if (worldBounds != null) {
            float padding = 0.25f; // small margin from edge to keep items visible
            float minX = worldBounds.x + padding;
            float maxX = worldBounds.x + worldBounds.width - padding;
            float minY = worldBounds.y + padding;
            float maxY = worldBounds.y + worldBounds.height - padding;

            // Mirror any overshoot back into the bounds
            endX = reflectWithin(endX, minX, maxX);
            endY = reflectWithin(endY, minY, maxY);

            // Keep final distance to a tight ring near the bank after rebound
            float offX = endX - centerX;
            float offY = endY - centerY;
            float len = (float) Math.sqrt(offX * offX + offY * offY);

            float minLen = minSpawnRadius + 0.05f;
            float maxLen = minSpawnRadius + 0.25f;

            float nx = offX, ny = offY;
            if (len <= 1e-4f) {
                nx = dirX;
                ny = dirY;
                len = 1f;
            }
            nx /= len;
            ny /= len;

            float desiredLen = MathUtils.clamp(len, minLen, maxLen);
            endX = centerX + nx * desiredLen;
            endY = centerY + ny * desiredLen - TOP_LAYER_Y_NUDGE;

            // Re-apply rebound to ensure position stays on the map
            endX = reflectWithin(endX, minX, maxX);
            endY = reflectWithin(endY, minY, maxY);

            // Ensure still not inside footprint after final rebound
            offX = endX - centerX;
            offY = endY - centerY;
            len = (float) Math.sqrt(offX * offX + offY * offY);
            if (len < minSpawnRadius) {
                endX = centerX + nx * (minSpawnRadius + 0.05f);
                endY = centerY + ny * (minSpawnRadius + 0.05f) - TOP_LAYER_Y_NUDGE;
                endX = reflectWithin(endX, minX, maxX);
                endY = reflectWithin(endY, minY, maxY);
            }
        }

        // Create currency at center (with TTL), add push-out animation, then register
        Entity currency = CurrencyFactory.createCurrency(type, amount, startX, startY, TTL_SECONDS);

        // Capture final copies for inner class use
        final float sX = startX;
        final float sY = startY;
        final float eX = endX;
        final float eY = endY;

        // Anonymous component to push it outward over time
        currency.addComponent(new Component() {
            private float t = 0f;
            @Override
            public void update() {
                float dt = ServiceLocator.getTimeSource() != null ? ServiceLocator.getTimeSource().getDeltaTime() : 0f;
                if (t >= PUSH_DURATION) return;
                t = Math.min(PUSH_DURATION, t + dt);
                float a = t / PUSH_DURATION; // linear; can swap for easing if desired
                float nx = sX + (eX - sX) * a;
                float ny = sY + (eY - sY) * a;
                entity.setPosition(nx, ny);
            }
        });

        ServiceLocator.getEntityService().register(currency);
        cm.addCurrencyEntity(currency);
    }

    // Reflect a coordinate inside [min, max] by mirroring any overshoot at the border
    private static float reflectWithin(float val, float min, float max) {
        if (val < min) {
            return min + (min - val);
        } else if (val > max) {
            return max - (val - max);
        }
        return val;
    }

    // Compute and cache the world-space rectangle of the current terrain (works for all orientations)
    private Rectangle getMapWorldBounds() {
        if (mapWorldBounds != null) {
            return mapWorldBounds;
        }
        ITerrainComponent terrain = findTerrain();
        if (terrain == null) return null;

        GridPoint2 size = terrain.getMapBounds(0);
        // Sample 4 corners in tile space and convert to world; take extrema to build an AABB
        Vector2 p00 = terrain.tileToWorldPosition(0, 0);
        Vector2 pW0 = terrain.tileToWorldPosition(size.x, 0);
        Vector2 p0H = terrain.tileToWorldPosition(0, size.y);
        Vector2 pWH = terrain.tileToWorldPosition(size.x, size.y);

        float minX = Math.min(Math.min(p00.x, pW0.x), Math.min(p0H.x, pWH.x));
        float maxX = Math.max(Math.max(p00.x, pW0.x), Math.max(p0H.x, pWH.x));
        float minY = Math.min(Math.min(p00.y, pW0.y), Math.min(p0H.y, pWH.y));
        float maxY = Math.max(Math.max(p00.y, pW0.y), Math.max(p0H.y, pWH.y));

        mapWorldBounds = new Rectangle(minX, minY, maxX - minX, maxY - minY);
        return mapWorldBounds;
    }

    // Find any terrain component (supports both terrain implementations)
    private ITerrainComponent findTerrain() {
        Array<Entity> all = safeEntities();
        if (all == null) return null;
        for (Entity e : all) {
            // Try TerrainComponent (areas.terrain)
            TerrainComponent t1 = e.getComponent(TerrainComponent.class);
            if (t1 != null) return t1;
            // Try TerrainComponent2 (areas2.terrainTwo)
            TerrainComponent2 t2 = e.getComponent(TerrainComponent2.class);
            if (t2 != null) return t2;
        }
        return null;
    }

    private Entity findPlayer() {
        Array<Entity> all = safeEntities();
        if (all == null) return null;
        for (Entity e : all) {
            if (e != null && e.getComponent(com.csse3200.game.components.player.PlayerActions.class) != null) {
                return e;
            }
        }
        return null;
    }

    private Array<Entity> safeEntities() {
        try {
            return ServiceLocator.getEntityService().getEntitiesCopy();
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Gets the current currency type being generated.
     *
     * @return The currency type.
     */
    public CurrencyType getCurrencyType() {
        return currencyType;
    }

    /**
     * Sets the currency type to generate.
     *
     * @param currencyType The new currency type.
     */
    public void setCurrencyType(CurrencyType currencyType) {
        this.currencyType = currencyType;
    }

    /**
     * Gets the amount of currency generated per interval.
     *
     * @return The currency amount.
     */
    public int getCurrencyAmount() {
        return currencyAmount;
    }

    /**
     * Sets the amount of currency generated per interval.
     *
     * @param currencyAmount The new currency amount.
     */
    public void setCurrencyAmount(int currencyAmount) {
        this.currencyAmount = currencyAmount;
    }

    /**
     * Gets the generation interval in seconds.
     *
     * @return The generation interval.
     */
    public float getGenerationInterval() {
        return generationInterval;
    }

    /**
     * Sets the generation interval in seconds.
     *
     * @param generationInterval The new generation interval.
     */
    public void setGenerationInterval(float generationInterval) {
        this.generationInterval = generationInterval;
    }

    // Helper: current camera (unused for bounds now, kept if referenced elsewhere)
    private Camera getCamera() {
        Renderer r = Renderer.getCurrentRenderer();
        if (r != null && r.getCamera() != null) {
            return r.getCamera().getCamera();
        }
        return null;
    }
}
