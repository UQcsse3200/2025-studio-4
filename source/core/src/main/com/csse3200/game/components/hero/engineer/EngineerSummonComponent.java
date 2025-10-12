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

import java.util.HashMap;
import java.util.Map;

/**
 * EngineerSummonComponent (Plan A: Event Interception)
 * <p>
 * Handles all logic related to the Engineer's summon system, including:
 * </p>
 * <ul>
 *   <li>Key triggers (1/2/3) to arm different summon types.</li>
 *   <li>Checks before placement via the event {@code "summon:canSpawn?"}.</li>
 *   <li>Tracks summon creation and removal via {@code "summon:spawned"} and {@code "summon:died"} events.</li>
 *   <li>Notifies HUD with {@code summonAliveChanged(alive, max)} for real-time display.</li>
 * </ul>
 */
public class EngineerSummonComponent extends Component {
    /**
     * Summon cooldown (in seconds).
     */
    private final float cooldownSec;

    /**
     * Maximum total number of summons allowed at once.
     */
    private int maxSummons;
    private int extraPerLevel = 1;

    /**
     * Default summon texture (optional).
     */
    private final String summonTexture;

    /**
     * Default summon speed (optional).
     */
    private final Vector2 summonSpeed;

    /**
     * Cooldown timer.
     */
    private float cd = 0f;

    /**
     * Number of currently active summons.
     */
    private int alive = 0;

    /**
     * Optional per-type summon caps (e.g., limit turrets separately).
     */
    private final Map<String, Integer> typeCaps = new HashMap<>();

    /**
     * Number of active summons for each type.
     */
    private final Map<String, Integer> aliveByType = new HashMap<>();

    /**
     * Keyboard adapter for hotkey input.
     */
    private InputAdapter qAdapter;

    private float lastEmitCd = -999f;

    /**
     * Constructor.
     *
     * @param cooldownSec   cooldown time between summons.
     * @param maxSummons    total maximum number of summons allowed.
     * @param summonTexture default summon texture (optional).
     * @param summonSpeed   default summon movement speed (optional).
     */
    public EngineerSummonComponent(float cooldownSec, int maxSummons,
                                   String summonTexture, Vector2 summonSpeed) {
        this.cooldownSec = cooldownSec;
        this.maxSummons = maxSummons;
        this.summonTexture = summonTexture;
        this.summonSpeed = summonSpeed;
    }

    /**
     * Initializes the component and registers event listeners for summon management.
     */
    @Override
    public void create() {
        super.create();

        // (1) Before placement: PlacementController asks "summon:canSpawn?"
        entity.getEvents().addListener("summon:canSpawn?", (String type, boolean[] allow) -> {
            if (!canPlace(type)) allow[0] = false;
        });

        // (2) When a summon is successfully created → +1 count
        entity.getEvents().addListener("summon:spawned", (Entity e, String type) -> onSummonSpawned(type));

        // (3) When a summon dies or despawns → -1 count
        entity.getEvents().addListener("summon:died", (Entity e, String type) -> onSummonDied(type));
        entity.getEvents().addListener("upgraded", (Integer newLevel,
                                                    com.csse3200.game.components.currencysystem.CurrencyComponent.CurrencyType costType,
                                                    Integer cost) -> {
            addMaxSummons(extraPerLevel);   // 默认每级 +1
        });

        // Attach keyboard input adapter for hotkeys (Plan A)
        attachToMultiplexer(qAdapter = makeKeyAdapter());
        emitCooldownEvent();
        entity.getEvents().trigger("summonAliveChanged", alive, maxSummons);
    }

    /**
     * Cleans up input listeners when the component is removed.
     */
    @Override
    public void dispose() {
        super.dispose();
        detachFromMultiplexer(qAdapter);
        qAdapter = null;
    }
    private void emitCooldownEvent() {
        float remaining = Math.max(cd, 0f);
        entity.getEvents().trigger("summon:cooldown", remaining, cooldownSec);
    }

    /**
     * Updates the cooldown timer every frame.
     */
    @Override
    public void update() {
        float dt = ServiceLocator.getTimeSource().getDeltaTime();
        if (cd > 0f) {
            float prev = cd;
            cd = Math.max(0f, cd - dt);

            // 以0.05s为步长节流广播，或从>0变为0时强制广播
            float quantPrev = (float)Math.floor(prev * 20f) / 20f;
            float quantNow  = (float)Math.floor(cd   * 20f) / 20f;
            if (quantNow != lastEmitCd || (prev > 0f && cd == 0f)) {
                lastEmitCd = quantNow;
                emitCooldownEvent();
            }
        }
    }

