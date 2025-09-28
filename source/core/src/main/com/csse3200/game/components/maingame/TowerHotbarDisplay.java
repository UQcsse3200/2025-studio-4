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
 * Towers are arranged in a 3x3 grid with a bold/pixel-art style title at the top.
 * Each button triggers an event to start placement of the corresponding tower type.
 * If a placement is already active, clicking a button cancels the current placement.
 * </p>
 */
public class TowerHotbarDisplay extends UIComponent {

    /** Root table containing the hotbar UI */
    private Table rootTable;

    /** Reference to the tower placement controller */
    private SimplePlacementController placementController;

    /** Skin for the tower title label */
    private Skin hotbarSkin;

    /**
     * Creates the hotbar UI with a 3x3 grid layout for tower buttons.
     * Adds a brown background and a bold "TOWERS" title at the top.
     */
    @Override
    public void create() {
        super.create();
        placementController = entity.getComponent(SimplePlacementController.class);

        // Create hotbar skin for the title
        hotbarSkin = createHotbarSkin();

        // Root container table (padding removed)
        rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.bottom().left(); // no pad

        // Brown background for the container
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0.6f, 0.3f, 0.0f, 1f));
        pixmap.fill();
        Texture bgTexture = new Texture(pixmap);
        pixmap.dispose();
        Drawable backgroundDrawable = new TextureRegionDrawable(new TextureRegion(bgTexture));

        // Container for hotbar content (padding removed)
        Container<Table> container = new Container<>();
        container.setBackground(backgroundDrawable);
        container.pad(0); // remove padding

        // Title label using the skin
        Label title = new Label("TOWERS", hotbarSkin);
        title.setAlignment(Align.center);

        // Tower buttons (same as before)
        TextureRegionDrawable boneImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/boneicon.png")));
        TextureRegionDrawable dinoImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/dinoicon.png")));
        TextureRegionDrawable cavemenImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/campfireicon.png")));

        ImageButton boneBtn = new ImageButton(boneImage);
        boneBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                handlePlacement("startPlacementBone");
            }
        });

        ImageButton dinoBtn = new ImageButton(dinoImage);
        dinoBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                handlePlacement("startPlacementDino");
            }
        });

        ImageButton cavemenBtn = new ImageButton(cavemenImage);
        cavemenBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                handlePlacement("startPlacementCavemen");
            }
        });

        // Button layout in 3x3 grid
        Table buttonTable = new Table();
        buttonTable.defaults().pad(0).center(); // removed padding here too

        // First row: actual tower buttons
        buttonTable.add(boneBtn).size(149, 150);
        buttonTable.add(dinoBtn).size(149, 150);
        buttonTable.add(cavemenBtn).size(149, 150);
        buttonTable.row();

        // Remaining rows: empty cells
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 3; j++) {
                buttonTable.add().size(100, 100); // no pad
            }
            buttonTable.row();
        }

        // Combine title and button grid
        Table content = new Table();
        content.add(title).colspan(3).center().padBottom(0).row(); // removed padding
        content.add(buttonTable);

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