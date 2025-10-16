package com.csse3200.game.components.hero.engineer;

import com.csse3200.game.entities.Entity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class OwnerComponentTest {

    @Test
    void returnsSameOwnerReference() {
        // Arrange
        Entity owner = mock(Entity.class);

        // Act
        OwnerComponent comp = new OwnerComponent(owner);

        // Assert
        assertSame(owner, comp.getOwner(),
                "getOwner() 应返回与构造时传入的同一引用");
    }

    @Test
    void allowsNullOwner() {
        // Arrange & Act
        OwnerComponent comp = new OwnerComponent(null);

        // Assert
        assertNull(comp.getOwner(), "当传入 null 时，getOwner() 应返回 null");
    }
}
