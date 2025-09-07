package com.csse3200.game.components.maingame;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
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
    private static final float Z_INDEX = 100f; 
    private Texture dimTexHandle; 

    private final GdxGame game;
    private Table overlayTable;
    private Image dimImage;
    private Image pauseIcon;
    private boolean shown = false;
    private Label statusLabel; 

    public PauseMenuDisplay(GdxGame game) {
        this.game = game;
    }

    @Override
    public void create() {
        super.create();
        addActors();
        
        entity.getEvents().addListener("showPauseUI", this::showOverlay);
        entity.getEvents().addListener("hidePauseUI", this::hideOverlay);
        entity.getEvents().addListener("showSaveSuccess", this::showSaveSuccess);
    }

    private void addActors() {
        
        Pixmap px = new Pixmap(1, 1, Format.RGBA8888);
        px.setColor(new Color(0f, 0f, 0f, 0.55f)); 
        px.fill();
        dimTexHandle = new Texture(px);
        px.dispose();
        dimImage = new Image(dimTexHandle);
        dimImage.setFillParent(true);
        dimImage.setVisible(false);
        stage.addActor(dimImage);

        
        overlayTable = new Table();
        overlayTable.setFillParent(true);
        overlayTable.setVisible(false);

        Table window = new Table(skin);
        window.defaults().pad(10f);

        Label title = new Label("Paused", skin, "title");

        
        statusLabel = new Label("", skin);
        statusLabel.setColor(Color.GREEN);
        statusLabel.setVisible(false);

        TextButton resumeBtn = new TextButton("Resume", skin);
        TextButton settingsBtn = new TextButton("Settings", skin);
        TextButton quitBtn = new TextButton("Quit to Main Menu", skin);

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

        quitBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) {
                entity.getEvents().trigger("quitToMenu");
            }
        });

        window.add(title).row();
        window.add(statusLabel).padBottom(10f).row(); 
        window.add(resumeBtn).width(280f).row();
        window.add(settingsBtn).width(280f).row();
        window.add(quitBtn).width(280f).row();

        overlayTable.add(window).center();
        stage.addActor(overlayTable);

        
        Texture pauseTex = ServiceLocator.getResourceService()
                .getAsset("images/pause_button.png", Texture.class);
        pauseIcon = new Image(pauseTex);
        Table topLeft = new Table();
        topLeft.setFillParent(true);
        topLeft.top().left().pad(12f);
        topLeft.add(pauseIcon).size(48f, 48f);
        stage.addActor(topLeft);

        
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
        
        if (statusLabel != null) {
            statusLabel.setVisible(false);
        }
    }

    private void showSaveSuccess() {
        if (statusLabel != null) {
            statusLabel.setText("Game saved successfully!");
            statusLabel.setVisible(true);
        }
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

