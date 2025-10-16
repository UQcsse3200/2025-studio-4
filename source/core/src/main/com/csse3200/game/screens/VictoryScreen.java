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

public class VictoryScreen implements Screen {
    private static final Logger logger = LoggerFactory.getLogger(VictoryScreen.class);
    
    private final GdxGame game;
    private Stage stage;
    private SpriteBatch batch;
    private float timeElapsed = 0f;
    private Skin skin;
    private String currentMapId;
    
    private enum VictoryStage {
        SCROLLING_TEXT,
        VICTORY_DISPLAY
    }
    
    private VictoryStage currentStage = VictoryStage.SCROLLING_TEXT;
    
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
    private TextButton nextMapButton;
    private TextButton exitGameButton;
    private Table mainTable;
    private Table buttonTable;
    
    private boolean buttonsShown = false;
    
    public VictoryScreen(GdxGame game) {
        this(game, null);
    }
    
    public VictoryScreen(GdxGame game, String mapId) {
        this.game = game;
        this.currentMapId = mapId;
        initializeServices();
        setupVictoryScreen();
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
    
    private void setupVictoryScreen() {
        stage = new Stage(new ScreenViewport());
        batch = new SpriteBatch();
        
        try {
            skin = new Skin(Gdx.files.internal("flat-earth/skin/flat-earth-ui.json"));
            logger.info("Successfully loaded flat-earth skin for victory screen");
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
        createScrollingText();
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
    
    private void createScrollingText() {
        String mapName = getMapName();
        String scrollText = currentMapId == null ? 
            "VICTORY!\n\n" +
            "Congratulations!\n\n" +
            "You have successfully defended the " + mapName + "!\n" +
            "Your strategic prowess has held the first line of defense.\n\n" +
            "Commander, your defense was exemplary!\n" +
            "The corrupted machines have been pushed back.\n\n" +
            "But the war is not over...\n" +
            "The enemy still approaches from another sector!\n\n" +
            "The enemy regroups with greater force.\n" +
            "Prepare yourself, Commander.\n" +
            "Your next battle awaits!" :
            "VICTORY!\n\n" +
            "Congratulations!\n\n" +
            "Outstanding, Commander!\n" +
            "You have defended both strongholds and secured victory!\n\n" +
            "You have successfully defended both sectors\n" +
            "against the relentless AI assault!\n\n" +
            "The corrupted AI forces have been defeated,\n" +
            "and humanity's future is once again secured.\n\n" +
            "Their mechanical armies lie in ruins.\n" +
            "You are a true hero of the resistance.\n" +
            "History will remember your valor!";
        
        scrollFont = new BitmapFont();
        scrollFont.getData().setScale(1.6f);
        
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = scrollFont;
        labelStyle.fontColor = Color.WHITE;
        
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
                    skipToVictoryDisplay();
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
            Actions.delay(3f),
            Actions.run(() -> {
                logger.info("Transitioning to victory display after centered pause");
                skipToVictoryDisplay();
            })
        ));
    }
    
    private void skipToVictoryDisplay() {
        if (scrollTextFinished) return;
        scrollTextFinished = true;
        if (scrollLabel != null) {
            scrollLabel.remove();
        }
        currentStage = VictoryStage.VICTORY_DISPLAY;
        createVictoryDisplay();
    }
    
    private void createVictoryDisplay() {
        timeElapsed = 0f;
        
        ResourceService resourceService = ServiceLocator.getResourceService();
        if (resourceService != null) {
            backgroundImage = new Image(resourceService
                .getAsset("images/Game_Victory.png", Texture.class));
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
        mainMenuButton = new TextButton("Main Menu", createButtonStyle());
        mainMenuButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                logger.info("Main Menu button clicked");
                game.setScreen(GdxGame.ScreenType.MAIN_MENU);
            }
        });
        mainMenuButton.addAction(Actions.alpha(0f));
        
        if (currentMapId == null) {
            nextMapButton = new TextButton("Next Map", createButtonStyle());
            nextMapButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent changeEvent, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                    logger.info("Next Map button clicked - transitioning to Map Two");
                    game.setScreen(GdxGame.ScreenType.MAIN_GAME, false, "MapTwo");
                }
            });
            nextMapButton.addAction(Actions.alpha(0f));
        }
        
        exitGameButton = new TextButton("Exit Game", createButtonStyle());
        exitGameButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                logger.info("Exit Game button clicked");
                Gdx.app.exit();
            }
        });
        exitGameButton.addAction(Actions.alpha(0f));
        
        buttonTable.add(mainMenuButton).size(200f, 60f).pad(15f);
        if (currentMapId == null) {
            buttonTable.add(nextMapButton).size(200f, 60f).pad(15f);
        }
        buttonTable.add(exitGameButton).size(200f, 60f).pad(15f);
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
        logger.info("Showing victory screen");
        Gdx.input.setInputProcessor(stage);
        submitCurrentScore();
    }
    
    @Override
    public void render(float delta) {
        timeElapsed += delta;
        ScreenUtils.clear(0, 0, 0, 1);
        
        if (currentStage == VictoryStage.SCROLLING_TEXT) {
            updateScrollingText(delta);
        } else if (currentStage == VictoryStage.VICTORY_DISPLAY) {
            updateVictoryAnimation(delta);
        }
        
        stage.act(delta);
        stage.draw();
    }
    
    private void updateVictoryAnimation(float delta) {
        if (timeElapsed >= 0.5f && !buttonsShown) {
            mainMenuButton.addAction(Actions.fadeIn(1.5f));
            if (nextMapButton != null) {
                nextMapButton.addAction(Actions.fadeIn(1.5f));
            }
            exitGameButton.addAction(Actions.fadeIn(1.5f));
            buttonsShown = true;
        }
    }
    
    private String getMapName() {
        if (currentMapId == null) {
            return "Forest Demo Sector";
        } else if ("MapTwo".equalsIgnoreCase(currentMapId)) {
            return "Map Two Sector";
        }
        return "Unknown Sector";
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
            
            boolean submitted = sessionManager.submitScoreIfNotSubmitted(true);
            
            if (submitted) {
                logger.info("Successfully submitted victory score to leaderboard");
            } else {
                logger.info("Score already submitted for this session, skipping duplicate submission");
            }
            
        } catch (Exception e) {
            logger.error("Failed to submit victory score to leaderboard", e);
        }
    }
    
}
