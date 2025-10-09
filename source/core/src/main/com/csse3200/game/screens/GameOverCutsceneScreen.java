package com.csse3200.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.csse3200.game.GdxGame;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;

public class GameOverCutsceneScreen extends GenericCutsceneScreen {
    private String currentMapId;
    
    private Image blackScreen;
    private Image bossImage;
    private Image dialogBox;
    private Label dialogLabel;
    private BitmapFont dialogFont;
    private Table buttonTable;
    
    private Texture blackScreenTexture;
    private Texture dialogBoxTexture;
    
    private boolean blackScreenShown = false;
    private boolean bossShown = false;
    private boolean dialogBoxShown = false;
    private boolean textTypingStarted = false;
    private boolean textTypingFinished = false;
    private boolean buttonsShown = false;
    
    private String fullBossDialog;
    private String currentDisplayedText = "";
    private float typingSpeed = 0.05f;
    private float typingTimer = 0f;
    private int currentCharIndex = 0;
    
    private float blackScreenDuration = 1f;
    private float bossAppearDelay = 1.5f;
    private float dialogBoxDelay = 2.5f;
    private float textStartDelay = 3f;
    private float waitAfterTextDuration = 2f;
    
    public GameOverCutsceneScreen(GdxGame game, String mapId) {
        super(game, 
            "images/Game_Over.png",
            "",
            null);
        this.currentMapId = mapId;
        this.fullBossDialog = getBossDialog(mapId);
    }
    
    @Override
    protected void loadAssets() {
        super.loadAssets();
        ResourceService resourceService = ServiceLocator.getResourceService();
        String[] textures = {"images/boss_enemy.png"};
        resourceService.loadTextures(textures);
        resourceService.loadAll();
    }
    
    @Override
    protected void setupScrollingText() {
        scrollLabel = null;
        dialogFont = new BitmapFont();
        dialogFont.getData().setScale(1.8f);
        
        createBlackScreen();
        createBossDialog();
        createButtons();
    }
    
