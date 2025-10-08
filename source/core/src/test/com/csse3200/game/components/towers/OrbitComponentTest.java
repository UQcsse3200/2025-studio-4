package com.csse3200.game.components.towers;

import com.csse3200.game.entities.Entity;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OrbitComponentTest {

    @Test
    void testConstructorAndGetRadius() {
        Entity target = new Entity();
        OrbitComponent orbit = new OrbitComponent(target, 2.5f, 1f);
        assertEquals(2.5f, orbit.getRadius());
    }

    @Test
    void testSetRadiusValid() {
        Entity target = new Entity();
        OrbitComponent orbit = new OrbitComponent(target, 1f, 1f);
        orbit.setRadius(3f);
        assertEquals(3f, orbit.getRadius());
    }

    @Test
    void testSetRadiusInvalid() {
        Entity target = new Entity();
        OrbitComponent orbit = new OrbitComponent(target, 1f, 1f);
        orbit.setRadius(-1f);
        assertEquals(1f, orbit.getRadius());
    }
}

