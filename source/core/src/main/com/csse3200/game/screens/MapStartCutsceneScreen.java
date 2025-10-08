package com.csse3200.game.screens;

import com.csse3200.game.GdxGame;

public class MapStartCutsceneScreen extends GenericCutsceneScreen {
    
    public MapStartCutsceneScreen(GdxGame game, String mapId) {
        super(game,
            getBackgroundForMap(mapId),
            getTextForMap(mapId),
            GdxGame.ScreenType.MAIN_GAME,
            mapId,
            false);
    }
    
    private static String getBackgroundForMap(String mapId) {
        if (mapId == null) {
            return "images/Opening_Cutscene_Screen.png";
        } else if ("MapTwo".equalsIgnoreCase(mapId)) {
            return "images/dim_bg.jpeg";
        }
        return "images/Opening_Cutscene_Screen.png";
    }
    
    private static String getTextForMap(String mapId) {
        if (mapId == null) {
            return "FOREST DEMO SECTOR\n\n" +
                   "Location: Natural Defense Zone Alpha\n\n" +
                   "Your mission: Defend the winding maze of forest barriers.\n" +
                   "This sector features natural obstacles that channel enemy forces\n" +
                   "into predictable paths.\n\n" +
                   "Use the terrain to your advantage!\n" +
                   "Place towers strategically along the pathways.\n" +
                   "Deploy hero units to support your defenses.\n\n" +
                   "Enemy forces detected approaching...\n" +
                   "Prepare for combat!";
        } else if ("MapTwo".equalsIgnoreCase(mapId)) {
            return "MAP TWO SECTOR\n\n" +
                   "Location: Open Battlefield Beta\n\n" +
                   "Your mission: Defend the wide-open combat zone.\n" +
                   "This sector has fewer natural barriers,\n" +
                   "requiring precise tower placement and timing.\n\n" +
                   "The enemy has multiple approach vectors!\n" +
                   "Coordinate your defenses across the entire map.\n" +
                   "Hero units will be critical to your success.\n\n" +
                   "Multiple enemy signatures detected...\n" +
                   "Battle stations!";
        }
        return "UNKNOWN SECTOR\n\nPrepare for combat!";
    }
}

