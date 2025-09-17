package com.csse3200.game.services;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.files.UserSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
class AudioServiceTest {

    @Mock
    private ResourceService resourceService;
    
    @Mock
    private Music mockMusic;
    
    @Mock
    private Sound mockSound;
    
    private AudioService audioService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ServiceLocator.registerResourceService(resourceService);
        audioService = new AudioService();
    }

    @Test
    void shouldRegisterMusic() {
        when(resourceService.getAsset("test.mp3", Music.class)).thenReturn(mockMusic);
        
        audioService.registerMusic("testMusic", "test.mp3");
        
        verify(resourceService).getAsset("test.mp3", Music.class);
    }

    @Test
    void shouldRegisterSound() {
        when(resourceService.getAsset("test.ogg", Sound.class)).thenReturn(mockSound);
        
        audioService.registerSound("testSound", "test.ogg");
        
        verify(resourceService).getAsset("test.ogg", Sound.class);
    }

    @Test
    void shouldPlayMusic() {
        when(resourceService.getAsset("test.mp3", Music.class)).thenReturn(mockMusic);
        audioService.registerMusic("testMusic", "test.mp3");
        
        audioService.playMusic("testMusic", true);
        
        verify(mockMusic).setVolume(audioService.getMusicVolume());
        verify(mockMusic).setLooping(true);
        verify(mockMusic).play();
    }

    @Test
    void shouldNotPlayMusicWhenDisabled() {
        when(resourceService.getAsset("test.mp3", Music.class)).thenReturn(mockMusic);
        audioService.registerMusic("testMusic", "test.mp3");
        audioService.setMusicEnabled(false);
        
        audioService.playMusic("testMusic", true);
        
        verify(mockMusic, never()).play();
    }

    @Test
    void shouldPlaySound() {
        when(resourceService.getAsset("test.ogg", Sound.class)).thenReturn(mockSound);
        audioService.registerSound("testSound", "test.ogg");
        
        audioService.playSound("testSound");
        
        verify(mockSound).play(audioService.getSoundVolume());
    }

    @Test
    void shouldNotPlaySoundWhenDisabled() {
        when(resourceService.getAsset("test.ogg", Sound.class)).thenReturn(mockSound);
        audioService.registerSound("testSound", "test.ogg");
        audioService.setSoundEnabled(false);
        
        audioService.playSound("testSound");
        
        verify(mockSound, never()).play(anyFloat());
    }

    @Test
    void shouldStopMusic() {
        when(resourceService.getAsset("test.mp3", Music.class)).thenReturn(mockMusic);
        audioService.registerMusic("testMusic", "test.mp3");
        audioService.playMusic("testMusic", true);
        
        audioService.stopMusic();
        
        verify(mockMusic).stop();
    }

    @Test
    void shouldPauseMusic() {
        when(resourceService.getAsset("test.mp3", Music.class)).thenReturn(mockMusic);
        audioService.registerMusic("testMusic", "test.mp3");
        audioService.playMusic("testMusic", true);
        when(mockMusic.isPlaying()).thenReturn(true);
        
        audioService.pauseMusic();
        
        verify(mockMusic).pause();
    }

    @Test
    void shouldResumeMusic() {
        when(resourceService.getAsset("test.mp3", Music.class)).thenReturn(mockMusic);
        audioService.registerMusic("testMusic", "test.mp3");
        audioService.playMusic("testMusic", true);
        when(mockMusic.isPlaying()).thenReturn(false);
        
        audioService.resumeMusic();
        
        verify(mockMusic, times(2)).play();
    }

    @Test
    void shouldSetMusicVolume() {
        when(resourceService.getAsset("test.mp3", Music.class)).thenReturn(mockMusic);
        audioService.registerMusic("testMusic", "test.mp3");
        audioService.playMusic("testMusic", true);
        
        audioService.setMusicVolume(0.8f);
        
        verify(mockMusic).setVolume(0.8f);
        assertEquals(0.8f, audioService.getMusicVolume());
    }

    @Test
    void shouldClampMusicVolume() {
        audioService.setMusicVolume(1.5f);
        assertEquals(1.0f, audioService.getMusicVolume());
        
        audioService.setMusicVolume(-0.5f);
        assertEquals(0.0f, audioService.getMusicVolume());
    }

    @Test
    void shouldSetSoundVolume() {
        audioService.setSoundVolume(0.7f);
        assertEquals(0.7f, audioService.getSoundVolume());
    }

    @Test
    void shouldClampSoundVolume() {
        audioService.setSoundVolume(1.5f);
        assertEquals(1.0f, audioService.getSoundVolume());
        
        audioService.setSoundVolume(-0.5f);
        assertEquals(0.0f, audioService.getSoundVolume());
    }

    @Test
    void shouldToggleMusicEnabled() {
        when(resourceService.getAsset("test.mp3", Music.class)).thenReturn(mockMusic);
        audioService.registerMusic("testMusic", "test.mp3");
        audioService.playMusic("testMusic", true);
        
        assertTrue(audioService.isMusicEnabled());
        
        audioService.setMusicEnabled(false);
        assertFalse(audioService.isMusicEnabled());
        verify(mockMusic).pause();
        
        audioService.setMusicEnabled(true);
        assertTrue(audioService.isMusicEnabled());
        verify(mockMusic, times(2)).play();
    }

    @Test
    void shouldToggleSoundEnabled() {
        assertTrue(audioService.isSoundEnabled());
        
        audioService.setSoundEnabled(false);
        assertFalse(audioService.isSoundEnabled());
    }

    @Test
    void shouldStopPreviousMusicWhenPlayingNew() {
        Music mockMusic2 = mock(Music.class);
        when(resourceService.getAsset("test1.mp3", Music.class)).thenReturn(mockMusic);
        when(resourceService.getAsset("test2.mp3", Music.class)).thenReturn(mockMusic2);
        
        audioService.registerMusic("music1", "test1.mp3");
        audioService.registerMusic("music2", "test2.mp3");
        
        audioService.playMusic("music1", true);
        audioService.playMusic("music2", true);
        
        verify(mockMusic).stop();
        verify(mockMusic2).play();
    }
}
