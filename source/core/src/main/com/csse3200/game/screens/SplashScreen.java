package com.csse3200.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.csse3200.game.GdxGame;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;

public class SplashScreen implements Screen {
    private final GdxGame game;
    private Stage stage;
    private SpriteBatch batch;
    private float timeElapsed = 0f;
    
    private enum SplashStage {
        LOGO_FADE_IN,
        LOGO_DISPLAY,
        LOGO_FADE_OUT,
        MAIN_SCREEN
    }
    
    private SplashStage currentStage = SplashStage.LOGO_FADE_IN;
    
    private Image logoImage;
    private Image backgroundImage;
    private TextButton startButton;
    private BitmapFont buttonFont;
    
    private static final float LOGO_FADE_IN_DURATION = 1.5f;
    private static final float LOGO_DISPLAY_DURATION = 2.0f;
    private static final float LOGO_FADE_OUT_DURATION = 1.0f;
    
    public SplashScreen(GdxGame game) {
        this.game = game;
        initializeServices();
        setupSplash();
    }
    
    private void initializeServices() {
        if (ServiceLocator.getResourceService() == null) {
            ServiceLocator.registerResourceService(new ResourceService());
        }
    }
    
    private void setupSplash() {
        stage = new Stage(new ScreenViewport());
        batch = new SpriteBatch();
        
        loadAssets();
        createLogoStage();
    }
    
    private void loadAssets() {
        ResourceService resourceService = ServiceLocator.getResourceService();
        
        String[] textures = {
            "images/logo.png",
            "images/main_menu_background.png",
            "images/Main_Game_Button.png"
        };
        
        resourceService.loadTextures(textures);
        resourceService.loadAll();
    }
    
    private void createLogoStage() {
        Texture blackTexture = new Texture(Gdx.files.internal("images/dim_bg.jpeg"));
        Image blackBackground = new Image(blackTexture);
        blackBackground.setFillParent(true);
        blackBackground.setColor(0, 0, 0, 1);
        stage.addActor(blackBackground);
        
        logoImage = new Image(ServiceLocator.getResourceService()
            .getAsset("images/logo.png", Texture.class));
        
        float logoWidth = 400f;
        float logoHeight = 200f;
        logoImage.setSize(logoWidth, logoHeight);
        logoImage.setPosition(
            (Gdx.graphics.getWidth() - logoWidth) / 2f,
            (Gdx.graphics.getHeight() - logoHeight) / 2f
        );
        logoImage.setColor(1, 1, 1, 0);
        
        stage.addActor(logoImage);
        
        logoImage.addAction(Actions.fadeIn(LOGO_FADE_IN_DURATION));
    }
    
    private void createMainScreen() {
        stage.clear();
        
        backgroundImage = new Image(ServiceLocator.getResourceService()
            .getAsset("images/main_menu_background.png", Texture.class));
        backgroundImage.setFillParent(true);
        backgroundImage.setColor(1, 1, 1, 0);
        stage.addActor(backgroundImage);
        
        backgroundImage.addAction(Actions.fadeIn(1.0f));
        
        buttonFont = new BitmapFont();
        buttonFont.getData().setScale(2.0f);
        
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = buttonFont;
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.downFontColor = Color.GRAY;
        buttonStyle.overFontColor = Color.YELLOW;
        
        startButton = new TextButton("Click to Start!", buttonStyle);
        startButton.setSize(300f, 80f);
        startButton.setPosition(
            (Gdx.graphics.getWidth() - 300f) / 2f,
            Gdx.graphics.getHeight() * 0.3f
        );
        
        startButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(GdxGame.ScreenType.MAIN_MENU);
            }
        });
        
        startButton.setColor(1, 1, 1, 0);
        startButton.addAction(Actions.sequence(
            Actions.delay(0.5f),
            Actions.fadeIn(1.0f),
            Actions.forever(Actions.sequence(
                Actions.scaleTo(1.1f, 1.1f, 0.5f),
                Actions.scaleTo(1.0f, 1.0f, 0.5f)
            ))
        ));
        
        stage.addActor(startButton);
        
        Table hintTable = new Table();
        hintTable.setFillParent(true);
        hintTable.bottom().padBottom(30f);
        
        TextButton.TextButtonStyle hintStyle = new TextButton.TextButtonStyle();
        BitmapFont hintFont = new BitmapFont();
        hintFont.getData().setScale(1.2f);
        hintStyle.font = hintFont;
        hintStyle.fontColor = new Color(1, 1, 1, 0.7f);
        
        TextButton hintLabel = new TextButton("Press any key or click anywhere to continue...", hintStyle);
        hintLabel.setColor(1, 1, 1, 0);
        hintLabel.addAction(Actions.sequence(
            Actions.delay(1.5f),
            Actions.fadeIn(1.0f),
            Actions.forever(Actions.sequence(
                Actions.alpha(0.4f, 1.0f),
                Actions.alpha(0.9f, 1.0f)
            ))
        ));
        
        hintTable.add(hintLabel);
        stage.addActor(hintTable);
    }
    
    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }
    
    @Override
    public void render(float delta) {
        timeElapsed += delta;
        
        handleInput();
        updateStage(delta);
        
        ScreenUtils.clear(0, 0, 0, 1);
        stage.act(delta);
        stage.draw();
    }
    
    private void handleInput() {
        if (currentStage == SplashStage.MAIN_SCREEN) {
            if (Gdx.input.justTouched() && !isClickOnButton()) {
                game.setScreen(GdxGame.ScreenType.MAIN_MENU);
            }
            
            if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ANY_KEY)) {
                game.setScreen(GdxGame.ScreenType.MAIN_MENU);
            }
        }
    }
    
    private boolean isClickOnButton() {
        if (startButton == null) return false;
        
        float x = Gdx.input.getX();
        float y = Gdx.graphics.getHeight() - Gdx.input.getY();
        
        return x >= startButton.getX() && x <= startButton.getX() + startButton.getWidth() &&
               y >= startButton.getY() && y <= startButton.getY() + startButton.getHeight();
    }
    
    private void updateStage(float delta) {
        switch (currentStage) {
            case LOGO_FADE_IN:
                if (timeElapsed >= LOGO_FADE_IN_DURATION) {
                    currentStage = SplashStage.LOGO_DISPLAY;
                    timeElapsed = 0f;
                }
                break;
                
            case LOGO_DISPLAY:
                if (timeElapsed >= LOGO_DISPLAY_DURATION) {
                    currentStage = SplashStage.LOGO_FADE_OUT;
                    timeElapsed = 0f;
                    logoImage.addAction(Actions.fadeOut(LOGO_FADE_OUT_DURATION));
                }
                break;
                
            case LOGO_FADE_OUT:
                if (timeElapsed >= LOGO_FADE_OUT_DURATION) {
                    currentStage = SplashStage.MAIN_SCREEN;
                    timeElapsed = 0f;
                    createMainScreen();
                }
                break;
                
            case MAIN_SCREEN:
                break;
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
        if (buttonFont != null) {
            buttonFont.dispose();
        }
    }
}

