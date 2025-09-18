package com.csse3200.game.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.components.currencysystem.CurrencyManagerComponent;
import com.csse3200.game.components.currencysystem.CurrencyComponent.CurrencyType;
import com.csse3200.game.entities.factories.ProjectileFactory;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector3;
import com.csse3200.game.components.projectile.ProjectileComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.rendering.Renderer;
import java.util.Map;

/**
 * Component representing a tower entity, including its type and size.
 * Handles tower attack logic in the update method.
 */
public class TowerComponent extends Component {
    private final String type;
    private final int width;  // in tiles
    private final int height; // in tiles
    
    // Additional fields for tower functionality
    private CurrencyType selectedPurchaseCurrency = CurrencyType.METAL_SCRAP;
    private boolean active = true;
    private boolean selected = false;
    private boolean showSellButton = false;
    private Entity headEntity;
    private AnimationRenderComponent headRenderer;

    /**
     * Constructs a single-tile tower component.
     * @param type The type of the tower
     */
    public TowerComponent(String type) {
        this(type, 1, 1); // default is 1x1
    }

    /**
     * Constructs a multi-tile tower component.
     * @param type The type of the tower
     * @param width Width in tiles
     * @param height Height in tiles
     */
    public TowerComponent(String type, int width, int height) {
        this.type = type;
        this.width = width;
        this.height = height;
    }

    /** @return The type of the tower */
    public String getType() {
        return type;
    }

    /** @return Width of the tower in tiles */
    public int getWidth() { return width; }

    /** @return Height of the tower in tiles */
    public int getHeight() { return height; }

    /**
     * Sets the selected purchase currency for this tower.
     *
     * @param currencyType The currency type to set.
     */
    public void setSelectedPurchaseCurrency(CurrencyType currencyType)
    {
        this.selectedPurchaseCurrency = currencyType;
    }

    /**
     * Gets the selected purchase currency for this tower.
     *
     * @return The selected currency type.
     */
    public CurrencyType getSelectedPurchaseCurrency()
    {
        return selectedPurchaseCurrency;
    }

    /**
     * Sets whether this tower is active.
     *
     * @param v True to activate, false to deactivate.
     * @return This TowerComponent for chaining.
     */
    public TowerComponent setActive(boolean v)
    {
        this.active = v;
        return this;
    }

    /**
     * Checks if this tower is active.
     *
     * @return True if active, false otherwise.
     */
    public boolean isActive()
    {
        return active;
    }

    /**
     * Checks if this tower is currently selected.
     *
     * @return True if selected, false otherwise.
     */
    public boolean isSelected()
    {
        return selected;
    }

    /**
     * Sets whether this tower is selected.
     *
     * @param selected True if selected, false otherwise.
     */
    public void setSelected(boolean selected)
    {
        this.selected = selected;
    }

    /**
     * Gets the head entity attached to this tower.
     *
     * @return The head entity, or null if none.
     */
    public Entity getHeadEntity()
    {
        return headEntity;
    }

    /**
     * Checks if this tower has a head entity.
     *
     * @return True if a head entity is attached, false otherwise.
     */
    public boolean hasHead()
    {
        return headEntity != null;
    }

    /**
     * Checks if the player can afford this tower with the selected currency.
     *
     * @param playerCurrencyManager The player's currency manager.
     * @return True if affordable, false otherwise.
     */
    public boolean canAffordWithSelectedCurrency(CurrencyManagerComponent playerCurrencyManager)
    {
        TowerCostComponent costComponent = entity.getComponent(TowerCostComponent.class);
        if (costComponent == null)
        {
            return false;
        }
        int cost = costComponent.getCost();
        return playerCurrencyManager.canAffordAndSpendCurrency(Map.of(selectedPurchaseCurrency, cost));
    }

    /**
     * Checks if an entity is a valid enemy target.
     *
     * @param e The entity to check.
     * @return True if the entity is a valid enemy target, false otherwise.
     */
    private static boolean isEnemyTarget(Entity e)
    {
        if (e.getComponent(ProjectileComponent.class) != null)
        {
            return false;
        }
        if (e.getComponent(CombatStatsComponent.class) == null)
        {
            return false;
        }

        HitboxComponent hb = e.getComponent(HitboxComponent.class);
        if (hb == null || hb.getFixture() == null || hb.getFixture().getFilterData() == null)
        {
            return false;
        }
        short cat = hb.getFixture().getFilterData().categoryBits;
        return PhysicsLayer.contains(PhysicsLayer.NPC, cat);
    }

    /**
     * Finds the player entity from a list of entities.
     *
     * @param entities The list of entities.
     * @return The player entity, or null if not found.
     */
    private Entity findPlayerEntity(java.util.List<Entity> entities)
    {
        for (Entity e : entities)
        {
            if (e.getComponent(CurrencyManagerComponent.class) != null)
            {
                return e;
            }
        }
        return null;
    }

