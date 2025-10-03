package com.csse3200.game.components.maingame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputAdapter;

import com.csse3200.game.ui.UIComponent;

/**
 * UI component that displays a hotbar for selecting and placing towers.
 * <p>
 * Towers are arranged in a grid with a bold/pixel-art style title at the top.
 * Each button triggers the placement of its corresponding tower type.
 * </p>
 */
public class TowerHotbarDisplay extends UIComponent {

    /** Root table containing the hotbar UI */
    private Table rootTable;

    /** Reference to the tower placement controller */
    private SimplePlacementController placementController;

    /** Skin for the tower title label */
    private Skin hotbarSkin;

    @Override
    public void create() {
        super.create();
        placementController = entity.getComponent(SimplePlacementController.class);

        // Create hotbar skin for the title
        hotbarSkin = createHotbarSkin();

        // Root container table
        rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.bottom().left();

        // Brown background
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0.6f, 0.3f, 0.0f, 1f));
        pixmap.fill();
        Texture bgTexture = new Texture(pixmap);
        pixmap.dispose();
        Drawable backgroundDrawable = new TextureRegionDrawable(new TextureRegion(bgTexture));

        // Container
        Container<Table> container = new Container<>();
        container.setBackground(backgroundDrawable);
        container.pad(0);

        // Title
        Label title = new Label("TOWERS", hotbarSkin);
        title.setAlignment(Align.center);

        // Images
        TextureRegionDrawable boneImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/boneicon.png")));
        TextureRegionDrawable dinoImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/dinoicon.png")));
        TextureRegionDrawable cavemenImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/campfireicon.png")));
        TextureRegionDrawable placeholderImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/placeholder.png")));

        // Create 4 buttons
        ImageButton boneBtn = new ImageButton(boneImage);
        ImageButton dinoBtn = new ImageButton(dinoImage);
        ImageButton cavemenBtn = new ImageButton(cavemenImage);
        ImageButton placeholderBtn = new ImageButton(placeholderImage);

        // Assign listeners
        boneBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                handlePlacement("startPlacementBone");
            }
        });

        dinoBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                handlePlacement("startPlacementDino");
            }
        });

        cavemenBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                handlePlacement("startPlacementCavemen");
            }
        });

        placeholderBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                handlePlacement("startPlacementPterodactyl"); // placeholder action
            }
        });

        // Button grid (3 columns)
        Table buttonTable = new Table();
        buttonTable.defaults().pad(0).center();

        ImageButton[] buttons = {boneBtn, dinoBtn, cavemenBtn, placeholderBtn};
        for (int i = 0; i < buttons.length; i++) {
            buttonTable.add(buttons[i]).size(148, 150);
            if ((i + 1) % 3 == 0) {
                buttonTable.row();
            }
        }

        // Combine title + buttons
        Table content = new Table();
        content.add(title).colspan(3).center().padBottom(0).row();
        content.add(buttonTable).colspan(3).center();

        container.setActor(content);
        rootTable.add(container);
        stage.addActor(rootTable);

        // Input multiplexer
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage);
        if (Gdx.input.getInputProcessor() != null) multiplexer.addProcessor(Gdx.input.getInputProcessor());
        multiplexer.addProcessor(new InputAdapter() {});
        Gdx.input.setInputProcessor(multiplexer);
    }

    private Skin createHotbarSkin() {
        Skin skin = new Skin();
        BitmapFont font = new BitmapFont();
        font.getData().setScale(2f);
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = font;
        labelStyle.fontColor = Color.WHITE;
        skin.add("default", labelStyle);
        return skin;
    }

    private void handlePlacement(String eventName) {
        if (placementController != null && placementController.isPlacementActive()) {
            placementController.cancelPlacement();
            return;
        }
        entity.getEvents().trigger(eventName);
    }

    @Override
    public void draw(SpriteBatch batch) { }

    @Override
    public void dispose() {
        rootTable.clear();
        super.dispose();
    }
}
