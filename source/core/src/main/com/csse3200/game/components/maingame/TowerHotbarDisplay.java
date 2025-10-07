package com.csse3200.game.components.maingame;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ui.UIComponent;
import com.csse3200.game.components.maingame.SimplePlacementController;

import javax.swing.event.ChangeEvent;

/**
 * UI component that displays a hotbar for selecting and placing towers.
 * <p>
 * Provides buttons for different tower types (Bone, Dino, Cavemen). When a button
 * is clicked, the corresponding placement event is triggered, or the current
 * placement is cancelled if one is active.
 * </p>
 */
public class TowerHotbarDisplay extends UIComponent {
    /** The root table holding the tower buttons */
    private Table table;
    /** Reference to the tower placement controller */
    private SimplePlacementController placementController;

    /**
     * Creates the hotbar UI with tower selection buttons.
     * <p>
     * Each button triggers an event that tells {@link SimplePlacementController}
     * to begin placement of the corresponding tower type. If a placement is already
     * active, clicking a button will cancel it instead.
     * </p>
     */
    @Override
    public void create() {
        super.create();
        placementController = entity.getComponent(SimplePlacementController.class);
        table = new Table();
        table.bottom().left();
        // Remove setFillParent(true);

        // --- Create brown background for hotbar only ---
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(0.6f, 0.3f, 0.0f, 1f); // Brown color RGBA
        pixmap.fill();
        com.badlogic.gdx.graphics.Texture bgTexture = new com.badlogic.gdx.graphics.Texture(pixmap);
        pixmap.dispose();
        table.setBackground(new TextureRegionDrawable(new TextureRegion(bgTexture)));

        // Load button textures
        TextureRegionDrawable dinoImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/dinoicon.png")));
        TextureRegionDrawable boneImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/boneicon.png")));
        TextureRegionDrawable cavemenImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/campfireicon.png")));

        // Create buttons (same as before)
        ImageButton boneBtn = new ImageButton(boneImage);
        boneBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (placementController != null && placementController.isPlacementActive()) {
                    placementController.cancelPlacement();
                    return;
                }
                entity.getEvents().trigger("startPlacementBone");
            }
        });

        ImageButton dinoBtn = new ImageButton(dinoImage);
        dinoBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (placementController != null && placementController.isPlacementActive()) {
                    placementController.cancelPlacement();
                    return;
                }
                entity.getEvents().trigger("startPlacementDino");
            }
        });

        ImageButton cavemenBtn = new ImageButton(cavemenImage);
        cavemenBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (placementController != null && placementController.isPlacementActive()) {
                    placementController.cancelPlacement();
                    return;
                }
                entity.getEvents().trigger("startPlacementCavemen");
            }
        });

        // Set button sizes
        dinoBtn.getImageCell().size(100, 100);
        boneBtn.getImageCell().size(100, 100);
        cavemenBtn.getImageCell().size(100, 100);

        // Add buttons to table layout
        table.add(boneBtn).pad(8f);
        table.row();
        table.add(dinoBtn).pad(8f);
        table.row();
        table.add(cavemenBtn).pad(8f);

        // Pack table to fit buttons
        table.pack();

        stage.addActor(table);
    }


    /**
     * Rendering is handled by the stage, so nothing is drawn directly here.
     *
     * @param batch sprite batch (unused)
     */
    @Override
    public void draw(SpriteBatch batch) {
    }

    /**
     * Disposes of UI elements when no longer needed.
     */
    @Override
    public void dispose() {
        table.clear();
        super.dispose();
    }


}
