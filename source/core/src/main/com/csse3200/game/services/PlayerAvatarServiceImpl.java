package com.csse3200.game.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 玩家头像服务的实现类
 * 在内存中存储玩家头像选择
 */
public class PlayerAvatarServiceImpl implements PlayerAvatarService {
    private static final Logger logger = LoggerFactory.getLogger(PlayerAvatarServiceImpl.class);
    
    // 默认头像ID
    private static final String DEFAULT_AVATAR = "avatar_1";
    
    // 可用的头像列表
    private static final String[] AVAILABLE_AVATARS = {
        "avatar_1", "avatar_2", "avatar_3", "avatar_4"
    };
    
    // 头像显示名称映射
    private static final String[] AVATAR_DISPLAY_NAMES = {
        "Knight", "Mage", "Warrior", "Archer"
    };
    
    // 头像图片路径映射 - 使用专门的头像图片
    private static final String[] AVATAR_IMAGE_PATHS = {
        "images/profile1.png",
        "images/profile2.png", 
        "images/profile3.png",
        "images/profile4.png"
    };
    
    private String playerAvatar;
    
    public PlayerAvatarServiceImpl() {
        this.playerAvatar = null;
        logger.debug("PlayerAvatarService initialized");
    }
    
    @Override
    public void setPlayerAvatar(String avatarId) {
        if (avatarId == null || avatarId.trim().isEmpty()) {
            logger.warn("Attempted to set empty or null avatar, using default");
            this.playerAvatar = DEFAULT_AVATAR;
        } else if (isValidAvatar(avatarId)) {
            this.playerAvatar = avatarId.trim();
            logger.info("Player avatar set to: {}", this.playerAvatar);
        } else {
            logger.warn("Invalid avatar ID: {}, using default", avatarId);
            this.playerAvatar = DEFAULT_AVATAR;
        }
    }
    
    @Override
    public String getPlayerAvatar() {
        if (playerAvatar == null || playerAvatar.trim().isEmpty()) {
            return DEFAULT_AVATAR;
        }
        return playerAvatar;
    }
    
    @Override
    public String[] getAvailableAvatars() {
        return AVAILABLE_AVATARS.clone();
    }
    
    @Override
    public String getAvatarDisplayName(String avatarId) {
        for (int i = 0; i < AVAILABLE_AVATARS.length; i++) {
            if (AVAILABLE_AVATARS[i].equals(avatarId)) {
                return AVATAR_DISPLAY_NAMES[i];
            }
        }
        return AVATAR_DISPLAY_NAMES[0]; // 返回默认头像的显示名称
    }
    
    @Override
    public String getAvatarImagePath(String avatarId) {
        for (int i = 0; i < AVAILABLE_AVATARS.length; i++) {
            if (AVAILABLE_AVATARS[i].equals(avatarId)) {
                return AVATAR_IMAGE_PATHS[i];
            }
        }
        return AVATAR_IMAGE_PATHS[0]; // 返回默认头像路径
    }
    
    @Override
    public boolean hasPlayerAvatar() {
        return playerAvatar != null && !playerAvatar.trim().isEmpty();
    }
    
    @Override
    public void clearPlayerAvatar() {
        logger.debug("Clearing player avatar");
        this.playerAvatar = null;
    }
    
    /**
     * 检查头像ID是否有效
     */
    private boolean isValidAvatar(String avatarId) {
        for (String avatar : AVAILABLE_AVATARS) {
            if (avatar.equals(avatarId)) {
                return true;
            }
        }
        return false;
    }
}
