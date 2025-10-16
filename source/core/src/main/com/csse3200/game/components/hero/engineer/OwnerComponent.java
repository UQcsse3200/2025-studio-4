package com.csse3200.game.components.hero.engineer;

import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;

/**
 * A simple component that stores a reference to the entity's owner (e.g., the player).
 * <p>
 * This is typically used by summoned entities, such as turrets or drones,
 * to link them back to the player who created them.
 * </p>
 */
public class OwnerComponent extends Component {
    /**
     * Reference to the owning entity (usually the player).
     */
    private final Entity owner;

    /**
     * Creates an OwnerComponent that links an entity to its owner.
     *
     * @param owner The entity that owns this one.
     */
    public OwnerComponent(Entity owner) {
        this.owner = owner;
    }

    /**
     * Returns the owner entity associated with this component.
     *
     * @return The owner entity.
     */
    public Entity getOwner() {
        return owner;
    }
}

