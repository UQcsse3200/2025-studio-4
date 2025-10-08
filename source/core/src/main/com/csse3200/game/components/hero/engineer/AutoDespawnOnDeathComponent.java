package com.csse3200.game.components.hero.engineer;

import com.badlogic.gdx.Gdx;
import com.csse3200.game.components.Component;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Used for summons/turrets: automatically and safely removes the entity once its HP reaches 0 (one-time only).
 */
public class AutoDespawnOnDeathComponent extends Component {
    private final AtomicBoolean scheduled = new AtomicBoolean(false);

    @Override
    public void create() {
        // Compatible with all three death-related events triggered in CombatStatsComponent
        entity.getEvents().addListener("death", this::onDeath);
        entity.getEvents().addListener("entityDeath", this::onDeath);
        entity.getEvents().addListener("setDead", (Boolean dead) -> {
            if (dead) onDeath();
        });
    }

    private void onDeath() {
        // One-time safety check: ensure it only runs once
        if (!scheduled.compareAndSet(false, true)) return;

        // Schedule safe removal in the next frame
        // (avoids disposing during callback or physics step)
        Gdx.app.postRunnable(() -> {
            if (entity != null) entity.dispose();
        });
    }
}


