package com.csse3200.game.components.maingame;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.SimpleUI;
import com.csse3200.game.ui.UIComponent;
import com.csse3200.game.ui.UIStyleHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Locale;

public class IntroDialogueComponent extends UIComponent {
  private static final Logger logger = LoggerFactory.getLogger(IntroDialogueComponent.class);

  private final List<DialogueEntry> entries;
  private final Runnable onComplete;
  private final float originalTimeScale;
  private final DialogueAudioManager audioManager; // 音频管理器

  private Table overlayRoot;
  private Table dialogueTable; // 对话框表格
  private Image portraitImage;
  private Label dialogueLabel;
  private Label speakerNameLabel; // 对话者名字标签
  private Table speakerNameContainer; // 名字标签容器，用于添加背景
  private TextButton continueButton; // 继续按钮
  private TextButton skipButton;
  private int currentIndex = -1;
  private boolean finished = false;
  private float screenWidth;
  private float screenHeight;
  private float dialogueWidth;
  private float dialogueHeight;
  private Cell<Table> dialogueCell;
  
  // 字体样式选择
  private BitmapFont dialogueFont;
  private BitmapFont speakerNameFont;

  public IntroDialogueComponent(List<DialogueEntry> entries, Runnable onComplete) {
    this.entries = Objects.requireNonNull(entries, "entries");
    this.onComplete = onComplete;
    this.audioManager = new DialogueAudioManager();
    var timeSource = ServiceLocator.getTimeSource();
    this.originalTimeScale = timeSource != null ? timeSource.getTimeScale() : 1f;
  }

  @Override
  public void create() {
    super.create();
    if (entries.isEmpty()) {
      logger.warn("IntroDialogueComponent started with no dialogue entries");
      finishDialogue();
      return;
    }

    preloadPortraits();
    audioManager.preloadSounds(entries); // 预加载音频
    pauseGameTime();
    buildOverlay();
    advanceDialogue();
  }

  private void preloadPortraits() {
    ResourceService resourceService = ServiceLocator.getResourceService();
    if (resourceService == null) {
      logger.warn("ResourceService not available, portraits may not load correctly");
      return;
    }

    Set<String> textures = new LinkedHashSet<>();
    // 预加载头像纹理
    entries.stream().map(DialogueEntry::portraitPath).filter(Objects::nonNull).forEach(textures::add);
    // 预加载对话框背景纹理
    entries.stream().map(DialogueEntry::dialogueBackgroundPath).filter(Objects::nonNull).forEach(textures::add);
    if (textures.isEmpty()) {
      return;
    }

    String[] paths = textures.toArray(new String[0]);
    resourceService.loadTextures(paths);
    resourceService.loadAll();
  }

  private void pauseGameTime() {
    if (ServiceLocator.getTimeSource() != null) {
      ServiceLocator.getTimeSource().setTimeScale(0f);
    }
  }

  private void buildOverlay() {
    // 获取屏幕尺寸
    this.screenWidth = com.badlogic.gdx.Gdx.graphics.getWidth();
    this.screenHeight = com.badlogic.gdx.Gdx.graphics.getHeight();
    float screenWidth = this.screenWidth;
    float screenHeight = this.screenHeight;
    
    overlayRoot = new Table();
    overlayRoot.setFillParent(true);
    overlayRoot.setTouchable(Touchable.enabled);
    overlayRoot.defaults().pad(screenWidth * 0.01f);
    overlayRoot.align(Align.bottom); // 底部对齐
    overlayRoot.setBackground(SimpleUI.solid(new Color(0f, 0f, 0f, 0.45f)));
    overlayRoot.addListener(new InputListener() {
      @Override
      public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        return true;
      }
    });
    portraitImage = new Image();
    portraitImage.setScaling(com.badlogic.gdx.utils.Scaling.fit);
    dialogueTable = new Table();
    dialogueTable.align(Align.center);
    dialogueTable.defaults().pad(screenWidth * 0.005f);
    
    // 初始不设置背景，在advanceDialogue中根据每条对话动态设置
    dialogueTable.setTouchable(Touchable.enabled);

    // 创建自定义字体样式
    createDialogueFont();
    createSpeakerNameFont();
    
