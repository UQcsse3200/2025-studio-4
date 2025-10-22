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
                "Man... I never thought I'd wake up again.\nThanks for bringing me back... \neven if it's into this broken world.", 
                "images/map1talker.png", 
                "sounds/map/man.mp3",   
                "left",
                "images/talker.png",  // 使用talker.png作为对话框背景
                "MAMBA"  // 对话者名字
            ),
            
            new IntroDialogueComponent.DialogueEntry(
                "You're lucky to still have a core. \nThe AI tore everything apart.\nWe rebuilt you because we need to \nunderstand what we're fighting.", 
                getPlayerAvatarPath(),  // 使用玩家选择的头像
                "sounds/map/man.mp3",
                "right",
                "images/talker2.png",  // 使用talker.png作为对话框背景
                getPlayerName(),  // 对话者名字
                0.8f  // 字体缩放比例
            ),
            
            new IntroDialogueComponent.DialogueEntry(
                "...What can I say...\n(He lifts his head, \nthe blue light in his eyes flickers to life.)\nThey're already here.", 
                "images/map1talker.png", 
                "sounds/map/say.mp3",
                "left",
                "images/talker.png",  // 使用talker.png作为对话框背景
                "MAMBA"  // 对话者名字
            ),
            
            new IntroDialogueComponent.DialogueEntry(
                "Then it's our turn.", 
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
                "Ha... Ha-ha-ha! Man!\n I missed this feeling, chaos, heat, and the pulse of war.\nGuess even death couldn't keep me away forever.", 
                "images/map2talker.png", 
                "sounds/map/man.mp3",   
                "left",
                "images/talker.png",  // 使用talker.png作为对话框背景
                "MAMBA",  // 对话者名字
                0.77f  // 字体缩放比例
            ),
            
            new IntroDialogueComponent.DialogueEntry(
                "Enjoy it while you can, MAMBA. \nThe city's heart is close, \nand it's crawling with AI patrols.\nThis is where we end it.", 
                getPlayerAvatarPath(),  // 使用玩家选择的头像
                "sounds/map/man.mp3",
                "right",
                "images/talker2.png",  // 使用talker.png作为对话框背景
                getPlayerName()  // 对话者名字
            ),
            
            new IntroDialogueComponent.DialogueEntry(
                "(grinning, blue light flickering in his eyes) \nYou think you can end it?\nThe network never sleeps, Commander... \nbut I like your spirit.", 
                "images/map2talker.png", 
                "sounds/map/say.mp3",
                "left",
                "images/talker.png",  // 使用talker.png作为对话框背景
                "MAMBA"  // 对话者名字
            ),
            
            new IntroDialogueComponent.DialogueEntry(
                "Then let's finish what we started.", 
                getPlayerAvatarPath(),  // 使用玩家选择的头像
                "sounds/map/out.mp3",
                "right",
                "images/talker2.png",  // 使用talker.png作为对话框背景
                getPlayerName()  // 对话者名字
            )
            
        );
    }
}

