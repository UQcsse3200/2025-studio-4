package com.csse3200.game.components.audio;

import com.csse3200.game.components.audio.BackgroundMusicComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.AudioService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
class BackgroundMusicComponentTest {

    @Mock
    private AudioService mockAudioService;
    
    private Entity entity;
    private BackgroundMusicComponent component;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ServiceLocator.registerAudioService(mockAudioService);
        
        entity = new Entity();
        component = new BackgroundMusicComponent();
        entity.addComponent(component);
        entity.create();
    }

    @Test
    void shouldPlayMusicWhenTriggered() {
        entity.getEvents().trigger("playMusic", "testMusic");
        
        verify(mockAudioService).playMusic("testMusic", true);
        assertEquals("testMusic", component.getCurrentMusicName());
    }

    @Test
    void shouldStopMusicWhenTriggered() {
        entity.getEvents().trigger("playMusic", "testMusic");
        entity.getEvents().trigger("stopMusic");
        
        verify(mockAudioService).stopMusic();
        assertNull(component.getCurrentMusicName());
    }

    @Test
    void shouldPauseMusicWhenTriggered() {
        entity.getEvents().trigger("pauseMusic");
        
        verify(mockAudioService).pauseMusic();
    }

    @Test
    void shouldResumeMusicWhenTriggered() {
        entity.getEvents().trigger("resumeMusic");
        
        verify(mockAudioService).resumeMusic();
    }

    @Test
    void shouldChangeMusicWhenTriggered() {
        entity.getEvents().trigger("playMusic", "music1");
        entity.getEvents().trigger("changeMusic", "music2");
        
        verify(mockAudioService).stopMusic();
        verify(mockAudioService).playMusic("music2", true);
        assertEquals("music2", component.getCurrentMusicName());
    }

    @Test
    void shouldNotPlayMusicWhenAudioServiceIsNull() {
        ServiceLocator.registerAudioService(null);
        
        entity.getEvents().trigger("playMusic", "testMusic");
        
        verify(mockAudioService, never()).playMusic(anyString(), anyBoolean());
        assertNull(component.getCurrentMusicName());
    }
}
