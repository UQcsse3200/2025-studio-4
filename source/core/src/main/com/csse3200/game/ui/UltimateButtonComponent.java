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
import com.badlogic.gdx.graphics.g2d.BitmapFont;


/**
 * Ultimate button utility: provides static method to create ULT button for embedding in other UI.
 * - Clicking the button or pressing the Q key triggers the "ultimate.request" event.
 * - During ultimate, the button is disabled (greyed out).
 * - When there is not enough currency, logs a failure message.
 */
public class UltimateButtonComponent extends Component {

    /**
     * Creates a ULT button that can be embedded in other UI components.
     * The button will listen to the hero entity's events and update accordingly.
     * 
     * @param heroEntity The hero entity that owns the ultimate ability
     * @return A configured TextButton ready to be added to any UI
     */
    public static TextButton createUltimateButton(com.csse3200.game.entities.Entity heroEntity) {
        Skin skin = new Skin();
        skin.add("default-font", SimpleUI.font(), BitmapFont.class);
        skin.add("default", SimpleUI.buttonStyle(), TextButton.TextButtonStyle.class);

        // Default button label (restored when ultimate ends)
        final String defaultText = "ULT (2)";
        TextButton ultBtn = new TextButton(defaultText, skin);

        // Click listener → trigger ultimate
        ultBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                heroEntity.getEvents().trigger("ultimate.request");
            }
        });

        // Disable button while ultimate is active & restore label when it ends
        heroEntity.getEvents().addListener("ultimate.state", (Boolean on) -> {
            boolean active = Boolean.TRUE.equals(on);
            ultBtn.setDisabled(active);
            if (!active) {
                ultBtn.setText(defaultText);
            }
        });

        // Receive countdown updates (HeroUltimateComponent triggers "ultimate.remaining" every 0.1s)
        heroEntity.getEvents().addListener("ultimate.remaining", (Float sec) -> {
            if (sec == null) return;
            float v = Math.max(0f, sec);
            // Show with 1 decimal place, e.g., "ULT 4.9s"
            ultBtn.setText(String.format("ULT %.1fs", v));
        });

        // Show failure reason when ultimate activation fails (e.g., not enough currency)
        heroEntity.getEvents().addListener("ultimate.failed", (String reason) -> {
            Gdx.app.log("ULT", "Failed: " + reason);
        });

        return ultBtn;
    }

    @Override
    public void create() {
        // Empty implementation - this component is now just a utility class
        // The actual UI creation is handled by createUltimateButton() static method
    }

    @Override
    public void dispose() {
        // Empty implementation - no UI to dispose
    }
}