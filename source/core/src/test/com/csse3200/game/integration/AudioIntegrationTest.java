package com.csse3200.game.integration;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.csse3200.game.components.audio.BackgroundMusicComponent;
import com.csse3200.game.components.audio.SoundEffectComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.files.UserSettings;
import com.csse3200.game.services.AudioService;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
class AudioIntegrationTest {

    @Mock
    private ResourceService mockResourceService;
    
    @Mock
    private Music mockMusic;
    
    @Mock
    private Sound mockSound;
    
    private AudioService audioService;
    private Entity musicEntity;
    private Entity soundEntity;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ServiceLocator.registerResourceService(mockResourceService);
        
        when(mockResourceService.getAsset("bgm.mp3", Music.class)).thenReturn(mockMusic);
        when(mockResourceService.getAsset("effect.ogg", Sound.class)).thenReturn(mockSound);
        
        audioService = new AudioService();
        ServiceLocator.registerAudioService(audioService);
        
        musicEntity = new Entity();
        musicEntity.addComponent(new BackgroundMusicComponent());
        musicEntity.create();
        
        soundEntity = new Entity();
        soundEntity.addComponent(new SoundEffectComponent());
        soundEntity.create();
    }

    @Test
    void shouldIntegrateBackgroundMusicFlow() {
        audioService.registerMusic("bgm", "bgm.mp3");
        
        musicEntity.getEvents().trigger("playMusic", "bgm");
        verify(mockMusic).setVolume(audioService.getMusicVolume());
        verify(mockMusic).setLooping(true);
        verify(mockMusic).play();
        
        when(mockMusic.isPlaying()).thenReturn(true);
        musicEntity.getEvents().trigger("pauseMusic");
        verify(mockMusic).pause();
        
        when(mockMusic.isPlaying()).thenReturn(false);
        musicEntity.getEvents().trigger("resumeMusic");
        verify(mockMusic, times(2)).play();
        
        musicEntity.getEvents().trigger("stopMusic");
        verify(mockMusic).stop();
    }

    @Test
    void shouldIntegrateSoundEffectFlow() {
        audioService.registerSound("effect", "effect.ogg");
        
        soundEntity.getEvents().trigger("playSound", "effect");
        verify(mockSound).play(audioService.getSoundVolume());
        
        soundEntity.getEvents().trigger("playSoundWithVolume", "effect", 0.8f);
        verify(mockSound).play(0.8f);
    }

    @Test
    void shouldHandleVolumeChanges() {
        audioService.registerMusic("bgm", "bgm.mp3");
        audioService.registerSound("effect", "effect.ogg");
        
        musicEntity.getEvents().trigger("playMusic", "bgm");
        
        audioService.setMusicVolume(0.8f);
        verify(mockMusic).setVolume(0.8f);
        
        audioService.setSoundVolume(0.3f);
        soundEntity.getEvents().trigger("playSound", "effect");
        verify(mockSound).play(0.3f);
    }

    @Test
    void shouldHandleSettingsIntegration() {
        UserSettings.Settings settings = new UserSettings.Settings();
        settings.musicVolume = 0.7f;
        settings.soundVolume = 0.6f;
        
        audioService.updateSettings();
        
        audioService.registerMusic("bgm", "bgm.mp3");
        musicEntity.getEvents().trigger("playMusic", "bgm");
        
        audioService.registerSound("effect", "effect.ogg");
        soundEntity.getEvents().trigger("playSound", "effect");
        
        verify(mockMusic).setVolume(anyFloat());
        verify(mockSound).play(anyFloat());
    }

    @Test
    void shouldHandleMusicTransitions() {
        Music mockMusic2 = mock(Music.class);
        when(mockResourceService.getAsset("bgm2.mp3", Music.class)).thenReturn(mockMusic2);
        
        audioService.registerMusic("bgm1", "bgm.mp3");
        audioService.registerMusic("bgm2", "bgm2.mp3");
        
        musicEntity.getEvents().trigger("playMusic", "bgm1");
        verify(mockMusic).play();
        
        musicEntity.getEvents().trigger("changeMusic", "bgm2");
        verify(mockMusic).stop();
        verify(mockMusic2).play();
    }

    @Test
    void shouldHandleMultipleEntitiesWithAudio() {
        Entity anotherMusicEntity = new Entity();
        anotherMusicEntity.addComponent(new BackgroundMusicComponent());
        anotherMusicEntity.create();
        
        Entity anotherSoundEntity = new Entity();
        anotherSoundEntity.addComponent(new SoundEffectComponent());
        anotherSoundEntity.create();
        
        audioService.registerMusic("bgm", "bgm.mp3");
        audioService.registerSound("effect", "effect.ogg");
        
        musicEntity.getEvents().trigger("playMusic", "bgm");
        anotherMusicEntity.getEvents().trigger("playMusic", "bgm");
        
        soundEntity.getEvents().trigger("playSound", "effect");
        anotherSoundEntity.getEvents().trigger("playSound", "effect");
        
        verify(mockMusic, times(2)).play();
        verify(mockSound, times(2)).play(anyFloat());
    }

    @Test
    void shouldHandleDisabledAudio() {
        audioService.registerMusic("bgm", "bgm.mp3");
        audioService.registerSound("effect", "effect.ogg");
        
        audioService.setMusicEnabled(false);
        audioService.setSoundEnabled(false);
        
        musicEntity.getEvents().trigger("playMusic", "bgm");
        soundEntity.getEvents().trigger("playSound", "effect");
        
        verify(mockMusic, never()).play();
        verify(mockSound, never()).play(anyFloat());
    }
}
