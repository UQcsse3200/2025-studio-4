package com.csse3200.game.components.maingame;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
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
  private TextButton continueButton; // 继续按钮
  private TextButton skipButton;
  private int currentIndex = -1;
  private boolean finished = false;
  
  // 字体样式选择
  private BitmapFont dialogueFont;

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
    float screenWidth = com.badlogic.gdx.Gdx.graphics.getWidth();
    float screenHeight = com.badlogic.gdx.Gdx.graphics.getHeight();
    
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
    
    dialogueLabel = new Label("", new Label.LabelStyle(dialogueFont, Color.WHITE));
    dialogueLabel.setWrap(true);
    dialogueLabel.setAlignment(Align.center, Align.center);

    continueButton = new TextButton("continue", UIStyleHelper.orangeButtonStyle());
    continueButton.getLabel().setColor(Color.WHITE);
    continueButton.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        advanceDialogue();
      }
    });

    skipButton = new TextButton("skip", SimpleUI.darkButton());
    skipButton.getLabel().setColor(Color.WHITE);
    skipButton.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        finishDialogue();
      }
    });

    Container<Label> dialogueContent = new Container<>(dialogueLabel);
    dialogueContent.align(Align.center);
    dialogueContent.fill();

    dialogueTable.add(dialogueContent)
            .width(screenWidth * 0.45f)
            .expand()
            .fill()
            .center()
            .padTop(screenHeight * 0.05f)  // 添加顶部间距，让文字往下移动
            .row();

    float continueButtonWidth = screenWidth * 0.135f;
    float continueButtonHeight = screenHeight * 0.065f;
    float skipButtonWidth = screenWidth * 0.094f;
    float skipButtonHeight = screenHeight * 0.056f;
    float buttonRowHeight = Math.max(continueButtonHeight, skipButtonHeight);

    Table continueRow = new Table();
    continueRow.align(Align.bottom);
    continueRow.setFillParent(true);
    continueRow.add().expandX();
    continueRow.add(continueButton)
            .width(continueButtonWidth)
            .height(continueButtonHeight)
            .center();
    continueRow.add().expandX();

    Table skipRow = new Table();
    skipRow.align(Align.bottomRight);
    skipRow.setFillParent(true);
    skipRow.add(skipButton)
            .width(skipButtonWidth)
            .height(skipButtonHeight)
            .right();

    Stack buttonStack = new Stack();
    buttonStack.add(continueRow);
    buttonStack.add(skipRow);

    dialogueTable.add(buttonStack)
            .growX()
            .height(buttonRowHeight)
            .padTop(screenHeight * 0.02f);
    
    // 对话框固定在底部中央
    overlayRoot.add(dialogueTable).width(screenWidth * 0.5f).minHeight(screenHeight * 0.25f).bottom().padBottom(screenHeight * 0.037f);
    
    // 头像单独添加到stage，初始隐藏
    portraitImage.setSize(screenWidth * 0.21f, screenHeight * 0.41f);
    portraitImage.setVisible(false); // 初始隐藏，在advanceDialogue中显示和定位
    stage.addActor(portraitImage);
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

    // 更新对话框背景
    updateDialogueBackground(entry.dialogueBackgroundPath());

    Texture portraitTexture = resolveTexture(entry.portraitPath());
    if (portraitTexture != null) {
      portraitImage.setDrawable(new Image(portraitTexture).getDrawable());
      // 根据头像位置调整布局
      adjustPortraitLayout(entry.portraitSide());
    } else {
      // 没有头像时隐藏
      portraitImage.setDrawable(null);
      portraitImage.setVisible(false);
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
        return;
      } else {
        logger.warn("Failed to load dialogue background: {}", backgroundPath);
      }
    }
    
    // 使用默认背景
    dialogueTable.setBackground(SimpleUI.roundRect(new Color(0.96f, 0.94f, 0.88f, 0.92f),
            new Color(0.2f, 0.2f, 0.2f, 1f), 16, 2));
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
    
    // 计算对话框的位置（屏幕底部中央）
    float dialogueX = (screenWidth - screenWidth * 0.5f) / 2; // 对话框X位置（居中）
    float dialogueY = screenHeight * 0.037f; // 对话框Y位置（距底部）
    
    float portraitWidth = screenWidth * 0.21f;
    
    if ("right".equalsIgnoreCase(portraitSide)) {
      // 头像在对话框右侧
      float portraitX = dialogueX + screenWidth * 0.5f; // 头像直接贴着对话框
      float portraitY = dialogueY;
      portraitImage.setPosition(portraitX, portraitY);
    } else {
      // 头像在对话框左侧（默认）
      float portraitX = dialogueX - portraitWidth; // 头像直接贴着对话框
      float portraitY = dialogueY;
      portraitImage.setPosition(portraitX, portraitY);
    }
  }

  private void finishDialogue() {
    if (finished) {
      return;
    }
    finished = true;

    // 停止当前播放的音频并清理资源
    audioManager.dispose();

    if (overlayRoot != null) {
      overlayRoot.remove();
      overlayRoot = null;
    }

    // 移除头像
    if (portraitImage != null) {
      portraitImage.remove();
      portraitImage = null;
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
      // 选择字体风格 - 您可以修改这里来改变字体
      dialogueFont = new BitmapFont(com.badlogic.gdx.Gdx.files.internal("flat-earth/skin/fonts/pixel_32.fnt"));
      dialogueFont.getData().setScale(0.8f); // 调整字体大小
      dialogueFont.setColor(Color.WHITE);
    } catch (Exception e) {
      logger.warn("Failed to load custom font, using default font", e);
      // 如果加载失败，使用默认字体
      dialogueFont = SimpleUI.font();
      dialogueFont.setColor(Color.WHITE);
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
    super.dispose();
  }

  /**
   * 对话条目记录
   * @param text 对话文本
   * @param portraitPath 头像图片路径（可选，传null表示不显示头像）
   * @param soundPath 对话音频路径（可选，传null表示无音频）
   * @param portraitSide 头像位置（"left"或"right"，可选，默认为"left"）
   * @param dialogueBackgroundPath 对话框背景图片路径（可选，传null使用默认背景）
   */
  public record DialogueEntry(String text, String portraitPath, String soundPath, String portraitSide, String dialogueBackgroundPath) {
    /**
     * 创建不带音频的对话条目（向后兼容）
     */
    public DialogueEntry(String text, String portraitPath) {
      this(text, portraitPath, null, "left", null);
    }
    
    /**
     * 创建带音频的对话条目（向后兼容）
     */
    public DialogueEntry(String text, String portraitPath, String soundPath) {
      this(text, portraitPath, soundPath, "left", null);
    }
    
    /**
     * 创建带头像位置的对话条目（向后兼容）
     */
    public DialogueEntry(String text, String portraitPath, String soundPath, String portraitSide) {
      this(text, portraitPath, soundPath, portraitSide, null);
    }
  }
}
