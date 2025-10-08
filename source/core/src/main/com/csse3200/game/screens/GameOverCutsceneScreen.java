package com.csse3200.game.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.csse3200.game.GdxGame;

public class GameOverCutsceneScreen extends GenericCutsceneScreen {
    private Table buttonTable;
    private boolean buttonsShown = false;
    private String currentMapId;
    
    public GameOverCutsceneScreen(GdxGame game, String mapId) {
        super(game, 
            "images/Game_Over.png",
            getDefeatText(mapId),
            null);
        this.currentMapId = mapId;
    }
    
    private static String getDefeatText(String mapId) {
        String mapName = getMapName(mapId);
        return "DEFEAT\n\n" +
               "Your defenses have fallen...\n\n" +
               "The corrupted AI forces have breached " + mapName + ".\n\n" +
               "The enemy swarms have overwhelmed your positions,\n" +
               "and the last stronghold has been compromised.\n\n" +
               "But this is not the end.\n" +
               "Regroup, strengthen your defenses, and try again.\n\n" +
               "The resistance continues!\n" +
               "Humanity's fate still hangs in the balance.";
    }
    
    private static String getMapName(String mapId) {
        if (mapId == null) {
            return "the Forest Demo Sector";
        } else if ("MapTwo".equalsIgnoreCase(mapId)) {
            return "Map Two Sector";
        }
        return "the sector";
    }
    
    @Override
    protected void setupScrollingText() {
        super.setupScrollingText();
        createButtons();
    }
    
    private void createButtons() {
        buttonTable = new Table();
        buttonTable.setFillParent(true);
        buttonTable.bottom().padBottom(100f);
        
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = scrollFont;
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
        super.updateScrollingText(delta);
        
        if (scrollTextFinished && !buttonsShown) {
            buttonTable.addAction(Actions.fadeIn(1.5f));
            buttonsShown = true;
        }
    }
    
    @Override
    protected void transitionToNextScreen() {
    }
}

