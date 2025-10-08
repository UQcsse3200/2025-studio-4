package com.csse3200.game.components.hero.engineer;

import com.badlogic.gdx.Gdx;
import com.csse3200.game.components.Component;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Automatically and safely removes the entity when its health drops to zero or below.
 * <p>
 * This component ensures that the entity is only removed once and that destruction
 * happens safely in the next frame (to avoid disposing during event callbacks or
 * within the physics update loop).
 * </p>
 * <p>
 * Before removal, it triggers a {@code "despawn"} event so that other systems
 * (such as {@link SummonOwnerComponent}) can react consistently — for example,
 * to decrement summon counts.
 * </p>
 */
public class AutoDespawnOnDeathComponent extends Component {
    /**
     * Flag to ensure the removal is only scheduled once.
     */
    private final AtomicBoolean scheduled = new AtomicBoolean(false);

    /**
     * Registers event listeners for various types of death signals.
     * <ul>
     *   <li>{@code "death"} — standard death event.</li>
     *   <li>{@code "entityDeath"} — alternate death event from other systems.</li>
     *   <li>{@code "setDead"} — boolean flag-based death trigger.</li>
     * </ul>
     */
    @Override
    public void create() {
        entity.getEvents().addListener("death", this::onDeath);
        entity.getEvents().addListener("entityDeath", this::onDeath);
        entity.getEvents().addListener("setDead", (Boolean dead) -> {
            if (dead) onDeath();
        });
    }

    /**
     * Called when the entity dies. Ensures:
     * <ul>
     *   <li>The "despawn" event is triggered for cleanup logic.</li>
     *   <li>The entity is safely disposed on the next frame.</li>
     *   <li>Only executes once per entity.</li>
     * </ul>
     */
    private void onDeath() {
        // Prevent duplicate scheduling
        if (!scheduled.compareAndSet(false, true)) return;

        // Trigger "despawn" event first (for centralized counting or cleanup)
        if (entity != null) {
            entity.getEvents().trigger("despawn");
        }

        // Schedule safe removal on the next frame (avoids removal inside physics step or callback)
        Gdx.app.postRunnable(() -> {
            if (entity != null) entity.dispose();
        });
    }
}



