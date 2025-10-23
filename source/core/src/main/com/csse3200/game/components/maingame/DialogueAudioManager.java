package com.csse3200.game.components.maingame;

import com.badlogic.gdx.audio.Sound;
import com.csse3200.game.files.UserSettings;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Manages dialogue audio playback, preloading, and volume control.
 * 
 * <p>This class handles the loading and playback of dialogue sounds during
 * conversation sequences. It provides volume management with a configurable
 * multiplier to make dialogue more audible, and includes methods for
 * preloading sounds, playing audio, and controlling playback state.</p>
 * 
 * <p>The manager integrates with the game's ResourceService for sound loading
 * and UserSettings for volume configuration, ensuring consistent audio
 * behavior across the application.</p>
 * 
 * @author Team1
 * @since sprint 4
 */
public class DialogueAudioManager {
    private static final Logger logger = LoggerFactory.getLogger(DialogueAudioManager.class);
    
    /** Currently playing sound instance */
    private Sound currentSound = null;
    /** ID of the currently playing sound for control operations */
    private long currentSoundId = -1;
    
    /** Dialogue volume multiplier to make dialogue sounds more audible */
    private static float DIALOGUE_VOLUME_MULTIPLIER = 1.5f;

    /**
     * Preloads all audio files from the given dialogue entries.
     * 
     * <p>Extracts unique sound paths from the dialogue entries and loads them
     * through the ResourceService. This ensures smooth playback without
     * loading delays during dialogue sequences.</p>
     * 
     * @param entries list of dialogue entries containing sound paths
     */
    public void preloadSounds(List<IntroDialogueComponent.DialogueEntry> entries) {
        ResourceService resourceService = ServiceLocator.getResourceService();
        if (resourceService == null) {
            logger.warn("ResourceService not available, sounds may not load correctly");
            return;
        }

        Set<String> sounds = new LinkedHashSet<>();
        entries.stream()
                .map(IntroDialogueComponent.DialogueEntry::soundPath)
                .filter(Objects::nonNull)
                .forEach(sounds::add);
        
        if (sounds.isEmpty()) {
            logger.debug("No dialogue sounds to preload");
            return;
        }

        String[] paths = sounds.toArray(new String[0]);
        resourceService.loadSounds(paths);
        resourceService.loadAll();
        logger.debug("Preloaded {} dialogue sounds", sounds.size());
    }

    /**
     * Plays dialogue audio using user's volume settings with volume multiplier.
     * 
     * <p>Loads the sound from ResourceService and plays it at a volume calculated
     * from the user's sound settings multiplied by the dialogue volume multiplier.
     * This ensures dialogue is audible even when general sound volume is low.</p>
     * 
     * @param soundPath path to the audio file to play
     */
    public void playSound(String soundPath) {
        if (soundPath == null || soundPath.isBlank()) {
            return;
        }

        ResourceService resourceService = ServiceLocator.getResourceService();
        if (resourceService == null) {
            logger.warn("ResourceService not available, cannot play sound");
            return;
        }

        try {
            Sound sound = resourceService.getAsset(soundPath, Sound.class);
            if (sound != null) {
                // Get user volume settings and apply dialogue volume multiplier
                UserSettings.Settings settings = UserSettings.get();
                float baseVolume = settings.soundVolume;
                float dialogueVolume = Math.min(1.0f, baseVolume * DIALOGUE_VOLUME_MULTIPLIER);
                
                // Play audio and record
                currentSound = sound;
                currentSoundId = sound.play(dialogueVolume);
                logger.debug("Playing dialogue sound: {} at volume {} (base: {}, multiplier: {})", 
                    soundPath, dialogueVolume, baseVolume, DIALOGUE_VOLUME_MULTIPLIER);
            } else {
                logger.warn("Sound not found: {}", soundPath);
            }
        } catch (Exception e) {
            logger.error("Error playing dialogue sound: {}", soundPath, e);
        }
    }

