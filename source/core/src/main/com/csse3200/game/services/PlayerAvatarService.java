package com.csse3200.game.services;

/**
 * 玩家头像服务接口
 * 管理玩家头像的选择和获取
 */
public interface PlayerAvatarService {
    
    /**
     * 设置玩家头像
     * @param avatarId 头像ID
     */
    void setPlayerAvatar(String avatarId);
    
    /**
     * 获取当前玩家头像ID
     * @return 头像ID，如果没有设置则返回默认头像ID
     */
    String getPlayerAvatar();
    
    /**
     * 获取所有可用的头像ID列表
     * @return 头像ID数组
     */
    String[] getAvailableAvatars();
    
    /**
     * 获取头像的显示名称
     * @param avatarId 头像ID
     * @return 头像显示名称
     */
    String getAvatarDisplayName(String avatarId);
    
    /**
     * 获取头像图片路径
     * @param avatarId 头像ID
     * @return 头像图片路径
     */
    String getAvatarImagePath(String avatarId);
    
    /**
     * 检查是否有设置头像
     * @return true如果已设置头像，false否则
     */
    boolean hasPlayerAvatar();
    
    /**
     * 清除玩家头像设置
     */
    void clearPlayerAvatar();
}
