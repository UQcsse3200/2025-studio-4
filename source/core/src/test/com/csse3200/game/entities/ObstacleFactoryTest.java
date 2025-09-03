package com.csse3200.game.entities.factories;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.PhysicsTestUtils;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(GameExtension.class)
class ObstacleFactoryTest {

    @BeforeEach
    void setup() {
        ServiceLocator.registerEntityService(new EntityService());
        ServiceLocator.registerPhysicsService(new PhysicsService());
    }



    @Test
    void createWall_setsScaleAndHasPhysicsAndCollider() {
        float w = 3.2f, h = 1.6f;
        Entity e = ObstacleFactory.createWall(w, h);

        assertNotNull(e.getComponent(PhysicsComponent.class));
        assertNotNull(e.getComponent(ColliderComponent.class));

        assertEquals(new Vector2(w, h), e.getScale());
    }

    @Test
    void constructor_isPrivateAndThrows() throws Exception {
        Constructor<?> ctor = ObstacleFactory.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        InvocationTargetException ex =
                assertThrows(InvocationTargetException.class, ctor::newInstance);
        assertTrue(ex.getCause() instanceof IllegalStateException);
    }
}