    public void addMaxSummons(int delta) {
        if (delta <= 0) return;
        maxSummons += delta;
        entity.getEvents().trigger("summonAliveChanged", alive, maxSummons);
        Gdx.app.log("EngineerSummon", "maxSummons increased to " + maxSummons);
    }

    // =================== Keyboard Controls ===================

    /**
     * Creates a key adapter that listens for number key presses to arm summon placement.
     * Keys:
     * <ul>
     *   <li>1 → melee summon</li>
     *   <li>2 → turret summon</li>
     *   <li>3 → currency bot summon</li>
     * </ul>
     */
    private InputAdapter makeKeyAdapter() {
        return new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (cd > 0f) return true; // Ignore key if on cooldown
                var summonCtrl = findSummonPlacementComponent();
                if (summonCtrl == null) return false;

                String type = null;
                String tex = null;

                if (keycode == Input.Keys.NUM_1) {
                    type = "melee";
                    tex = "images/engineer/Sentry.png";
                } else if (keycode == Input.Keys.NUM_2) {
                    type = "turret";
                    tex = "images/engineer/Turret.png";
                } else if (keycode == Input.Keys.NUM_3) {
                    type = "currencyBot";
                    tex = "images/engineer/Currency_tower.png";
                } else {
                    return false;
                }

                // Switch to placement mode; actual spawn validation happens later via events
                summonCtrl.armSummon(
                        new com.csse3200.game.components.hero.engineer.SummonPlacementComponent.SummonSpec(tex, type)
                );
                return true;
            }
        };
    }

    /**
     * Finds the SummonPlacementComponent in the current entity service.
     * 确保 SummonPlacementComponent 挂在了你创建的 ui 实体上。
     */
    private com.csse3200.game.components.hero.engineer.SummonPlacementComponent findSummonPlacementComponent() {
        var all = ServiceLocator.getEntityService().getEntitiesCopy();
        if (all == null) return null;
        for (var e : all) {
            if (e == null) continue;
            var c = e.getComponent(com.csse3200.game.components.hero.engineer.SummonPlacementComponent.class);
            if (c != null) return c;
        }
        return null;
    }

    // =================== Summon Limit Logic ===================

    /**
     * Determines whether a summon of the given type can be placed.
     *
     * @param type summon type (e.g., "turret", "melee").
     * @return true if allowed to place; false if global or per-type limits reached.
     */
    private boolean canPlace(String type) {
        if (alive >= maxSummons) return false;
        Integer cap = typeCaps.get(type);
        if (cap != null) {
            int cur = aliveByType.getOrDefault(type, 0);
            if (cur >= cap) return false;
        }
        return true;
    }

    // =================== Summon Tracking and HUD Updates ===================

    /**
     * Called when a summon is created successfully.
     *
     * @param type type of the summon.
     */
    public void onSummonSpawned(String type) {
        alive++;
        aliveByType.put(type, aliveByType.getOrDefault(type, 0) + 1);
        cd = cooldownSec;
        entity.getEvents().trigger("summonAliveChanged", alive, maxSummons);
        emitCooldownEvent();
    }

    /**
     * Called when a summon is destroyed or removed.
     *
     * @param type type of the summon.
     */
    public void onSummonDied(String type) {
        if (alive > 0) alive--;
        if (type != null) {
            int cur = aliveByType.getOrDefault(type, 0);
            aliveByType.put(type, Math.max(0, cur - 1));
        }
        entity.getEvents().trigger("summonAliveChanged", alive, maxSummons);
    }

    // =================== Input Multiplexer Helpers ===================

    /**
     * Attaches an input adapter to the global input multiplexer.
     *
     * @param adapter the input adapter to attach.
     */
    private static void attachToMultiplexer(InputAdapter adapter) {
        var cur = Gdx.input.getInputProcessor();
        if (cur instanceof InputMultiplexer mux) {
            mux.addProcessor(0, adapter);
        } else {
            InputMultiplexer mux = new InputMultiplexer();
            mux.addProcessor(adapter);
            if (cur != null) mux.addProcessor(cur);
            Gdx.input.setInputProcessor(mux);
        }
    }

    /**
     * Removes an input adapter from the global input multiplexer.
     *
     * @param adapter the input adapter to remove.
     */
    private static void detachFromMultiplexer(InputAdapter adapter) {
        var cur = Gdx.input.getInputProcessor();
        if (cur instanceof InputMultiplexer mux && adapter != null) {
            mux.removeProcessor(adapter);
        }
    }

    // =================== Utility: Placement Controller Finder ===================

    /**
     * Searches for a {@link SimplePlacementController} instance in the current entity service.
     *
     * @return the placement controller if found, or null otherwise.
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
}

