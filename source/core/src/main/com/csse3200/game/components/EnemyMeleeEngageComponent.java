package com.csse3200.game.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.DamageTypeConfig;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Enemy-side melee engagement component.
 * <p>
 * When the enemy's hitbox overlaps with a target (filtered by {@code targetMask}),
 * this component periodically applies damage ("ticks") to the engaged target(s).
 * Optionally, it can pause the AI while at least one target is engaged, and resume
 * when all targets disengage.
 *
 * <h3>Key behaviors</h3>
 * <ul>
 *   <li>On {@code collisionStart}: if the other fixture's category matches {@code targetMask},
 *       the other entity is added to the engaged set.</li>
 *   <li>On {@code collisionEnd}: the entity is removed from the engaged set.</li>
 *   <li>On each {@code update}: once the per-target timer reaches {@code tickSeconds},
 *       it deals damage (using this entity's {@link CombatStatsComponent} if present,
 *       otherwise using {@code damagePerTickFallback}).</li>
 * </ul>
 *
 * <p><b>Note:</b> The field {@code pushForce} is currently not applied; if you need
 * a knock-back effect, you can apply an impulse to the target's body when delivering
 * damage (see TODO in code).</p>
 */
public class EnemyMeleeEngageComponent extends Component {
    /**
     * Physics category mask for valid targets (e.g., {@link PhysicsLayer#ALLY}).
     */
    private final short targetMask;
    /**
     * Fallback damage per tick if this entity has no {@link CombatStatsComponent}.
     */
    private final int damagePerTickFallback;
    /**
     * Interval (seconds) between damage ticks per engaged target.
     */
    private final float tickSeconds;
    /**
     * Optional force magnitude for knock-back (currently unused).
     */
    private final float pushForce;

    /**
     * If true, trigger "ai:pause" when any target is engaged, "ai:resume" when none.
     */
    private final boolean pauseWhileEngaged;

    /**
     * This entity's hitbox used to validate collision callbacks.
     */
    private HitboxComponent hitbox;
    /**
     * This entity's combat stats (may be null, in which case fallback damage is used).
     */
    private CombatStatsComponent myStats;
    /**
     * This entity's physics component (used if you later implement pushForce).
     */
    private PhysicsComponent myPhys;

    /**
     * Map of currently engaged targets to their per-target tick timers (accumulated delta).
     * <p>Non-thread-safe by design; updated on the LibGDX render thread.</p>
     */
    private final Map<Entity, Float> engaged = new HashMap<>();

    /**
     * Default: engage {@link PhysicsLayer#ALLY}, 4 dmg/tick, 0.35s tick, no push, no AI pause.
     */
    public EnemyMeleeEngageComponent() {
        this(PhysicsLayer.ALLY, /*fallback*/ 4, /*tick*/ 0.35f, /*push*/ 0f, /*pause*/ false);
    }

    /**
     * Full constructor.
     *
     * @param targetMask            physics category bits to treat as valid targets
     * @param damagePerTickFallback damage per tick when this entity lacks CombatStats
     * @param tickSeconds           seconds between damage applications per target
     * @param pushForce             planned knock-back force (not currently applied)
     * @param pauseWhileEngaged     whether to pause/resume AI while targets are engaged
     */
    public EnemyMeleeEngageComponent(short targetMask, int damagePerTickFallback, float tickSeconds,
                                     float pushForce, boolean pauseWhileEngaged) {
        this.targetMask = targetMask;
        this.damagePerTickFallback = damagePerTickFallback;
        this.tickSeconds = tickSeconds;
        this.pushForce = pushForce;
        this.pauseWhileEngaged = pauseWhileEngaged;
    }

    @Override
    public void create() {
        // Cache commonly used components.
        hitbox = entity.getComponent(HitboxComponent.class);
        myStats = entity.getComponent(CombatStatsComponent.class);
        myPhys = entity.getComponent(PhysicsComponent.class);

        // Listen for physics collision events raised by HitboxComponent/Physics system.
        entity.getEvents().addListener("collisionStart", this::onStart);
        entity.getEvents().addListener("collisionEnd", this::onEnd);
    }

