package com.csse3200.game.components.projectile;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;

/**
 * Periodically searches for the nearest active tower projectile within range and fires
 * an interceptor toward it. Interception and destruction are handled by
 * InterceptOnHitComponent on the interceptor via radius overlap checks.
 * <p>
 * Uses EntityService pooling with key "interceptor:&lt;spritePath&gt;" and
 * ProjectileComponent.activate()/deactivate() to reuse entities efficiently.
 */
public class
AntiProjectileShooterComponent extends Component {
    private final float range;
    private final float cooldown;
    private final float speed;
    private final float lifetime;
    private final String spritePath;

    private float timer = 0f;

    /**
     * Create a shooter that launches interceptors on a cooldown toward hostile projectiles.
     * @param range      maximum search radius in world units
     * @param cooldown   seconds between interceptor launches
     * @param speed      interceptor linear speed in world units per second
     * @param lifetime   interceptor lifetime in seconds before auto-despawn
     * @param spritePath texture path used for the interceptor
     */
    public AntiProjectileShooterComponent(float range, float cooldown, float speed, float lifetime, String spritePath) {
        this.range = range;
        this.cooldown = cooldown;
        this.speed = speed;
        this.lifetime = lifetime;
        this.spritePath = spritePath;
    }

    /**
     * Advance the internal timer and, when off cooldown, acquire a target and fire
     * an interceptor. No-ops if required services are unavailable.
     */
    @Override
    public void update() {
        var time = ServiceLocator.getTimeSource();
        if (time == null) return;
        var physicsSvc = ServiceLocator.getPhysicsService();
        if (physicsSvc == null) return;
        var entitySvc = ServiceLocator.getEntityService();
        if (entitySvc == null) return;

        timer += time.getDeltaTime();
        if (timer < cooldown) return;

        // Only reset timer when we actually find a target and fire
        Entity target = findNearestTowerProjectile();
        if (target == null) return;  // Keep timer running if no target found

        // Reset timer only when firing
        timer = 0f;
        fireInterceptorToward(target);
    }

    /**
     * Scan registered entities for the closest valid tower projectile in range.
     * Interceptor-tagged entities and inactive projectiles are ignored.
     * @return nearest target or null if none found within range
     */
    private Entity findNearestTowerProjectile() {
        EntityService es = ServiceLocator.getEntityService();
        if (es == null) return null;
        Array<Entity> all = es.getEntities();
        Vector2 myPos = entity.getPosition();
        float bestD2 = range * range;
        Entity best = null;
        for (int i = 0; i < all.size; i++) {
            Entity e = all.get(i);
            ProjectileComponent pc = e.getComponent(ProjectileComponent.class);
            if (pc == null || pc.isInactive()) continue; // only live projectiles
            if (e.getComponent(InterceptorTagComponent.class) != null) continue; // skip own interceptors
            float d2 = e.getPosition().dst2(myPos);
            if (d2 <= bestD2) { bestD2 = d2; best = e; }
        }
        return best;
    }

    /**
     * Fire interceptor toward target. Creates a properly configured interceptor with
     * Box2D physics components and pooling support for memory safety.
     * @param target projectile entity to intercept
     */
    private void fireInterceptorToward(Entity target) {
        // Trigger attack animation on the tank
        entity.getEvents().trigger("attackStart");

        Vector2 tankCenter = entity.getPosition();
        Vector2 to = target.getPosition();
        Vector2 dir = to.cpy().sub(tankCenter);
        if (dir.isZero()) return;
        dir.nor();

        // Calculate turret offset (turret barrel is on the right and upper portion of the tank)
        float turretOffsetX = 1f;
        float turretOffsetY = 0.7f;

        Vector2 turretOffset = new Vector2(turretOffsetX, turretOffsetY);
        Vector2 from = tankCenter.cpy().add(turretOffset);

        // Create interceptor with proper Box2D components for safe disposal
        Entity interceptor = new Entity();
        interceptor.addComponent(new TextureRenderComponent(spritePath));
        interceptor.setScale(0.5f, 0.5f); // radius for collision overlap

        // Add physics component with proper body type
        PhysicsComponent physics = new PhysicsComponent().setBodyType(BodyDef.BodyType.KinematicBody);
        interceptor.addComponent(physics);

        // Add hitbox component for proper collision detection
        HitboxComponent hitbox = new HitboxComponent();
        hitbox.setLayer(PhysicsLayer.PROJECTILE);
        hitbox.setSensor(true);
        interceptor.addComponent(hitbox);

        // Add ProjectileComponent with pooling support
        String poolKey = "interceptor:" + spritePath;
        ProjectileComponent projectileComp = new ProjectileComponent(dir.x * speed, dir.y * speed, lifetime);
        projectileComp.setPoolKey(poolKey);
        interceptor.addComponent(projectileComp);

        // Add interceptor-specific components
        interceptor.addComponent(new InterceptorTagComponent());
        interceptor.addComponent(new InterceptOnHitComponent());

        // Set position and register
        interceptor.setPosition(from);

        EntityService es = ServiceLocator.getEntityService();
        if (es != null) {
            es.register(interceptor);
        }
    }
}
