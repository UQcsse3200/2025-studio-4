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

/**
 * 左键点击英雄，弹出英雄数值与升级界面。
 */
public class HeroClickableComponent extends Component {
    private float clickRadius = 0.8f;
    private HeroStatsDialog dialog; // 保证同一时间只存在一个

    public HeroClickableComponent() { }
    public HeroClickableComponent(float radius) { this.clickRadius = radius; }

    @Override
    public void update() {
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