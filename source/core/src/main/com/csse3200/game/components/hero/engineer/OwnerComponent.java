package com.csse3200.game.components.hero.engineer;

import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;

/** 储存召唤物的归属玩家引用 */
public class OwnerComponent extends Component {
    private final Entity owner;

    public OwnerComponent(Entity owner) {
        this.owner = owner;
    }

    public Entity getOwner() {
        return owner;
    }
}
