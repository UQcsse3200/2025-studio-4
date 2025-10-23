package com.csse3200.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
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

import java.util.ArrayList;
import java.util.List;

public class DefeatScreen implements Screen {
    private static final Logger logger = LoggerFactory.getLogger(DefeatScreen.class);
    
    private final GdxGame game;
    private Stage stage;
    private SpriteBatch batch;
    private float timeElapsed = 0f;
    private Skin skin;
    private String currentMapId;
    
    private enum DefeatStage {
        ANIMATION_PLAYING,
        SCROLLING_TEXT,
        DEFEAT_DISPLAY
    }
    
    private DefeatStage currentStage;
    
    private List<Texture> animationFrames;
    private int currentFrame = 0;
    private float frameTime = 0f;
    private static final float FRAME_DURATION = 1.0f / 15.0f;
    private boolean animationFinished = false;
    
    private Label scrollLabel;
    private BitmapFont scrollFont;
    private float scrollSpeed = 50f;
    private float scrollStartY;
    private float scrollCenterY;
    private boolean scrollTextFinished = false;
    private boolean scrollTextCentered = false;
    
    private int clickCount = 0;
    private float lastClickTime = 0f;
    private float clickTimeout = 0.5f;
    
    private Image backgroundImage;
    private TextButton mainMenuButton;
    private TextButton restartButton;
    private Table mainTable;
    private Table buttonTable;
    
    private boolean buttonsShown = false;
    
    public DefeatScreen(GdxGame game) {
        this(game, null);
    }
    
    public DefeatScreen(GdxGame game, String mapId) {
        this.game = game;
        this.currentMapId = mapId;
        initializeServices();
        setupDefeatScreen();
        submitCurrentScore();
    }
    
    private void initializeServices() {
        if (ServiceLocator.getResourceService() == null) {
            ServiceLocator.registerResourceService(new ResourceService());
        }
        if (ServiceLocator.getEntityService() == null) {
            ServiceLocator.registerEntityService(new com.csse3200.game.entities.EntityService());
        }
    }
    
    private void setupDefeatScreen() {
        stage = new Stage(new ScreenViewport());
        batch = new SpriteBatch();
        
        try {
            skin = new Skin(Gdx.files.internal("flat-earth/skin/flat-earth-ui.json"));
            logger.info("Successfully loaded flat-earth skin for defeat screen");
        } catch (Exception e) {
            logger.warn("Could not load flat-earth skin, using default skin: " + e.getMessage());
            skin = new Skin();
            BitmapFont defaultFont = new BitmapFont();
            skin.add("default", defaultFont);
            skin.add("font", defaultFont);
            skin.add("title", defaultFont);
            
            Label.LabelStyle defaultLabelStyle = new Label.LabelStyle();
            defaultLabelStyle.font = defaultFont;
            skin.add("default", defaultLabelStyle);
            skin.add("title", defaultLabelStyle);
            
            TextButton.TextButtonStyle defaultButtonStyle = new TextButton.TextButtonStyle();
            defaultButtonStyle.font = defaultFont;
            skin.add("default", defaultButtonStyle);
        }
        
        loadAssets();
        
        if (currentMapId == null && Gdx.files.internal("images/Map1_Defeat").exists()) {
            currentStage = DefeatStage.ANIMATION_PLAYING;
            loadAnimationFrames();
        } else {
            currentStage = DefeatStage.SCROLLING_TEXT;
            createScrollingText();
        }
    }
    
    private void loadAssets() {
        ResourceService resourceService = ServiceLocator.getResourceService();
        if (resourceService == null) {
            logger.error("ResourceService is null, cannot load assets");
            return;
        }
        
        String[] textures = {
            "images/Defeat.jpg",
            "images/Main_Menu_Button_Background.png",
            "images/Main_Game_Button.png"
        };
        
        try {
            resourceService.loadTextures(textures);
            resourceService.loadAll();
        } catch (Exception e) {
            logger.error("Failed to load defeat screen assets", e);
        }
    }
    
    private void loadAnimationFrames() {
        animationFrames = new ArrayList<>();
        
        try {
            logger.info("Loading Map1 defeat animation frames");
            
            for (int i = 0; i <= 100; i++) {
                String framePath = "images/Map1_Defeat/Map1_Defeat_" + i + ".png";
                
                if (Gdx.files.internal(framePath).exists()) {
                    Texture frame = new Texture(Gdx.files.internal(framePath));
                    animationFrames.add(frame);
                } else {
                    if (i == 0) {
                        logger.warn("No animation frames found, skipping to scrolling text");
                        skipAnimationToScrollingText();
                        return;
                    }
                    break;
                }
            }
            
            if (animationFrames.isEmpty()) {
                logger.warn("No animation frames loaded, skipping to scrolling text");
                skipAnimationToScrollingText();
            } else {
                logger.info("Loaded " + animationFrames.size() + " animation frames");
            }
            
        } catch (Exception e) {
            logger.error("Error loading animation frames: " + e.getMessage());
            skipAnimationToScrollingText();
        }
    }
    
