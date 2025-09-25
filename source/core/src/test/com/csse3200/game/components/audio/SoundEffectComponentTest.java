package com.csse3200.game.components.audio;

import com.csse3200.game.components.audio.SoundEffectComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.AudioService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
class SoundEffectComponentTest {

    @Mock
    private AudioService mockAudioService;
    
    private Entity entity;
    private SoundEffectComponent component;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ServiceLocator.registerAudioService(mockAudioService);
        
        entity = new Entity();
        component = new SoundEffectComponent();
        entity.addComponent(component);
        entity.create();
    }

    @Test
    void shouldPlaySoundWhenTriggered() {
        entity.getEvents().trigger("playSound", "testSound");
        
        verify(mockAudioService).playSound("testSound");
    }

    @Test
    void shouldPlaySoundWithVolumeWhenTriggered() {
        entity.getEvents().trigger("playSoundWithVolume", "testSound", 0.8f);
        
        verify(mockAudioService).playSound("testSound", 0.8f);
    }

    @Test
    void shouldNotPlaySoundWhenAudioServiceIsNull() {
        ServiceLocator.registerAudioService(null);
        
        entity.getEvents().trigger("playSound", "testSound");
        
        verify(mockAudioService, never()).playSound(anyString());
    }
}
