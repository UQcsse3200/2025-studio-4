package com.csse3200.game.components.audio;

import com.csse3200.game.components.Component;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackgroundMusicComponent extends Component {
    private static final Logger logger = LoggerFactory.getLogger(BackgroundMusicComponent.class);
    private String currentMusicName;
    
    @Override
    public void create() {
        super.create();
        
        entity.getEvents().addListener("playMusic", this::playMusic);
        entity.getEvents().addListener("stopMusic", this::stopMusic);
        entity.getEvents().addListener("pauseMusic", this::pauseMusic);
        entity.getEvents().addListener("resumeMusic", this::resumeMusic);
        entity.getEvents().addListener("changeMusic", this::changeMusic);
    }
    
    private void playMusic(String musicName) {
        if (ServiceLocator.getAudioService() != null) {
            ServiceLocator.getAudioService().playMusic(musicName, true);
            currentMusicName = musicName;
            logger.debug("Started playing music: {}", musicName);
        }
    }
    
    private void stopMusic() {
        if (ServiceLocator.getAudioService() != null) {
            ServiceLocator.getAudioService().stopMusic();
            currentMusicName = null;
            logger.debug("Stopped music");
        }
    }
    
    private void pauseMusic() {
        if (ServiceLocator.getAudioService() != null) {
            ServiceLocator.getAudioService().pauseMusic();
            logger.debug("Paused music");
        }
    }
    
    private void resumeMusic() {
        if (ServiceLocator.getAudioService() != null) {
            ServiceLocator.getAudioService().resumeMusic();
            logger.debug("Resumed music");
        }
    }
    
    private void changeMusic(String newMusicName) {
        if (ServiceLocator.getAudioService() != null) {
            ServiceLocator.getAudioService().stopMusic();
            ServiceLocator.getAudioService().playMusic(newMusicName, true);
            currentMusicName = newMusicName;
            logger.debug("Changed music to: {}", newMusicName);
        }
    }
    
    public String getCurrentMusicName() {
        return currentMusicName;
    }
}
