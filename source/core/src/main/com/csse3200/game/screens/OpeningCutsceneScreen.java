package com.csse3200.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
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
    
    // Animation timing
    private float logoDuration = 3f;
    private float blackScreenDelay = 1f; // 1 second after logo
    private float fadeDuration = 2f;
    
    // Animation states
    private boolean logoShown = false;
    private boolean backgroundFaded = false;
    private boolean scrollTextStarted = false;
    private boolean scrollTextFinished = false;
    
    // Scrolling text
    private Label scrollLabel;
    private float scrollSpeed = 30f; // pixels per second - slower for better reading
    private float scrollStartY;
    private float scrollEndY;
    
    // Skip functionality
    private int clickCount = 0;
    private float lastClickTime = 0f;
    private float clickTimeout = 1f; // 1 second timeout between clicks
    private Label skipLabel;
    private boolean skipPressed = false;
    
    // Fonts for cleanup
    private BitmapFont scrollFont;
    private BitmapFont skipFont;
    
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
        logoImage.addAction(Actions.alpha(0f)); // Start invisible
        mainTable.add(logoImage).center().padBottom(50f);
        mainTable.row();
        
        // Scrolling text setup
        setupScrollingText();
    }
    
    private void setupScrollingText() {
        // Create scrolling text label
        String scrollText = "In a distant future, Earth has been conquered by a mechanical army...\n\n" +
                           "As the last resistance commander, you must establish the final defense line\n" +
                           "in the Box Forest against the invasion of the mechanical forces.\n\n" +
                           "Face the aerial threats of drone swarms, the frontal assault of grunts,\n" +
                           "the crushing force of heavy tanks, and mysterious dividers...\n\n" +
                           "Build defensive towers, deploy hero units, and use strategy and wisdom\n" +
                           "to protect your base and fight for humanity's future!";
        
        // Create a custom font for better rendering
        scrollFont = new BitmapFont();
        scrollFont.getData().setScale(1.8f);
        
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = scrollFont;
        labelStyle.fontColor = Color.WHITE;
        
        scrollLabel = new Label(scrollText, labelStyle);
        scrollLabel.setAlignment(Align.center);
        scrollLabel.setWrap(true);
        scrollLabel.setWidth(Gdx.graphics.getWidth() * 0.7f);
        
        // Position off-screen at the bottom initially
        scrollStartY = -scrollLabel.getPrefHeight() - 100f;
        scrollEndY = Gdx.graphics.getHeight() - 50f; // Stop at top of screen
        scrollLabel.setPosition(
            (Gdx.graphics.getWidth() - scrollLabel.getWidth()) / 2f,
            scrollStartY
        );
        scrollLabel.addAction(Actions.alpha(0f)); // Start invisible
        
        stage.addActor(scrollLabel);
        
        // Setup skip hint
        setupSkipHint();
    }
    
    private void setupSkipHint() {
        // Create a custom font for skip hint
        skipFont = new BitmapFont();
        skipFont.getData().setScale(1.2f);
        
        Label.LabelStyle skipStyle = new Label.LabelStyle();
        skipStyle.font = skipFont;
        skipStyle.fontColor = Color.GRAY;
        
        skipLabel = new Label("Double Click to Skip", skipStyle);
        skipLabel.setAlignment(Align.center);
        skipLabel.setPosition(
            (Gdx.graphics.getWidth() - skipLabel.getPrefWidth()) / 2f,
            20f
        );
        skipLabel.addAction(Actions.alpha(0f)); // Start invisible
        stage.addActor(skipLabel);
    }
    
    @Override
    public void show() {
        logger.info("Showing opening cutscene");
        Gdx.input.setInputProcessor(stage);
    }
    
    @Override
    public void render(float delta) {
        timeElapsed += delta;
        
        // Handle skip input BEFORE stage processing
        handleSkipInput(delta);
        
        updateAnimation(delta);
        
        ScreenUtils.clear(0, 0, 0, 1);
        
        // Only process stage if not skipping
        if (!cutsceneFinished) {
            stage.act(delta);
            stage.draw();
        }
        
        if (cutsceneFinished) {
            ScreenUtils.clear(248f/255f, 249f/255f, 178f/255f, 1f);
            transitionToMainMenu();
        }
    }
    
    private void handleSkipInput(float delta) {
        // Check for left mouse button click
        boolean leftClick = Gdx.input.isButtonJustPressed(Input.Buttons.LEFT);
        
        if (leftClick) {
            float currentTime = timeElapsed;
            
            // Check if this is the second click within timeout
            if (clickCount == 1 && (currentTime - lastClickTime) <= clickTimeout) {
                // Second click within timeout - skip!
                cutsceneFinished = true;
                logger.info("Cutscene skipped by double click");
                return;
            } else if (clickCount == 0 || (currentTime - lastClickTime) > clickTimeout) {
                // First click or timeout expired - start counting
                clickCount = 1;
                lastClickTime = currentTime;
                logger.info("First click detected, waiting for second click...");
            }
        }
        
        // Reset click count if timeout exceeded
        if (clickCount > 0 && (timeElapsed - lastClickTime) > clickTimeout) {
            clickCount = 0;
            logger.info("Click timeout, resetting click count");
        }
    }
    
    private void updateAnimation(float delta) {
        // Logo fade in
        if (timeElapsed >= 1f && !logoShown) {
            logoImage.addAction(Actions.fadeIn(1f));
            logoShown = true;
        }
        
        // Fade background and logo to black after logo is shown
        if (timeElapsed >= logoDuration + blackScreenDelay && !backgroundFaded) {
            backgroundImage.addAction(Actions.fadeOut(1f));
            logoImage.addAction(Actions.fadeOut(1f));
            backgroundFaded = true;
        }
        
        // Start scrolling text after background fades
        if (timeElapsed >= logoDuration + blackScreenDelay + 1f && !scrollTextStarted) {
            scrollLabel.addAction(Actions.fadeIn(1f));
            scrollTextStarted = true;
            // Show skip hint when text starts
            skipLabel.addAction(Actions.fadeIn(1f));
        }
        
        // Update scrolling text position
        if (scrollTextStarted && !scrollTextFinished) {
            float currentY = scrollLabel.getY();
            float newY = currentY + scrollSpeed * delta;
            
            if (newY >= scrollEndY) {
                scrollTextFinished = true;
                // Fade out the scrolling text when it reaches the top
                scrollLabel.addAction(Actions.sequence(
                    Actions.fadeOut(1f), // Fade out the text
                    Actions.run(() -> {
                        // After text fades out, fade out the entire screen
                        stage.addAction(Actions.sequence(
                            Actions.delay(0.5f), // Brief pause
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
    
    private void skipCutscene() {
        if (skipPressed) {
            return; // Prevent multiple skip calls
        }
        skipPressed = true;
        logger.info("Opening cutscene skipped by user");
        
        // Immediately fade out and transition
        stage.addAction(Actions.sequence(
            Actions.fadeOut(0.5f),
            Actions.run(() -> cutsceneFinished = true)
        ));
    }
    
    private void transitionToMainMenu() {
        logger.info("Opening cutscene finished, transitioning to main menu");
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
        // 恢复InputService作为输入处理器
        if (ServiceLocator.getInputService() != null) {
            Gdx.input.setInputProcessor(ServiceLocator.getInputService());
            logger.info("Restored InputService as input processor");
        }
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
        if (scrollFont != null) {
            scrollFont.dispose();
        }
        if (skipFont != null) {
            skipFont.dispose();
        }
    }
}
