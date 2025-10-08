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

        // use shared skin
        hotbarSkin = skin;

        // Root table anchored bottom-left
        rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.bottom().left();

        // Brown background for container
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0.6f, 0.3f, 0.0f, 1f));
        pixmap.fill();
        Texture bgTexture = new Texture(pixmap);
        pixmap.dispose();
        Drawable backgroundDrawable = new TextureRegionDrawable(new TextureRegion(bgTexture));

        Container<Table> container = new Container<>();
        container.setBackground(backgroundDrawable);
        container.pad(6f);

        // Title label using shared "title" style if present
        Label title = new Label("TOWERS", skin, "title");
        title.setAlignment(Align.center);

        // Images
        TextureRegionDrawable boneImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/towers/boneicon.png")));
        TextureRegionDrawable dinoImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/towers/dinoicon.png")));
        TextureRegionDrawable cavemenImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/towers/campfireicon.png")));
        // use cavemen image as the icon for SuperCavemen slot (replace with dedicated image if available)
        TextureRegionDrawable superCavemenImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/towers/supercavemenicon.png")));
        TextureRegionDrawable placeholderImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/placeholder.png")));
        // pterodactyl icon for the second-row first-column slot
        TextureRegionDrawable pteroImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/towers/pterodactylicon.png")));
        TextureRegionDrawable totemImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/towers/totemicon.png")));
        TextureRegionDrawable bankImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/towers/bank_tower.png")));

        // Create 12 buttons (3x4 grid). First 4 are functional, rest use placeholder.
        ImageButton boneBtn = new ImageButton(boneImage);
        ImageButton dinoBtn = new ImageButton(dinoImage);
        ImageButton cavemenBtn = new ImageButton(cavemenImage);
        ImageButton pteroBtn = new ImageButton(pteroImage); // pterodactyl slot
        ImageButton totemBtn = new ImageButton(totemImage); // totem slot
        ImageButton bankBtn = new ImageButton(bankImage); // bank slot

        ImageButton[] allButtons = new ImageButton[12];
        allButtons[0] = boneBtn;
        allButtons[1] = dinoBtn;
        allButtons[2] = cavemenBtn;
        allButtons[3] = pteroBtn;
        allButtons[4] = totemBtn;
        allButtons[5] = bankBtn;
        for (int i = 6; i < allButtons.length; i++) {
            // last button (index 11) is the SuperCavemen placement button
            if (i == 11) {
                allButtons[i] = new ImageButton(superCavemenImage);
            } else {
                allButtons[i] = new ImageButton(placeholderImage);
            }
        }

        // Assign listeners for the first four
        boneBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (placementController != null) {
                    if (placementController.isPlacementActive()) {
                        if ("bone".equalsIgnoreCase(placementController.getPendingType())) {
                            placementController.cancelPlacement();
                            return;
                        } else {
                            placementController.cancelPlacement();
                        }
                    }
                    placementController.requestPlacement("bone");
                } else handlePlacement("startPlacementBone");
            }
        });
        dinoBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (placementController != null) {
                    if (placementController.isPlacementActive()) {
                        if ("dino".equalsIgnoreCase(placementController.getPendingType())) {
                            placementController.cancelPlacement();
                            return;
                        } else {
                            placementController.cancelPlacement();
                        }
                    }
                    placementController.requestPlacement("dino");
                } else handlePlacement("startPlacementDino");
            }
        });
        cavemenBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (placementController != null) {
                    if (placementController.isPlacementActive()) {
                        if ("cavemen".equalsIgnoreCase(placementController.getPendingType())) {
                            placementController.cancelPlacement();
                            return;
                        } else {
                            placementController.cancelPlacement();
                        }
                    }
                    placementController.requestPlacement("cavemen");
                } else handlePlacement("startPlacementCavemen");
            }
        });
        pteroBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (placementController != null) {
                    if (placementController.isPlacementActive()) {
                        if ("pterodactyl".equalsIgnoreCase(placementController.getPendingType())) {
                            placementController.cancelPlacement();
                            return;
                        } else {
                            placementController.cancelPlacement();
                        }
                    }
                    placementController.requestPlacement("pterodactyl");
                } else handlePlacement("startPlacementPterodactyl");
            }
        });
        totemBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (placementController != null) {
                    if (placementController.isPlacementActive()) {
                        if ("totem".equalsIgnoreCase(placementController.getPendingType())) {
                            placementController.cancelPlacement();
                            return;
                        } else {
                            placementController.cancelPlacement();
                        }
                    }
                    placementController.requestPlacement("totem");
                } else handlePlacement("startPlacementTotem");
            }
        });
        bankBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (placementController != null) {
                    if (placementController.isPlacementActive()) {
                        if ("bank".equalsIgnoreCase(placementController.getPendingType())) {
                            placementController.cancelPlacement();
                            return;
                        } else {
                            placementController.cancelPlacement();
                        }
                    }
                    placementController.requestPlacement("bank");
                } else handlePlacement("startPlacementBank");
            }
        });
        allButtons[11].addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (placementController != null) {
                    if (placementController.isPlacementActive()) {
                        if ("supercavemen".equalsIgnoreCase(placementController.getPendingType())) {
                            placementController.cancelPlacement();
                            return;
                        } else {
                            placementController.cancelPlacement();
                        }
                    }
                    placementController.requestPlacement("supercavemen");
                } else handlePlacement("startPlacementSuperCavemen");
            }
        });

        // Button grid (3 columns x 4 rows) with modest padding so visuals aren't cramped
        Table buttonTable = new Table();
        final float BUTTON_W = 132.5f;
        final float BUTTON_H = 140f;
        final float BUTTON_PAD = 6f;
        buttonTable.defaults().pad(BUTTON_PAD).center();

        for (int i = 0; i < allButtons.length; i++) {
            buttonTable.add(allButtons[i]).size(BUTTON_W, BUTTON_H);
            if ((i + 1) % 3 == 0) buttonTable.row();
        }

        // Content: title + button grid
        Table content = new Table();
        content.add(title).colspan(3).center().padBottom(6f).row();
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

    private void handlePlacement(String eventName) {
        if (placementController != null && placementController.isPlacementActive()) {
            placementController.cancelPlacement();
            return;
        }
        // fallback: trigger on our UI entity (legacy)
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
