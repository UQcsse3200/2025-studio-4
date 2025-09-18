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
    private Label titleLabel;
    private Label subtitleLabel;
    private Label storyLabel;
    private Label instructionLabel;
    private Label versionLabel;
    private Table mainTable;
    private Table textTable;
    
    // Animation timing
    private float totalDuration = 12f; // 12 seconds total
    private float logoDuration = 3f;
    private float titleDuration = 2f;
    private float storyDuration = 4f;
    private float fadeDuration = 2f;
    
    // Animation states
    private boolean logoShown = false;
    private boolean titleShown = false;
    private boolean storyShown = false;
    private boolean instructionShown = false;
    private boolean fadeStarted = false;
    
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
        
        // Text table
        textTable = new Table();
        textTable.setFillParent(true);
        textTable.top().padTop(300f);
        stage.addActor(textTable);
        
        // Title
        titleLabel = new Label("Box Forest Defense", MinimalSkinFactory.create(), "title");
        titleLabel.setAlignment(Align.center);
        titleLabel.setColor(Color.WHITE);
        titleLabel.setFontScale(3.0f);
        titleLabel.addAction(Actions.alpha(0f));
        textTable.add(titleLabel).expandX().center().padBottom(20f);
        textTable.row();
        
        // Subtitle
        subtitleLabel = new Label("A Tower Defense Adventure", MinimalSkinFactory.create(), "default");
        subtitleLabel.setAlignment(Align.center);
        subtitleLabel.setColor(Color.LIGHT_GRAY);
        subtitleLabel.setFontScale(1.5f);
        subtitleLabel.addAction(Actions.alpha(0f));
        textTable.add(subtitleLabel).expandX().center().padBottom(40f);
        textTable.row();
        
        // Story
        storyLabel = new Label("In the mystical Box Forest, ancient enemies are awakening...\n" +
                              "As the last defender, you must build towers and protect your base\n" +
                              "from waves of dangerous creatures.\n\n" +
                              "Use strategy, timing, and courage to survive!",
                              MinimalSkinFactory.create(), "default");
        storyLabel.setAlignment(Align.center);
        storyLabel.setColor(Color.WHITE);
        storyLabel.setFontScale(1.3f);
        storyLabel.addAction(Actions.alpha(0f));
        textTable.add(storyLabel).expandX().center().padBottom(30f);
        textTable.row();
        
        // Instructions
        instructionLabel = new Label("Click 'New Game' to begin your adventure!\n" +
                                   "Use WASD to move, mouse to place towers, and survive as long as possible!",
                                   MinimalSkinFactory.create(), "default");
        instructionLabel.setAlignment(Align.center);
        instructionLabel.setColor(Color.YELLOW);
        instructionLabel.setFontScale(1.4f);
        instructionLabel.addAction(Actions.alpha(0f));
        textTable.add(instructionLabel).expandX().center().padBottom(20f);
        textTable.row();
        
        // Version
        versionLabel = new Label("Version 1.0 | Made with LibGDX", MinimalSkinFactory.create(), "default");
        versionLabel.setAlignment(Align.center);
        versionLabel.setColor(Color.GRAY);
        versionLabel.setFontScale(1.0f);
        versionLabel.addAction(Actions.alpha(0f));
        textTable.add(versionLabel).expandX().center();
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
        // Logo fade in
        if (timeElapsed >= 1f && !logoShown) {
            logoImage.addAction(Actions.fadeIn(1f));
            logoShown = true;
        }
        
        // Title fade in
        if (timeElapsed >= logoDuration + 1f && !titleShown) {
            titleLabel.addAction(Actions.fadeIn(1f));
            subtitleLabel.addAction(Actions.fadeIn(1f));
            titleShown = true;
        }
        
        // Story fade in
        if (timeElapsed >= logoDuration + titleDuration + 1f && !storyShown) {
            storyLabel.addAction(Actions.fadeIn(1.5f));
            storyShown = true;
        }
        
        // Instructions fade in
        if (timeElapsed >= logoDuration + titleDuration + storyDuration + 1f && !instructionShown) {
            instructionLabel.addAction(Actions.fadeIn(1f));
            versionLabel.addAction(Actions.fadeIn(1f));
            instructionShown = true;
        }
        
        // Start fade out after all content is shown
        if (timeElapsed >= totalDuration && !fadeStarted) {
            fadeStarted = true;
            stage.addAction(Actions.sequence(
                Actions.fadeOut(fadeDuration),
                Actions.run(() -> cutsceneFinished = true)
            ));
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
