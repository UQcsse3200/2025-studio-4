package com.csse3200.game.components.heroselect;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.utils.Align;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;

/**
 * Hero selection UI: two hero buttons + Back.
 * Triggers events only; logic handled by Actions component.
 */
public class HeroSelectDisplay extends UIComponent {
    private Table root;

    @Override
    public void create() {
        super.create();

        // Use the same skin as the rest of the game
        //Skin skin = ServiceLocator.getResourceService().getSkin();

        // Clone default styles and force font = segoe_ui to avoid missing glyphs
        TextButtonStyle baseBtn = skin.get(TextButtonStyle.class);
        TextButtonStyle btn = new TextButtonStyle(baseBtn);
        btn.font = skin.getFont("segoe_ui");

        LabelStyle titleStyle = new LabelStyle(skin.getFont("segoe_ui"), null);

        root = new Table();
        root.setFillParent(true);
        root.defaults().pad(12f);

        Label title = new Label("Select Your Hero", titleStyle);
        title.setAlignment(Align.center);

        TextButton heroBtn = new TextButton("Hero", btn);
        TextButton engineerBtn = new TextButton("Engineer", btn);
        TextButton backBtn = new TextButton("Back", btn);

        heroBtn.addListener(e -> {
            if (!heroBtn.isPressed()) return false;
            if (entity != null) entity.getEvents().trigger("pickHero");
            return true;
        });

        engineerBtn.addListener(e -> {
            if (!engineerBtn.isPressed()) return false;
            if (entity != null) entity.getEvents().trigger("pickEngineer");
            return true;
        });

        backBtn.addListener(e -> {
            if (!backBtn.isPressed()) return false;
            if (entity != null) entity.getEvents().trigger("goBackToMenu");
            return true;
        });

        // Layout
        root.add(title).padTop(18f).row();
        root.add(heroBtn).width(240f).height(64f).row();
        root.add(engineerBtn).width(240f).height(64f).row();
        root.add(backBtn).width(240f).height(56f).padTop(6f).row();

        stage.addActor(root);
    }

    @Override
    public void draw(SpriteBatch batch) {
        // Do NOT call stage.draw() here; global renderer will handle it.
    }

    @Override
    public void dispose() {
        if (root != null) {
            root.clear();
            root.remove();
            root = null;
        }
        super.dispose();
    }
}
