package com.csse3200.game.ui;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

/** 屏幕底部的临时提示条 */
public final class HintToast {
    private HintToast() {}

    public static void show(Stage stage, String text, float seconds) {
        Label label = new Label(text, SimpleUI.label());
        label.setColor(Color.WHITE);
        label.setAlignment(Align.center);

        Table container = new Table();
        container.setBackground(SimpleUI.solid(new Color(0f,0f,0f,0.55f)));
        container.add(label).growX().pad(6f);

        float width = Math.min(stage.getWidth() - 80f, 720f);
        float height = 36f;
        container.setSize(width, height);
        container.setPosition((stage.getWidth() - width)/2f, 24f);

        stage.addActor(container);
        container.getColor().a = 0f;
        container.addAction(Actions.sequence(
                Actions.alpha(1f, 0.15f),
                Actions.delay(seconds),
                Actions.alpha(0f, 0.25f),
                Actions.removeActor()
        ));
    }
} 