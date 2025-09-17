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
        table.setFillParent(true);

        // Load button textures
        TextureRegionDrawable dinoImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/dinoicon.png")));
        TextureRegionDrawable boneImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/boneicon.png")));
        TextureRegionDrawable cavemenImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/campfireicon.png")));

        // Bone Tower button
        ImageButton boneBtn = new ImageButton(boneImage);
        boneBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (placementController != null && placementController.isPlacementActive()) {
                    // Cancel current placement if already active
                    placementController.cancelPlacement();
                    System.out.println(">>> cancelled existing placement");
                    return;
                }

                System.out.println(">>> startPlacementBone fired");
                entity.getEvents().trigger("startPlacementBone");
            }
        });

        // Dino Tower button
        ImageButton dinoBtn = new ImageButton(dinoImage);
        dinoBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (placementController != null && placementController.isPlacementActive()) {
                    placementController.cancelPlacement();
                    System.out.println(">>> cancelled existing placement");
                    return;
                }

                System.out.println(">>> startPlacementDino fired");
                entity.getEvents().trigger("startPlacementDino");
            }
        });

        // Cavemen Tower button
        ImageButton cavemenBtn = new ImageButton(cavemenImage);
        cavemenBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (placementController != null && placementController.isPlacementActive()) {
                    placementController.cancelPlacement();
                    System.out.println(">>> cancelled existing placement");
                    return;
                }

                System.out.println(">>> startPlacementCavemen fired");
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
        stage.addActor(table);
    }

    /**
     * Rendering is handled by the stage, so nothing is drawn directly here.
     *
     * @param batch sprite batch (unused)
     */
    @Override
    public void draw(SpriteBatch batch) { /* stage draws */ }

    /**
     * Disposes of UI elements when no longer needed.
     */
    @Override
    public void dispose() {
        table.clear();
        super.dispose();
    }


}
