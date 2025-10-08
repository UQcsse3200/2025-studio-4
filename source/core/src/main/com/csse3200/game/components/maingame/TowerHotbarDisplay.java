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
 * Towers are arranged in a grid with a pixel-art style title at the top.
 * Each button triggers the placement of its corresponding tower type.
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

        // Root table anchored bottom-left
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

        Container<Table> container = new Container<>();
        container.setBackground(backgroundDrawable);
        container.pad(6f);

        Label title = new Label("TOWERS", skin, "title");
        title.setAlignment(Align.center);

        // Load tower icons
        TextureRegionDrawable boneImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/towers/boneicon.png")));
        TextureRegionDrawable dinoImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/towers/dinoicon.png")));
        TextureRegionDrawable cavemenImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/towers/campfireicon.png")));
        TextureRegionDrawable superCavemenImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/towers/supercavemenicon.png")));
        TextureRegionDrawable placeholderImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/placeholder.png")));
        TextureRegionDrawable pteroImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/towers/pterodactylicon.png")));
        TextureRegionDrawable totemImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/towers/totemicon.png")));
        TextureRegionDrawable bankImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/towers/bank_tower.png")));

        // Create buttons
        ImageButton boneBtn = new ImageButton(boneImage);
        ImageButton dinoBtn = new ImageButton(dinoImage);
        ImageButton cavemenBtn = new ImageButton(cavemenImage);
        ImageButton pteroBtn = new ImageButton(pteroImage); // pterodactyl slot
        ImageButton totemBtn = new ImageButton(totemImage); // totem slot
        ImageButton bankBtn = new ImageButton(bankImage); // bank slot
        ImageButton superCavemenBtn = new ImageButton(superCavemenImage);
        ImageButton placeholderBtn = new ImageButton(placeholderImage);

        // Only include allowed towers in the hotbar
        ImageButton[] allButtons = {
                boneBtn, dinoBtn, cavemenBtn, pteroBtn, totemBtn, bankBtn, superCavemenBtn, placeholderBtn
        };

        // Add listeners for all tower types
        addPlacementListener(boneBtn, "bone");
        addPlacementListener(dinoBtn, "dino");
        addPlacementListener(cavemenBtn, "cavemen");
        addPlacementListener(pteroBtn, "pterodactyl");
        addPlacementListener(totemBtn, "totem");
        addPlacementListener(superCavemenBtn, "supercavemen");
        addPlacementListener(bankBtn, "bank");

        // Button grid
        Table buttonTable = new Table();
        final float BUTTON_W = 132.5f;
        final float BUTTON_H = 140f;
        final float BUTTON_PAD = 6f;
        buttonTable.defaults().pad(BUTTON_PAD).center();

        for (int i = 0; i < allButtons.length; i++) {
            buttonTable.add(allButtons[i]).size(BUTTON_W, BUTTON_H);
            if ((i + 1) % 3 == 0) buttonTable.row();
        }

        // Add title and grid to container
        Table content = new Table();
        content.add(title).colspan(3).center().padBottom(6f).row();
        content.add(buttonTable).colspan(3).center();

        container.setActor(content);
        rootTable.add(container);
        stage.addActor(rootTable);

        // Input multiplexer setup
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage);
        if (Gdx.input.getInputProcessor() != null) {
            multiplexer.addProcessor(Gdx.input.getInputProcessor());
        }
        multiplexer.addProcessor(new InputAdapter() {});
        Gdx.input.setInputProcessor(multiplexer);
    }

    /** Adds placement logic for each button */
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

    /** Helper to capitalize tower type for event name */
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
    public void draw(SpriteBatch batch) { }

    @Override
    public void dispose() {
        rootTable.clear();
        super.dispose();
    }
}