    /**
     * Plays dialogue audio with a specified volume level.
     * 
     * <p>Similar to the parameterless playSound method, but allows direct
     * volume control. The specified volume is still multiplied by the
     * dialogue volume multiplier for consistency.</p>
     * 
     * @param soundPath path to the audio file to play
     * @param volume volume level (0.0 to 1.0)
     */
    public void playSound(String soundPath, float volume) {
        if (soundPath == null || soundPath.isBlank()) {
            return;
        }

        ResourceService resourceService = ServiceLocator.getResourceService();
        if (resourceService == null) {
            logger.warn("ResourceService not available, cannot play sound");
            return;
        }

        try {
            Sound sound = resourceService.getAsset(soundPath, Sound.class);
            if (sound != null) {
                // Ensure volume is within valid range and apply dialogue volume multiplier
                float baseVolume = Math.max(0f, Math.min(1f, volume));
                float dialogueVolume = Math.min(1.0f, baseVolume * DIALOGUE_VOLUME_MULTIPLIER);
                
                // Play audio and record
                currentSound = sound;
                currentSoundId = sound.play(dialogueVolume);
                logger.debug("Playing dialogue sound: {} at volume {} (base: {}, multiplier: {})", 
                    soundPath, dialogueVolume, baseVolume, DIALOGUE_VOLUME_MULTIPLIER);
            } else {
                logger.warn("Sound not found: {}", soundPath);
            }
        } catch (Exception e) {
            logger.error("Error playing dialogue sound: {}", soundPath, e);
        }
    }

    /**
     * Stops the currently playing dialogue sound.
     * 
     * <p>If a sound is currently playing, this method stops it and clears
     * the current sound references. Safe to call even if no sound is playing.</p>
     */
    public void stopCurrentSound() {
        if (currentSound != null && currentSoundId != -1) {
            try {
                currentSound.stop(currentSoundId);
                logger.debug("Stopped current dialogue sound");
            } catch (Exception e) {
                logger.warn("Error stopping current sound", e);
            }
            currentSound = null;
            currentSoundId = -1;
        }
    }

    /**
     * Pauses the currently playing dialogue sound.
     * 
     * <p>Pauses playback without stopping the sound, allowing it to be
     * resumed later. Safe to call even if no sound is playing.</p>
     */
    public void pauseCurrentSound() {
        if (currentSound != null && currentSoundId != -1) {
            try {
                currentSound.pause(currentSoundId);
                logger.debug("Paused current dialogue sound");
            } catch (Exception e) {
                logger.warn("Error pausing current sound", e);
            }
        }
    }

    /**
     * Resumes the currently paused dialogue sound.
     * 
     * <p>Resumes playback of a previously paused sound. Safe to call even
     * if no sound is paused or playing.</p>
     */
    public void resumeCurrentSound() {
        if (currentSound != null && currentSoundId != -1) {
            try {
                currentSound.resume(currentSoundId);
                logger.debug("Resumed current dialogue sound");
            } catch (Exception e) {
                logger.warn("Error resuming current sound", e);
            }
        }
    }

    /**
     * Checks if a dialogue sound is currently playing.
     * 
     * @return true if a sound is currently playing, false otherwise
     */
    public boolean isPlaying() {
        return currentSound != null && currentSoundId != -1;
    }

    /**
     * Sets the volume of the currently playing audio.
     * 
     * <p>Adjusts the volume of the currently playing sound in real-time.
     * The volume is clamped to the valid range [0.0, 1.0].</p>
     * 
     * @param volume volume level (0.0 to 1.0)
     */
    public void setVolume(float volume) {
        if (currentSound != null && currentSoundId != -1) {
            try {
                float clampedVolume = Math.max(0f, Math.min(1f, volume));
                currentSound.setVolume(currentSoundId, clampedVolume);
                logger.debug("Set dialogue sound volume to {}", clampedVolume);
            } catch (Exception e) {
                logger.warn("Error setting sound volume", e);
            }
        }
    }

    /**
     * Sets the dialogue volume multiplier for all dialogue sounds.
     * 
     * <p>This multiplier is applied to all dialogue sounds to make them
     * more audible relative to other game sounds. The value is clamped
     * to a reasonable range to prevent audio distortion.</p>
     * 
     * @param multiplier volume multiplier (recommended range: 0.5 - 3.0)
     */
    public static void setVolumeMultiplier(float multiplier) {
        DIALOGUE_VOLUME_MULTIPLIER = Math.max(0.1f, Math.min(5.0f, multiplier));
        logger.info("Dialogue volume multiplier set to: {}", DIALOGUE_VOLUME_MULTIPLIER);
    }

    /**
     * Gets the current dialogue volume multiplier.
     * 
     * @return the current volume multiplier value
     */
    public static float getVolumeMultiplier() {
        return DIALOGUE_VOLUME_MULTIPLIER;
    }

    /**
     * Cleans up resources and stops any playing sounds.
     * 
     * <p>Stops the current sound and clears all references. Should be called
     * when the dialogue audio manager is no longer needed to prevent
     * resource leaks.</p>
     */
    public void dispose() {
        stopCurrentSound();
        logger.debug("Dialogue audio manager disposed");
    }
}

