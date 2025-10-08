package com.csse3200.game.components.hero.engineer;

import com.csse3200.game.components.Component;

/**
 * Component attached to each summon entity.
 * <p>
 * Its purpose is to notify the owning {@link EngineerSummonComponent} when a summon
 * is successfully spawned or when it dies / is removed.
 * </p>
 * <ul>
 *   <li>On creation → triggers a {@code "summon:spawned"} event once.</li>
 *   <li>When the summon dies or is despawned → triggers a {@code "summon:died"} event.</li>
 * </ul>
 */
public class SummonOwnerComponent extends Component {
    /**
     * Reference to the owning EngineerSummonComponent (the engineer hero).
     */
    private final EngineerSummonComponent owner;

    /**
     * The summon type (e.g., "turret", "wall", "drone").
     */
    private final String type;

    /**
     * Ensures the summon is only counted once for its lifetime.
     */
    private boolean counted = false;

    /**
     * Creates a SummonOwnerComponent.
     *
     * @param owner The engineer's summon controller that owns this summon.
     * @param type  The type of summon being created.
     */
    public SummonOwnerComponent(EngineerSummonComponent owner, String type) {
        this.owner = owner;
        this.type = type;
    }

    /**
     * Called when the summon entity is created in the game world.
     * <p>
     * - Triggers the "summon:spawned" event (only once).
     * - Subscribes to multiple possible "death" or "despawn" events
     * so that it can notify the owner when the summon is gone.
     * </p>
     */
    @Override
    public void create() {
        // Notify that the summon has successfully spawned (only once)
        if (!counted) {
            owner.getEntity().getEvents().trigger("summon:spawned", entity, type);
            counted = true;
        }

        // Listen for various possible death or despawn events
        entity.getEvents().addListener("death", this::onGone);
        entity.getEvents().addListener("entityDeath", this::onGone);
        entity.getEvents().addListener("setDead", (Boolean d) -> {
            if (d) onGone();
        });
        entity.getEvents().addListener("despawn", this::onGone); // For manual removal
    }

    /**
     * Called when the summon is removed, dies, or despawns.
     * <p>
     * - Triggers a "summon:died" event for the owning engineer.
     * - Ensures this is only called once.
     * </p>
     */
    private void onGone() {
        if (counted) {
            owner.getEntity().getEvents().trigger("summon:died", entity, type);
            counted = false;
        }
    }
}

