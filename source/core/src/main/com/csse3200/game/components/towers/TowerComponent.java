package com.csse3200.game.components.towers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.csse3200.game.components.currencysystem.CurrencyComponent.CurrencyType;
import com.csse3200.game.components.currencysystem.CurrencyManagerComponent;
import com.csse3200.game.components.projectile.ProjectileComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.ProjectileFactory;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.rendering.Renderer;
import com.csse3200.game.rendering.RotatingAnimationRenderComponent;
import com.csse3200.game.rendering.RotatingTextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.towers.OrbitComponent;
import com.csse3200.game.components.towers.BeamAttackComponent;
import com.csse3200.game.entities.EntityService;

import java.util.Map;

/**
 * Component representing a tower entity, including its type and size.
 * Handles tower attack logic, sell logic, and tower selection.
 */
public class TowerComponent extends Component {
    private final String type;
    private final int width;
    private final int height;
    private CurrencyType selectedPurchaseCurrency = CurrencyType.METAL_SCRAP;
    private boolean showSellButton = false;

    private Entity headEntity;
    private RotatingAnimationRenderComponent headRenderer;
    private final Vector2 headOffset = new Vector2(0f, 0f);
    private float zNudge = 0.01f;
    private boolean active = true;

    private boolean selected = false;

    /**
     * Constructs a single-tile tower.
     *
     * @param type The type of the tower.
     */
    public TowerComponent(String type)
    {
        this(type, 1, 1);
    }

    /**
     * Constructs a multi-tile tower.
     *
     * @param type The type of the tower.
     * @param width The width of the tower in tiles.
     * @param height The height of the tower in tiles.
     */
    public TowerComponent(String type, int width, int height)
    {
        this.type = type;
        this.width = width;
        this.height = height;
    }



    private int level = 1;
    /**
     * Current upgrade level of the tower. Minimum 1.
     * @return tower level
     */
    public int getLevel() {return level;}

    /**
     * Set the tower's level. Values less than 1 are clamped to 1.
     * @param lvl desired level
     */
    public void setLevel(int lvl) { this.level = Math.max(1, lvl); }

    /**
     * Attaches a head entity and renderer to this tower.
     *
     * @param head The head entity.
     * @param headRender The head's renderer.
     * @param offset The offset for the head's position.
     * @param zNudge The z-axis nudge for rendering.
     * @return This TowerComponent for chaining.
     */
    public TowerComponent withHead(Entity head, RotatingAnimationRenderComponent headRender, Vector2 offset, float zNudge)
    {
        this.headEntity = head;
        this.headRenderer = headRender;
        if (offset != null)
        {
            this.headOffset.set(offset);
        }
        if (zNudge > 0f)
        {
            this.zNudge = zNudge;
        }
        return this;
    }

    /**
     * Changes the texture of the tower's head at runtime.
     *
     * @param texturePath The new texture file path.
     */
    public void changeHeadTexture(String texturePath, int level) {
        if (headRenderer != null) {
            headRenderer.setNewTexture(texturePath, level);
            System.out.println("[Tower] Head texture changed for " + type + " tower to " + texturePath);
        }
    }

    /**
     * Gets the type of the tower.
     *
     * @return The tower type.
     */
    public String getType()
    {
        return type;
    }

    /**
     * Gets the width of the tower in tiles.
     *
     * @return The tower width.
     */
    public int getWidth()
    {
        return width;
    }

