package com.csse3200.game.components.hero.samurai;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.rendering.RotatingSheetAnimationRenderComponent;

/** 使用 PNG sprite sheet 的“赛博武士剑气”动画封装。 */
public final class SwordQiAnimatorPng {
    private SwordQiAnimatorPng() {}

    /**
     * @param entity   目标实体
     * @param pngPath  PNG 路径，例如 "images/samurai/slash_sheet_6x1_64.png"
     * @param cols     列数（例如 6）
     * @param rows     行数（例如 1）
     * @param frameW   每帧宽（像素）
     * @param frameH   每帧高（像素）
     * @param frameDur 每帧时长（秒）
     * @param angleDeg 世界朝向角（0°向右，90°向上）
     * @param baseRotation 素材基准朝向：素材默认向上=90f，默认向右=0f
     * @param loop     是否循环（true=LOOP，false=Play once）
     */
    public static void apply(Entity entity,
                             String pngPath,
                             int cols, int rows, int frameW, int frameH,
                             float frameDur,
                             float angleDeg,
                             float baseRotation,
                             boolean loop) {

        var comp = RotatingSheetAnimationRenderComponent.fromSheet(
                pngPath, cols, rows, frameW, frameH, frameDur,
                loop ? Animation.PlayMode.LOOP : Animation.PlayMode.NORMAL,
                "samurai_qi"
        );
        comp.setBaseRotation(baseRotation);
        comp.setRotation(angleDeg);
        comp.setScale(1f, 1f);

        // 关键：放到靠前的层 & 极高 zIndex，防止任何遮挡
        comp.setLayer(5);
        comp.setZIndexOverride(9999f);

        entity.addComponent(comp);

        // 尺寸防呆：若未设置实体 scale 或为 0，则给一个可见大小
        if (entity.getScale().isZero()) {
            entity.setScale(3.0f, 3.0f); // 先大点确认能看见；确认后可调回 1.2f
        }

        comp.start();
    }
}



