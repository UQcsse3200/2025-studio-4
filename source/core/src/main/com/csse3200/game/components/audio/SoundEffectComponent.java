package com.csse3200.game.components.audio;

import com.csse3200.game.components.Component;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SoundEffectComponent extends Component {
    private static final Logger logger = LoggerFactory.getLogger(SoundEffectComponent.class);
    
    @Override
    public void create() {
        super.create();
        
        entity.getEvents().addListener("playSound", this::playSound);
        entity.getEvents().addListener("playSoundWithVolume", this::playSoundWithVolume);
    }
    
    private void playSound(String soundName) {
        if (ServiceLocator.getAudioService() != null) {
            ServiceLocator.getAudioService().playSound(soundName);
            logger.debug("Playing sound: {}", soundName);
        }
    }
    
    private void playSoundWithVolume(String soundName, Float volume) {
        if (ServiceLocator.getAudioService() != null) {
            ServiceLocator.getAudioService().playSound(soundName, volume);
            logger.debug("Playing sound: {} at volume {}", soundName, volume);
        }
    }
}
