package com.csse3200.game.entities.factories;

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsUtils;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.badlogic.gdx.physics.box2d.BodyDef;

/**
 * Factory to create obstacle entities.
 *
 * <p>Each obstacle entity type should have a creation method that returns a corresponding entity.
 */
public class ObstacleFactory {

    /**
     * Creates a tree entity.
     * @return entity
     */
    public static Entity createTree() {
        Entity tree =
                new Entity()
                        .addComponent(new TextureRenderComponent("images/tree.png"))
                        .addComponent(new PhysicsComponent())
                        .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));

        tree.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
        tree.getComponent(TextureRenderComponent.class).scaleEntity();
        tree.scaleHeight(2.5f);
        PhysicsUtils.setScaledCollider(tree, 0.5f, 0.2f);
        return tree;
    }
    public static Entity createRiver() {
        Entity river = new Entity()
                .addComponent(new TextureRenderComponent("images/river.png"))
                .addComponent(new PhysicsComponent().setBodyType(BodyDef.BodyType.StaticBody))
                .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));
        river.getComponent(TextureRenderComponent.class).scaleEntity();
        return river;
    }


    public static Entity createRock() {
        Entity rock = new Entity()
                .addComponent(new TextureRenderComponent("images/rock.png")) // 渲染
                .addComponent(new PhysicsComponent().setBodyType(BodyDef.BodyType.StaticBody)) // 固定物体
                .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));        // 碰撞层（障碍物）

        rock.getComponent(TextureRenderComponent.class).scaleEntity(); // 自动缩放
        rock.scaleHeight(2.0f);  // 可选：调大小
        return rock;
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
