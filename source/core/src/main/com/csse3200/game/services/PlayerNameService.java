package com.csse3200.game.services;

/**
 * 玩家姓名服务接口
 * 用于管理玩家输入的姓名
 */
public interface PlayerNameService {
    
    /**
     * 设置玩家姓名
     * @param playerName 玩家姓名
     */
    void setPlayerName(String playerName);
    
    /**
     * 获取玩家姓名
     * @return 玩家姓名，如果未设置则返回默认值
     */
    String getPlayerName();
    
    /**
     * 检查是否已设置玩家姓名
     * @return 如果已设置姓名返回true，否则返回false
     */
    boolean hasPlayerName();
    
    /**
     * 清除玩家姓名
     */
    void clearPlayerName();
}