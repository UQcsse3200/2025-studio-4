package com.csse3200.game.entities.factories;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.hero.samurai.SwordQiAnimatorPng;
import com.csse3200.game.components.projectile.DestroyOnHitComponent;
import com.csse3200.game.components.projectile.ProjectileComponent;
import com.csse3200.game.components.TouchAttackComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.entities.configs.DamageTypeConfig;

/**
 * 专用于创建“赛博武士剑气（PNG sprite sheet 动画）”的投射物实体。
 * 不添加任何静态贴图渲染组件。
 */
public final class SwordQiFactory {
    private SwordQiFactory() {}

    /**
     * @param spawn        出生点（世界坐标）
     * @param vx,vy        初速度
     * @param life         存活时长（秒）
     * @param damage       伤害
     * @param drawW,drawH  渲染尺寸（世界单位，对应 entity.setScale）
     * @param pngPath      PNG 精灵图路径（assets 相对路径）
     * @param cols,rows    切帧网格（列/行）
     * @param frameW,frameH 每帧像素尺寸
     * @param frameDur     每帧时长（秒）
     * @param angleDeg     世界朝向（0=向右，90=向上）
     * @param baseRotation 素材基准朝向（素材朝上=90f，朝右=0f）
     * @param loop         是否循环播放动画
     */
    public static Entity createSwordQi(
            Vector2 spawn,
            float vx, float vy,
            float life,
            int damage,
            float drawW, float drawH,
            String pngPath,
            int cols, int rows, int frameW, int frameH,
            float frameDur,
            float angleDeg,
            float baseRotation,
            boolean loop
    ) {
        // ===== 0) 防呆：保证所有关键参数都是“有限”的 =====
        if (spawn == null
                || !Float.isFinite(spawn.x) || !Float.isFinite(spawn.y)
                || !Float.isFinite(vx)     || !Float.isFinite(vy)
                || !Float.isFinite(life)   || life <= 0f
                || !Float.isFinite(drawW)  || !Float.isFinite(drawH) || drawW <= 0f || drawH <= 0f
                || !Float.isFinite(angleDeg) || !Float.isFinite(baseRotation)
                || cols <= 0 || rows <= 0 || frameW <= 0 || frameH <= 0
                || !Float.isFinite(frameDur) || frameDur <= 0f) {
            // 直接放弃这次生成，避免 JNI 崩溃
            com.badlogic.gdx.Gdx.app.error("SwordQiFactory", "Bad params; skip create. spawn="+spawn+" vx="+vx+" vy="+vy+" angle="+angleDeg);
            return null;
        }

        // 可选的坐标钳制（避免极端远点）
        float px = com.badlogic.gdx.math.MathUtils.clamp(spawn.x, -10000f, 10000f);
        float py = com.badlogic.gdx.math.MathUtils.clamp(spawn.y, -10000f, 10000f);
        float safeAngle = angleDeg;
        // 规范化角度到 [-360, 360] 以内，避免无意义的大角度
        while (safeAngle > 360f) safeAngle -= 360f;
        while (safeAngle < -360f) safeAngle += 360f;

        // ===== 1) 组件装配 =====
        var physics = new com.csse3200.game.physics.components.PhysicsComponent();
        physics.setBodyType(com.badlogic.gdx.physics.box2d.BodyDef.BodyType.KinematicBody);

        var hitbox = new com.csse3200.game.physics.components.HitboxComponent();
        hitbox.setLayer(com.csse3200.game.physics.PhysicsLayer.PROJECTILE);
        hitbox.setSensor(true); // 投射物用传感器

        Entity qi = new Entity()
                .addComponent(physics)
                .addComponent(hitbox)
                .addComponent(new ProjectileComponent(vx, vy, life))
                .addComponent(new CombatStatsComponent(
                        1, damage, DamageTypeConfig.None, DamageTypeConfig.None))
                .addComponent(new TouchAttackComponent(PhysicsLayer.NPC, 0f))
                .addComponent(new DestroyOnHitComponent(PhysicsLayer.NPC));

        // 先设置渲染尺寸与位置（通常在 entity.create() 之前完成）
        qi.setScale(drawW, drawH);
        qi.setPosition(px, py);

        // ===== 2) 安全地添加 PNG 动画渲染 =====
        SwordQiAnimatorPng.apply(
                qi, pngPath, cols, rows, frameW, frameH, frameDur,
                safeAngle, baseRotation, loop
        );

        // ===== 3) 不要在这里立即碰刚体 =====
        // 注意：此时 PhysicsComponent 可能还没 createBody（要等 entity 被加入游戏世界后）。
        // 直接 getBody().setTransform(...) 在某些时机会 NPE 或 JNI 崩溃。
        //
        // 安全做法：等到下一帧再尝试同步旋转（此时刚体已创建），并且再次判空。
        final float fx = px;
        final float fy = py;
        final float fAngleRad = (float)Math.toRadians(safeAngle + baseRotation);
        final var physicsRef = physics; // 可选，只是让语义更清晰

        com.badlogic.gdx.Gdx.app.postRunnable(() -> {
            if (physicsRef.getBody() != null) {
                physicsRef.getBody().setTransform(fx, fy, fAngleRad);
            }
        });

        return qi;
    }

}
