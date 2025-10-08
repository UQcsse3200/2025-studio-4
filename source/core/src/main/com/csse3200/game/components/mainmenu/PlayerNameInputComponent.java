package com.csse3200.game.components.mainmenu;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.services.PlayerNameServiceImpl;
import com.csse3200.game.ui.UIComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 玩家姓名输入组件
 * 在游戏开始前让玩家输入姓名
 */
public class PlayerNameInputComponent extends UIComponent {
    private static final Logger logger = LoggerFactory.getLogger(PlayerNameInputComponent.class);
    private static final float Z_INDEX = 2f;
    
    private Table table;
    private TextField nameField;
    private TextButton confirmButton;
    private Label titleLabel;
    private Label instructionLabel;
    
    @Override
    public void create() {
        super.create();
        addActors();
    }
    
    private void addActors() {
        // 确保玩家姓名服务已注册
        if (ServiceLocator.getPlayerNameService() == null) {
            ServiceLocator.registerPlayerNameService(new PlayerNameServiceImpl());
        }
        
        table = new Table();
        table.setFillParent(true);
        
        // 创建UI元素
        createUIElements();
        
        // 布局
        layoutElements();
        
        stage.addActor(table);
    }
    
    private void createUIElements() {
        // 获取皮肤
        Skin skin = ServiceLocator.getResourceService().getAsset("flat-earth/skin/flat-earth-ui.json", Skin.class);
        
        // 标题
        titleLabel = new Label("Enter Your Name", skin, "title");
        titleLabel.setColor(Color.WHITE);
        
        // 说明文字
        instructionLabel = new Label("Please enter your name for the leaderboard:", skin);
        instructionLabel.setColor(Color.LIGHT_GRAY);
        
        // 姓名输入框
        nameField = new TextField("", skin);
        nameField.setMessageText("Your Name");
        nameField.setMaxLength(20); // 限制姓名长度
        
        // 确认按钮
        confirmButton = new TextButton("Start Game", skin);
        confirmButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                handleNameConfirm();
            }
        });
    }
    
    private void layoutElements() {
        table.center();
        
        table.add(titleLabel).padBottom(20).row();
        table.add(instructionLabel).padBottom(15).row();
        table.add(nameField).width(300).height(40).padBottom(20).row();
        table.add(confirmButton).width(200).height(50);
        
        table.pack();
    }
    
    private void handleNameConfirm() {
        String playerName = nameField.getText().trim();
        
        if (playerName.isEmpty()) {
            // 如果没有输入姓名，使用默认值
            playerName = "Player";
            logger.info("No name entered, using default: {}", playerName);
        }
        
        // 保存玩家姓名
        ServiceLocator.getPlayerNameService().setPlayerName(playerName);
        logger.info("Player name set to: {}", playerName);
        
        // 触发事件，通知可以开始游戏
        entity.getEvents().trigger("playerNameConfirmed", playerName);
    }
    
    /**
     * 显示姓名输入界面
     */
    public void show() {
        if (table != null) {
            table.setVisible(true);
            // 聚焦到输入框
            stage.setKeyboardFocus(nameField);
        }
    }
    
    /**
     * 隐藏姓名输入界面
     */
    public void hide() {
        if (table != null) {
            table.setVisible(false);
        }
    }
    
    @Override
    public void draw(SpriteBatch batch) {
        // UI组件由stage自动绘制
    }
    
    @Override
    public float getZIndex() {
        return Z_INDEX;
    }
    
    @Override
    public void dispose() {
        super.dispose();
        if (table != null) {
            table.remove();
        }
    }
}
