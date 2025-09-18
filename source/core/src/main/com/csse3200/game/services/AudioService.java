package com.csse3200.game.services;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.ObjectMap;
import com.csse3200.game.files.UserSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AudioService {
    private static final Logger logger = LoggerFactory.getLogger(AudioService.class);
    
    private Music currentMusic;
    private ObjectMap<String, Music> musicMap;
    private ObjectMap<String, Sound> soundMap;
    private float musicVolume = 0.5f;
    private float soundVolume = 0.5f;
    private boolean musicEnabled = true;
    private boolean soundEnabled = true;
    
    public AudioService() {
        musicMap = new ObjectMap<>();
        soundMap = new ObjectMap<>();
        loadSettings();
    }
    
    private void loadSettings() {
        UserSettings.Settings settings = UserSettings.get();
        this.musicVolume = settings.musicVolume;
        this.soundVolume = settings.soundVolume;
    }
    
    public void registerMusic(String name, String filepath) {
        Music music = ServiceLocator.getResourceService().getAsset(filepath, Music.class);
        musicMap.put(name, music);
    }
    
    public void registerSound(String name, String filepath) {
        Sound sound = ServiceLocator.getResourceService().getAsset(filepath, Sound.class);
        soundMap.put(name, sound);
    }
    
    public void playMusic(String name, boolean loop) {
        if (!musicEnabled) return;
        
        Music music = musicMap.get(name);
        if (music == null) {
            logger.warn("Music not found: {}", name);
            return;
        }
        
        if (currentMusic != null && currentMusic != music) {
            currentMusic.stop();
        }
        
        currentMusic = music;
        currentMusic.setVolume(musicVolume);
        currentMusic.setLooping(loop);
        currentMusic.play();
    }
    
    public void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic = null;
        }
    }
    
    public void pauseMusic() {
        if (currentMusic != null && currentMusic.isPlaying()) {
            currentMusic.pause();
        }
    }
    
    public void resumeMusic() {
        if (currentMusic != null && !currentMusic.isPlaying()) {
            currentMusic.play();
        }
    }
    
    public void playSound(String name) {
        playSound(name, soundVolume);
    }
    
    public void playSound(String name, float volume) {
        if (!soundEnabled) return;
        
        Sound sound = soundMap.get(name);
        if (sound == null) {
            logger.warn("Sound not found: {}", name);
            return;
        }
        
        sound.play(volume);
    }
    
    public void setMusicVolume(float volume) {
        this.musicVolume = Math.max(0f, Math.min(1f, volume));
        if (currentMusic != null) {
            currentMusic.setVolume(this.musicVolume);
        }
    }
    
    public void setSoundVolume(float volume) {
        this.soundVolume = Math.max(0f, Math.min(1f, volume));
    }
    
    public void setMusicEnabled(boolean enabled) {
        this.musicEnabled = enabled;
        if (!enabled && currentMusic != null) {
            currentMusic.pause();
        } else if (enabled && currentMusic != null) {
            currentMusic.play();
        }
    }
    
    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
    }
    
    public float getMusicVolume() {
        return musicVolume;
    }
    
    public float getSoundVolume() {
        return soundVolume;
    }
    
    public boolean isMusicEnabled() {
        return musicEnabled;
    }
    
    public boolean isSoundEnabled() {
        return soundEnabled;
    }
    
    public void updateSettings() {
        loadSettings();
        if (currentMusic != null) {
            currentMusic.setVolume(musicVolume);
        }
    }
    
    public void dispose() {
        stopMusic();
        for (Music music : musicMap.values()) {
            music.dispose();
        }
        for (Sound sound : soundMap.values()) {
            sound.dispose();
        }
        musicMap.clear();
        soundMap.clear();
    }
}
