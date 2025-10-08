package com.csse3200.game.components.mainmenu;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
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
        // 使用 MinimalSkinFactory 创建统一风格的皮肤
        Skin skin = com.csse3200.game.ui.leaderboard.MinimalSkinFactory.create();
        
        // 标题 - 使用温暖的棕色字体（Book风格）
        Label.LabelStyle titleStyle = new Label.LabelStyle(
            skin.getFont("default"), 
            new com.badlogic.gdx.graphics.Color(0.17f, 0.14f, 0.09f, 1f)); // #2c2416 棕色
        titleLabel = new Label("Enter Your Name", titleStyle);
        titleLabel.setFontScale(1.5f); // 加大标题字体
        
        // 说明文字 - 使用柔和的棕色
        Label.LabelStyle instructionStyle = new Label.LabelStyle(
            skin.getFont("default"), 
            new com.badlogic.gdx.graphics.Color(0.4f, 0.35f, 0.25f, 1f));
        instructionLabel = new Label("Please enter your name for the leaderboard:", instructionStyle);
        
        // 姓名输入框 - 使用Book风格
        TextField.TextFieldStyle fieldStyle = createBookStyleTextFieldStyle(skin);
        nameField = new TextField("", fieldStyle);
        nameField.setMessageText("Your Name");
        nameField.setMaxLength(20); // 限制姓名长度
        
        // 确认按钮 - 使用Book的绿色按钮风格
        TextButton.TextButtonStyle buttonStyle = createBookStyleButtonStyle(skin);
        confirmButton = new TextButton("Start Game", buttonStyle);
        confirmButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                handleNameConfirm();
            }
        });
    }
    
    /**
     * 创建Book风格的文本输入框样式
     */
    private TextField.TextFieldStyle createBookStyleTextFieldStyle(Skin skin) {
        TextField.TextFieldStyle style = new TextField.TextFieldStyle();
        style.font = skin.getFont("default");
        style.fontColor = new Color(0.17f, 0.14f, 0.09f, 1f); // #2c2416 棕色
        style.background = com.csse3200.game.ui.SimpleUI.roundRect(
            new Color(1f, 1f, 0.95f, 1f),
            new Color(0.7f, 0.6f, 0.4f, 0.8f),
            5, 1); // 浅奶黄色带边框
        style.focusedBackground = com.csse3200.game.ui.SimpleUI.roundRect(
            new Color(1f, 0.98f, 0.9f, 1f),
            new Color(0.16f, 0.64f, 0.35f, 0.9f),
            5, 2); // 聚焦时绿色边框
        style.cursor = com.csse3200.game.ui.SimpleUI.solid(new Color(0.17f, 0.14f, 0.09f, 1f));
        style.selection = com.csse3200.game.ui.SimpleUI.solid(new Color(0.16f, 0.64f, 0.35f, 0.5f)); // 绿色选择
        style.messageFontColor = new Color(0.5f, 0.45f, 0.35f, 0.7f);
        return style;
    }
    
    /**
     * 创建Book风格的按钮样式（绿色）
     */
    private TextButton.TextButtonStyle createBookStyleButtonStyle(Skin skin) {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.font = skin.getFont("default");
        style.fontColor = Color.WHITE;
        // Book的绿色按钮：#2aa35a (普通), #35cc70 (悬停), #228a4a (按下)
        style.up = com.csse3200.game.ui.SimpleUI.roundRect(
            new Color(0.16f, 0.64f, 0.35f, 1f), null, 8, 0);
        style.over = com.csse3200.game.ui.SimpleUI.roundRect(
            new Color(0.21f, 0.80f, 0.44f, 1f), null, 8, 0);
        style.down = com.csse3200.game.ui.SimpleUI.roundRect(
            new Color(0.13f, 0.54f, 0.29f, 1f), null, 8, 0);
        return style;
    }
    
    private void layoutElements() {
        table.center();
        
        // 使用自定义的背景图片
        try {
            Texture bgTexture = ServiceLocator.getResourceService()
                .getAsset("images/name and leaderbooard background.png", Texture.class);
            table.setBackground(new TextureRegionDrawable(new TextureRegion(bgTexture)));
        } catch (Exception e) {
            // 如果加载失败，使用默认的Book风格背景
            table.setBackground(com.csse3200.game.ui.SimpleUI.roundRect(
                new com.badlogic.gdx.graphics.Color(1f, 0.97f, 0.88f, 0.95f),
                new com.badlogic.gdx.graphics.Color(0.8f, 0.7f, 0.5f, 0.8f),
                com.csse3200.game.ui.Theme.RADIUS, 2));
        }
        
        // 增加内边距以匹配Book风格
        float padding = com.csse3200.game.ui.Theme.PAD * 2;
        table.pad(padding);
        
        table.add(titleLabel).padBottom(com.csse3200.game.ui.Theme.PAD).row();
        table.add(instructionLabel).padBottom(com.csse3200.game.ui.Theme.PAD).row();
        table.add(nameField).width(300).height(40).padBottom(com.csse3200.game.ui.Theme.PAD).row();
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
