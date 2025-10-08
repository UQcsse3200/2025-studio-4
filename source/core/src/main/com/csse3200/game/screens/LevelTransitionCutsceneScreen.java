package com.csse3200.game.screens;

import com.csse3200.game.GdxGame;

public class LevelTransitionCutsceneScreen extends GenericCutsceneScreen {
    
    public LevelTransitionCutsceneScreen(GdxGame game, String fromMapId, String toMapId) {
        super(game,
            "images/dim_bg.jpeg",
            getTransitionText(fromMapId, toMapId),
            GdxGame.ScreenType.MAIN_GAME,
            toMapId,
            false);
    }
    
    private static String getTransitionText(String fromMapId, String toMapId) {
        String fromName = getMapName(fromMapId);
        String toName = getMapName(toMapId);
        
        return "SECTOR SECURED!\n\n" +
               "Congratulations, Commander!\n\n" +
               "You have successfully defended " + fromName + ".\n" +
               "The enemy forces have been repelled,\n" +
               "and the sector is now secure.\n\n" +
               "But the war is not over...\n\n" +
               "Intelligence reports indicate new enemy activity at " + toName + ".\n" +
               "Your forces are being redeployed to defend this critical position.\n\n" +
               "Prepare your defenses!\n" +
               "The next wave is approaching...";
    }
    
    private static String getMapName(String mapId) {
        if (mapId == null) {
            return "the Forest Demo Sector";
        } else if ("MapTwo".equalsIgnoreCase(mapId)) {
            return "Map Two Sector";
        }
        return "the sector";
    }
}