    /**
     * Gets the current camera from the renderer.
     *
     * @return The camera, or null if not found.
     */
    private com.badlogic.gdx.graphics.Camera getCamera()
    {
        Renderer renderer = Renderer.getCurrentRenderer();
        if (renderer != null && renderer.getCamera() != null)
        {
            return renderer.getCamera().getCamera();
        }
        return null;
    }

    /**
     * Sells this tower, refunding a portion of its cost to the player.
     *
     * @param entities The list of entities to search for the player.
     */
    private void sellTower(java.util.List<Entity> entities)
    {
        TowerCostComponent costComponent = entity.getComponent(TowerCostComponent.class);
        if (costComponent == null)
        {
            return;
        }
        float refundRate = 0.75f;
        Entity player = findPlayerEntity(entities);
        if (player == null)
        {
            return;
        }
        CurrencyManagerComponent currencyManager = player.getComponent(CurrencyManagerComponent.class);
        if (currencyManager == null)
        {
            return;
        }
        int cost = costComponent.getCost();
        if (cost > 0)
        {
            currencyManager.refundCurrency(Map.of(selectedPurchaseCurrency, cost), refundRate);
        }
        entity.dispose();
    }

    /**
     * Updates the tower logic, including attacking and selection/sell logic.
     */
    @Override
    public void update() {
        TowerStatsComponent stats = entity.getComponent(TowerStatsComponent.class);
        if (stats == null) return;

        float delta = 1/60f; // or pass in the real delta time
        stats.updateAttackTimer(delta);

        if (!stats.canAttack()) return;

        Vector2 myCenter = entity.getCenterPosition();
        float range = stats.getRange();
        Entity target = null;

        // Find the nearest enemy target within range
        for (Entity other : ServiceLocator.getEntityService().getEntitiesCopy()) {
            if (other == entity) continue;

            // Only attack entities that have CombatStatsComponent
            CombatStatsComponent targetStats = other.getComponent(CombatStatsComponent.class);
            if (targetStats == null) continue;

            // Check if entity is a valid enemy target
            if (!isEnemyTarget(other)) continue;

            Vector2 toOther = other.getCenterPosition().cpy().sub(myCenter);
            if (toOther.len() <= range) {
                target = other;
                break;
            }
        }

        // Attack the target if found
        if (target != null) {
            Vector2 dir = target.getCenterPosition().cpy().sub(myCenter);
            if (!dir.isZero(0.0001f)) {
                dir.nor(); // Normalize direction

                // Note: AnimationRenderComponent doesn't support rotation
                // Could be changed to RotatingTextureRenderComponent if rotation is needed
                if (headRenderer != null) {
                    // headRenderer.setRotation(dir.angleDeg()); // Method not available
                }

                // --- Adjust projectile speed and life to match tower range ---
                float towerRange = stats.getRange();
                float baseProjectileSpeed = stats.getProjectileSpeed() != 0f ? stats.getProjectileSpeed() : 400f;

                // Set speed proportional to default base (optional) or just use base speed
                float speed = baseProjectileSpeed;

                // Set projectile life so it dies at max range: life = range / speed
                float life = towerRange / speed;

                String tex = stats.getProjectileTexture() != null ? stats.getProjectileTexture() : "images/bullet.png";
                int damage = (int) stats.getDamage();

                Entity bullet = ProjectileFactory.createBullet(tex, myCenter, dir.x * speed, dir.y * speed, life, damage);
                var es = ServiceLocator.getEntityService();
                if (es != null) {
                    Gdx.app.postRunnable(() -> es.register(bullet));
                }
            }

            stats.resetAttackTimer();
        }

        // --- SELL / SELECTION LOGIC ---
        java.util.List<Entity> entities = new java.util.ArrayList<>();
        for (Entity e : ServiceLocator.getEntityService().getEntitiesCopy())
        {
            entities.add(e);
        }

        if (isSelected())
        {
            if (!showSellButton)
            {
                showSellButton = true;
                System.out.println("[Tower] Sell button shown for " + type + " tower at " + entity.getPosition());
            }
            if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT))
            {
                sellTower(entities);
                showSellButton = false;
            }
        }
        else if (showSellButton)
        {
            showSellButton = false;
            System.out.println("[Tower] Sell button hidden for " + type + " tower at " + entity.getPosition());
        }

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT))
        {
            Vector3 worldClickPos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            com.badlogic.gdx.graphics.Camera camera = getCamera();
            if (camera != null)
            {
                camera.unproject(worldClickPos);
                Vector2 towerPos = entity.getPosition();
                float clickRadius = 1.0f;
                setSelected(Math.abs(worldClickPos.x - towerPos.x) < clickRadius &&
                        Math.abs(worldClickPos.y - towerPos.y) < clickRadius);
            }
        }
    }
}