    private void updateAnimation(float delta) {
        if (animationFrames == null || animationFrames.isEmpty()) {
            skipAnimationToScrollingText();
            return;
        }
        
        frameTime += delta;
        
        if (frameTime >= FRAME_DURATION) {
            frameTime = 0f;
            currentFrame++;
            
            if (currentFrame >= animationFrames.size()) {
                currentFrame = animationFrames.size() - 1;
                if (!animationFinished) {
                    animationFinished = true;
                    logger.info("Animation finished, transitioning to scrolling text");
                    Gdx.app.postRunnable(this::skipAnimationToScrollingText);
                }
            }
        }
        
        if (Gdx.input.justTouched()) {
            logger.info("Click detected, skipping animation");
            skipAnimationToScrollingText();
        }
    }
    
    private void skipAnimationToScrollingText() {
        if (animationFrames != null) {
            for (Texture frame : animationFrames) {
                frame.dispose();
            }
            animationFrames.clear();
            animationFrames = null;
        }
        currentStage = DefeatStage.SCROLLING_TEXT;
        timeElapsed = 0f;
        createScrollingText();
    }
    
    private void createScrollingText() {
        String scrollText = currentMapId == null ? 
            "DEFEAT: The frost reclaims everything.\n" +
            "Your magic fades beneath the endless blizzard.\n" +
            "The machines rise again, their eyes burning like frozen stars.\n\n" +
            "The light fades.\n\n" +
            "The snow covers the ruins, burying both man and machine alike.\n" +
            "In the silence, only the wind remembers your name." :
            "DEFEAT: The city falls into the void.\n" +
            "Your last spell fades before the flood of machines.\n" +
            "Cold light devours the sky.\n\n" +
            "Humanity's flame flickers… and vanishes into the endless hum of circuits.\n\n" +
            "Yet somewhere, deep beneath the ruins, a faint heartbeat still remains.";
        
        scrollFont = new BitmapFont();
        scrollFont.getData().setScale(1.6f);
        
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = scrollFont;
        labelStyle.fontColor = Color.RED;
        
        scrollLabel = new Label(scrollText, labelStyle);
        scrollLabel.setAlignment(Align.center);
        scrollLabel.setWrap(true);
        scrollLabel.setWidth(Gdx.graphics.getWidth() * 0.8f);
        
        scrollStartY = -scrollLabel.getPrefHeight() - 100f;
        scrollCenterY = (Gdx.graphics.getHeight() - scrollLabel.getPrefHeight()) / 2f;
        scrollLabel.setPosition(
            (Gdx.graphics.getWidth() - scrollLabel.getWidth()) / 2f,
            scrollStartY
        );
        
        stage.addActor(scrollLabel);
        
        Label hintLabel = new Label("Double-click to skip", labelStyle);
        hintLabel.setFontScale(0.8f);
        hintLabel.setColor(new Color(1f, 1f, 1f, 0.6f));
        hintLabel.setPosition(
            (Gdx.graphics.getWidth() - hintLabel.getPrefWidth()) / 2f,
            50f
        );
        stage.addActor(hintLabel);
        hintLabel.addAction(Actions.sequence(
            Actions.fadeIn(1f),
            Actions.delay(2f),
            Actions.fadeOut(1f),
            Actions.removeActor()
        ));
    }
    
    private void updateScrollingText(float delta) {
        handleSkipInput(delta);
        
        if (scrollLabel != null && !scrollTextFinished && !scrollTextCentered) {
            float currentY = scrollLabel.getY();
            float newY = currentY + scrollSpeed * delta;
            
            if (newY >= scrollCenterY) {
                scrollLabel.setY(scrollCenterY);
                scrollTextCentered = true;
                logger.info("Scrolling text centered, waiting 3 seconds before continuing");
                scheduleTransition();
            } else {
                scrollLabel.setY(newY);
            }
        }
    }
    