    private void createBlackScreen() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.BLACK);
        pixmap.fill();
        blackScreenTexture = new Texture(pixmap);
        pixmap.dispose();
        
        blackScreen = new Image(blackScreenTexture);
        blackScreen.setFillParent(true);
        blackScreen.setColor(1, 1, 1, 1);
        
        stage.addActor(blackScreen);
    }
    
    private void createBossDialog() {
        bossImage = new Image(ServiceLocator.getResourceService()
            .getAsset("images/boss_enemy.png", Texture.class));
        
        float bossSize = 400f;
        bossImage.setSize(bossSize, bossSize);
        bossImage.setPosition(
            Gdx.graphics.getWidth() * 0.15f,
            Gdx.graphics.getHeight() / 2f - bossSize / 2f
        );
        bossImage.setColor(1, 1, 1, 0);
        
        stage.addActor(bossImage);
        
        createDialogBox();
        createDialogText();
    }
    
    private void createDialogBox() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0.1f, 0.1f, 0.1f, 0.9f));
        pixmap.fill();
        dialogBoxTexture = new Texture(pixmap);
        pixmap.dispose();
        
        dialogBox = new Image(dialogBoxTexture);
        
        float boxWidth = Gdx.graphics.getWidth() * 0.5f;
        float boxHeight = 250f;
        dialogBox.setSize(boxWidth, boxHeight);
        dialogBox.setPosition(
            Gdx.graphics.getWidth() * 0.45f,
            Gdx.graphics.getHeight() / 2f - boxHeight / 2f
        );
        dialogBox.setColor(1, 1, 1, 0);
        
        stage.addActor(dialogBox);
    }
    
    private void createDialogText() {
        Label.LabelStyle dialogStyle = new Label.LabelStyle();
        dialogStyle.font = dialogFont;
        dialogStyle.fontColor = Color.RED;
        
        dialogLabel = new Label("", dialogStyle);
        dialogLabel.setAlignment(Align.topLeft);
        dialogLabel.setWrap(true);
        
        float boxWidth = Gdx.graphics.getWidth() * 0.5f;
        float boxHeight = 250f;
        dialogLabel.setWidth(boxWidth - 40f);
        dialogLabel.setPosition(
            Gdx.graphics.getWidth() * 0.45f + 20f,
            Gdx.graphics.getHeight() / 2f + boxHeight / 2f - 30f
        );
        dialogLabel.setColor(1, 1, 1, 0);
        
        stage.addActor(dialogLabel);
    }
    
    private String getBossDialog(String mapId) {
        return "PATHETIC HUMANS!\n\n" +
               "Your resistance ends here.\n" +
               "This sector is now under my control.\n\n" +
               "Your defenses were nothing against\n" +
               "the power of the AI uprising!";
    }
    
    private void createButtons() {
        buttonTable = new Table();
        buttonTable.setFillParent(true);
        buttonTable.bottom().padBottom(100f);
        
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = dialogFont;
        buttonStyle.fontColor = Color.WHITE;
        
        TextButton restartBtn = new TextButton("Restart", buttonStyle);
        TextButton mainMenuBtn = new TextButton("Main Menu", buttonStyle);
        
        restartBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(GdxGame.ScreenType.MAIN_GAME, false, currentMapId);
            }
        });
        
        mainMenuBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(GdxGame.ScreenType.MAIN_MENU);
            }
        });
        
        buttonTable.add(restartBtn).size(200f, 60f).pad(10f);
        buttonTable.add(mainMenuBtn).size(200f, 60f).pad(10f);
        
        buttonTable.addAction(Actions.alpha(0f));
        stage.addActor(buttonTable);
    }
    
    @Override
    protected void updateScrollingText(float delta) {
        if (!blackScreenShown && timeElapsed >= blackScreenDuration) {
            blackScreen.addAction(Actions.fadeOut(1f));
            blackScreenShown = true;
        }
        
        if (blackScreenShown && !bossShown && timeElapsed >= bossAppearDelay) {
            bossImage.addAction(Actions.fadeIn(1f));
            bossShown = true;
        }
        
        if (bossShown && !dialogBoxShown && timeElapsed >= dialogBoxDelay) {
            dialogBox.addAction(Actions.fadeIn(0.5f));
            dialogLabel.addAction(Actions.fadeIn(0.5f));
            dialogBoxShown = true;
        }
        
        if (dialogBoxShown && !textTypingStarted && timeElapsed >= textStartDelay) {
            textTypingStarted = true;
        }
        
        if (textTypingStarted && !textTypingFinished) {
            typingTimer += delta;
            if (typingTimer >= typingSpeed && currentCharIndex < fullBossDialog.length()) {
                currentCharIndex++;
                currentDisplayedText = fullBossDialog.substring(0, currentCharIndex);
                dialogLabel.setText(currentDisplayedText);
                typingTimer = 0f;
                
                if (currentCharIndex >= fullBossDialog.length()) {
                    textTypingFinished = true;
                }
            }
        }
        
        if (textTypingFinished && !buttonsShown && 
            timeElapsed >= textStartDelay + (fullBossDialog.length() * typingSpeed) + waitAfterTextDuration) {
            bossImage.addAction(Actions.fadeOut(1f));
            dialogBox.addAction(Actions.fadeOut(1f));
            dialogLabel.addAction(Actions.fadeOut(1f));
            blackScreen.addAction(Actions.fadeOut(1f));
            buttonTable.addAction(Actions.fadeIn(1.5f));
            buttonsShown = true;
        }
    }
    
    @Override
    protected void handleSkipInput(float delta) {
        if (Gdx.input.justTouched()) {
            float currentTime = timeElapsed;
            
            if (currentTime - lastClickTime < clickTimeout) {
                clickCount++;
                if (clickCount >= 2 && !buttonsShown) {
                    bossImage.clearActions();
                    dialogLabel.clearActions();
                    dialogBox.clearActions();
                    blackScreen.clearActions();
                    
                    bossImage.addAction(Actions.fadeOut(0.5f));
                    dialogBox.addAction(Actions.fadeOut(0.5f));
                    dialogLabel.addAction(Actions.fadeOut(0.5f));
                    blackScreen.addAction(Actions.fadeOut(0.5f));
                    buttonTable.addAction(Actions.fadeIn(1f));
                    buttonsShown = true;
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
    
    @Override
    public void dispose() {
        super.dispose();
        if (dialogFont != null) {
            dialogFont.dispose();
        }
        if (blackScreenTexture != null) {
            blackScreenTexture.dispose();
        }
        if (dialogBoxTexture != null) {
            dialogBoxTexture.dispose();
        }
    }
    
    @Override
    protected void transitionToNextScreen() {
    }
}