    dialogueLabel = new Label("", new Label.LabelStyle(dialogueFont, Color.WHITE));
    dialogueLabel.setWrap(true);
    dialogueLabel.setAlignment(Align.center, Align.center);

    // 创建对话者名字标签
    speakerNameLabel = new Label("", new Label.LabelStyle(speakerNameFont, Color.WHITE));
    speakerNameLabel.setAlignment(Align.center, Align.center);
    
    // 创建名字标签容器，添加黑色底纹背景
    speakerNameContainer = new Table();
    speakerNameContainer.align(Align.center);
    speakerNameContainer.setBackground(SimpleUI.solid(new Color(0f, 0f, 0f, 0.8f)));
    speakerNameContainer.add(speakerNameLabel).pad(8f, 12f, 8f, 12f); // 添加内边距
    speakerNameContainer.pack(); // 确保容器有正确的尺寸

    continueButton = new TextButton("continue", UIStyleHelper.continueButtonStyle());
    continueButton.getLabel().setColor(Color.WHITE);
    continueButton.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        advanceDialogue();
      }
    });

     skipButton = new TextButton("skip", UIStyleHelper.skipButtonStyle());
    skipButton.getLabel().setColor(Color.WHITE);
    skipButton.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        finishDialogue();
      }
    });

    // 创建对话内容容器
    Container<Label> dialogueContent = new Container<>(dialogueLabel);
    dialogueContent.align(Align.center);
    dialogueContent.fill();

    // 添加对话内容到对话框
    dialogueTable.add(dialogueContent)
            .grow()
            .center()
            .padTop(screenHeight * 0.05f);  // 添加顶部间距，让文字往下移动
    dialogueTable.row();

    float continueButtonWidth = screenWidth * 0.18f;  // 从0.135f增加到0.18f
    float continueButtonHeight = screenHeight * 0.2f;  // 从0.065f增加到0.08f
    float skipButtonWidth = screenWidth * 0.13f;
    float skipButtonHeight = screenHeight * 0.15f;

    // 对话框固定在底部中央（保持原始位置）
    dialogueWidth = screenWidth * 0.5f;
    dialogueHeight = screenHeight * 0.25f;
    dialogueCell = overlayRoot.add(dialogueTable)
            .width(dialogueWidth)
            .height(dialogueHeight)
            .bottom()
            .padBottom(screenHeight * 0.08f);
    
    // 创建独立的按钮容器，直接放在屏幕底部
    Table buttonContainer = new Table();
    buttonContainer.setFillParent(true);
    buttonContainer.align(Align.bottom);
    
    // continue按钮居中
    Table continueRow = new Table();
    continueRow.add().expandX();
    continueRow.add(continueButton)
            .width(continueButtonWidth)
            .height(continueButtonHeight)
            .center();
    continueRow.add().expandX();
    
    // skip按钮右对齐
    Table skipRow = new Table();
    skipRow.add().expandX();
    skipRow.add(skipButton)
            .width(skipButtonWidth)
            .height(skipButtonHeight)
            .right()
            .padRight(screenWidth * 0.15f);
    
    // 将两个按钮行叠加
    Stack buttonStack = new Stack();
    buttonStack.add(continueRow);
    buttonStack.add(skipRow);
    
    buttonContainer.add(buttonStack)
            .growX()
            .height(continueButtonHeight)
            .padBottom(screenHeight * 0.005f);
    
    // 将按钮容器添加到overlayRoot
    overlayRoot.addActor(buttonContainer);
    
    // 头像单独添加到stage，初始隐藏
    portraitImage.setSize(screenWidth * 0.15f, screenHeight * 0.3f);
    portraitImage.setVisible(false); // 初始隐藏，在advanceDialogue中显示和定位
    stage.addActor(portraitImage);
    
    // 对话者名字容器单独添加到stage，初始隐藏
    speakerNameContainer.setVisible(false); // 初始隐藏，在advanceDialogue中显示和定位
    stage.addActor(speakerNameContainer);
    dialogueTable.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        Actor target = event.getTarget();
        if (target != null
                && (target == skipButton
                    || skipButton.isAscendantOf(target)
                    || target == continueButton
                    || continueButton.isAscendantOf(target))) {
          return;
        }
        advanceDialogue();
      }
    });

    stage.addActor(overlayRoot);
  }

  private void advanceDialogue() {
    if (finished) {
      return;
    }

    // 停止之前的音频
    audioManager.stopCurrentSound();

    currentIndex++;
    if (currentIndex >= entries.size()) {
      finishDialogue();
      return;
    }

    DialogueEntry entry = entries.get(currentIndex);
    dialogueLabel.setText(entry.text());
    
    // 更新对话者名字
    if (entry.speakerName() != null && !entry.speakerName().isEmpty()) {
      speakerNameLabel.setText(entry.speakerName());
      speakerNameLabel.setColor(Color.WHITE); // 确保文字是白色
      speakerNameContainer.clearChildren(); // 清除旧内容
      speakerNameContainer.add(speakerNameLabel).pad(8f, 12f, 8f, 12f); // 重新添加标签
      speakerNameContainer.pack(); // 重新打包容器以适应新文本
      speakerNameContainer.setVisible(true);
    } else {
      speakerNameLabel.setText("");
      speakerNameContainer.setVisible(false);
    }

    // 更新对话框背景
    updateDialogueBackground(entry.dialogueBackgroundPath());

    Texture portraitTexture = resolveTexture(entry.portraitPath());
    if (portraitTexture != null) {
      portraitImage.setDrawable(new Image(portraitTexture).getDrawable());
      // 根据头像位置调整布局
      adjustPortraitLayout(entry.portraitSide());
      // 根据头像位置调整对话框文字位置
      adjustDialogueTextPosition(entry.portraitSide());
    } else {
      // 没有头像时隐藏
      portraitImage.setDrawable(null);
      portraitImage.setVisible(false);
      // 重置对话框文字位置
      adjustDialogueTextPosition(null);
    }

    // 播放对话音频
    audioManager.playSound(entry.soundPath());
  }

  private Texture resolveTexture(String path) {
    if (path == null || path.isBlank()) {
      return null;
    }
    ResourceService resourceService = ServiceLocator.getResourceService();
    if (resourceService == null) {
      return null;
    }
    return resourceService.getAsset(path, Texture.class);
  }

  /**
   * 更新对话框背景
   * @param backgroundPath 背景图片路径（可选，传null使用默认背景）
   */
  private void updateDialogueBackground(String backgroundPath) {
    if (backgroundPath != null && !backgroundPath.isBlank()) {
      Texture backgroundTexture = resolveTexture(backgroundPath);
      if (backgroundTexture != null) {
        TextureRegion textureRegion = new TextureRegion(backgroundTexture);
        TextureRegionDrawable drawable = new TextureRegionDrawable(textureRegion);
        dialogueTable.setBackground(drawable);
        updateDialogueSizing(backgroundTexture, isTalkerBackground(backgroundPath));
        return;
      } else {
        logger.warn("Failed to load dialogue background: {}", backgroundPath);
      }
    }
    
    // 使用默认背景
    dialogueTable.setBackground(SimpleUI.roundRect(new Color(0.96f, 0.94f, 0.88f, 0.92f),
            new Color(0.2f, 0.2f, 0.2f, 1f), 16, 2));
    updateDialogueSizing(null, false);
  }

  /**
   * 根据头像位置调整布局
   * @param portraitSide 头像位置（"left"或"right"）
   */
  private void adjustPortraitLayout(String portraitSide) {
    if (portraitSide == null || portraitSide.isBlank()) {
      portraitSide = "left"; // 默认为左侧
    }
    
    // 显示头像
    portraitImage.setVisible(true);
    
    float screenWidth = com.badlogic.gdx.Gdx.graphics.getWidth();
    float screenHeight = com.badlogic.gdx.Gdx.graphics.getHeight();
    float currentDialogueWidth = dialogueWidth > 0f ? dialogueWidth : screenWidth * 0.5f;
    // 计算对话框的位置（屏幕底部中央）
    float dialogueX = (screenWidth - currentDialogueWidth) / 2; // 对话框X位置（居中）
    float dialogueY = screenHeight * 0.08f; // 对话框Y位置（距底部）
    
    float portraitWidth = screenWidth * 0.15f;
    float portraitHeight = screenHeight * 0.3f;
    
    if ("right".equalsIgnoreCase(portraitSide)) {
      // 头像在对话框右侧
      float portraitX = dialogueX + currentDialogueWidth; // 头像直接贴着对话框
      float portraitY = dialogueY + screenHeight * 0.1f; // 头像往上移一点
      portraitImage.setPosition(portraitX, portraitY);
      
      // 对话者名字在头像上方居中，紧贴头像框
      if (speakerNameContainer.isVisible()) {
        float nameX = portraitX + portraitWidth / 2; // 头像中心X位置
        float nameY = portraitY + portraitHeight; // 紧贴头像顶部
        speakerNameContainer.setPosition(nameX - speakerNameContainer.getWidth() / 2, nameY);
      }
    } else {
      // 头像在对话框左侧（默认）
      float portraitX = dialogueX - portraitWidth; // 头像直接贴着对话框
      float portraitY = dialogueY + screenHeight * 0.1f; // 头像往上移一点，与右侧头像对齐
      portraitImage.setPosition(portraitX, portraitY);
      
      // 对话者名字在头像上方居中，紧贴头像框
      if (speakerNameContainer.isVisible()) {
        float nameX = portraitX + portraitWidth / 2; // 头像中心X位置
        float nameY = portraitY + portraitHeight; // 紧贴头像顶部
        speakerNameContainer.setPosition(nameX - speakerNameContainer.getWidth() / 2, nameY);
      }
    }
  }

  /**
   * 根据头像位置调整对话框文字位置
   * @param portraitSide 头像位置（"left"或"right"）
   */
  private void adjustDialogueTextPosition(String portraitSide) {
    if (dialogueTable == null) {
      return;
    }
    
    float screenHeight = com.badlogic.gdx.Gdx.graphics.getHeight();
    
    // 清除现有的对话内容
    dialogueTable.clearChildren();
    
    // 创建对话内容容器
    Container<Label> dialogueContent = new Container<>(dialogueLabel);
    dialogueContent.align(Align.center);
    dialogueContent.fill();
    
    // 根据头像位置设置不同的顶部间距
    float topPadding;
    if ("left".equalsIgnoreCase(portraitSide)) {
      // 头像在左侧时，将对话框文字向上移动
      topPadding = screenHeight * 0.02f; // 减少顶部间距
    } else {
      // 头像在右侧或没有头像时，使用默认位置
      topPadding = screenHeight * 0.05f; // 默认顶部间距
    }
    
    // 重新添加对话内容到对话框
    dialogueTable.add(dialogueContent)
            .grow()
            .center()
            .padTop(topPadding);
    dialogueTable.row();
    
    // 重新布局
    dialogueTable.invalidateHierarchy();
    overlayRoot.invalidateHierarchy();
  }

  private void finishDialogue() {
    if (finished) {
      return;
    }
    finished = true;

    // 停止当前播放的音频并清理资源
    audioManager.dispose();

    // 显示"Ready for Fight!"提示
    showReadyForFightMessage();

    if (overlayRoot != null) {
      overlayRoot.remove();
      overlayRoot = null;
    }

    // 移除头像
    if (portraitImage != null) {
      portraitImage.remove();
      portraitImage = null;
    }

    // 移除对话者名字容器
    if (speakerNameContainer != null) {
      speakerNameContainer.remove();
      speakerNameContainer = null;
    }

    if (ServiceLocator.getTimeSource() != null) {
      ServiceLocator.getTimeSource().setTimeScale(originalTimeScale);
    }

    if (onComplete != null) {
      try {
        onComplete.run();
      } catch (Exception e) {
        logger.error("Error running intro dialogue completion callback", e);
      }
    }

    entity.dispose();
  }

  @Override
  protected void draw(SpriteBatch batch) {
    // Stage handles drawing
  }

  /**
   * 创建对话框字体样式
   * 您可以选择不同的字体风格：
   * 1. pixel_32.fnt - 像素风格字体（推荐用于游戏对话）
   * 2. arial_black_32.fnt - 粗体Arial字体（清晰易读）
   * 3. segoe_ui_18.fnt - 现代UI字体
   * 4. 默认字体 - 简单默认字体
   */
  private void createDialogueFont() {
    try {
      float screenWidth = com.badlogic.gdx.Gdx.graphics.getWidth();
      float screenHeight = com.badlogic.gdx.Gdx.graphics.getHeight();
      
      // 根据屏幕分辨率选择不同大小的字体文件
      String fontPath;
      if (screenWidth >= 2560 || screenHeight >= 1440) {
        // 高分辨率屏幕 (2K/4K) - 使用更大的字体
        fontPath = "flat-earth/skin/fonts/pixel_32.fnt";
      } else if (screenWidth >= 1920 || screenHeight >= 1080) {
        // 标准分辨率屏幕 (1080p) - 使用中等字体
        fontPath = "flat-earth/skin/fonts/pixel_32.fnt";
      } else {
        // 低分辨率屏幕 (720p及以下) - 使用较小字体
        fontPath = "flat-earth/skin/fonts/segoe_ui_18.fnt";
      }
      
      dialogueFont = new BitmapFont(com.badlogic.gdx.Gdx.files.internal(fontPath));
      dialogueFont.getData().setScale(0.8f); // 缩放到80%
      dialogueFont.setColor(Color.WHITE);
    } catch (Exception e) {
      logger.warn("Failed to load custom font, using default font", e);
      // 如果加载失败，使用默认字体
      dialogueFont = SimpleUI.font();
      dialogueFont.getData().setScale(0.8f); // 缩放到80%
      dialogueFont.setColor(Color.WHITE);
    }
  }

  /**
   * 创建对话者名字字体样式
   */
  private void createSpeakerNameFont() {
    try {
      float screenWidth = com.badlogic.gdx.Gdx.graphics.getWidth();
      float screenHeight = com.badlogic.gdx.Gdx.graphics.getHeight();
      
      // 根据屏幕分辨率选择不同大小的字体文件
      String fontPath;
      if (screenWidth >= 2560 || screenHeight >= 1440) {
        // 高分辨率屏幕 (2K/4K) - 使用更大的字体
        fontPath = "flat-earth/skin/fonts/arial_black_32.fnt";
      } else if (screenWidth >= 1920 || screenHeight >= 1080) {
        // 标准分辨率屏幕 (1080p) - 使用中等字体
        fontPath = "flat-earth/skin/fonts/arial_black_32.fnt";
      } else {
        // 低分辨率屏幕 (720p及以下) - 使用较小字体
        fontPath = "flat-earth/skin/fonts/segoe_ui_18.fnt";
      }
      
      speakerNameFont = new BitmapFont(com.badlogic.gdx.Gdx.files.internal(fontPath));
      speakerNameFont.setColor(Color.WHITE);
    } catch (Exception e) {
      logger.warn("Failed to load speaker name font, using default font", e);
      // 如果加载失败，使用默认字体
      speakerNameFont = SimpleUI.font();
      speakerNameFont.setColor(Color.WHITE);
    }
  }

  @Override
  public void dispose() {
    if (!finished) {
      finishDialogue();
    }
    // 释放自定义字体资源
    if (dialogueFont != null && dialogueFont != SimpleUI.font()) {
      dialogueFont.dispose();
    }
    if (speakerNameFont != null && speakerNameFont != SimpleUI.font()) {
      speakerNameFont.dispose();
    }
    super.dispose();
  }

  /**
   * 对话条目记录
   * @param text 对话文本
   * @param portraitPath 头像图片路径（可选，传null表示不显示头像）
   * @param soundPath 对话音频路径（可选，传null表示无音频）
   * @param portraitSide 头像位置（"left"或"right"，可选，默认为"left"）
   * @param dialogueBackgroundPath 对话框背景图片路径（可选，传null使用默认背景）
   * @param speakerName 对话者名字（可选，传null表示不显示名字）
   */
  public record DialogueEntry(String text, String portraitPath, String soundPath, String portraitSide, String dialogueBackgroundPath, String speakerName) {
    /**
     * 创建不带音频的对话条目（向后兼容）
     */
    public DialogueEntry(String text, String portraitPath) {
      this(text, portraitPath, null, "left", null, null);
    }
    
    /**
     * 创建带音频的对话条目（向后兼容）
     */
    public DialogueEntry(String text, String portraitPath, String soundPath) {
      this(text, portraitPath, soundPath, "left", null, null);
    }
    
    /**
     * 创建带头像位置的对话条目（向后兼容）
     */
    public DialogueEntry(String text, String portraitPath, String soundPath, String portraitSide) {
      this(text, portraitPath, soundPath, portraitSide, null, null);
    }
    
    /**
     * 创建带对话框背景的对话条目（向后兼容）
     */
    public DialogueEntry(String text, String portraitPath, String soundPath, String portraitSide, String dialogueBackgroundPath) {
      this(text, portraitPath, soundPath, portraitSide, dialogueBackgroundPath, null);
    }
  }

  private void updateDialogueSizing(Texture backgroundTexture, boolean preserveAspect) {
    if (dialogueCell == null) {
      return;
    }

    if (preserveAspect && backgroundTexture != null) {
      float maxWidth = screenWidth * 0.715f;
      float maxHeight = screenHeight * 0.455f;
      float textureWidth = backgroundTexture.getWidth();
      float textureHeight = backgroundTexture.getHeight();
      if (textureWidth <= 0f || textureHeight <= 0f) {
        preserveAspect = false;
      } else {
        float scale = Math.min(maxWidth / textureWidth, maxHeight / textureHeight);
        if (scale <= 0f) {
          scale = 1f;
        }
        dialogueWidth = textureWidth * scale;
        dialogueHeight = textureHeight * scale;
      }
    }

    if (!preserveAspect || backgroundTexture == null) {
      dialogueWidth = screenWidth * 0.5f;
      dialogueHeight = screenHeight * 0.25f;
    }

    dialogueCell.width(dialogueWidth);
    dialogueCell.height(dialogueHeight);
    dialogueTable.invalidateHierarchy();
    overlayRoot.invalidateHierarchy();
  }

  private boolean isTalkerBackground(String path) {
    if (path == null) {
      return false;
    }
    String normalised = path.replace("\\", "/").toLowerCase(Locale.ROOT);
    return normalised.endsWith("talker.png") || normalised.endsWith("talker2.png");
  }

  /**
   * 显示"Ready for Fight!"提示消息
   */
  private void showReadyForFightMessage() {
    // 创建更大更清晰的字体
    BitmapFont largeFont = createLargeFont();
    
    // 分解文本为三个单词
    String[] words = {"Ready", "for", "Fight!"};
    float screenWidth = com.badlogic.gdx.Gdx.graphics.getWidth();
    float screenHeight = com.badlogic.gdx.Gdx.graphics.getHeight();
    
    // 为每个单词创建标签
    Label[] wordLabels = new Label[words.length];
    for (int i = 0; i < words.length; i++) {
      // 使用指定的颜色 RGB(246, 255, 205) - 淡黄绿色
      Color customColor = new Color(246f/255f, 255f/255f, 205f/255f, 1f);
      Label.LabelStyle labelStyle = new Label.LabelStyle(largeFont, customColor);
      wordLabels[i] = new Label(words[i], labelStyle);
      wordLabels[i].setAlignment(Align.center);
      
      // 设置初始位置（屏幕中央）
      wordLabels[i].setPosition(
          (screenWidth - wordLabels[i].getPrefWidth()) / 2f,
          (screenHeight - wordLabels[i].getPrefHeight()) / 2f
      );
      
      // 设置初始缩放（很大）
      wordLabels[i].setScale(3.0f);
      
      // 初始隐藏所有单词
      wordLabels[i].setVisible(false);
      
      // 添加到舞台
      stage.addActor(wordLabels[i]);
    }
    
    // 为每个单词添加动画效果
    animateWordsSequentially(wordLabels, largeFont);
  }
  
  /**
   * 为单词添加顺序动画效果
   */
  private void animateWordsSequentially(Label[] wordLabels, BitmapFont largeFont) {
    animateSingleWord(wordLabels, 0, largeFont);
  }
  
  /**
   * 为单个单词添加动画效果
   */
  private void animateSingleWord(Label[] wordLabels, int wordIndex, BitmapFont largeFont) {
    if (wordIndex >= wordLabels.length) {
      // 所有单词动画完成，清理资源
      com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
        @Override
        public void run() {
          // 清理所有标签
          for (Label label : wordLabels) {
            if (label != null) {
              label.remove();
            }
          }
          // 清理字体资源
          if (largeFont != null && largeFont != SimpleUI.font()) {
            largeFont.dispose();
          }
        }
      }, 0.1f); // 最后一个单词消失后立即清理
      return;
    }
    
    final Label currentLabel = wordLabels[wordIndex];
    final int nextWordIndex = wordIndex + 1;
    
    // 显示当前单词
    currentLabel.setVisible(true);
    
    // 开始缩放动画：从3.0倍缩放到1.0倍
    animateScale(currentLabel, 3.0f, 1.0f, 0.4f, () -> {
      // 缩放完成后，等待0.2秒然后开始消失动画
      com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
        @Override
        public void run() {
          // 开始消失动画：从1.0倍匀速缩小到0.1倍
          animateScaleLinear(currentLabel, 1.0f, 0.1f, 0.2f, () -> {
            // 消失完成后隐藏单词
            currentLabel.setVisible(false);
            
            // 立即开始下一个单词的动画
            animateSingleWord(wordLabels, nextWordIndex, largeFont);
          });
        }
      }, 0.2f);
    });
  }
  
  /**
   * 执行缩放动画
   */
  private void animateScale(Label label, float startScale, float endScale, float duration, Runnable onComplete) {
    final float[] currentTime = {0f};
    final float totalDuration = duration;
    
    com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
      @Override
      public void run() {
        currentTime[0] += 0.016f; // 约60FPS
        
        if (currentTime[0] >= totalDuration) {
          label.setScale(endScale);
          this.cancel();
          if (onComplete != null) {
            onComplete.run();
          }
          return;
        }
        
        // 使用缓动函数创建平滑动画
        //float progress = currentTime[0] / totalDuration;
        //float easedProgress = easeOutElastic(progress); // 使用弹性缓动产生跳动效果
        
        //float currentScale = startScale + (endScale - startScale) * easedProgress;
        //label.setScale(currentScale);
      }
    }, 0f, 0.016f); // 每帧执行一次
  }
  
  /**
   * 执行匀速缩放动画
   */
  private void animateScaleLinear(Label label, float startScale, float endScale, float duration, Runnable onComplete) {
    final float[] currentTime = {0f};
    final float totalDuration = duration;
    
    com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
      @Override
      public void run() {
        currentTime[0] += 0.016f; // 约60FPS
        
        if (currentTime[0] >= totalDuration) {
          label.setScale(endScale);
          this.cancel();
          if (onComplete != null) {
            onComplete.run();
          }
          return;
        }
        
        // 使用线性插值创建匀速动画
        float progress = currentTime[0] / totalDuration;
        float currentScale = startScale + (endScale - startScale) * progress;
        label.setScale(currentScale);
      }
    }, 0f, 0.016f); // 每帧执行一次
  }
  
  /**
   * 弹跳缓动函数 - 产生明显的弹跳效果
   */
  // private float easeOutBounce(float t) {
  //   if (t < 1f / 2.75f) {
  //     return 7.5625f * t * t;
  //   } else if (t < 2f / 2.75f) {
  //     t -= 1.5f / 2.75f;
  //     return 7.5625f * t * t + 0.75f;
  //   } else if (t < 2.5f / 2.75f) {
  //     t -= 2.25f / 2.75f;
  //     return 7.5625f * t * t + 0.9375f;
  //   } else {
  //     t -= 2.625f / 2.75f;
  //     return 7.5625f * t * t + 0.984375f;
  //   }
  // }
  
  // /**
  //  * 弹性缓动函数 - 产生更明显的弹性效果
  //  */
  // private float easeOutElastic(float t) {
  //   if (t == 0) return 0;
  //   if (t == 1) return 1;
    
  //   float p = 0.3f;
  //   float s = p / 4f;
    
  //   return (float) (Math.pow(2, -10 * t) * Math.sin((t - s) * (2 * Math.PI) / p) + 1);
  // }
  
  /**
   * 创建大字体
   */
  private BitmapFont createLargeFont() {
    try {
      // 尝试加载更大的字体
      BitmapFont font = new BitmapFont(com.badlogic.gdx.Gdx.files.internal("flat-earth/skin/fonts/arial_black_32.fnt"));
      font.getData().setScale(2.0f); // 放大2倍
      return font;
    } catch (Exception e) {
      // 如果加载失败，使用默认字体并放大
      BitmapFont defaultFont = SimpleUI.font();
      defaultFont.getData().setScale(3.0f); // 放大3倍
      return defaultFont;
    }
  }
}