    /**
     * Handles start of a collision with this entity's hitbox.
     * Adds the other entity to the engaged map if it matches the {@code targetMask}.
     */
    private void onStart(Fixture me, Fixture other) {
        // Ensure this callback pertains to our own hitbox fixture.
        if (hitbox == null || hitbox.getFixture() != me) return;

        // Filter by layer mask (categoryBits).
        if (!PhysicsLayer.contains(targetMask, other.getFilterData().categoryBits)) return;

        // Retrieve the game Entity from Box2D user data. Assumes BodyUserData is attached.
        Body body = other.getBody();
        Object ud = body.getUserData();
        if (!(ud instanceof BodyUserData bud) || bud.entity == null) return;

        Entity target = bud.entity;

        // Start tracking this target with a zeroed tick timer.
        engaged.put(target, 0f);

        // Optionally pause AI while engaged.
        if (pauseWhileEngaged) {
            entity.getEvents().trigger("ai:pause");
        }
    }

    /**
     * Handles end of a collision with this entity's hitbox.
     * Removes the other entity from the engaged map and may resume AI.
     */
    private void onEnd(Fixture me, Fixture other) {
        if (hitbox == null || hitbox.getFixture() != me) return;

        Object ud = other.getBody().getUserData();
        if (!(ud instanceof BodyUserData bud) || bud.entity == null) return;

        Entity target = bud.entity;
        engaged.remove(target);

        if (pauseWhileEngaged && engaged.isEmpty()) {
            entity.getEvents().trigger("ai:resume");
        }
    }

    @Override
    public void update() {
        if (engaged.isEmpty()) return;

        // Accumulate per-target timers and apply damage when they reach tickSeconds.
        float dt = Gdx.graphics.getDeltaTime();
        Iterator<Map.Entry<Entity, Float>> it = engaged.entrySet().iterator();

        while (it.hasNext()) {
            var e = it.next();
            Entity target = e.getKey();

            // Validate target still has required components; otherwise disengage it.
            if (target == null
                    || target.getComponent(PhysicsComponent.class) == null
                    || (target.getComponent(CombatStatsComponent.class) == null
                    && target.getComponent(PlayerCombatStatsComponent.class) == null)) {
                it.remove();
                continue;
            }

            // Advance this target's tick timer.
            float t = e.getValue() + dt;

            // Apply damage on tick.
            if (t >= tickSeconds) {
                t -= tickSeconds;

                // Compute damage from our CombatStats if available; otherwise fallback constant.
                int dmg = (myStats != null ? Math.max(1, myStats.getBaseAttack())
                        : Math.max(1, damagePerTickFallback));

                // Prefer hitting generic CombatStats targets; otherwise hit PlayerCombatStats.
                CombatStatsComponent targetStats = target.getComponent(CombatStatsComponent.class);
                if (targetStats != null) {
                    CombatStatsComponent attacker = (myStats != null)
                            ? myStats
                            : new CombatStatsComponent(
                            /*health*/ 1,
                            /*baseAttack*/ Math.max(1, dmg),
                            /*atkType*/ DamageTypeConfig.None,
                            /*defType*/ DamageTypeConfig.None
                    );
                    targetStats.hit(attacker);

                    // Optional: drive floating damage text or similar UI.
                    target.getEvents().trigger("showDamage", dmg, target.getCenterPosition().cpy());

                } else {
                    // Player/base damage path.
                    PlayerCombatStatsComponent pcs = target.getComponent(PlayerCombatStatsComponent.class);
                    if (pcs != null) {
                        CombatStatsComponent attacker = (myStats != null)
                                ? myStats
                                : new CombatStatsComponent(
                                1, Math.max(1, dmg),
                                DamageTypeConfig.None, DamageTypeConfig.None
                        );
                        pcs.hit(attacker);
                    }
                }

                // TODO (optional knock-back):
                // if (pushForce > 0f) {
                //     PhysicsComponent tgtPhys = target.getComponent(PhysicsComponent.class);
                //     if (tgtPhys != null && myPhys != null) {
                //         Vector2 dir = target.getCenterPosition().cpy()
                //                 .sub(entity.getCenterPosition()).nor();
                //         tgtPhys.getBody().applyLinearImpulse(dir.scl(pushForce),
                //                 tgtPhys.getBody().getWorldCenter(), true);
                //     }
                // }
            }

            // Persist (possibly reduced) timer value for this target.
            e.setValue(t);
        }
    }
}

