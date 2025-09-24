package com.csse3200.game.components.hero;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.PhysicsComponent;

/**
 * Keeps the sword (child entity) orbiting around the owner (samurai) by updating the Box2D
 * body's transform each frame. The sword is aligned RADIAL: handle lies on the radius line.
 *
 * Responsibilities:
 * - Compute orbit position/orientation.
 * - Call body.setTransform(centerX, centerY, angleRad).
 *
 * Not responsible for:
 * - Dealing damage (use your collisionStart damage component).
 * - Rendering (provide your own render component).
 */
public class SwordOrbitPhysicsComponent extends Component {
    private final Entity owner;     // the samurai entity
    private float radius;           // radius where the HANDLE sits (world units)
    private float angularSpeedDeg;  // degrees per second, CCW positive

    // Fine-tuning:
    /** Sprite default forward direction: right=0, up=90, left=180, down=270. */
    private float spriteForwardOffsetDeg = 0f;
    /** Distance from sprite center to handle along forward dir (world units). Usually negative. */
    private float centerToHandle = -0.25f;
    /** Extra offset to shift the orbit pivot to match the visual center of the owner (world units). */
    private final Vector2 pivotOffset = new Vector2(); // (0,0) by default

    private PhysicsComponent physics; // sword's PhysicsComponent (Box2D body)
    private float angleDeg = 0f;      // current angle along the orbit
    private final Vector2 ownerCenter = new Vector2();

    public SwordOrbitPhysicsComponent(Entity owner, float radius, float angularSpeedDeg) {
        this.owner = owner;
        this.radius = radius;
        this.angularSpeedDeg = angularSpeedDeg;
    }

    /** Runtime change angular speed (deg/s). */
    public SwordOrbitPhysicsComponent setAngularSpeedDeg(float deg) {
        this.angularSpeedDeg = deg;
        return this;
    }

    /** Runtime change radius. */
    public SwordOrbitPhysicsComponent setRadius(float r) {
        this.radius = r;
        return this;
    }

    /** Adjust alignment if your sprite faces up or handle offset differs. */
    public SwordOrbitPhysicsComponent setSpriteForwardOffsetDeg(float deg) {
        this.spriteForwardOffsetDeg = deg;
        return this;
    }

    public SwordOrbitPhysicsComponent setCenterToHandle(float d) {
        this.centerToHandle = d;
        return this;
    }

    /** Shift the orbit pivot so the rotation visually happens around the hero image center. */
    public SwordOrbitPhysicsComponent setPivotOffset(float ox, float oy) {
        this.pivotOffset.set(ox, oy);
        return this;
    }

    @Override
    public void create() {
        physics = entity.getComponent(PhysicsComponent.class);
        if (physics == null) {
            throw new IllegalStateException(
                    "SwordOrbitPhysicsComponent requires PhysicsComponent on the sword entity.");
        }
        // Try to auto-calibrate pivot offset from owner's render component origin (best effort)
        autoCalibratePivotFromOwnerOrigin();
    }

    @Override
    public void update() {
        if (owner == null || physics == null || physics.getBody() == null) return;

        float dt = (Gdx.graphics != null) ? Gdx.graphics.getDeltaTime() : 1f / 60f;
        angleDeg = (angleDeg + angularSpeedDeg * dt) % 360f;

        // 1) Base center of owner
        getEntityCenter(owner, ownerCenter);
        // 1.1) Apply extra pivot offset to match visual center
        ownerCenter.add(pivotOffset);

        // 2) radial unit vector (windmill: blade points outwards)
        float rad = (float) Math.toRadians(angleDeg);
        float dx = (float) Math.cos(rad);
        float dy = (float) Math.sin(rad);

        // 3) physics/render angle = radial + sprite forward offset
        float swordAngleDeg = angleDeg + spriteForwardOffsetDeg;
        float swordAngleRad = (float) Math.toRadians(swordAngleDeg);

        // 4) place HANDLE on the circle; entity center = handle - centerToHandle * dir
        float handleX = ownerCenter.x + radius * dx;
        float handleY = ownerCenter.y + radius * dy;
        float centerX = handleX - centerToHandle * dx;
        float centerY = handleY - centerToHandle * dy;

        // 5) sync body transform
        physics.getBody().setTransform(centerX, centerY, swordAngleRad);
        // physics.getBody().setLinearVelocity(0, 0); // optional

        // 6) sync render angle (if your render doesn't auto-follow body angle)
        var rot = entity.getComponent(com.csse3200.game.rendering.RotatingTextureRenderComponent.class);
        if (rot != null) {
            rot.setRotation(swordAngleDeg);
        }
    }

    /* ===== Helpers ===== */

    private static void getEntityCenter(Entity e, Vector2 out) {
        try {
            Vector2 c = e.getCenterPosition();
            if (c != null) { out.set(c); return; }
        } catch (Throwable ignored) { }
        Vector2 pos = e.getPosition();
        Vector2 scale = e.getScale();
        out.set(
                pos.x + (scale != null ? scale.x * 0.5f : 0.5f),
                pos.y + (scale != null ? scale.y * 0.5f : 0.5f)
        );
    }

    /**
     * Best-effort: try to read owner's render origin and width/height (in world units) to
     * compute how far the "visual center" is from the default center we use.
     * If successful, sets pivotOffset accordingly; otherwise keeps current offset.
     */
    private void autoCalibratePivotFromOwnerOrigin() {
        try {
            Object render = owner.getComponent(com.csse3200.game.rendering.RotatingTextureRenderComponent.class);
            if (render == null) return;

            float originX = invokeFloat(render, "getOriginX", Float.NaN);
            float originY = invokeFloat(render, "getOriginY", Float.NaN);
            float width   = invokeFloat(render, "getWidth",   Float.NaN);
            float height  = invokeFloat(render, "getHeight",  Float.NaN);

            if (Float.isNaN(originX) || Float.isNaN(originY) || Float.isNaN(width) || Float.isNaN(height)) {
                // Try alternative API names if your component differs
                originX = invokeFloat(render, "originX", Float.NaN);
                originY = invokeFloat(render, "originY", Float.NaN);
                width   = invokeFloat(render, "getTextureWidth", Float.NaN);
                height  = invokeFloat(render, "getTextureHeight", Float.NaN);
            }

            if (Float.isNaN(originX) || Float.isNaN(originY) || Float.isNaN(width) || Float.isNaN(height)) {
                return; // can't auto-calibrate
            }

            // Default "center" we used is width/2, height/2.
            // If origin != (w/2,h/2), we compute how much to shift pivot in world units.
            float dx = originX - width  * 0.5f;
            float dy = originY - height * 0.5f;

            // Some renderers already express width/height in world units; if not, you may need to
            // multiply by pixels-to-world scale. Assuming they are world units here.
            pivotOffset.add(dx, dy);
        } catch (Throwable ignored) {
            // Silent best-effort; you can log if needed
        }
    }

    private static float invokeFloat(Object obj, String method, float fallback) {
        try {
            var m = obj.getClass().getMethod(method);
            Object r = m.invoke(obj);
            return (r instanceof Number) ? ((Number) r).floatValue() : fallback;
        } catch (Throwable e) {
            return fallback;
        }
    }
}
