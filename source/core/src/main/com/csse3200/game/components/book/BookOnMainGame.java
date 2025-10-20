package com.csse3200.game.ui.leaderboard;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.csse3200.game.services.ServiceLocator;
import com.badlogic.gdx.Gdx;

/**
 * Fullscreen overlay with a background image and a single "Back to game" button.
 * It pauses the game on show and resumes on close.
 */
public class BookOnMainGame extends Window {
    private final Skin skin;
    private TextButton backBtn;
    private Stage stage = ServiceLocator.getRenderService().getStage();

    /** Background images for buttons: enemies, currencies, towers, back button. */
    private final String[] buttonBackGround = {
            "images/book/enemies_book.png",
            "images/book/currencies_book.png",
            "images/book/towers_book.png",
            "images/book/hologram.png",
            "images/book/heroes_book.png"
    };

    /**
     * @param skin UI skin
     */
    public BookOnMainGame(Skin skin) {
        super("", skin);
        this.skin = skin;

        setModal(true);
        setMovable(false);
        setFillParent(true);
        pad(0);

        this.renderBackGround();
        // this.renderContentList();
        this.renderExitButton();
    }

    private void renderBackGround() {
        String backgroundPath = "images/book/encyclopedia_theme.png";

        // Background image
        try {
            Texture bg = ServiceLocator.getResourceService().getAsset(backgroundPath, Texture.class);
            setBackground(new TextureRegionDrawable(new TextureRegion(bg)));
        } catch (Exception ignored) {
            // No background found, continue without it
        }
    }

    private void renderExitButton() {
        float screenW = 300;
        float screenH = 300;

        float buttonWidth = screenW;
        float buttonHeight = screenH;

        TextButton.TextButtonStyle exitButtonStyle = createCustomButtonStyle(buttonBackGround[3]);
        TextButton exitButton = new TextButton("", exitButtonStyle);
        exitButton.getLabel().setColor(Color.WHITE);
        exitButton.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) { dismiss(); }
        });

        // Container that positions the button at top-right
        Table exitTable = new Table();
        exitTable.setFillParent(true);
        exitTable.top().right();
        exitTable.add(exitButton)
                .size(buttonWidth, buttonHeight)
                .pad(screenW * 0.01f);

        addActor(exitTable);
    }

    public void showOn(Stage stage) {
        // Find the entity with PauseInputComponent and trigger its pause
        triggerPauseSystem(true);

        stage.addActor(this);
        pack();
        setPosition(Math.round((stage.getWidth() - getWidth()) / 2f),
                Math.round((stage.getHeight() - getHeight()) / 2f));
    }

    public void dismiss() {
        triggerPauseSystem(false);
        addAction(Actions.sequence(
                Actions.parallel(Actions.alpha(0f, 0.12f), Actions.scaleTo(0.98f, 0.98f, 0.12f)),
                Actions.removeActor()
        ));
    }

    /** Pause/resume via PauseInputComponent if present, else fall back to timeScale */
    private void triggerPauseSystem(boolean pause) {
        try {
            if (ServiceLocator.getEntityService() == null) {
                // Fallback to direct time scale manipulation
                ServiceLocator.getTimeSource().setTimeScale(pause ? 0f : 1f);
                return;
            }

            com.badlogic.gdx.utils.Array<com.csse3200.game.entities.Entity> all =
                    ServiceLocator.getEntityService().getEntities();

            // Find the entity with PauseInputComponent
            for (int i = 0; i < all.size; i++) {
                com.csse3200.game.entities.Entity entity = all.get(i);
                if (entity.getComponent(com.csse3200.game.components.maingame.PauseInputComponent.class) != null) {
                    // Found it! Trigger the event it listens for
                    if (pause) {
                        // Check if already paused by checking time scale
                        if (ServiceLocator.getTimeSource().getTimeScale() > 0) {
                            entity.getEvents().trigger("togglePause");
                        }
                    } else {
                        entity.getEvents().trigger("resume");
                    }
                    return;
                }
            }

            // If we didn't find PauseInputComponent, fall back to direct time scale manipulation
            ServiceLocator.getTimeSource().setTimeScale(pause ? 0f : 1f);

        } catch (Exception e) {
            // Fallback to direct time scale manipulation if anything fails
            ServiceLocator.getTimeSource().setTimeScale(pause ? 0f : 1f);
        }
    }

    /**
     * Helper method to create a custom button style using a texture.
     *
     * @param backGround the texture path for the button
     * @return configured TextButtonStyle
     */
    private TextButton.TextButtonStyle createCustomButtonStyle(String backGround) {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();

        style.font = skin.getFont("default-font");

        Texture buttonTexture = ServiceLocator.getResourceService()
                .getAsset(backGround, Texture.class);
        TextureRegion buttonRegion = new TextureRegion(buttonTexture);

        NinePatch buttonPatch = new NinePatch(buttonRegion, 10, 10, 10, 10);

        NinePatch pressedPatch = new NinePatch(buttonRegion, 10, 10, 10, 10);
        pressedPatch.setColor(new Color(0.8f, 0.8f, 0.8f, 1f));

        NinePatch hoverPatch = new NinePatch(buttonRegion, 10, 10, 10, 10);
        hoverPatch.setColor(new Color(1.1f, 1.1f, 1.1f, 1f));

        style.up = new NinePatchDrawable(buttonPatch);
        style.down = new NinePatchDrawable(pressedPatch);
        style.over = new NinePatchDrawable(hoverPatch);

        style.fontColor = Color.WHITE;
        style.downFontColor = Color.LIGHT_GRAY;
        style.overFontColor = Color.WHITE;

        return style;
    }
}