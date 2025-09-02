package com.csse3200.game.entities.factories;

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsUtils;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;

/**
 * Factory to create obstacle entities.
 *
 * <p>Each obstacle entity type should have a creation method that returns a corresponding entity.
 */
public class ObstacleFactory {

    /**
     * Creates a tree entity (占 1 个 tile).
     * @return entity
     */
    public static Entity createTree() {
        Entity tree = new Entity()
                .addComponent(new TextureRenderComponent("images/tree.png"))
                .addComponent(new PhysicsComponent().setBodyType(BodyType.StaticBody))
                .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));

        tree.getComponent(TextureRenderComponent.class).scaleEntity();
        PhysicsUtils.setScaledCollider(tree, 1f, 1f);
        return tree;
    }

    // public static Entity createRock() {
    //     Entity rock = new Entity()
    //             .addComponent(new TextureRenderComponent("images/rock.png"))
    //             .addComponent(new PhysicsComponent().setBodyType(BodyType.StaticBody))
    //             .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));

    //     rock.getComponent(TextureRenderComponent.class).scaleEntity();
    //     PhysicsUtils.setScaledCollider(rock, 1f, 1f);
    //     return rock;
    // }

    public static Entity createRiver() {
        Entity river = new Entity()
                .addComponent(new TextureRenderComponent("images/river.png"))
                .addComponent(new PhysicsComponent().setBodyType(BodyType.StaticBody))
                .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));

        river.getComponent(TextureRenderComponent.class).scaleEntity();
        PhysicsUtils.setScaledCollider(river, 1f, 1f);
        return river;
    }

    /**
     * Creates a crystal entity (占 1 个 tile).
     * @return entity
     */
    public static Entity createCrystal() {
        Entity crystal = new Entity()
                .addComponent(new TextureRenderComponent("images/crystal.png"))
                .addComponent(new PhysicsComponent().setBodyType(BodyType.StaticBody))
                .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));

        crystal.getComponent(TextureRenderComponent.class).scaleEntity();
        PhysicsUtils.setScaledCollider(crystal, 1f, 1f);
        return crystal;
    }

    /**
     * Creates an invisible physics wall.
     * @param width Wall width in world units
     * @param height Wall height in world units
     * @return Wall entity of given width and height
     */
    public static Entity createWall(float width, float height) {
        Entity wall = new Entity()
                .addComponent(new PhysicsComponent().setBodyType(BodyType.StaticBody))
                .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));

        wall.setScale(width, height);
        return wall;
    }

    private ObstacleFactory() {
        throw new IllegalStateException("Instantiating static util class");
    }
}
