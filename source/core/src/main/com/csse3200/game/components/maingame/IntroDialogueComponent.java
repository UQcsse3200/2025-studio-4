package com.csse3200.game.components.maingame;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.utils.Align;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.SimpleUI;
import com.csse3200.game.ui.UIComponent;
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
  private Image portraitImage;
  private Label dialogueLabel;
  private TextButton skipButton;
  private int currentIndex = -1;
  private boolean finished = false;

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
    entries.stream().map(DialogueEntry::portraitPath).filter(Objects::nonNull).forEach(textures::add);
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
    overlayRoot = new Table();
    overlayRoot.setFillParent(true);
    overlayRoot.setTouchable(Touchable.enabled);
    overlayRoot.defaults().pad(20f);
    overlayRoot.align(Align.bottom); // 底部对齐，水平居中
    overlayRoot.setBackground(SimpleUI.solid(new Color(0f, 0f, 0f, 0.45f)));
    overlayRoot.addListener(new InputListener() {
      @Override
      public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        return true;
      }
    });
    portraitImage = new Image();
    portraitImage.setScaling(com.badlogic.gdx.utils.Scaling.fit);
    Table dialogueTable = new Table();
    dialogueTable.align(Align.topLeft);
    dialogueTable.defaults().pad(10f);
    dialogueTable.setBackground(SimpleUI.roundRect(new Color(0.96f, 0.94f, 0.88f, 0.92f),
            new Color(0.2f, 0.2f, 0.2f, 1f), 16, 2));
    dialogueTable.setTouchable(Touchable.enabled);

    dialogueLabel = new Label("", SimpleUI.label());
    dialogueLabel.setWrap(true);
    dialogueLabel.setAlignment(Align.topLeft);
    dialogueLabel.setColor(Color.BLACK);
    dialogueLabel.setFontScale(1.1f);

    final TextButton continueButton = new TextButton("continue", SimpleUI.primaryButton());
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

    dialogueTable.add(dialogueLabel).width(1040f).top().left().row();
    dialogueTable.add(continueButton)
            .width(260f)
            .height(70f)
            .padTop(220f)
            .center()
            .expandX();
    dialogueTable.row();

    Table skipRow = new Table();
    skipRow.add().expandX();
    skipRow.add(skipButton).width(180f).height(60f).right();
    dialogueTable.add(skipRow).growX();
    Table topRow = new Table();
    topRow.align(Align.topLeft);
    topRow.add(portraitImage).width(400f).height(440f).left().top().padRight(30f);
    topRow.add(dialogueTable).width(1200f).minHeight(320f).top().left();

    overlayRoot.add(topRow).expand().bottom().padBottom(40f); // 底部对齐，左右居中
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

    Texture portraitTexture = resolveTexture(entry.portraitPath());
    if (portraitTexture != null) {
      portraitImage.setDrawable(new Image(portraitTexture).getDrawable());
    } else {
      portraitImage.setDrawable(null);
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

  @Override
  public void dispose() {
    if (!finished) {
      finishDialogue();
    }
    super.dispose();
  }

  /**
   * 对话条目记录
   * @param text 对话文本
   * @param portraitPath 头像图片路径（可选，传null表示不显示头像）
   * @param soundPath 对话音频路径（可选，传null表示无音频）
   */
  public record DialogueEntry(String text, String portraitPath, String soundPath) {
    /**
     * 创建不带音频的对话条目（向后兼容）
     */
    public DialogueEntry(String text, String portraitPath) {
      this(text, portraitPath, null);
    }
  }
}
