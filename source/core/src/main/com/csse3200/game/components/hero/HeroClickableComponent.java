package com.csse3200.game.components.hero;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.csse3200.game.components.Component;
import com.csse3200.game.rendering.Renderer;
import com.csse3200.game.ui.HeroStatsDialog;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.services.ServiceLocator;
import com.badlogic.gdx.Input;

/**
 * 左键点击英雄，弹出英雄数值与升级界面。
 */
public class HeroClickableComponent extends Component {
    private float clickRadius = 0.8f;
    private HeroStatsDialog dialog; // 保证同一时间只存在一个

    // 防抖：避免放置时的同一次点击立刻打开
    private boolean waitRelease = true;
    private float armDelaySec = 0.2f; // 200ms

    public HeroClickableComponent() { }
    public HeroClickableComponent(float radius) { this.clickRadius = radius; }

    @Override
    public void create() {
        super.create();
        waitRelease = true;
    }

    @Override
    public void update() {
        // 先等待鼠标松开或过了延迟时间
        if (waitRelease) {
            if (!Gdx.input.isButtonPressed(Input.Buttons.LEFT) && !Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
                waitRelease = false;
            } else {
                // 仍在按压，继续等待
                return;
            }
        }
        if (armDelaySec > 0f) {
            float dt = (Gdx.graphics != null) ? Gdx.graphics.getDeltaTime() : (1f/60f);
            armDelaySec -= dt;
            return;
        }

        if (!Gdx.input.justTouched()) return;

        int screenX = Gdx.input.getX();
        int screenY = Gdx.input.getY();

        Camera camera = getCamera();
        if (camera == null) return;

        Vector3 worldClick = new Vector3(screenX, screenY, 0);
        camera.unproject(worldClick);

        Vector2 epos = entity.getPosition();
        if (Math.abs(worldClick.x - epos.x) <= clickRadius && Math.abs(worldClick.y - epos.y) <= clickRadius) {
            Stage stage = ServiceLocator.getRenderService().getStage();

            // 若已有窗口且仍在舞台上，则置顶并返回
            if (dialog != null && dialog.getStage() != null) {
                dialog.toFront();
                return;
            }

            // 否则创建新窗口并缓存
            dialog = new HeroStatsDialog(entity);
            dialog.showOn(stage);
        }
    }

    private Camera getCamera() {
        var r = Renderer.getCurrentRenderer();
        return (r != null && r.getCamera() != null) ? r.getCamera().getCamera() : null;
    }
} 