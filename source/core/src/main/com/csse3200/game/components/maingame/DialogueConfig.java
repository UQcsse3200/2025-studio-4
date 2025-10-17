package com.csse3200.game.components.maingame;

import java.util.List;
import com.csse3200.game.services.ServiceLocator;

/**
 * 对话框配置类
 * 统一管理不同地图的对话内容配置
 */
public class DialogueConfig {
    
    /**
     * 获取玩家名字，如果服务不可用则返回默认值
     * @return 玩家名字
     */
    private static String getPlayerName() {
        try {
            if (ServiceLocator.getPlayerNameService() != null) {
                return ServiceLocator.getPlayerNameService().getPlayerName();
            }
        } catch (Exception e) {
            // 如果服务不可用，使用默认名字
        }
        return "Player"; // 默认名字
    }
    
    /**
     * 获取玩家头像路径，如果服务不可用则返回默认值
     * @return 玩家头像路径
     */
    private static String getPlayerAvatarPath() {
        try {
            if (ServiceLocator.getPlayerAvatarService() != null) {
                String avatarId = ServiceLocator.getPlayerAvatarService().getPlayerAvatar();
                return ServiceLocator.getPlayerAvatarService().getAvatarImagePath(avatarId);
            }
        } catch (Exception e) {
            // 如果服务不可用，使用默认头像
        }
        return "images/hero/Heroshoot.png"; // 默认头像路径
    }
    
    /**
     * 获取地图1的对话脚本
     * @return 对话条目列表
     */
    public static List<IntroDialogueComponent.DialogueEntry> getMap1Dialogue() {
        return List.of(
            new IntroDialogueComponent.DialogueEntry(
                "1111111111111111", 
                "images/talker1.png", 
                "sounds/map/man.mp3",   
                "left",
                "images/talker.png",  // 使用talker.png作为对话框背景
                "NPC"  // 对话者名字
            ),
            
            new IntroDialogueComponent.DialogueEntry(
                "2222222222222222", 
                getPlayerAvatarPath(),  // 使用玩家选择的头像
                "sounds/map/man.mp3",
                "right",
                "images/talker2.png",  // 使用talker.png作为对话框背景
                getPlayerName()  // 对话者名字
            ),
            
            new IntroDialogueComponent.DialogueEntry(
                "3333333333333333", 
                "images/talker1.png", 
                "sounds/map/say.mp3",
                "left",
                "images/talker.png",  // 使用talker.png作为对话框背景
                "NPC"  // 对话者名字
            ),
            
            new IntroDialogueComponent.DialogueEntry(
                "4444444444444444", 
                getPlayerAvatarPath(),  // 使用玩家选择的头像
                "sounds/map/out.mp3",
                "right",
                "images/talker2.png",  // 使用talker.png作为对话框背景
                getPlayerName()  // 对话者名字
            )
        );
    }
    
    /**
     * 获取地图2的对话脚本
     * @return 对话条目列表
     */
    public static List<IntroDialogueComponent.DialogueEntry> getMap2Dialogue() {
        return List.of(
            new IntroDialogueComponent.DialogueEntry(
                "1111111111111111", 
                "images/talker1.png", 
                "sounds/map/man.mp3",   
                "left",
                "images/talker.png",  // 使用talker.png作为对话框背景
                "NPC"  // 对话者名字
            ),
            
            new IntroDialogueComponent.DialogueEntry(
                "2222222222222222", 
                getPlayerAvatarPath(),  // 使用玩家选择的头像
                "sounds/map/man.mp3",
                "right",
                "images/talker2.png",  // 使用talker.png作为对话框背景
                getPlayerName()  // 对话者名字
            ),
            
            new IntroDialogueComponent.DialogueEntry(
                "3333333333333333", 
                "images/talker1.png", 
                "sounds/map/say.mp3",
                "left",
                "images/talker.png",  // 使用talker.png作为对话框背景
                "NPC"  // 对话者名字
            ),
            
            new IntroDialogueComponent.DialogueEntry(
                "4444444444444444", 
                getPlayerAvatarPath(),  // 使用玩家选择的头像
                "sounds/map/out.mp3",
                "right",
                "images/talker2.png",  // 使用talker.png作为对话框背景
                getPlayerName()  // 对话者名字
            )
            
        );
    }
}

