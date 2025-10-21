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

public class EnemyMeleeEngageComponent extends Component {
    private final short targetMask;
    private final int damagePerTickFallback;
    private final float tickSeconds;
    private final float pushForce;

    private final boolean pauseWhileEngaged;

    private HitboxComponent hitbox;
    private CombatStatsComponent myStats;
    private PhysicsComponent myPhys;

    private final Map<Entity, Float> engaged = new HashMap<>();

    public EnemyMeleeEngageComponent() {
        this(PhysicsLayer.ALLY, /*fallback*/ 4, /*tick*/ 0.35f, /*push*/ 0f, /*pause*/ false);
    }

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
        hitbox = entity.getComponent(HitboxComponent.class);
        myStats = entity.getComponent(CombatStatsComponent.class);
        myPhys  = entity.getComponent(PhysicsComponent.class);

        entity.getEvents().addListener("collisionStart", this::onStart);
        entity.getEvents().addListener("collisionEnd", this::onEnd);
    }

    private void onStart(Fixture me, Fixture other) {
        if (hitbox == null || hitbox.getFixture() != me) return;
        if (!PhysicsLayer.contains(targetMask, other.getFilterData().categoryBits)) return;

        Entity target = ((BodyUserData) other.getBody().getUserData()).entity;

        engaged.put(target, 0f);

        if (pauseWhileEngaged) {
            entity.getEvents().trigger("ai:pause");
        }
    }

    private void onEnd(Fixture me, Fixture other) {
        if (hitbox == null || hitbox.getFixture() != me) return;

        Entity target = ((BodyUserData) other.getBody().getUserData()).entity;
        engaged.remove(target);

        if (pauseWhileEngaged && engaged.isEmpty()) {
            entity.getEvents().trigger("ai:resume");
        }
    }

    @Override
    public void update() {
        if (engaged.isEmpty()) return;

        float dt = Gdx.graphics.getDeltaTime();
        Iterator<Map.Entry<Entity, Float>> it = engaged.entrySet().iterator();
        while (it.hasNext()) {
            var e = it.next();
            Entity target = e.getKey();

            // ✅ 替代 isDisposed() 的健壮检测
            if (target == null
                    || target.getComponent(PhysicsComponent.class) == null
                    || (target.getComponent(CombatStatsComponent.class) == null
                    && target.getComponent(PlayerCombatStatsComponent.class) == null)) {
                it.remove();
                continue;
            }

            float t = e.getValue() + dt;
            if (t >= tickSeconds) {
                t -= tickSeconds;

                int dmg = (myStats != null ? Math.max(1, myStats.getBaseAttack())
                        : Math.max(1, damagePerTickFallback));

                CombatStatsComponent targetStats = target.getComponent(CombatStatsComponent.class);
                if (targetStats != null) {
                    // ✅ 按签名传入“攻击者”的 CombatStatsComponent
                    CombatStatsComponent attacker = (myStats != null)
                            ? myStats
                            : new CombatStatsComponent(1, Math.max(1, dmg), DamageTypeConfig.None, DamageTypeConfig.None);
                    targetStats.hit(attacker);
                    target.getEvents().trigger("showDamage", dmg, target.getCenterPosition().cpy());
                } else {
                    PlayerCombatStatsComponent pcs = target.getComponent(PlayerCombatStatsComponent.class);
                    if (pcs != null) {
                        CombatStatsComponent attacker = (myStats != null)
                                ? myStats
                                : new CombatStatsComponent(1, Math.max(1, dmg), DamageTypeConfig.None, DamageTypeConfig.None);
                        pcs.hit(attacker);
                    }
                }

            }
            e.setValue(t);
        }
    }


}
