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
 * Temporary test component: places an "ULT" button on the bottom-right corner of the HUD.
 * - Clicking the button or pressing the Q key triggers the "ultimate.request" event.
 * - During ultimate, the button is disabled (greyed out).
 * - When there is not enough currency, logs a failure message.
 */
public class UltimateButtonComponent extends Component {
    private Stage stage;
    private Skin skin;
    private Table root;
    private TextButton ultBtn;

    @Override
    public void create() {
        stage = ServiceLocator.getRenderService().getStage();

        // Use SimpleUI to provide button style
        skin = new Skin();
        skin.add("default-font", SimpleUI.font(), com.badlogic.gdx.graphics.g2d.BitmapFont.class);
        skin.add("default", SimpleUI.buttonStyle(), TextButton.TextButtonStyle.class);

        // Default button label (restored when ultimate ends)
        final String defaultText = "ULT (2)";
        ultBtn = new TextButton(defaultText, skin);

        // Click listener → trigger ultimate
        ultBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                entity.getEvents().trigger("ultimate.request");
            }
        });

        // Place in bottom-right corner
        root = new Table();
        root.setFillParent(true);
        root.bottom().right().pad(10);
        root.add(ultBtn).width(100).height(44);

        stage.addActor(root);

        // Disable button while ultimate is active & restore label when it ends
        entity.getEvents().addListener("ultimate.state", (Boolean on) -> {
            boolean active = Boolean.TRUE.equals(on);
            ultBtn.setDisabled(active);
            if (!active) {
                ultBtn.setText(defaultText);
            }
        });

        // Receive countdown updates (HeroUltimateComponent triggers "ultimate.remaining" every 0.1s)
        entity.getEvents().addListener("ultimate.remaining", (Float sec) -> {
            if (sec == null) return;
            float v = Math.max(0f, sec);
            // Show with 1 decimal place, e.g., "ULT 4.9s"
            ultBtn.setText(String.format("ULT %.1fs", v));
        });

        // Show failure reason when ultimate activation fails (e.g., not enough currency)
        entity.getEvents().addListener("ultimate.failed", (String reason) -> {
            Gdx.app.log("ULT", "Failed: " + reason);
        });

        // Keyboard Q key as shortcut trigger (for quick testing)
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
