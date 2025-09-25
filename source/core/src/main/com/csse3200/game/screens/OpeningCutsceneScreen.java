package com.csse3200.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.csse3200.game.GdxGame;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.ui.leaderboard.MinimalSkinFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpeningCutsceneScreen implements Screen {
    private static final Logger logger = LoggerFactory.getLogger(OpeningCutsceneScreen.class);
    
    private final GdxGame game;
    private Stage stage;
    private SpriteBatch batch;
    private float timeElapsed = 0f;
    private boolean cutsceneFinished = false;
    
    // Background options
    private static final String[] BACKGROUND_OPTIONS = {
        "images/Opening_Cutscene_Screen.png",
        "images/dim_bg.jpeg",
        "images/main_menu_background.png", 
        "images/desert.png",
        "images/snow.png",
        "images/terrain_ortho.png"
    };
    private String selectedBackground = "images/Opening_Cutscene_Screen.png";
    
    // UI Elements
    private Image backgroundImage;
    private Image logoImage;
    private Table mainTable;
    
    private float logoDuration = 3f;
    private float blackScreenDelay = 1f;
    private float fadeDuration = 2f;
    
    private boolean logoShown = false;
    private boolean backgroundFaded = false;
    private boolean scrollTextStarted = false;
    private boolean scrollTextFinished = false;
    
    private Label scrollLabel;
    private float scrollSpeed = 30f;
    private float scrollStartY;
    private float scrollEndY;
    
    public OpeningCutsceneScreen(GdxGame game) {
        this(game, BACKGROUND_OPTIONS[0]);
    }
    
    public OpeningCutsceneScreen(GdxGame game, String backgroundPath) {
        this.game = game;
        this.selectedBackground = backgroundPath;
        initializeServices();
        setupCutscene();
    }
    
    /**

     * @param game
     * @param backgroundIndex
     * @return
     */
    public static OpeningCutsceneScreen withBackground(GdxGame game, int backgroundIndex) {
        if (backgroundIndex < 0 || backgroundIndex >= BACKGROUND_OPTIONS.length) {
            backgroundIndex = 0;
        }
        return new OpeningCutsceneScreen(game, BACKGROUND_OPTIONS[backgroundIndex]);
    }
    
    /**

     * @return
     */
    public static String[] getAvailableBackgrounds() {
        return BACKGROUND_OPTIONS.clone();
    }
    
    private void initializeServices() {
        if (ServiceLocator.getResourceService() == null) {
            ServiceLocator.registerResourceService(new ResourceService());
        }
        if (ServiceLocator.getRenderService() == null) {
            ServiceLocator.registerRenderService(new RenderService());
        }
    }
    
    private void setupCutscene() {
        stage = new Stage(new ScreenViewport());
        batch = new SpriteBatch();
        
        loadAssets();
        createUI();
    }
    
    private void loadAssets() {
        ResourceService resourceService = ServiceLocator.getResourceService();
        
        String[] textures = {
            selectedBackground,
            "images/logo.png",
            "images/box_boy.png"
        };
        
        resourceService.loadTextures(textures);
        resourceService.loadAll();
    }
    
    private void createUI() {
        // Background
        backgroundImage = new Image(ServiceLocator.getResourceService()
            .getAsset(selectedBackground, Texture.class));
        backgroundImage.setFillParent(true);
        stage.addActor(backgroundImage);
        
        // Main table for layout
        mainTable = new Table();
        mainTable.setFillParent(true);
        stage.addActor(mainTable);
        
        // Logo
        logoImage = new Image(ServiceLocator.getResourceService()
            .getAsset("images/logo.png", Texture.class));
        logoImage.setSize(300, 150);
        logoImage.addAction(Actions.alpha(0f));
        mainTable.add(logoImage).center().padBottom(50f);
        mainTable.row();
        
        setupScrollingText();
    }
    
    private void setupScrollingText() {
        String scrollText = "In a distant future, Earth has been conquered by a mechanical army...\n\n" +
                           "As the last resistance commander, you must establish\n" +
                           "the final defense line in the Box Forest against the invasion.\n\n" +
                           "Face the aerial threats of drone swarms, the frontal assault of grunts,\n" +
                           "the crushing force of heavy tanks, and mysterious dividers...\n\n" +
                           "Build defensive towers, deploy hero units, and use strategy and wisdom\n" +
                           "to protect your base and fight for humanity's future!";
        
        scrollLabel = new Label(scrollText, MinimalSkinFactory.create(), "default");
        scrollLabel.setAlignment(Align.center);
        scrollLabel.setColor(Color.WHITE);
        scrollLabel.setFontScale(1.8f);
        scrollLabel.setWrap(true);
        scrollLabel.setWidth(Gdx.graphics.getWidth() * 0.7f);
        
        scrollStartY = -scrollLabel.getPrefHeight() - 100f;
        scrollEndY = (Gdx.graphics.getHeight() - scrollLabel.getPrefHeight()) / 2f + 100f;
        scrollLabel.setPosition(
            (Gdx.graphics.getWidth() - scrollLabel.getWidth()) / 2f,
            scrollStartY
        );
        scrollLabel.addAction(Actions.alpha(0f));
        
        stage.addActor(scrollLabel);
    }
    
    @Override
    public void show() {
        logger.info("Showing opening cutscene");
        Gdx.input.setInputProcessor(stage);
    }
    
    @Override
    public void render(float delta) {
        timeElapsed += delta;
        
        updateAnimation(delta);
        
        // Keep background color consistent with main branch (light yellow)
        ScreenUtils.clear(248f/255f, 249f/255f, 178f/255f, 1f);
        stage.act(delta);
        stage.draw();
        
        if (cutsceneFinished) {
            transitionToMainMenu();
        }
    }
    
    private void updateAnimation(float delta) {
        if (timeElapsed >= 1f && !logoShown) {
            logoImage.addAction(Actions.fadeIn(1f));
            logoShown = true;
        }
        
        if (timeElapsed >= logoDuration + blackScreenDelay && !backgroundFaded) {
            backgroundImage.addAction(Actions.fadeOut(1f));
            logoImage.addAction(Actions.fadeOut(1f));
            backgroundFaded = true;
        }
        
        if (timeElapsed >= logoDuration + blackScreenDelay + 1f && !scrollTextStarted) {
            scrollLabel.addAction(Actions.fadeIn(1f));
            scrollTextStarted = true;
        }
        
        if (scrollTextStarted && !scrollTextFinished) {
            float currentY = scrollLabel.getY();
            float newY = currentY + scrollSpeed * delta;
            
            if (newY >= scrollEndY) {
                scrollTextFinished = true;
                scrollLabel.setY(scrollEndY);
                scrollLabel.addAction(Actions.sequence(
                    Actions.delay(3f),
                    Actions.fadeOut(1f),
                    Actions.run(() -> {
                        stage.addAction(Actions.sequence(
                            Actions.delay(0.5f),
                            Actions.fadeOut(fadeDuration),
                            Actions.run(() -> cutsceneFinished = true)
                        ));
                    })
                ));
            } else {
                scrollLabel.setY(newY);
            }
        }
    }
    
    private void transitionToMainMenu() {
        logger.info("Opening cutscene finished, transitioning to main menu");
        // Reset global clear color to light yellow before switching screens to avoid leaking black bg
        Gdx.gl.glClearColor(248f/255f, 249f/255f, 178f/255f, 1f);
        game.setScreen(GdxGame.ScreenType.MAIN_MENU);
    }
    
    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }
    
    @Override
    public void pause() {
        logger.info("Opening cutscene paused");
    }
    
    @Override
    public void resume() {
        logger.info("Opening cutscene resumed");
    }
    
    @Override
    public void hide() {
        logger.info("Opening cutscene hidden");
    }
    
    @Override
    public void dispose() {
        logger.debug("Disposing opening cutscene screen");
        if (stage != null) {
            stage.dispose();
        }
        if (batch != null) {
            batch.dispose();
        }
    }
}