    private void handleSkipInput(float delta) {
        if (Gdx.input.justTouched()) {
            float currentTime = timeElapsed;
            
            if (currentTime - lastClickTime < clickTimeout) {
                clickCount++;
                if (clickCount >= 2) {
                    logger.info("Double click detected, skipping scrolling text");
                    skipToDefeatDisplay();
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
    
    private void scheduleTransition() {
        scrollLabel.addAction(Actions.sequence(
            Actions.delay(8f),
            Actions.run(() -> {
                logger.info("Transitioning to defeat display after centered pause");
                skipToDefeatDisplay();
            })
        ));
    }
    
    private void skipToDefeatDisplay() {
        if (scrollTextFinished) return;
        scrollTextFinished = true;
        if (scrollLabel != null) {
            scrollLabel.remove();
        }
        currentStage = DefeatStage.DEFEAT_DISPLAY;
        createDefeatDisplay();
    }
    
    private void createDefeatDisplay() {
        timeElapsed = 0f;
        
        ResourceService resourceService = ServiceLocator.getResourceService();
        if (resourceService != null) {
            backgroundImage = new Image(resourceService
                .getAsset("images/Defeat.jpg", Texture.class));
            backgroundImage.setFillParent(true);
            backgroundImage.addAction(Actions.alpha(0f));
            stage.addActor(backgroundImage);
            backgroundImage.addAction(Actions.fadeIn(1f));
        }
        
        mainTable = new Table();
        mainTable.setFillParent(true);
        stage.addActor(mainTable);
        
        buttonTable = new Table();
        buttonTable.center();
        mainTable.add(buttonTable).expandX().center();
        
        createButtons();
    }
    
    private void createButtons() {
        restartButton = new TextButton("Restart", createButtonStyle());
        restartButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                logger.info("Restart button clicked");
                game.setScreen(GdxGame.ScreenType.MAIN_GAME, false, currentMapId);
            }
        });
        restartButton.addAction(Actions.alpha(0f));
        
        mainMenuButton = new TextButton("Main Menu", createButtonStyle());
        mainMenuButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                logger.info("Main Menu button clicked");
                game.setScreen(GdxGame.ScreenType.MAIN_MENU);
            }
        });
        mainMenuButton.addAction(Actions.alpha(0f));
        
        buttonTable.add(restartButton).size(200f, 60f).pad(15f);
        buttonTable.add(mainMenuButton).size(200f, 60f).pad(15f);
    }
    
    private TextButton.TextButtonStyle createButtonStyle() {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        
        try {
            style.font = skin.getFont("font");
        } catch (Exception e) {
            style.font = skin.getFont("default");
            if (style.font == null) {
                style.font = new BitmapFont();
            }
        }
        
        ResourceService resourceService = ServiceLocator.getResourceService();
        if (resourceService != null) {
            Texture buttonTexture = resourceService
                .getAsset("images/Main_Game_Button.png", Texture.class);
            style.up = new TextureRegionDrawable(buttonTexture);
            style.down = new TextureRegionDrawable(buttonTexture);
            style.over = new TextureRegionDrawable(buttonTexture);
        }
        
        style.fontColor = Color.BLUE;
        style.downFontColor = new Color(0.0f, 0.0f, 0.8f, 1.0f);
        style.overFontColor = new Color(0.2f, 0.2f, 1.0f, 1.0f);
        
        return style;
    }
    
    @Override
    public void show() {
        logger.info("Showing defeat screen");
        Gdx.input.setInputProcessor(stage);
        submitCurrentScore();
    }
    
    @Override
    public void render(float delta) {
        timeElapsed += delta;
        ScreenUtils.clear(0, 0, 0, 1);
        
        if (currentStage == DefeatStage.ANIMATION_PLAYING) {
            updateAnimation(delta);
            if (animationFrames != null && !animationFrames.isEmpty() && currentFrame < animationFrames.size()) {
                batch.begin();
                batch.draw(animationFrames.get(currentFrame), 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
                batch.end();
            }
        } else if (currentStage == DefeatStage.SCROLLING_TEXT) {
            updateScrollingText(delta);
        } else if (currentStage == DefeatStage.DEFEAT_DISPLAY) {
            updateDefeatAnimation(delta);
        }
        
        stage.act(delta);
        stage.draw();
    }
    
    private void updateDefeatAnimation(float delta) {
        if (timeElapsed >= 2.0f && !buttonsShown) {
            restartButton.addAction(Actions.fadeIn(2.0f));
            mainMenuButton.addAction(Actions.fadeIn(2.0f));
            buttonsShown = true;
        }
    }
    
    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        
        if (scrollLabel != null) {
            scrollLabel.setWidth(width * 0.8f);
            scrollLabel.setX((width - scrollLabel.getWidth()) / 2f);
        }
    }
    
    @Override
    public void pause() {
        logger.info("Defeat screen paused");
    }
    
    @Override
    public void resume() {
        logger.info("Defeat screen resumed");
    }
    
    @Override
    public void hide() {
        logger.info("Defeat screen hidden");
    }
    
    @Override
    public void dispose() {
        logger.debug("Disposing defeat screen");
        if (animationFrames != null) {
            for (Texture frame : animationFrames) {
                frame.dispose();
            }
            animationFrames.clear();
        }
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
    
    private void submitCurrentScore() {
        try {
            com.csse3200.game.services.GameSessionManager sessionManager = 
                ServiceLocator.getGameSessionManager();
            
            if (sessionManager == null) {
                logger.error("Game session manager not available");
                return;
            }
            
            boolean submitted = sessionManager.submitScoreIfNotSubmitted(false);
            
            if (submitted) {
                logger.info("Successfully submitted defeat score to leaderboard");
            } else {
                logger.info("Score already submitted for this session, skipping duplicate submission");
            }
            
        } catch (Exception e) {
            logger.error("Failed to submit defeat score to leaderboard", e);
        }
    }
}
