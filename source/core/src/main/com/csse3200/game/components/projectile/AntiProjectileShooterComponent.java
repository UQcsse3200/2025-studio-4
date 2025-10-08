package com.csse3200.game.components.projectile;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fires interceptor at nearest tower projectile. Interception/destruction
 * handled by InterceptOnHitComponent radius-overlap logic.
 */
public class AntiProjectileShooterComponent extends Component {
    private final float range;
    private final float cooldown;
    private final float speed;
    private final float lifetime;
    private final String spritePath;

    private float timer = 0f;
    private static final Logger logger = LoggerFactory.getLogger(AntiProjectileShooterComponent.class);

    public AntiProjectileShooterComponent(float range, float cooldown, float speed, float lifetime, String spritePath) {
        this.range = range;
        this.cooldown = cooldown;
        this.speed = speed;
        this.lifetime = lifetime;
        this.spritePath = spritePath;
    }

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
        timer = 0f;

        Entity target = findNearestTowerProjectile();
        if (target == null) return;
        fireInterceptorToward(target);
    }

    private Entity findNearestTowerProjectile() {
        EntityService es = ServiceLocator.getEntityService();
        if (es == null) return null;
        Array<Entity> all = es.getEntities();
        Vector2 myPos = entity.getPosition();
        float bestD2 = range * range;
        Entity best = null;
        for (int i = 0; i < all.size; i++) {
            Entity e = all.get(i);
            if (e.getComponent(ProjectileComponent.class) == null) continue; // must be projectile
            if (e.getComponent(InterceptorTagComponent.class) != null) continue; // skip own interceptors
            float d2 = e.getPosition().dst2(myPos);
            if (d2 <= bestD2) { bestD2 = d2; best = e; }
        }
        return best;
    }

    private void fireInterceptorToward(Entity target) {
        Vector2 from = entity.getPosition();
        Vector2 to = target.getPosition();
        Vector2 dir = to.cpy().sub(from);
        if (dir.isZero()) return;
        dir.nor();

        Entity interceptor = new Entity();
        interceptor.addComponent(new TextureRenderComponent(spritePath));
        interceptor.setScale(0.5f, 0.5f); // radius for collision overlap

        PhysicsComponent physics = new PhysicsComponent().setBodyType(BodyDef.BodyType.KinematicBody);
        interceptor.addComponent(physics);

        interceptor.addComponent(new ProjectileComponent(dir.x * speed, dir.y * speed, lifetime));
        interceptor.addComponent(new InterceptorTagComponent());
        interceptor.addComponent(new InterceptOnHitComponent());
        interceptor.setPosition(from);

        EntityService es = ServiceLocator.getEntityService();
        if (es != null) {
            es.register(interceptor);
        }
    }
}
