package com.csse3200.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.csse3200.game.GdxGame;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;

public class GenericCutsceneScreen implements Screen {
    protected final GdxGame game;
    protected Stage stage;
    protected SpriteBatch batch;
    protected float timeElapsed = 0f;
    protected boolean cutsceneFinished = false;
    
    protected Image backgroundImage;
    protected Label scrollLabel;
    protected BitmapFont scrollFont;
    
    protected float scrollSpeed = 50f;
    protected float scrollStartY;
    protected float scrollEndY;
    protected boolean scrollTextFinished = false;
    
    protected int clickCount = 0;
    protected float lastClickTime = 0f;
    protected float clickTimeout = 0.5f;
    
    protected String backgroundPath;
    protected String cutsceneText;
    protected GdxGame.ScreenType nextScreen;
    protected String nextScreenArg;
    protected boolean isContinue = false;
    
    public GenericCutsceneScreen(GdxGame game, String backgroundPath, String cutsceneText, 
                                GdxGame.ScreenType nextScreen, String nextScreenArg, boolean isContinue) {
        this.game = game;
        this.backgroundPath = backgroundPath;
        this.cutsceneText = cutsceneText;
        this.nextScreen = nextScreen;
        this.nextScreenArg = nextScreenArg;
        this.isContinue = isContinue;
        
        initializeServices();
        setupCutscene();
    }
    
    public GenericCutsceneScreen(GdxGame game, String backgroundPath, String cutsceneText, 
                                GdxGame.ScreenType nextScreen) {
        this(game, backgroundPath, cutsceneText, nextScreen, null, false);
    }
    
    protected void initializeServices() {
        if (ServiceLocator.getResourceService() == null) {
            ServiceLocator.registerResourceService(new ResourceService());
        }
    }
    
    protected void setupCutscene() {
        stage = new Stage(new ScreenViewport());
        batch = new SpriteBatch();
        
        loadAssets();
        createUI();
    }
    
    protected void loadAssets() {
        ResourceService resourceService = ServiceLocator.getResourceService();
        String[] textures = {backgroundPath};
        resourceService.loadTextures(textures);
        resourceService.loadAll();
    }
    
    protected void createUI() {
        backgroundImage = new Image(ServiceLocator.getResourceService()
            .getAsset(backgroundPath, Texture.class));
        backgroundImage.setFillParent(true);
        stage.addActor(backgroundImage);
        
        setupScrollingText();
    }
    
    protected void setupScrollingText() {
        scrollFont = new BitmapFont();
        scrollFont.getData().setScale(1.6f);
        
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = scrollFont;
        labelStyle.fontColor = Color.WHITE;
        
        scrollLabel = new Label(cutsceneText, labelStyle);
        scrollLabel.setAlignment(Align.center);
        scrollLabel.setWrap(true);
        scrollLabel.setWidth(Gdx.graphics.getWidth() * 0.8f);
        
        scrollStartY = -scrollLabel.getPrefHeight() - 100f;
        scrollEndY = Gdx.graphics.getHeight() - 50f;
        scrollLabel.setPosition(
            (Gdx.graphics.getWidth() - scrollLabel.getWidth()) / 2f,
            scrollStartY
        );
        scrollLabel.addAction(Actions.fadeIn(1f));
        
        stage.addActor(scrollLabel);
        
        Label hintLabel = new Label("Double Click to Skip", labelStyle);
        hintLabel.setFontScale(0.8f);
        hintLabel.setColor(new Color(1f, 1f, 1f, 0.6f));
        hintLabel.setPosition(
            (Gdx.graphics.getWidth() - hintLabel.getPrefWidth()) / 2f,
            30f
        );
        stage.addActor(hintLabel);
    }
    
    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }
    
    @Override
    public void render(float delta) {
        timeElapsed += delta;
        
        handleSkipInput(delta);
        updateScrollingText(delta);
        
        ScreenUtils.clear(0, 0, 0, 1);
        stage.act(delta);
        stage.draw();
        
        if (cutsceneFinished) {
            transitionToNextScreen();
        }
    }
    
    protected void handleSkipInput(float delta) {
        if (Gdx.input.justTouched()) {
            float currentTime = timeElapsed;
            
            if (currentTime - lastClickTime < clickTimeout) {
                clickCount++;
                if (clickCount >= 2) {
                    cutsceneFinished = true;
                }
            } else {
                clickCount = 1;
            }
            lastClickTime = currentTime;
        }
        
        if (clickCount > 0 && timeElapsed - lastClickTime > clickTimeout) {
            clickCount = 0;
        }
    }
    
    protected void updateScrollingText(float delta) {
        if (scrollLabel != null && !scrollTextFinished) {
            float currentY = scrollLabel.getY();
            float newY = currentY + scrollSpeed * delta;
            
            if (newY >= scrollEndY) {
                scrollTextFinished = true;
                scrollLabel.addAction(Actions.sequence(
                    Actions.fadeOut(1f),
                    Actions.run(() -> cutsceneFinished = true)
                ));
            } else {
                scrollLabel.setY(newY);
            }
        }
    }
    
    protected void transitionToNextScreen() {
        if (nextScreen != null) {
            if (nextScreenArg != null) {
                game.setScreen(nextScreen, isContinue, nextScreenArg);
            } else {
                game.setScreen(nextScreen);
            }
        }
    }
    
    @Override
    public void resize(int width, int height) {
        if (stage != null) {
            stage.getViewport().update(width, height, true);
        }
    }
    
    @Override
    public void pause() {
    }
    
    @Override
    public void resume() {
    }
    
    @Override
    public void hide() {
    }
    
    @Override
    public void dispose() {
        if (stage != null) {
            stage.dispose();
        }
        if (batch != null) {
            batch.dispose();
        }
        if (scrollFont != null) {
            scrollFont.dispose();
        }
    }
}

