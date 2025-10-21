package com.csse3200.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.csse3200.game.components.Component;
import com.csse3200.game.rendering.Renderer;
import com.csse3200.game.services.ServiceLocator;

public class DamagePopupComponent extends Component {
    private Stage stage;
    private BitmapFont font;

    @Override
    public void create() {
        stage = ServiceLocator.getRenderService().getStage();
        font = new BitmapFont();
        font.getData().setScale(4f); //Set the Size of text
        entity.getEvents().addListener("showDamage", this::onShowDamage);
    }

    private void onShowDamage(Integer amount, Vector2 worldPos) {
        if (stage == null || amount == null || worldPos == null) return;

        // World position -> screen position
        Vector3 screen = new Vector3(worldPos.x, worldPos.y, 0f);
        if (Renderer.getCurrentRenderer() != null && Renderer.getCurrentRenderer().getCamera() != null) {
            Renderer.getCurrentRenderer().getCamera().getCamera().project(screen);
        }

        // 如果是伤害（正数）显示红色，如果是回复（负数）显示绿色并加上+号
        boolean isDamage = amount > 0;
        String displayText = isDamage ? String.valueOf(amount) : "+" + Math.abs(amount);
        Color textColor = isDamage ? Color.RED : Color.GREEN;
        
        Label.LabelStyle style = new Label.LabelStyle(font, textColor);
        Label label = new Label(displayText, style);
        label.setPosition(screen.x, screen.y);
        label.getColor().a = 1f;

        // Flow up, and destroy
        float rise = 24f;      // flow up distance
        float duration = 0.6f; // flow up time
        label.addAction(Actions.sequence(
                Actions.parallel(
                        Actions.moveBy(0f, rise, duration),
                        Actions.fadeOut(duration)
                ),
                Actions.run(label::remove)
        ));

        stage.addActor(label);
    }

    @Override
    public void dispose() {
        if (font != null) {
            font.dispose();
            font = null;
        }
    }
}