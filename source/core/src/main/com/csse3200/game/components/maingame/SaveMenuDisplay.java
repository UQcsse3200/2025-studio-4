package com.csse3200.game.components.maingame;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.csse3200.game.GdxGame;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Save menu display component that shows save options and status.
 * This is separate from the pause menu to keep save and pause functionality distinct.
 */
public class SaveMenuDisplay extends UIComponent {
    private static final Logger logger = LoggerFactory.getLogger(SaveMenuDisplay.class);
    private static final float Z_INDEX = 120f; // Above pause menu but below settings

    private final GdxGame game;
    private Table overlayTable;
    private Image dimImage;
    private boolean shown = false;
    private Label statusLabel;
    private Texture dimTexHandle;

    public SaveMenuDisplay(GdxGame game) {
        this.game = game;
    }

    @Override
    public void create() {
        super.create();
        addActors();
        
        entity.getEvents().addListener("showSaveUI", this::showOverlay);
        entity.getEvents().addListener("hideSaveUI", this::hideOverlay);
        entity.getEvents().addListener("showSaveSuccess", this::showSaveSuccess);
        entity.getEvents().addListener("showSaveError", this::showSaveError);
    }

    private void addActors() {
        // Create dim background
        Pixmap px = new Pixmap(1, 1, Format.RGBA8888);
        px.setColor(new Color(0f, 0f, 0f, 0.6f)); // Semi-transparent black
        px.fill();
        dimTexHandle = new Texture(px);
        px.dispose();
        dimImage = new Image(dimTexHandle);
        dimImage.setFillParent(true);
        dimImage.setVisible(false);
        stage.addActor(dimImage);

        // Create save menu overlay
        overlayTable = new Table();
        overlayTable.setFillParent(true);
        overlayTable.setVisible(false);

        Table window = new Table(skin);
        window.defaults().pad(10f);

        Label title = new Label("Save Game", skin, "title");

        // Create custom button style
        TextButtonStyle customButtonStyle = createCustomButtonStyle();
        
        // Status label for save feedback
        statusLabel = new Label("", skin);
        statusLabel.setColor(Color.GREEN);
        statusLabel.setVisible(false);

        // Save buttons
        TextButton saveBtn = new TextButton("Save Game", customButtonStyle);
        TextButton cancelBtn = new TextButton("Cancel", customButtonStyle);

        // Button listeners
        saveBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) {
                entity.getEvents().trigger("performSave");
            }
        });

        // Save As removed per requirement

        cancelBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) {
                entity.getEvents().trigger("hideSaveUI");
            }
        });

        // Layout
        window.add(title).row();
        window.add(statusLabel).padBottom(10f).row();
        window.add(saveBtn).size(280f, 50f).row();
        window.add(cancelBtn).size(280f, 50f).row();

        overlayTable.add(window).center();
        stage.addActor(overlayTable);
    }

    private void showOverlay() {
        shown = true;
        dimImage.setVisible(true);
        overlayTable.setVisible(true);
        // Clear any previous status
        if (statusLabel != null) {
            statusLabel.setVisible(false);
        }
    }

    private void hideOverlay() {
        shown = false;
        dimImage.setVisible(false);
        overlayTable.setVisible(false);
        
        if (statusLabel != null) {
            statusLabel.setVisible(false);
        }
    }

    private void showSaveSuccess() {
        if (statusLabel != null) {
            statusLabel.setText("Game saved successfully!");
            statusLabel.setColor(Color.GREEN);
            statusLabel.setVisible(true);
        }
    }

    private void showSaveError() {
        if (statusLabel != null) {
            statusLabel.setText("Save failed! Please try again.");
            statusLabel.setColor(Color.RED);
            statusLabel.setVisible(true);
        }
    }

    /**
     * Creates custom button style using button background image
     */
    private TextButtonStyle createCustomButtonStyle() {
        TextButtonStyle style = new TextButtonStyle();
        
        // Use Segoe UI font
        style.font = skin.getFont("segoe_ui");
        
        // Load button background image
        Texture buttonTexture = ServiceLocator.getResourceService()
            .getAsset("images/Main_Game_Button.png", Texture.class);
        TextureRegion buttonRegion = new TextureRegion(buttonTexture);
        
        // Create NinePatch for scalable button background
        NinePatch buttonPatch = new NinePatch(buttonRegion, 10, 10, 10, 10);
        
        // Create pressed state NinePatch (slightly darker)
        NinePatch pressedPatch = new NinePatch(buttonRegion, 10, 10, 10, 10);
        pressedPatch.setColor(new Color(0.8f, 0.8f, 0.8f, 1f));
        
        // Create hover state NinePatch (slightly brighter)
        NinePatch hoverPatch = new NinePatch(buttonRegion, 10, 10, 10, 10);
        hoverPatch.setColor(new Color(1.1f, 1.1f, 1.1f, 1f));
        
        // Set button states
        style.up = new NinePatchDrawable(buttonPatch);
        style.down = new NinePatchDrawable(pressedPatch);
        style.over = new NinePatchDrawable(hoverPatch);
        
        style.fontColor = Color.CYAN;
        style.downFontColor = new Color(0.0f, 0.6f, 0.8f, 1.0f);
        style.overFontColor = new Color(0.2f, 0.8f, 1.0f, 1.0f);
        
        return style;
    }

    @Override
    protected void draw(SpriteBatch batch) {
        // stage draws for us
    }

    @Override
    public float getZIndex() {
        return Z_INDEX;
    }

    @Override
    public void dispose() {
        if (overlayTable != null) overlayTable.clear();
        if (dimTexHandle != null) dimTexHandle.dispose();
        super.dispose();
    }
}
