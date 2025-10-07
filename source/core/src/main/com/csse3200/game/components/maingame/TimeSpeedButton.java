package com.csse3200.game.components.maingame;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * UI component that displays a button to control game time speed.
 * Clicking the button toggles between normal speed (1x) and double speed (2x).
 */
public class TimeSpeedButton extends UIComponent {
    private static final Logger logger = LoggerFactory.getLogger(TimeSpeedButton.class);
    private static final float NORMAL_SPEED = 1.0f;
    private static final float DOUBLE_SPEED = 2.0f;
    private static final float Z_INDEX = 2f;

    private Table table;
    private TextButton speedButton;
    private boolean isDoubleSpeed = false;

    /**
     * Creates the time speed button UI.
     */
    @Override
    public void create() {
        super.create();
        addActors();
    }

    private void addActors() {
        table = new Table();
        table.top().right();
        table.setFillParent(true);

        TextButtonStyle customButtonStyle = createCustomButtonStyle();

        // Create the speed button
        speedButton = new TextButton("Speed: 1x", customButtonStyle);

        float buttonWidth = 120f;
        float buttonHeight = 40f;

        speedButton.getLabel().setColor(Color.CYAN);
        speedButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                toggleSpeed();
            }
        });
        table.add(speedButton).size(buttonWidth, buttonHeight).padTop(145f).padRight(10f);
        stage.addActor(table);
        logger.info("Time speed button created");
    }

    /**
     * Creates custom button style using button background image
     */
    private TextButtonStyle createCustomButtonStyle() {
        TextButtonStyle style = new TextButtonStyle();

        style.font = skin.getFont("segoe_ui");

        Texture buttonTexture = ServiceLocator.getResourceService()
            .getAsset("images/Main_Game_Button.png", Texture.class);
        TextureRegion buttonRegion = new TextureRegion(buttonTexture);

        NinePatch buttonPatch = new NinePatch(buttonRegion, 10, 10, 10, 10);

        NinePatch pressedPatch = new NinePatch(buttonRegion, 10, 10, 10, 10);
        pressedPatch.setColor(new Color(0.8f, 0.8f, 0.8f, 1f));

        NinePatch hoverPatch = new NinePatch(buttonRegion, 10, 10, 10, 10);
        hoverPatch.setColor(new Color(1.1f, 1.1f, 1.1f, 1f));

        style.up = new NinePatchDrawable(buttonPatch);
        style.down = new NinePatchDrawable(pressedPatch);
        style.over = new NinePatchDrawable(hoverPatch);

        style.fontColor = Color.CYAN;
        style.downFontColor = new Color(0.0f, 0.6f, 0.8f, 1.0f);
        style.overFontColor = new Color(0.2f, 0.8f, 1.0f, 1.0f);

        return style;
    }

    /**
     * Toggles the game speed between normal (1x) and double (2x).
     */
    private void toggleSpeed() {
        isDoubleSpeed = !isDoubleSpeed;

        if (isDoubleSpeed) {
            ServiceLocator.getTimeSource().setTimeScale(DOUBLE_SPEED);
            speedButton.setText("Speed: 2x");
            logger.info("Game speed set to 2x");
        } else {
            ServiceLocator.getTimeSource().setTimeScale(NORMAL_SPEED);
            speedButton.setText("Speed: 1x");
            logger.info("Game speed set to 1x");
        }
    }

    @Override
    public void draw(SpriteBatch batch) {
        // draw is handled by the stage
    }

    @Override
    public float getZIndex() {
        return Z_INDEX;
    }

    @Override
    public void dispose() {
        table.clear();
        super.dispose();
    }
}
