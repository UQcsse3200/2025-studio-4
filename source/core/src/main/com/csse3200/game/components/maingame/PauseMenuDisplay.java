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

public class PauseMenuDisplay extends UIComponent {
    private static final Logger logger = LoggerFactory.getLogger(PauseMenuDisplay.class);
    private static final float Z_INDEX = 100f; // above other UI
    private Texture dimTexHandle; // generated at runtime

    private final GdxGame game;
    private Table overlayTable;
    private Image dimImage;
    private Image pauseIcon;
    private boolean shown = false;

    public PauseMenuDisplay(GdxGame game) {
        this.game = game;
    }

    @Override
    public void create() {
        super.create();
        addActors();
        // listen for actions component commands
        entity.getEvents().addListener("showPauseUI", this::showOverlay);
        entity.getEvents().addListener("hidePauseUI", this::hideOverlay);
    }

    private void addActors() {
        // Dim background
        Pixmap px = new Pixmap(1, 1, Format.RGBA8888);
        px.setColor(new Color(0f, 0f, 0f, 0.55f)); // semi-transparent black
        px.fill();
        dimTexHandle = new Texture(px);
        px.dispose();
        dimImage = new Image(dimTexHandle);
        dimImage.setFillParent(true);
        dimImage.setVisible(false);
        stage.addActor(dimImage);

        // Centered pause menu
        overlayTable = new Table();
        overlayTable.setFillParent(true);
        overlayTable.setVisible(false);

        Table window = new Table(skin);
        window.defaults().pad(10f);

        Label title = new Label("Paused", skin, "title");

        // Create custom button style
        TextButtonStyle customButtonStyle = createCustomButtonStyle();

        TextButton resumeBtn = new TextButton("Resume", customButtonStyle);
        TextButton settingsBtn = new TextButton("Settings", customButtonStyle);
        TextButton rankingBtn = new TextButton("Ranking", customButtonStyle);
        TextButton quitBtn = new TextButton("Quit to Main Menu", customButtonStyle);

        resumeBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) {
                entity.getEvents().trigger("resume");
            }
        });

        settingsBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) {
                entity.getEvents().trigger("hidePauseUI");
                entity.getEvents().trigger("showSettingsOverlay");
            }
        });

        rankingBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) {
                entity.getEvents().trigger("hidePauseUI");
                entity.getEvents().trigger("showRanking");
            }
        });

        quitBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) {
                entity.getEvents().trigger("quitToMenu");
            }
        });

        window.add(title).row();
        window.add(resumeBtn).size(280f, 50f).row();
        window.add(settingsBtn).size(280f, 50f).row();
        window.add(rankingBtn).size(280f, 50f).row();
        window.add(quitBtn).size(280f, 50f).row();

        overlayTable.add(window).center();
        stage.addActor(overlayTable);

        // Small pause button (top-right)
        Texture pauseTex = ServiceLocator.getResourceService()
                .getAsset("images/pause_button.png", Texture.class);
        pauseIcon = new Image(pauseTex);
        Table topLeft = new Table();
        topLeft.setFillParent(true);
        topLeft.top().left().pad(12f);
        topLeft.add(pauseIcon).size(48f, 48f);
        stage.addActor(topLeft);

        // Toggle on click
        pauseIcon.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) {
                entity.getEvents().trigger("togglePause");
            }
        });
    }

    private void showOverlay() {
        shown = true;
        dimImage.setVisible(true);
        overlayTable.setVisible(true);
    }

    private void hideOverlay() {
        shown = false;
        dimImage.setVisible(false);
        overlayTable.setVisible(false);
    }

    @Override
    protected void draw(SpriteBatch batch) {
        // stage draws for us
    }

    @Override
    public float getZIndex() {
        return Z_INDEX;
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
    public void dispose() {
        if (overlayTable != null) overlayTable.clear();
        if (dimTexHandle != null) dimTexHandle.dispose();
        super.dispose();
    }

}

