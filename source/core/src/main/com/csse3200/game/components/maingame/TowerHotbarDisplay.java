package com.csse3200.game.components.maingame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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
 * Dynamically resizes, tightly packed, and fits neatly beside the map.
 */
public class TowerHotbarDisplay extends UIComponent {

    private Table rootTable;
    private SimplePlacementController placementController;
    private Skin hotbarSkin;

    @Override
    public void create() {
        super.create();
        placementController = entity.getComponent(SimplePlacementController.class);
        hotbarSkin = skin;

        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();

        // Root table bottom-left
        rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.bottom().left();

        // Background
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0.6f, 0.3f, 0.0f, 1f));
        pixmap.fill();
        Texture bgTexture = new Texture(pixmap);
        pixmap.dispose();
        Drawable backgroundDrawable = new TextureRegionDrawable(new TextureRegion(bgTexture));

        Container<Table> container = new Container<>();
        container.setBackground(backgroundDrawable);
        container.pad(screenWidth * 0.0025f); // minimal border padding

        Label title = new Label("TOWERS", skin, "title");
        title.setAlignment(Align.center);

        // Load tower icons
        TextureRegionDrawable boneImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/towers/boneicon.png")));
        TextureRegionDrawable dinoImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/towers/dinoicon.png")));
        TextureRegionDrawable cavemenImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/towers/cavemenicon.png")));
        TextureRegionDrawable superCavemenImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/towers/supercavemenicon.png")));
        TextureRegionDrawable placeholderImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/placeholder.png")));
        TextureRegionDrawable pteroImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/towers/pterodactylicon.png")));
        TextureRegionDrawable totemImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/towers/totemicon.png")));
        TextureRegionDrawable bankImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/towers/bankicon.png")));
        TextureRegionDrawable raftImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/towers/rafticon.png")));
        TextureRegionDrawable frozenmamoothskullImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/towers/mamoothskullicon.png")));
        TextureRegionDrawable bouldercatapultImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/towers/bouldercatapulticon.png")));
        TextureRegionDrawable villageshamanImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/towers/villageshamanicon.png")));

        // Buttons
        ImageButton boneBtn = new ImageButton(boneImage);
        ImageButton dinoBtn = new ImageButton(dinoImage);
        ImageButton cavemenBtn = new ImageButton(cavemenImage);
        ImageButton pteroBtn = new ImageButton(pteroImage);
        ImageButton totemBtn = new ImageButton(totemImage);
        ImageButton bankBtn = new ImageButton(bankImage);
        ImageButton raftBtn = new ImageButton(raftImage);
        ImageButton frozenmamoothskullBtn = new ImageButton(frozenmamoothskullImage);
        ImageButton bouldercatapultBtn = new ImageButton(bouldercatapultImage);
        ImageButton villageshamanBtn = new ImageButton(villageshamanImage);
        ImageButton superCavemenBtn = new ImageButton(superCavemenImage);
        ImageButton placeholderBtn = new ImageButton(placeholderImage);

        ImageButton[] allButtons = {
                boneBtn, dinoBtn, cavemenBtn, pteroBtn, totemBtn, bankBtn,
                raftBtn, frozenmamoothskullBtn, bouldercatapultBtn, villageshamanBtn,
                superCavemenBtn, placeholderBtn
        };

        addPlacementListener(boneBtn, "bone");
        addPlacementListener(dinoBtn, "dino");
        addPlacementListener(cavemenBtn, "cavemen");
        addPlacementListener(pteroBtn, "pterodactyl");
        addPlacementListener(totemBtn, "totem");
        addPlacementListener(bankBtn, "bank");
        addPlacementListener(raftBtn, "raft");
        addPlacementListener(frozenmamoothskullBtn, "frozenmamoothskull");
        addPlacementListener(bouldercatapultBtn, "bouldercatapult");
        addPlacementListener(villageshamanBtn, "villageshaman");
        addPlacementListener(superCavemenBtn, "supercavemen");

        // Grid with almost no gaps
        Table buttonTable = new Table();
        float BUTTON_W = screenWidth * 0.07f;    // slightly narrower
        float BUTTON_H = screenHeight * 0.10f;   // consistent height
        float BUTTON_PAD = screenWidth * 0.0015f; // very tight gap
        buttonTable.defaults().pad(BUTTON_PAD).center();

        for (int i = 0; i < allButtons.length; i++) {
            buttonTable.add(allButtons[i]).size(BUTTON_W, BUTTON_H);
            if ((i + 1) % 3 == 0) buttonTable.row();
        }

        // Layout
        Table content = new Table();
        content.add(title).colspan(3).center().padBottom(screenHeight * 0.006f).row();

        ScrollPane scrollPane = new ScrollPane(buttonTable, skin);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setFadeScrollBars(false);
        content.add(scrollPane).colspan(3).center().expand().fill();

        container.setActor(content);

        // Slightly narrower width for perfect map fit
        rootTable.add(container).width(screenWidth * 0.232f);
        stage.addActor(rootTable);

        // Input setup
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage);
        if (Gdx.input.getInputProcessor() != null) {
            multiplexer.addProcessor(Gdx.input.getInputProcessor());
        }
        multiplexer.addProcessor(new InputAdapter() {});
        Gdx.input.setInputProcessor(multiplexer);
    }

    private void addPlacementListener(ImageButton button, String towerType) {
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (placementController != null) {
                    if (placementController.isPlacementActive()) {
                        if (towerType.equalsIgnoreCase(placementController.getPendingType())) {
                            placementController.cancelPlacement();
                            return;
                        } else {
                            placementController.cancelPlacement();
                        }
                    }
                    placementController.requestPlacement(towerType);
                } else {
                    handlePlacement("startPlacement" + capitalize(towerType));
                }
            }
        });
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private void handlePlacement(String eventName) {
        if (placementController != null && placementController.isPlacementActive()) {
            placementController.cancelPlacement();
            return;
        }
        entity.getEvents().trigger(eventName);
    }

    @Override
    public void draw(SpriteBatch batch) {}

    @Override
    public void dispose() {
        rootTable.clear();
        super.dispose();
    }
}
