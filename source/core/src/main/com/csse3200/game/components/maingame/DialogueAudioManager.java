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
 * 对话音频管理器
 * 负责预加载、播放和停止对话音频
 */
public class DialogueAudioManager {
    private static final Logger logger = LoggerFactory.getLogger(DialogueAudioManager.class);
    
    private Sound currentSound = null;
    private long currentSoundId = -1;
    
    // 对话音量倍数，让对话声音更响亮
    private static float DIALOGUE_VOLUME_MULTIPLIER = 1.5f;

    /**
     * 预加载对话条目中的所有音频
     * @param entries 对话条目列表
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
     * 播放对话音频
     * @param soundPath 音频文件路径
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
                // 获取用户音量设置并应用对话音量倍数
                UserSettings.Settings settings = UserSettings.get();
                float baseVolume = settings.soundVolume;
                float dialogueVolume = Math.min(1.0f, baseVolume * DIALOGUE_VOLUME_MULTIPLIER);
                
                // 播放音频并记录
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
     * 播放对话音频（可指定音量）
     * @param soundPath 音频文件路径
     * @param volume 音量（0.0 到 1.0）
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
                // 确保音量在有效范围内并应用对话音量倍数
                float baseVolume = Math.max(0f, Math.min(1f, volume));
                float dialogueVolume = Math.min(1.0f, baseVolume * DIALOGUE_VOLUME_MULTIPLIER);
                
                // 播放音频并记录
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
     * 停止当前播放的音频
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
     * 暂停当前播放的音频
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
     * 恢复当前暂停的音频
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
     * 检查是否有音频正在播放
     * @return true 如果有音频正在播放
     */
    public boolean isPlaying() {
        return currentSound != null && currentSoundId != -1;
    }

    /**
     * 设置当前播放音频的音量
     * @param volume 音量（0.0 到 1.0）
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
     * 设置对话音量倍数
     * @param multiplier 音量倍数（建议范围：0.5 - 3.0）
     */
    public static void setVolumeMultiplier(float multiplier) {
        DIALOGUE_VOLUME_MULTIPLIER = Math.max(0.1f, Math.min(5.0f, multiplier));
        logger.info("Dialogue volume multiplier set to: {}", DIALOGUE_VOLUME_MULTIPLIER);
    }

    /**
     * 获取当前对话音量倍数
     * @return 当前音量倍数
     */
    public static float getVolumeMultiplier() {
        return DIALOGUE_VOLUME_MULTIPLIER;
    }

    /**
     * 清理资源
     */
    public void dispose() {
        stopCurrentSound();
        logger.debug("Dialogue audio manager disposed");
    }
}

