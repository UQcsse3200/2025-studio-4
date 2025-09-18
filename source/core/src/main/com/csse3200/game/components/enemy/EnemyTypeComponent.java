package com.csse3200.game.components.enemy;

import com.csse3200.game.components.Component;

/**
 * Simple marker component to tag an enemy with a concrete type string
 * so save/load can reconstruct the correct factory.
 */
public class EnemyTypeComponent extends Component {
    private final String type;

    public EnemyTypeComponent(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}


