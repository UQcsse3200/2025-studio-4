package com.csse3200.game.components.maingame;

import java.util.List;
import com.csse3200.game.services.ServiceLocator;

/**
 * Configuration class for dialogue content across different game maps.
 * 
 * <p>This class provides centralized management of dialogue scripts for different
 * game areas. It handles player customization by dynamically retrieving player
 * names and avatar paths from services, with fallback defaults when services
 * are unavailable.</p>
 * 
 * <p>The dialogue entries include text content, speaker avatars, audio files,
 * positioning, background images, and speaker names. Each map has its own
 * unique dialogue sequence that reflects the story progression.</p>
 * 
 * @author Team1
 * @since sprint 4
 */
public class DialogueConfig {
    
    /**
     * Retrieves the player's name from the service, with fallback to default value.
     * 
     * <p>Attempts to get the player name from the PlayerNameService. If the service
     * is unavailable or throws an exception, returns the default name "Player".</p>
     * 
     * @return the player's name, or "Player" if service is unavailable
     */
    private static String getPlayerName() {
        try {
            if (ServiceLocator.getPlayerNameService() != null) {
                return ServiceLocator.getPlayerNameService().getPlayerName();
            }
        } catch (Exception e) {
            // If service is unavailable, use default name
        }
        return "Player"; // Default name
    }
    
    /**
     * Retrieves the player's avatar path from the service, with fallback to default value.
     * 
     * <p>Attempts to get the player's selected avatar from the PlayerAvatarService.
     * If the service is unavailable or throws an exception, returns the default
     * hero avatar path.</p>
     * 
     * @return the player's avatar image path, or default hero path if service is unavailable
     */
    private static String getPlayerAvatarPath() {
        try {
            if (ServiceLocator.getPlayerAvatarService() != null) {
                String avatarId = ServiceLocator.getPlayerAvatarService().getPlayerAvatar();
                return ServiceLocator.getPlayerAvatarService().getAvatarImagePath(avatarId);
            }
        } catch (Exception e) {
            // If service is unavailable, use default avatar
        }
        return "images/hero/Heroshoot.png"; // Default avatar path
    }
    
    /**
     * Gets the dialogue script for Map 1 (Icebox).
     * 
     * <p>Returns a sequence of dialogue entries that tell the story of awakening
     * in the frozen wasteland. The dialogue features MAMBA, a resurrected character,
     * and the player character discussing the AI threat and the need to fight back.</p>
     * 
     * <p>The dialogue includes dynamic player customization through avatar and name
     * retrieval, with appropriate audio files and positioning for each speaker.</p>
     * 
     * @return list of dialogue entries for Map 1
     */
    public static List<IntroDialogueComponent.DialogueEntry> getMap1Dialogue() {
        return List.of(
            new IntroDialogueComponent.DialogueEntry(
                "Man... I never thought I'd wake up again.\nThanks for bringing me back... \neven if it's into this broken world.", 
                "images/map1talker.png", 
                "sounds/map/man.mp3",   
                "left",
                "images/talker.png",
                "MAMBA"
            ),
            
            new IntroDialogueComponent.DialogueEntry(
                "You're lucky to still have a core. \nThe AI tore everything apart.\nWe rebuilt you because we need to \nunderstand what we're fighting.", 
                getPlayerAvatarPath(),
                "sounds/map/man.mp3",
                "right",
                "images/talker2.png",
                getPlayerName(),
                0.8f
            ),
            
            new IntroDialogueComponent.DialogueEntry(
                "...What can I say...\n(He lifts his head, \nthe blue light in his eyes flickers to life.)\nThey're already here.", 
                "images/map1talker.png", 
                "sounds/map/say.mp3",
                "left",
                "images/talker.png",
                "MAMBA"
            ),
            
            new IntroDialogueComponent.DialogueEntry(
                "Then it's our turn.", 
                getPlayerAvatarPath(),
                "sounds/map/out.mp3",
                "right",
                "images/talker2.png",
                getPlayerName()
            )
        );
    }
    
    /**
     * Gets the dialogue script for Map 2 (Ascent).
     * 
     * <p>Returns a sequence of dialogue entries that continue the story in the
     * AI-controlled city. The dialogue features MAMBA expressing excitement
     * about returning to battle, and the player character discussing the final
     * confrontation with the AI network.</p>
     * 
     * <p>The dialogue maintains the same customization features as Map 1,
     * with dynamic player avatar and name integration.</p>
     * 
     * @return list of dialogue entries for Map 2
     */
    public static List<IntroDialogueComponent.DialogueEntry> getMap2Dialogue() {
        return List.of(
            new IntroDialogueComponent.DialogueEntry(
                "Ha... Ha-ha-ha! Man!\n I missed this feeling, chaos, heat, and the pulse of war.\nGuess even death couldn't keep me away forever.", 
                "images/map2talker.png", 
                "sounds/map/man.mp3",   
                "left",
                "images/talker.png",
                "MAMBA",
                0.77f
            ),
            
            new IntroDialogueComponent.DialogueEntry(
                "Enjoy it while you can, MAMBA. \nThe city's heart is close, \nand it's crawling with AI patrols.\nThis is where we end it.", 
                getPlayerAvatarPath(),
                "sounds/map/man.mp3",
                "right",
                "images/talker2.png",
                getPlayerName()
            ),
            
            new IntroDialogueComponent.DialogueEntry(
                "(grinning, blue light flickering in his eyes) \nYou think you can end it?\nThe network never sleeps, Commander... \nbut I like your spirit.", 
                "images/map2talker.png", 
                "sounds/map/say.mp3",
                "left",
                "images/talker.png",
                "MAMBA"
            ),
            
            new IntroDialogueComponent.DialogueEntry(
                "Then let's finish what we started.", 
                getPlayerAvatarPath(),
                "sounds/map/out.mp3",
                "right",
                "images/talker2.png",
                getPlayerName()
            )
            
        );
    }
}

