package com.csse3200.game.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 玩家姓名服务的实现类
 * 在内存中存储玩家姓名
 */
public class PlayerNameServiceImpl implements PlayerNameService {
    private static final Logger logger = LoggerFactory.getLogger(PlayerNameServiceImpl.class);
    private static final String DEFAULT_PLAYER_NAME = "Player";
    
    private String playerName;
    
    public PlayerNameServiceImpl() {
        this.playerName = null;
        logger.debug("PlayerNameService initialized");
    }
    
    @Override
    public void setPlayerName(String playerName) {
        if (playerName == null || playerName.trim().isEmpty()) {
            logger.warn("Attempted to set empty or null player name, using default");
            this.playerName = DEFAULT_PLAYER_NAME;
        } else {
            this.playerName = playerName.trim();
            logger.info("Player name set to: {}", this.playerName);
        }
    }
    
    @Override
    public String getPlayerName() {
        if (playerName == null || playerName.trim().isEmpty()) {
            return DEFAULT_PLAYER_NAME;
        }
        return playerName;
    }
    
    @Override
    public boolean hasPlayerName() {
        return playerName != null && !playerName.trim().isEmpty();
    }
    
    @Override
    public void clearPlayerName() {
        logger.debug("Clearing player name");
        this.playerName = null;
    }
}


