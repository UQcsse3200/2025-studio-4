package com.csse3200.game.components.maingame;

import java.util.List;

/**
 * 对话框配置类
 * 统一管理不同地图的对话内容配置
 */
public class DialogueConfig {
    
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
                "left"
            ),
            
            new IntroDialogueComponent.DialogueEntry(
                "2222222222222222", 
                "images/hero/Heroshoot.png",
                "sounds/map/say.mp3",
                "right"
            ),
            
            new IntroDialogueComponent.DialogueEntry(
                "3333333333333333", 
                "images/talker1.png", 
                "sounds/map/out.mp3",
                "left"
            ),
            
            new IntroDialogueComponent.DialogueEntry(
                "4444444444444444", 
                "images/hero/Heroshoot.png", 
                "sounds/page_flip.mp3",
                "right"
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
                "111111111111111111", 
                "images/talker1.png",
                null,
                "left"
            ),
            
            new IntroDialogueComponent.DialogueEntry(
                "222222222222222222", 
                "images/hero/Heroshoot.png",
                null,
                "right"
            ),
            
            new IntroDialogueComponent.DialogueEntry(
                "333333333333333333", 
                "images/talker1.png",
                null,
                "left"
            ),
            
            new IntroDialogueComponent.DialogueEntry(
                "444444444444444444", 
                "images/hero/Heroshoot.png",
                null,
                "right"
            )
        );
    }
}

