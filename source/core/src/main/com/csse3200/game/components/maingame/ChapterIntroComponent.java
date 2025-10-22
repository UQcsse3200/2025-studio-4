package com.csse3200.game.components.maingame;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.Align;
import com.csse3200.game.ui.SimpleUI;
import com.csse3200.game.ui.UIComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 章节介绍组件，在屏幕中心显示故事文本
 */
public class ChapterIntroComponent extends UIComponent {
    private static final Logger logger = LoggerFactory.getLogger(ChapterIntroComponent.class);
    
    private final String[] storyTexts;
    private final Runnable onComplete;
    private Table overlayTable;
    private int currentTextIndex = 0;
    private boolean finished = false;
    private BitmapFont storyFont;
    private BitmapFont titleFont;
    
    public ChapterIntroComponent(String[] storyTexts, Runnable onComplete) {
        this.storyTexts = storyTexts;
        this.onComplete = onComplete;
    }
    
    @Override
    public void create() {
        super.create();
        
        if (storyTexts == null || storyTexts.length == 0) {
            logger.warn("ChapterIntroComponent started with no story texts");
            finishIntro();
            return;
        }
        
        createFonts();
        buildOverlay();
        showNextText();
    }
    
    private void createFonts() {
        try {
            // 创建标题字体（更大）
            titleFont = new BitmapFont(com.badlogic.gdx.Gdx.files.internal("flat-earth/skin/fonts/arial_black_32.fnt"));
            titleFont.getData().setScale(1.5f);
            titleFont.setColor(Color.WHITE);
        } catch (Exception e) {
            logger.warn("Failed to load title font, using default font", e);
            titleFont = SimpleUI.font();
            titleFont.getData().setScale(2.0f);
            titleFont.setColor(Color.WHITE);
        }
        
        try {
            // 创建故事文本字体
            storyFont = new BitmapFont(com.badlogic.gdx.Gdx.files.internal("flat-earth/skin/fonts/pixel_32.fnt"));
            storyFont.getData().setScale(1.2f);
            storyFont.setColor(new Color(0.9f, 0.9f, 0.9f, 1f)); // 淡灰色
        } catch (Exception e) {
            logger.warn("Failed to load story font, using default font", e);
            storyFont = SimpleUI.font();
            storyFont.getData().setScale(1.5f);
            storyFont.setColor(new Color(0.9f, 0.9f, 0.9f, 1f));
        }
    }
    
    private void buildOverlay() {
        float screenWidth = com.badlogic.gdx.Gdx.graphics.getWidth();
        float screenHeight = com.badlogic.gdx.Gdx.graphics.getHeight();
        
        // 创建半透明黑色背景
        overlayTable = new Table();
        overlayTable.setFillParent(true);
        overlayTable.setBackground(SimpleUI.solid(new Color(0f, 0f, 0f, 0.8f)));
        overlayTable.setTouchable(Touchable.enabled);
        
        // 添加点击监听器，点击任意位置继续
        overlayTable.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showNextText();
            }
        });
        
        stage.addActor(overlayTable);
    }
    
    private void showNextText() {
        if (finished || currentTextIndex >= storyTexts.length) {
            finishIntro();
            return;
        }
        
        // 清除之前的内容
        overlayTable.clearChildren();
        
        String currentText = storyTexts[currentTextIndex];
        
        // 创建文本标签
        Label.LabelStyle labelStyle;
        if (currentTextIndex == 0) {
            // 第一段是标题，使用标题字体
            labelStyle = new Label.LabelStyle(titleFont, Color.WHITE);
        } else {
            // 其他段落使用故事字体
            labelStyle = new Label.LabelStyle(storyFont, new Color(0.9f, 0.9f, 0.9f, 1f));
        }
        
        Label textLabel = new Label(currentText, labelStyle);
        textLabel.setWrap(true);
        textLabel.setAlignment(Align.center);
        
        // 设置文本区域大小（屏幕宽度的70%，高度自适应）
        float textWidth = com.badlogic.gdx.Gdx.graphics.getWidth() * 0.7f;
        float textHeight = com.badlogic.gdx.Gdx.graphics.getHeight() * 0.6f;
        
        overlayTable.add(textLabel)
                .width(textWidth)
                .height(textHeight)
                .center();
        
        currentTextIndex++;
        
        // 自动继续（3秒后）
        com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
            @Override
            public void run() {
                showNextText();
            }
        }, 3.0f);
    }
    
    private void finishIntro() {
        if (finished) {
            return;
        }
        finished = true;
        
        // 移除覆盖层
        if (overlayTable != null) {
            overlayTable.remove();
            overlayTable = null;
        }
        
        // 释放字体资源
        if (storyFont != null && storyFont != SimpleUI.font()) {
            storyFont.dispose();
        }
        if (titleFont != null && titleFont != SimpleUI.font()) {
            titleFont.dispose();
        }
        
        // 执行完成回调
        if (onComplete != null) {
            try {
                onComplete.run();
            } catch (Exception e) {
                logger.error("Error running chapter intro completion callback", e);
            }
        }
        
        entity.dispose();
    }
    
    @Override
    protected void draw(SpriteBatch batch) {
        // Stage handles drawing
    }
    
    @Override
    public void dispose() {
        if (!finished) {
            finishIntro();
        }
        super.dispose();
    }
}
