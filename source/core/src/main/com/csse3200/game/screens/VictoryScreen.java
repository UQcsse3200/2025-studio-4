package com.csse3200.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.csse3200.game.GdxGame;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.services.ResourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VictoryScreen implements Screen {
    private static final Logger logger = LoggerFactory.getLogger(VictoryScreen.class);
    
    private final GdxGame game;
    private Stage stage;
    private SpriteBatch batch;
    private float timeElapsed = 0f;
    private Skin skin;
    
    // UI Elements
    private Image backgroundImage;
    private Label titleLabel;
    private Label subtitleLabel;
    private Label messageLabel;
    private TextButton mainMenuButton;
    private TextButton playAgainButton;
    private TextButton exitGameButton;
    private Table mainTable;
    private Table buttonTable;
    
    // Animation timing
    private float fadeInDuration = 2f;
    private float buttonDelay = 3f;
    
    // Animation states
    private boolean titleShown = false;
    private boolean messageShown = false;
    private boolean buttonsShown = false;
    
    public VictoryScreen(GdxGame game) {
        this.game = game;
        initializeServices();
        setupVictoryScreen();
    }
    
    private void initializeServices() {
        if (ServiceLocator.getResourceService() == null) {
            ServiceLocator.registerResourceService(new ResourceService());
        }
    }
    
    private void setupVictoryScreen() {
        stage = new Stage(new ScreenViewport());
        batch = new SpriteBatch();
        
        // Initialize skin
        skin = new Skin(Gdx.files.internal("flat-earth/skin/flat-earth-ui.json"));
        
        loadAssets();
        createUI();
    }
    
    private void loadAssets() {
        ResourceService resourceService = ServiceLocator.getResourceService();
        if (resourceService == null) {
            logger.error("ResourceService is null, cannot load assets");
            return;
        }
        
        String[] textures = {
            "images/Game_Victory.png",
            "images/Main_Menu_Button_Background.png",
            "images/Main_Game_Button.png"
        };
        
        try {
            resourceService.loadTextures(textures);
            resourceService.loadAll();
        } catch (Exception e) {
            logger.error("Failed to load victory screen assets", e);
        }
    }
    
    private void createUI() {
        // Background
        ResourceService resourceService = ServiceLocator.getResourceService();
        if (resourceService != null) {
            backgroundImage = new Image(resourceService
                .getAsset("images/Game_Victory.png", Texture.class));
            backgroundImage.setFillParent(true);
            stage.addActor(backgroundImage);
        }
        
        // Main table for layout
        mainTable = new Table();
        mainTable.setFillParent(true);
        stage.addActor(mainTable);
        
        // Victory title
        titleLabel = new Label("VICTORY!", skin, "title");
        titleLabel.setAlignment(Align.center);
        titleLabel.setColor(Color.GOLD);
        titleLabel.setFontScale(4.0f);
        titleLabel.addAction(Actions.alpha(0f));
        mainTable.add(titleLabel).expandX().center().padBottom(20f);
        mainTable.row();
        
        // Subtitle
        subtitleLabel = new Label("Congratulations!", skin, "default");
        subtitleLabel.setAlignment(Align.center);
        subtitleLabel.setColor(Color.WHITE);
        subtitleLabel.setFontScale(2.0f);
        subtitleLabel.addAction(Actions.alpha(0f));
        mainTable.add(subtitleLabel).expandX().center().padBottom(40f);
        mainTable.row();
        
        // Victory message
        messageLabel = new Label("You have successfully defended the Box Forest!\n" +
                               "Your strategic prowess and courage have saved the realm.\n\n" +
                               "The ancient enemies have been vanquished,\n" +
                               "and peace has been restored to the mystical lands.",
                               skin, "default");
        messageLabel.setAlignment(Align.center);
        messageLabel.setColor(Color.LIGHT_GRAY);
        messageLabel.setFontScale(1.4f);
        messageLabel.addAction(Actions.alpha(0f));
        mainTable.add(messageLabel).expandX().center().padBottom(50f);
        mainTable.row();
        
        // Button table
        buttonTable = new Table();
        buttonTable.center();
        mainTable.add(buttonTable).expandX().center();
        
        // Create buttons
        createButtons();
    }
    
    private void createButtons() {
        // Main Menu button
        mainMenuButton = new TextButton("Main Menu", createButtonStyle());
        mainMenuButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                logger.info("Main Menu button clicked");
                game.setScreen(GdxGame.ScreenType.MAIN_MENU);
            }
        });
        mainMenuButton.addAction(Actions.alpha(0f));
        
        // Play Again button
        playAgainButton = new TextButton("Play Again", createButtonStyle());
        playAgainButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                logger.info("Play Again button clicked");
                game.setScreen(GdxGame.ScreenType.MAIN_GAME);
            }
        });
        playAgainButton.addAction(Actions.alpha(0f));
        
        // Exit Game button
        exitGameButton = new TextButton("Exit Game", createButtonStyle());
        exitGameButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                logger.info("Exit Game button clicked");
                Gdx.app.exit();
            }
        });
        exitGameButton.addAction(Actions.alpha(0f));
        
        // Add buttons to table
        buttonTable.add(mainMenuButton).size(200f, 60f).pad(15f);
        buttonTable.add(playAgainButton).size(200f, 60f).pad(15f);
        buttonTable.add(exitGameButton).size(200f, 60f).pad(15f);
    }
    
    private TextButton.TextButtonStyle createButtonStyle() {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        
        // Use skin font
        style.font = skin.getFont("font");
        
        // Load button background
        ResourceService resourceService = ServiceLocator.getResourceService();
        if (resourceService != null) {
            Texture buttonTexture = resourceService
                .getAsset("images/Main_Game_Button.png", Texture.class);
            style.up = new TextureRegionDrawable(buttonTexture);
            style.down = new TextureRegionDrawable(buttonTexture);
            style.over = new TextureRegionDrawable(buttonTexture);
        }
        
        // Set font colors
        style.fontColor = Color.BLUE;
        style.downFontColor = new Color(0.0f, 0.0f, 0.8f, 1.0f);
        style.overFontColor = new Color(0.2f, 0.2f, 1.0f, 1.0f);
        
        return style;
    }
    
    @Override
    public void show() {
        logger.info("Showing victory screen");
        Gdx.input.setInputProcessor(stage);
    }
    
    @Override
    public void render(float delta) {
        timeElapsed += delta;
        
        updateAnimation(delta);
        
        ScreenUtils.clear(0, 0, 0, 1);
        stage.act(delta);
        stage.draw();
    }
    
    private void updateAnimation(float delta) {
        // Title fade in
        if (timeElapsed >= 1f && !titleShown) {
            titleLabel.addAction(Actions.fadeIn(fadeInDuration));
            subtitleLabel.addAction(Actions.fadeIn(fadeInDuration));
            titleShown = true;
        }
        
        // Message fade in
        if (timeElapsed >= 2f && !messageShown) {
            messageLabel.addAction(Actions.fadeIn(fadeInDuration));
            messageShown = true;
        }
        
        // Buttons fade in
        if (timeElapsed >= buttonDelay && !buttonsShown) {
            mainMenuButton.addAction(Actions.fadeIn(1f));
            playAgainButton.addAction(Actions.fadeIn(1f));
            exitGameButton.addAction(Actions.fadeIn(1f));
            buttonsShown = true;
        }
    }
    
    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }
    
    @Override
    public void pause() {
        logger.info("Victory screen paused");
    }
    
    @Override
    public void resume() {
        logger.info("Victory screen resumed");
    }
    
    @Override
    public void hide() {
        logger.info("Victory screen hidden");
    }
    
    @Override
    public void dispose() {
        logger.debug("Disposing victory screen");
        if (stage != null) {
            stage.dispose();
        }
        if (batch != null) {
            batch.dispose();
        }
    }
}
