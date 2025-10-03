package core.src.main.com.csse3200.game.components;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.components.Component;

/**
 * Component to make an entity orbit around a target entity.
 */
public class OrbitComponent extends Component {
    private final Entity target;
    private final float radius;
    private final float speed;
    private float angle;

    public OrbitComponent(Entity target, float radius, float speed) {
        this.target = target;
        this.radius = radius;
        this.speed = speed;
        this.angle = 0f;
    }

    @Override
    public void update() {
        if (entity == null || target == null) return;

        angle += speed * 0.016f; // assuming ~60fps
        Vector2 targetPos = target.getPosition();
        float x = targetPos.x + radius * (float) Math.cos(angle);
        float y = targetPos.y + radius * (float) Math.sin(angle);
        entity.setPosition(x, y);
    }
}