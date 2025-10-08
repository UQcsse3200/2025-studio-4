package com.csse3200.game.components.hero.engineer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.maingame.SimplePlacementController;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;

/**
 * Engineer Summon Component (Implementation Plan A):
 * <p>
 * Handles summoning logic for the Engineer hero using LibGDX's {@link InputMultiplexer}
 * to directly intercept keyboard inputs.
 * This approach avoids conflicts with the project’s {@code InputService} or UI layers.
 * </p>
 *
 * <ul>
 *   <li>Listens to numeric keys (1–3) to summon different robots/turrets.</li>
 *   <li>Uses {@link SimplePlacementController} for placement handling.</li>
 *   <li>Manages cooldown and summon count limits.</li>
 * </ul>
 */
public class EngineerSummonComponent extends Component {
    /**
     * Cooldown duration (in seconds) between summons.
     */
    private final float cooldownSec;

    /**
     * Maximum number of active summons allowed simultaneously.
     */
    private final int maxSummons;

    /**
     * Path to the summon’s texture (used for preview/ghost rendering).
     */
    private final String summonTexture;

    /**
     * Initial summon speed or spawn offset, depending on implementation.
     */
    private final Vector2 summonSpeed;

    /**
     * Remaining cooldown time.
     */
    private float cd = 0f;

    /**
     * Current number of active summons.
     */
    private int alive = 0;

    /**
     * Input listener for Plan A.
     * <p>Note: This is a plain {@link InputAdapter}, not the project’s custom InputComponent.</p>
     */
    private InputAdapter qAdapter;

    /**
     * Constructs the summon component.
     *
     * @param cooldownSec   Cooldown time (seconds) between summoning actions.
     * @param maxSummons    Maximum number of simultaneous active summons.
     * @param summonTexture Default summon texture path.
     * @param summonSpeed   Summon’s initial speed vector.
     */
    public EngineerSummonComponent(float cooldownSec, int maxSummons,
                                   String summonTexture, Vector2 summonSpeed) {
        this.cooldownSec = cooldownSec;
        this.maxSummons = maxSummons;
        this.summonTexture = summonTexture;
        this.summonSpeed = summonSpeed;
    }

    @Override
    public void create() {
        super.create();

        // 1) Create an InputAdapter that intercepts numeric keys (1–3)
        qAdapter = new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                // Prevent summoning during cooldown or when at max capacity
                if (cd > 0f) return true;
                if (alive >= maxSummons) return true;

                // Find placement controller (responsible for placing summons on the map)
                var ctrl = findPlacementController();
                if (ctrl == null) return false;

                // Key bindings for different summon types
                if (keycode == Input.Keys.NUM_1) {
                    ctrl.armSummon(new SimplePlacementController.SummonSpec(
                            "images/engineer/Sentry.png", "melee"
                    ));
                    return true;
                } else if (keycode == Input.Keys.NUM_2) {
                    ctrl.armSummon(new SimplePlacementController.SummonSpec(
                            "images/engineer/Turret.png", "turret"
                    ));
                    return true;
                } else if (keycode == Input.Keys.NUM_3) {
                    ctrl.armSummon(new SimplePlacementController.SummonSpec(
                            "images/engineer/Currency_tower.png",  // Currency generator robot texture
                            "currencyBot"                          // Custom type identifier
                    ));
                    return true;
                }

                return false;
            }
        };

        // 2) Attach the InputAdapter to the global multiplexer at the front (index = 0)
        attachToMultiplexer(qAdapter);
    }

    @Override
    public void dispose() {
        super.dispose();
        // 3) Remove the adapter from the multiplexer on disposal to prevent memory leaks
        detachFromMultiplexer(qAdapter);
        qAdapter = null;
    }

    @Override
    public void update() {
        // Update cooldown timer
        float dt = ServiceLocator.getTimeSource().getDeltaTime();
        if (cd > 0f) cd -= dt;
    }

    // ===== Utility: Attach/Detach InputAdapter from the global InputMultiplexer =====

    /**
     * Attaches an InputAdapter to the global LibGDX InputMultiplexer.
     * The adapter is placed at index 0 to ensure it receives key events first.
     */
    private static void attachToMultiplexer(InputAdapter adapter) {
        var cur = Gdx.input.getInputProcessor();
        if (cur instanceof InputMultiplexer mux) {
            mux.addProcessor(0, adapter); // Highest priority
        } else {
            InputMultiplexer mux = new InputMultiplexer();
            mux.addProcessor(adapter);
            if (cur != null) mux.addProcessor(cur);
            Gdx.input.setInputProcessor(mux);
        }
    }

    /**
     * Detaches the InputAdapter from the global InputMultiplexer.
     * Ensures cleanup when the entity is destroyed or disabled.
     */
    private static void detachFromMultiplexer(InputAdapter adapter) {
        var cur = Gdx.input.getInputProcessor();
        if (cur instanceof InputMultiplexer mux && adapter != null) {
            mux.removeProcessor(adapter);
        }
    }

    // ===== Utility: Find the placement controller for summons =====

    /**
     * Searches all entities in the game for a {@link SimplePlacementController} instance.
     * Used to initiate the summon placement process.
     *
     * @return The first found {@link SimplePlacementController}, or {@code null} if none exist.
     */
    private SimplePlacementController findPlacementController() {
        var all = ServiceLocator.getEntityService().getEntitiesCopy();
        if (all == null) return null;
        for (var e : all) {
            if (e == null) continue;
            SimplePlacementController c = e.getComponent(SimplePlacementController.class);
            if (c != null) return c;
        }
        return null;
    }

    // ===== Optional: Callbacks for summon lifecycle =====

    /**
     * Called when a new summon is spawned.
     * Increments active summon count and triggers cooldown.
     */
    public void onSummonSpawned() {
        alive++;
        cd = cooldownSec;
    }

    /**
     * Called when a summon dies or is destroyed.
     * Decrements the active summon count.
     */
    public void onSummonDied() {
        if (alive > 0) alive--;
    }
}