    /**
     * Gets the height of the tower in tiles.
     *
     * @return The tower height.
     */
    public int getHeight()
    {
        return height;
    }

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
        int cost = costComponent.getCostForCurrency(selectedPurchaseCurrency);
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
     * Updates the tower logic, including attacking and selection/sell logic.
     */
    @Override
    public void update() {
        if (!active) return;

        TowerStatsComponent stats = entity.getComponent(TowerStatsComponent.class);
        if (stats == null) return;

        // Use GameTime's delta time which respects time scale (paused = 0, double speed = 2x)
        GameTime gameTime = ServiceLocator.getTimeSource();
        float dt = gameTime != null ? gameTime.getDeltaTime() : 0f;

        stats.updateAttackTimer(dt);

        // Update head position only if it does NOT have an OrbitComponent
        if (headEntity != null) {
            if (headEntity.getComponent(OrbitComponent.class) == null) {
                headEntity.setPosition(
                        entity.getPosition().x + headOffset.x,
                        entity.getPosition().y + headOffset.y - zNudge
                );
            }
        }
        // 🔄 Auto-revert fire -> idle when a non-looping fire clip finishes
        if (headRenderer != null) {
            String curr = headRenderer.getCurrentAnimation();
            if (curr != null && curr.startsWith("fire") && headRenderer.isFinished()) {
                String idleName = "idle";
                headRenderer.startAnimation(idleName);
            }
        }

        // Disable update logic for totem towers
        if ("totem".equalsIgnoreCase(type)) return;

        // SuperCavemen behaviour: assign nearest enemy to head's BeamAttackComponent and rotate head
        if ("supercavemen".equalsIgnoreCase(type)) {
            // find nearest enemy within range
            Entity nearest = null;
            float minDist = Float.MAX_VALUE;
            Vector2 myCenter = entity.getCenterPosition();
            float range = stats.getRange();

            for (Entity other : ServiceLocator.getEntityService().getEntitiesCopy()) {
                if (other == entity || !isEnemyTarget(other)) continue;
                Vector2 otherCenter = other.getCenterPosition();
                if (otherCenter == null) continue;
                float d = otherCenter.dst(myCenter);
                if (d <= range && d < minDist) {
                    nearest = other;
                    minDist = d;
                }
            }

            // set/clear beam target on head's BeamAttackComponent
            if (headEntity != null) {
                var beam = headEntity.getComponent(BeamAttackComponent.class);
                if (beam != null) {
                    if (nearest != null) {
                        beam.setTarget(nearest);
                        // rotate head to face target
                        Vector2 spawnOrigin = headEntity.getCenterPosition() != null ? headEntity.getCenterPosition() : myCenter;
                        Vector2 dir = nearest.getCenterPosition().cpy().sub(spawnOrigin);
                        if (headRenderer != null) {
                            headRenderer.startAnimation("fire");
                        }
                        if (dir.len() <= 0.0001f) dir.set(1f, 0f);
                        else dir.nor();
                        if (headRenderer != null) headRenderer.setRotation(dir.angleDeg());
                    } else {
                        beam.clearTarget();
                    }
                }
            }

            // skip normal projectile logic for supercavemen
            return;
        }

        if (stats.canAttack()) {
            Vector2 myCenter = entity.getCenterPosition();

            // unified spawn origin: use head center when available, otherwise tower center
            Vector2 spawnOrigin = myCenter;
            if (headEntity != null) {
                Vector2 headCenter = headEntity.getCenterPosition();
                if (headCenter != null) spawnOrigin = headCenter;
            }

            // Normal tower targeting logic (used for all tower types now)
            Entity target = null;
            float range = stats.getRange();

            for (Entity other : ServiceLocator.getEntityService().getEntitiesCopy()) {
                if (other == entity || !isEnemyTarget(other)) continue;
                CombatStatsComponent targetStats = other.getComponent(CombatStatsComponent.class);
                if (targetStats == null) continue;

                Vector2 toOther = other.getCenterPosition().cpy().sub(myCenter);
                if (toOther.len() <= range) {
                    target = other;
                    break;
                }
            }

            if (target != null) {
                // Unified single-shot behaviour for all towers
                Vector2 dir = target.getCenterPosition().cpy().sub(spawnOrigin);
                if (dir.len() <= 0.0001f) {
                    dir.set(1f, 0f); // fallback direction to avoid NaN
                } else {
                    dir.nor();
                }
                if (headRenderer != null) headRenderer.setRotation(dir.angleDeg());

                float speed = stats.getProjectileSpeed() != 0f ? stats.getProjectileSpeed() : 400f;
                float life = "pterodactyl".equalsIgnoreCase(type) ? Float.POSITIVE_INFINITY : stats.getRange() / speed;
                String tex = stats.getProjectileTexture() != null ? stats.getProjectileTexture() : "images/bullet.png";
                int damage = (int) stats.getDamage();

                final String poolKey = "bullet:" + tex;
                EntityService es = ServiceLocator.getEntityService();

                if (es != null) {
                    // Make captured values final/effectively final for the lambda
                    final Vector2 originFinal = spawnOrigin;
                    final float vxFinal = dir.x * speed;
                    final float vyFinal = dir.y * speed;
                    final float lifeFinal = life;
                    final int dmgFinal = damage;
                    final String texFinal = tex;

                    Entity bullet = es.obtain(poolKey, () -> {
                        Entity b = ProjectileFactory.createBullet(
                                texFinal, originFinal, vxFinal, vyFinal, lifeFinal, dmgFinal
                        );
                        ProjectileComponent pc0 = b.getComponent(ProjectileComponent.class);
                        if (pc0 != null) pc0.setPoolKey(poolKey);
                        Gdx.app.postRunnable(() -> es.register(b));
                        return b;
                    });

                    // If coming from pool, reactivate/reset
                    ProjectileComponent pc = bullet.getComponent(ProjectileComponent.class);
                    if (pc != null && pc.isInactive()) {
                        // IMPORTANT: set position BEFORE activate so body starts at the spawn point
                        bullet.setPosition(originFinal);
                        pc.setPoolKey(poolKey).activate(vxFinal, vyFinal, lifeFinal);
                        // Optional: update damage if supported
                        CombatStatsComponent bStats = bullet.getComponent(CombatStatsComponent.class);
                        if (bStats != null) {
                            try { bStats.setBaseAttack(dmgFinal); } catch (Throwable ignored) {}
                        }
                    }

                    if (headRenderer != null) {
                        headRenderer.startAnimation("fire");
                    }
                } else {
                    // Fallback: original create/register path
                    Entity bullet = ProjectileFactory.createBullet(tex, spawnOrigin, dir.x * speed, dir.y * speed, life, damage);
                    var svc = ServiceLocator.getEntityService();
                    if (svc != null) Gdx.app.postRunnable(() -> svc.register(bullet));
                    if (headRenderer != null) headRenderer.startAnimation("fire");
                }
            }

            stats.resetAttackTimer();
        }

        // Handle tower selection by click
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            Vector3 worldClickPos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            Camera camera = getCamera();
            if (camera != null) {
                camera.unproject(worldClickPos);
                Vector2 towerPos = entity.getPosition();
                float clickRadius = 1.0f;
                setSelected(Math.abs(worldClickPos.x - towerPos.x) < clickRadius &&
                        Math.abs(worldClickPos.y - towerPos.y) < clickRadius);
            }
        }
    }

    // helper
    private static boolean isFinite(float v) {
        return !(Float.isNaN(v) || Float.isInfinite(v));
    }
}
