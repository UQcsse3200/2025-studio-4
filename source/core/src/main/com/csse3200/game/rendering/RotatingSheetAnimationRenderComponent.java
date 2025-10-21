package com.csse3200.game.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;

/**
 * 使用 PNG sprite sheet（非 .atlas），按网格切帧的“可旋转”帧动画渲染组件。
 * - 支持：旋转、缩放、原点、播完事件（animationFinished）、前景层/自定义 zIndex。
 */
public class RotatingSheetAnimationRenderComponent extends RenderComponent implements AutoCloseable {
    private final GameTime time = ServiceLocator.getTimeSource();
    private final Animation<TextureRegion> animation;
    private final String animationName;
    private final Texture ownedTexture;

    private float playTime = 0f;

    private float rotationDeg = 0f;      // 最终绘制角度（度）
    private float baseRotationDeg = 0f;  // 素材基准朝向（上=90，右=0）
    private float scaleX = 1f, scaleY = 1f;
    private float originX = -1f, originY = -1f; // <0 = 使用帧中心
    private boolean autoRemoveOnFinish = false;

    // ===== 新增：层 & zIndex =====
    private int layer = 5;                // 默认放在较前的层（RenderComponent 默认是 1）
    private Float zOverride = null;       // 若不为 null，则用该值作为 zIndex

    // 调试：只打印前几帧
    private int dbgCount = 0;

    private RotatingSheetAnimationRenderComponent(TextureRegion[] frames,
                                                  float frameDur,
                                                  PlayMode playMode,
                                                  String animName,
                                                  Texture owned) {
        this.animation = new Animation<>(frameDur, frames);
        this.animation.setPlayMode(playMode == null ? PlayMode.NORMAL : playMode);
        this.animationName = animName == null ? "anim" : animName;
        this.ownedTexture = owned;
    }

    /** 从 PNG 网格切帧构造组件。（内部自行 new Texture）*/
    public static RotatingSheetAnimationRenderComponent fromSheet(String pngPath,
                                                                  int cols, int rows,
                                                                  int frameW, int frameH,
                                                                  float frameDur,
                                                                  PlayMode playMode,
                                                                  String animName) {
        Texture sheet = new Texture(pngPath);
        TextureRegion[][] raw = TextureRegion.split(sheet, frameW, frameH);
        TextureRegion[] frames = new TextureRegion[cols * rows];
        int idx = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                frames[idx++] = raw[r][c];
            }
        }
        return new RotatingSheetAnimationRenderComponent(frames, frameDur, playMode, animName, sheet);
    }

    // ===== 公开控制 API =====
    public void start() { playTime = 0f; }
    public void setRotation(float deg) { this.rotationDeg = deg; }
    public void setBaseRotation(float deg) { this.baseRotationDeg = deg; }
    public void setScale(float sx, float sy) { this.scaleX = sx; this.scaleY = sy; }
    /** 原点（相对 entity.scale 的大小单位）。传负值=中心。*/
    public void setOrigin(float ox, float oy) { this.originX = ox; this.originY = oy; }
    /** NORMAL 模式播完后自动移除本组件（停止绘制）。*/
    public void setAutoRemoveOnFinish(boolean v) { this.autoRemoveOnFinish = v; }
    /** 设置渲染层：RenderService 会按层分桶渲染。*/
    public void setLayer(int layer) { this.layer = layer; }
    /** 强制覆盖 zIndex（不跟随 -pos.y）。*/
    public void setZIndexOverride(Float z) { this.zOverride = z; }

    @Override
    public int getLayer() {
        return layer;
    }

    @Override
    public float getZIndex() {
        if (zOverride != null) return zOverride;
        return super.getZIndex(); // 默认：-entity.getPosition().y
    }

    @Override
    public void create() {
        super.create(); // 注册到 RenderService
        //Gdx.app.log("SwordQi", "created layer=" + layer + " z=" + getZIndex());
    }

    @Override
    protected void draw(SpriteBatch batch) {
        TextureRegion frame = animation.getKeyFrame(playTime);
        if (frame == null) return;

        var pos = entity.getPosition();
        var size = entity.getScale(); // 宽高由实体 scale 决定
        float w = size.x, h = size.y;

        float ox = originX < 0 ? w * 0.5f : originX;
        float oy = originY < 0 ? h * 0.5f : originY;

        if (dbgCount < 3) {
            // 只打印少量帧，确认被调用
            //Gdx.app.log("SwordQi.draw", "pos=" + pos + " size=" + size + " layer=" + getLayer() + " z=" + getZIndex());
            dbgCount++;
        }

        batch.draw(frame,
                pos.x, pos.y,
                ox, oy,
                w, h,
                scaleX, scaleY,
                rotationDeg + baseRotationDeg
        );

        float last = playTime;
        playTime += time.getDeltaTime();

        if (animation.getPlayMode() == PlayMode.NORMAL
                && last < animation.getAnimationDuration()
                && playTime >= animation.getAnimationDuration()) {
            entity.getEvents().trigger("animationFinished", animationName);
            if (autoRemoveOnFinish) {
                // 可按你们引擎约定停止绘制，如有 enabled/visible 标志可置为 false
                // 这里简单地把 playTime 固定到最后一帧前
                playTime = animation.getAnimationDuration() - 0.0001f;
            }
        }
    }

    @Override
    public void dispose() {
        if (ownedTexture != null) ownedTexture.dispose();
        super.dispose();
    }

    @Override
    public void close() { dispose(); }
}
