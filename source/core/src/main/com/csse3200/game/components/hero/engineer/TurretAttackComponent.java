package com.csse3200.game.components.hero.engineer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.ProjectileFactory;
import com.csse3200.game.services.ServiceLocator;

/**
 * TurretAttackComponent:
 * - 给机器人/炮台使用
 * - 定时朝固定方向发射子弹
 * - 子弹伤害从 CombatStatsComponent 读取
 */
public class TurretAttackComponent extends Component {
    private final Vector2 direction;
    private final float cooldown;
    private final float bulletSpeed;
    private final float bulletLife;
    private final String bulletTexture;

    private float cdTimer = 0f;

    public TurretAttackComponent(Vector2 direction, float cooldown,
                                 float bulletSpeed, float bulletLife, String bulletTexture) {
        this.direction = direction.nor();
        this.cooldown = cooldown;
        this.bulletSpeed = bulletSpeed;
        this.bulletLife = bulletLife;
        this.bulletTexture = bulletTexture;
    }

    @Override
    public void update() {
        if (entity == null) return;

        float dt = (Gdx.graphics != null) ? Gdx.graphics.getDeltaTime() : (1f / 60f);
        if (cdTimer > 0f) {
            cdTimer -= dt;
            return;
        }

        // 计算发射位置
        Vector2 firePos = getEntityCenter(entity);

        // 计算速度分量
        float vx = direction.x * bulletSpeed;
        float vy = direction.y * bulletSpeed;

        // 从 CombatStatsComponent 取攻击力
        int dmg = computeDamageFromStats();

        // 生成子弹
        final Entity bullet = ProjectileFactory.createBullet(
                bulletTexture, firePos, vx, vy, bulletLife, dmg
        );

        var es = ServiceLocator.getEntityService();
        if (es != null) {
            Gdx.app.postRunnable(() -> es.register(bullet));
        }

        cdTimer = cooldown;
    }

    private int computeDamageFromStats() {
        CombatStatsComponent stats = entity.getComponent(CombatStatsComponent.class);
        int base = (stats != null) ? stats.getBaseAttack() : 1;
        return Math.max(1, base);
    }

    private static Vector2 getEntityCenter(Entity e) {
        try {
            Vector2 center = e.getCenterPosition();
            if (center != null) return center;
        } catch (Throwable ignored) { }
        Vector2 pos = e.getPosition();
        Vector2 scale = e.getScale();
        float cx = pos.x + (scale != null ? scale.x * 0.5f : 0.5f);
        float cy = pos.y + (scale != null ? scale.y * 0.5f : 0.5f);
        return new Vector2(cx, cy);
    }
}


