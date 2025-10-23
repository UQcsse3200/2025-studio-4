package com.csse3200.game.ui.Hero;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.Align;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.rendering.Renderer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.Gdx;
import com.csse3200.game.ui.SimpleUI;

/**
 * One-time hint window: shows only the weapon-switch hint and a close button.
 */
public class HeroHintDialog extends Window {
    private final Entity hero;
    private final Label hintLabel;
    private final TextButton closeBtn;

    private final Vector3 tmp = new Vector3();
    private final Vector2 offset = new Vector2(0f, 72f);

    // High-resolution fonts (dispose on remove)
    private BitmapFont fontLabel;
    private BitmapFont fontButton;

    public HeroHintDialog(Entity hero) {
        super("", SimpleUI.windowStyle());
        this.hero = hero;

        setModal(false);
        setMovable(true);
        setTouchable(Touchable.enabled);
        pad(10);

        hintLabel = new Label("Hint: press keyboard 1/2/3 use hero power", SimpleUI.label());
        closeBtn = new TextButton("Close", SimpleUI.darkButton());

        // Use high-resolution bitmap fonts (avoid blur from upscaling): load a 32px font and scale it down slightly
        fontLabel = new BitmapFont(Gdx.files.internal("flat-earth/skin/fonts/arial_black_32.fnt"));
        fontLabel.getData().setScale(0.7f); // Scale down rather than up to keep it crisp
        fontLabel.setColor(Color.BLACK);

        fontButton = new BitmapFont(Gdx.files.internal("flat-earth/skin/fonts/arial_black_32.fnt"));
        fontButton.getData().setScale(0.7f);
        fontButton.setColor(Color.BLACK);

        Label.LabelStyle labelStyle = new Label.LabelStyle(fontLabel, Color.BLACK);
        hintLabel.setStyle(labelStyle);

        TextButton.TextButtonStyle closeStyle = SimpleUI.darkButton();
        closeStyle.font = fontButton;
        closeStyle.fontColor = Color.BLACK;
        closeStyle.overFontColor = Color.BLACK;
        closeStyle.downFontColor = Color.BLACK;
        // White background
        closeStyle.up = SimpleUI.solid(Color.WHITE);
        closeStyle.over = SimpleUI.solid(Color.WHITE);
        closeStyle.down = SimpleUI.solid(Color.WHITE);
        closeBtn.setStyle(closeStyle);

        Table content = new Table();
        content.defaults().pad(6);
        content.add(hintLabel).left().row();
        content.add(closeBtn).right();

        add(content).grow().row();

        // Bring-to-front on click
        addCaptureListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                toFront();
                if (getStage() != null) setZIndex(getStage().getActors().size - 1);
                return false;
            }
        });

        closeBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                remove();
            }
        });
    }

    public void showOnceOn(Stage stage) {
        // If already on stage, do not show again
        if (getStage() != null) return;
        stage.addActor(this);
        pack();
        setSize(Math.max(getWidth(), 520f), Math.max(getHeight(), 120f));
        updateFollowPosition(stage);
        toFront();
        setZIndex(stage.getActors().size - 1);
    }

    @Override
    public boolean remove() {
        boolean r = super.remove();
        if (r) {
            if (fontLabel != null) { fontLabel.dispose(); fontLabel = null; }
            if (fontButton != null) { fontButton.dispose(); fontButton = null; }
        }
        return r;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        Stage s = getStage();
        if (s != null) updateFollowPosition(s);
    }

    private void updateFollowPosition(Stage stage) {
        Camera cam = getWorldCamera();
        if (cam == null || hero == null) return;

        tmp.set(hero.getPosition().x, hero.getPosition().y, 0f);
        cam.project(tmp);

        float px = tmp.x - getWidth() / 2f + offset.x;
        float py = tmp.y + offset.y;

        float margin = 8f;
        px = Math.max(margin, Math.min(px, stage.getWidth() - getWidth() - margin));
        py = Math.max(margin, Math.min(py, stage.getHeight() - getHeight() - margin));
        setPosition(Math.round(px), Math.round(py));
    }

    private Camera getWorldCamera() {
        Renderer r = Renderer.getCurrentRenderer();
        return (r != null && r.getCamera() != null) ? r.getCamera().getCamera() : null;
    }
}
