package com.csse3200.game.components.projectile;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.RotatingAnimationRenderComponent;
import com.csse3200.game.services.ServiceLocator;

public class AntiProjectileShooterComponent extends Component {
    private final float range;
    private final float cooldown;
    private final float speed;
    private final float lifetime;
    private final String atlasPath;
    private final String animationName;
    private float timer = 0f;

    public AntiProjectileShooterComponent(float range, float cooldown, float speed,
                                          float lifetime, String atlasPath,
                                          String animationName) {
        this.range = range;
        this.cooldown = cooldown;
        this.speed = speed;
        this.lifetime = lifetime;
        this.atlasPath = atlasPath;
        this.animationName = animationName;
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

        Entity target = findNearestTowerProjectile();
        if (target == null) return;

        timer = 0f;
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
            ProjectileComponent pc = e.getComponent(ProjectileComponent.class);
            if (pc == null || pc.isInactive()) continue;
            if (e.getComponent(InterceptorTagComponent.class) != null) continue;

            Vector2 targetPos = e.getPosition();
            // Removed directional restriction - tanks can now shoot in any direction

            float d2 = targetPos.dst2(myPos);
            if (d2 <= bestD2) { bestD2 = d2; best = e; }
        }
        return best;
    }

    private void fireInterceptorToward(Entity target) {
        entity.getEvents().trigger("attackStart");

        Vector2 tankCenter = entity.getPosition();
        Vector2 to = target.getPosition();
        Vector2 dir = to.cpy().sub(tankCenter);
        if (dir.isZero()) return;
        dir.nor();

        float turretOffsetX = 1f;
        float turretOffsetY = 0.7f;
        Vector2 from = tankCenter.cpy().add(turretOffsetX, turretOffsetY);

        Entity interceptor = new Entity();

        TextureAtlas atlas = ServiceLocator.getResourceService().getAsset(atlasPath, TextureAtlas.class);
        RotatingAnimationRenderComponent arc = new RotatingAnimationRenderComponent(atlas);
        arc.addAnimation(animationName, 0.06f, com.badlogic.gdx.graphics.g2d.Animation.PlayMode.LOOP);
        arc.startAnimation(animationName);

        // Calculate the angle to rotate the fireball to face its direction of travel
        // The spritesheet has fireballs facing southeast (~-45 degrees or 315 degrees)
        float angleRad = (float) Math.atan2(dir.y, dir.x);
        float angleDeg = (float) Math.toDegrees(angleRad);

        // Set base rotation to 0 so our rotation is absolute, not relative to -90
        arc.setBaseRotation(0f);
        // Adjust for the southeast-facing sprite (southeast is -45 degrees, so we add 45 to compensate)
        arc.setRotation(angleDeg + 45f);

        interceptor.addComponent(arc);

        interceptor.setScale(0.8f, 0.8f);


        PhysicsComponent physics = new PhysicsComponent().setBodyType(BodyDef.BodyType.KinematicBody);
        interceptor.addComponent(physics);

        HitboxComponent hitbox = new HitboxComponent();
        hitbox.setLayer(PhysicsLayer.PROJECTILE);
        hitbox.setSensor(true);
        interceptor.addComponent(hitbox);

        String poolKey = "interceptor:" + atlasPath + "#" + animationName;
        ProjectileComponent projectileComp = new ProjectileComponent(dir.x * speed, dir.y * speed, lifetime);
        projectileComp.setPoolKey(poolKey);
        interceptor.addComponent(projectileComp);

        interceptor.addComponent(new InterceptorTagComponent());
        interceptor.addComponent(new InterceptOnHitComponent());

        interceptor.setPosition(from);

        EntityService es = ServiceLocator.getEntityService();
        if (es != null) {
            es.register(interceptor);
        }
    }
}
