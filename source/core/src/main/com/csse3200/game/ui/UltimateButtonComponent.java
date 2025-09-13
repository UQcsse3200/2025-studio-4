package com.csse3200.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.csse3200.game.components.Component;
import com.csse3200.game.services.ServiceLocator;

/**
 * 临时测试用：在 HUD 右下角放一个“ULT”按钮。
 * 点击按钮或按下键盘 Q 时，会触发 "ultimate.request" 事件。
 * 大招期间按钮置灰；余额不足时在日志里提示。
 */

public class UltimateButtonComponent extends Component{
    private Stage stage;
    private Skin skin;
    private Table root;
    private TextButton ultBtn;

    @Override
    public void create() {
        stage = ServiceLocator.getRenderService().getStage();

        // 用 SimpleUI 提供的按钮样式
        skin = new Skin();
        skin.add("default-font", SimpleUI.font(), com.badlogic.gdx.graphics.g2d.BitmapFont.class);
        skin.add("default", SimpleUI.buttonStyle(), TextButton.TextButtonStyle.class);

        ultBtn = new TextButton("ULT (2)", skin); // 简单写死 50，后面可替换

        // 点击触发大招
        ultBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                entity.getEvents().trigger("ultimate.request");
            }
        });

        // 放在右下角
        root = new Table();
        root.setFillParent(true);
        root.bottom().right().pad(10);
        root.add(ultBtn).width(100).height(44);

        stage.addActor(root);

        // 大招期间禁用按钮
        entity.getEvents().addListener("ultimate.state", (Boolean on) -> {
            ultBtn.setDisabled(Boolean.TRUE.equals(on));
        });

        // 余额不足提示
        entity.getEvents().addListener("ultimate.failed", (String reason) -> {
            Gdx.app.log("ULT", "Failed: " + reason);
        });

        // 键盘 Q 触发（方便快速测试）
        stage.addListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.Q) {
                    entity.getEvents().trigger("ultimate.request");
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void dispose() {
        if (root != null) root.remove();
        if (skin != null) skin.dispose();
    }
}
