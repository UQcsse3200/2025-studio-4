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
import com.csse3200.game.services.GameStateService;
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

/**
 * Component that displays introduction dialogue sequences with portraits, audio, and custom backgrounds.
 * This component pauses game time while active and provides skip/continue functionality.
 * 
 * @author Team1
 * @since sprint 4
 */
public class IntroDialogueComponent extends UIComponent {
  private static final Logger logger = LoggerFactory.getLogger(IntroDialogueComponent.class);

  private final List<DialogueEntry> entries;
  private final Runnable onComplete;
  private final float originalTimeScale;
  /** Audio manager for handling dialogue sound effects */
  private final DialogueAudioManager audioManager;

  private Table overlayRoot;
  /** Table container for dialogue box */
  private Table dialogueTable;
  private Image portraitImage;
  private Label dialogueLabel;
  /** Label displaying the speaker's name */
  private Label speakerNameLabel;
  /** Container for speaker name label with background */
  private Table speakerNameContainer;
  /** Button to continue to next dialogue */
  private TextButton continueButton;
  private TextButton skipButton;
  private int currentIndex = -1;
  private boolean finished = false;
  private float screenWidth;
  private float screenHeight;
  private float dialogueWidth;
  private float dialogueHeight;
  private Cell<Table> dialogueCell;
  
  /** Font style for dialogue text */
  private BitmapFont dialogueFont;
  /** Font style for speaker name */
  private BitmapFont speakerNameFont;

  /**
   * Constructs a new IntroDialogueComponent with specified dialogue entries and completion callback.
   * 
   * @param entries List of dialogue entries to display in sequence
   * @param onComplete Callback to execute when dialogue sequence completes or is skipped
   * @throws NullPointerException if entries is null
   */
  public IntroDialogueComponent(List<DialogueEntry> entries, Runnable onComplete) {
    this.entries = Objects.requireNonNull(entries, "entries");
    this.onComplete = onComplete;
    this.audioManager = new DialogueAudioManager();
    var timeSource = ServiceLocator.getTimeSource();
    this.originalTimeScale = timeSource != null ? timeSource.getTimeScale() : 1f;
  }

  /**
   * Initializes the dialogue component by preloading resources, pausing game time,
   * building the UI overlay, and starting the dialogue sequence.
   */
  @Override
  public void create() {
    super.create();
    GameStateService gameState = ServiceLocator.getGameStateService();
    if (gameState != null) {
      gameState.resetReadyPromptFinished();
    }
    if (entries.isEmpty()) {
      logger.warn("IntroDialogueComponent started with no dialogue entries");
      finishDialogue();
      return;
    }

    preloadPortraits();
    audioManager.preloadSounds(entries);
    pauseGameTime();
    buildOverlay();
    advanceDialogue();
  }

  /**
   * Preloads all portrait textures and dialogue background textures from dialogue entries.
   * This ensures smooth transitions between dialogues without loading delays.
   */
  private void preloadPortraits() {
    ResourceService resourceService = ServiceLocator.getResourceService();
    if (resourceService == null) {
      logger.warn("ResourceService not available, portraits may not load correctly");
      return;
    }

    Set<String> textures = new LinkedHashSet<>();
    // Preload Portrait Textures
    entries.stream().map(DialogueEntry::portraitPath).filter(Objects::nonNull).forEach(textures::add);
    // Preload Dialogue Box Background Textures
    entries.stream().map(DialogueEntry::dialogueBackgroundPath).filter(Objects::nonNull).forEach(textures::add);
    if (textures.isEmpty()) {
      return;
    }

    String[] paths = textures.toArray(new String[0]);
    resourceService.loadTextures(paths);
    resourceService.loadAll();
  }

  /**
   * Pauses game time by setting the time scale to 0.
   * The original time scale is restored when dialogue finishes.
   */
  private void pauseGameTime() {
    if (ServiceLocator.getTimeSource() != null) {
      ServiceLocator.getTimeSource().setTimeScale(0f);
    }
  }

  /**
   * Builds the UI overlay including dialogue box, buttons, portrait image, and speaker name container.
   * Creates the complete dialogue interface with proper positioning and styling.
   */
  private void buildOverlay() {
    this.screenWidth = com.badlogic.gdx.Gdx.graphics.getWidth();
    this.screenHeight = com.badlogic.gdx.Gdx.graphics.getHeight();
    float screenWidth = this.screenWidth;
    float screenHeight = this.screenHeight;
    
    overlayRoot = new Table();
    overlayRoot.setFillParent(true);
    overlayRoot.setTouchable(Touchable.enabled);
    overlayRoot.defaults().pad(screenWidth * 0.01f);
    overlayRoot.align(Align.bottom);
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
    
    // Background will be set dynamically in advanceDialogue for each dialogue entry
    dialogueTable.setTouchable(Touchable.enabled);

    // Create custom font styles
    createDialogueFont();
    createSpeakerNameFont();
    
    dialogueLabel = new Label("", new Label.LabelStyle(dialogueFont, Color.WHITE));
    dialogueLabel.setWrap(true);
    dialogueLabel.setAlignment(Align.center, Align.center);

    // Create speaker name label
    speakerNameLabel = new Label("", new Label.LabelStyle(speakerNameFont, Color.WHITE));
    speakerNameLabel.setAlignment(Align.center, Align.center);
    
    // Create speaker name container with dark background
    speakerNameContainer = new Table();
    speakerNameContainer.align(Align.center);
    speakerNameContainer.setBackground(SimpleUI.solid(new Color(0f, 0f, 0f, 0.8f)));
    speakerNameContainer.add(speakerNameLabel).pad(8f, 12f, 8f, 12f);
    speakerNameContainer.pack();

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

    // Create dialogue content container
    Container<Label> dialogueContent = new Container<>(dialogueLabel);
    dialogueContent.align(Align.center);
    dialogueContent.fill();

    // Add dialogue content to dialogue table
    dialogueTable.add(dialogueContent)
            .grow()
            .center()
            .padTop(screenHeight * 0.05f);
    dialogueTable.row();

    float continueButtonWidth = screenWidth * 0.18f;
    float continueButtonHeight = screenHeight * 0.2f;
    float skipButtonWidth = screenWidth * 0.13f;
    float skipButtonHeight = screenHeight * 0.15f;

    // Dialogue box fixed at bottom center
    dialogueWidth = screenWidth * 0.5f;
    dialogueHeight = screenHeight * 0.25f;
    dialogueCell = overlayRoot.add(dialogueTable)
            .width(dialogueWidth)
            .height(dialogueHeight)
            .bottom()
            .padBottom(screenHeight * 0.08f);
    
    // Create separate button container at screen bottom
    Table buttonContainer = new Table();
    buttonContainer.setFillParent(true);
    buttonContainer.align(Align.bottom);
    
    // Continue button centered
    Table continueRow = new Table();
    continueRow.add().expandX();
    continueRow.add(continueButton)
            .width(continueButtonWidth)
            .height(continueButtonHeight)
            .center();
    continueRow.add().expandX();
    
    // Skip button right-aligned
    Table skipRow = new Table();
    skipRow.add().expandX();
    skipRow.add(skipButton)
            .width(skipButtonWidth)
            .height(skipButtonHeight)
            .right()
            .padRight(screenWidth * 0.15f);
    
    // Stack button rows
    Stack buttonStack = new Stack();
    buttonStack.add(continueRow);
    buttonStack.add(skipRow);
    
    buttonContainer.add(buttonStack)
            .growX()
            .height(continueButtonHeight)
            .padBottom(screenHeight * 0.005f);
    
    // Add button container to overlayRoot
    overlayRoot.addActor(buttonContainer);
    
    // Add portrait image to stage, initially hidden
    portraitImage.setSize(screenWidth * 0.15f, screenHeight * 0.3f);
    portraitImage.setVisible(false);
    stage.addActor(portraitImage);
    
    // Add speaker name container to stage, initially hidden
    speakerNameContainer.setVisible(false);
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

  /**
   * Advances to the next dialogue entry in the sequence.
   * Stops current audio, updates text, portrait, speaker name, and background.
   * Finishes dialogue if no more entries remain.
   */
  private void advanceDialogue() {
    if (finished) {
      return;
    }

    // Stop previous audio
    audioManager.stopCurrentSound();

    currentIndex++;
    if (currentIndex >= entries.size()) {
      finishDialogue();
      return;
    }

    DialogueEntry entry = entries.get(currentIndex);
    dialogueLabel.setText(entry.text());
    
    // Apply font scaling
    if (entry.fontScale() != null) {
      dialogueFont.getData().setScale(entry.fontScale());
    }
    
    // Update speaker name
    if (entry.speakerName() != null && !entry.speakerName().isEmpty()) {
      speakerNameLabel.setText(entry.speakerName());
      speakerNameLabel.setColor(Color.WHITE);
      speakerNameContainer.clearChildren();
      speakerNameContainer.add(speakerNameLabel).pad(8f, 12f, 8f, 12f);
      speakerNameContainer.pack();
      speakerNameContainer.setVisible(true);
    } else {
      speakerNameLabel.setText("");
      speakerNameContainer.setVisible(false);
    }

    // Update dialogue background
    updateDialogueBackground(entry.dialogueBackgroundPath());

    Texture portraitTexture = resolveTexture(entry.portraitPath());
    if (portraitTexture != null) {
      portraitImage.setDrawable(new Image(portraitTexture).getDrawable());
      // Adjust layout based on portrait position
      adjustPortraitLayout(entry.portraitSide());
      // Adjust dialogue text position based on portrait position
      adjustDialogueTextPosition(entry.portraitSide());
    } else {
      // Hide portrait when not available
      portraitImage.setDrawable(null);
      portraitImage.setVisible(false);
      // Reset dialogue text position
      adjustDialogueTextPosition(null);
    }

    // Play dialogue audio
    audioManager.playSound(entry.soundPath());
  }

  /**
   * Resolves a texture from a given file path using the ResourceService.
   * 
   * @param path Path to the texture file
   * @return Texture object if found and loaded, null otherwise
   */
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
   * Updates the dialogue box background with custom texture or default style.
   * 
   * @param backgroundPath Path to background image (optional, null uses default background)
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
    
    // Use default background
    dialogueTable.setBackground(SimpleUI.roundRect(new Color(0.96f, 0.94f, 0.88f, 0.92f),
            new Color(0.2f, 0.2f, 0.2f, 1f), 16, 2));
    updateDialogueSizing(null, false);
  }

  /**
   * Adjusts the portrait image layout based on its specified side position.
   * Also positions the speaker name container above the portrait.
   * 
   * @param portraitSide Portrait position ("left" or "right", defaults to "left" if null)
   */
  private void adjustPortraitLayout(String portraitSide) {
    if (portraitSide == null || portraitSide.isBlank()) {
      portraitSide = "left";
    }
    
    // Show portrait
    portraitImage.setVisible(true);
    
    float screenWidth = com.badlogic.gdx.Gdx.graphics.getWidth();
    float screenHeight = com.badlogic.gdx.Gdx.graphics.getHeight();
    float currentDialogueWidth = dialogueWidth > 0f ? dialogueWidth : screenWidth * 0.5f;
    // Calculate dialogue box position (bottom center of screen)
    float dialogueX = (screenWidth - currentDialogueWidth) / 2;
    float dialogueY = screenHeight * 0.08f;
    
    float portraitWidth = screenWidth * 0.15f;
    float portraitHeight = screenHeight * 0.3f;
    
    if ("right".equalsIgnoreCase(portraitSide)) {
      // Portrait on right side of dialogue box
      float portraitX = dialogueX + currentDialogueWidth;
      float portraitY = dialogueY + screenHeight * 0.1f;
      portraitImage.setPosition(portraitX, portraitY);
      
      // Speaker name centered above portrait, flush with portrait frame
      if (speakerNameContainer.isVisible()) {
        float nameX = portraitX + portraitWidth / 2;
        float nameY = portraitY + portraitHeight;
        speakerNameContainer.setPosition(nameX - speakerNameContainer.getWidth() / 2, nameY);
      }
    } else {
      // Portrait on left side of dialogue box (default)
      float portraitX = dialogueX - portraitWidth;
      float portraitY = dialogueY + screenHeight * 0.1f;
      portraitImage.setPosition(portraitX, portraitY);
      
      // Speaker name centered above portrait, flush with portrait frame
      if (speakerNameContainer.isVisible()) {
        float nameX = portraitX + portraitWidth / 2;
        float nameY = portraitY + portraitHeight;
        speakerNameContainer.setPosition(nameX - speakerNameContainer.getWidth() / 2, nameY);
      }
    }
  }

  /**
   * Adjusts dialogue text position within the dialogue box based on portrait side.
   * Text moves up when portrait is on the left side.
   * 
   * @param portraitSide Portrait position ("left" or "right")
   */
  private void adjustDialogueTextPosition(String portraitSide) {
    if (dialogueTable == null) {
      return;
    }
    
    float screenHeight = com.badlogic.gdx.Gdx.graphics.getHeight();
    
    // Clear existing dialogue content
    dialogueTable.clearChildren();
    
    // Create dialogue content container
    Container<Label> dialogueContent = new Container<>(dialogueLabel);
    dialogueContent.align(Align.center);
    dialogueContent.fill();
    
    // Set different top padding based on portrait position
    float topPadding;
    if ("left".equalsIgnoreCase(portraitSide)) {
      // When portrait is on left, move dialogue text upward
      topPadding = screenHeight * 0.02f;
    } else {
      // When portrait is on right or no portrait, use default position
      topPadding = screenHeight * 0.05f;
    }
    
    // Re-add dialogue content to dialogue table
    dialogueTable.add(dialogueContent)
            .grow()
            .center()
            .padTop(topPadding);
    dialogueTable.row();
    
    // Re-layout
    dialogueTable.invalidateHierarchy();
    overlayRoot.invalidateHierarchy();
  }

  /**
   * Finishes the dialogue sequence by cleaning up resources, restoring time scale,
   * showing "Ready for Fight!" message, and calling the completion callback.
   */
  private void finishDialogue() {
    if (finished) {
      return;
    }
    finished = true;

    // Stop current audio and clean up resources
    audioManager.dispose();

    // Show "Ready for Fight!" message
    showReadyForFightMessage();

    if (overlayRoot != null) {
      overlayRoot.remove();
      overlayRoot = null;
    }

    // Remove portrait image
    if (portraitImage != null) {
      portraitImage.remove();
      portraitImage = null;
    }

    // Remove speaker name container
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

  /**
   * Draws the component. Stage handles all drawing internally.
   * 
   * @param batch SpriteBatch for rendering
   */
  @Override
  protected void draw(SpriteBatch batch) {
    // Stage handles drawing
  }

  /**
   * Creates and configures the font style for dialogue text.
   * Selects appropriate font size based on screen resolution.
   * Available font styles:
   * <ul>
   *   <li>pixel_32.fnt - Pixel style font (recommended for game dialogue)</li>
   *   <li>arial_black_32.fnt - Bold Arial font (clear and readable)</li>
   *   <li>segoe_ui_18.fnt - Modern UI font</li>
   *   <li>Default font - Simple fallback font</li>
   * </ul>
   */
  private void createDialogueFont() {
    try {
      float screenWidth = com.badlogic.gdx.Gdx.graphics.getWidth();
      float screenHeight = com.badlogic.gdx.Gdx.graphics.getHeight();
      
      // Select font size based on screen resolution
      String fontPath;
      if (screenWidth >= 2560 || screenHeight >= 1440) {
        // High resolution screens (2K/4K) - use larger font
        fontPath = "flat-earth/skin/fonts/pixel_32.fnt";
      } else if (screenWidth >= 1920 || screenHeight >= 1080) {
        // Standard resolution screens (1080p) - use medium font
        fontPath = "flat-earth/skin/fonts/pixel_32.fnt";
      } else {
        // Low resolution screens (720p and below) - use smaller font
        fontPath = "flat-earth/skin/fonts/segoe_ui_18.fnt";
      }
      
      dialogueFont = new BitmapFont(com.badlogic.gdx.Gdx.files.internal(fontPath));
      dialogueFont.getData().setScale(0.8f);
      dialogueFont.setColor(Color.WHITE);
    } catch (Exception e) {
      logger.warn("Failed to load custom font, using default font", e);
      // If loading fails, use default font
      dialogueFont = SimpleUI.font();
      dialogueFont.getData().setScale(0.8f);
      dialogueFont.setColor(Color.WHITE);
    }
  }

  /**
   * Creates and configures the font style for speaker name text.
   * Selects appropriate font size based on screen resolution.
   */
  private void createSpeakerNameFont() {
    try {
      float screenWidth = com.badlogic.gdx.Gdx.graphics.getWidth();
      float screenHeight = com.badlogic.gdx.Gdx.graphics.getHeight();
      
      // Select font size based on screen resolution
      String fontPath;
      if (screenWidth >= 2560 || screenHeight >= 1440) {
        // High resolution screens (2K/4K) - use larger font
        fontPath = "flat-earth/skin/fonts/arial_black_32.fnt";
      } else if (screenWidth >= 1920 || screenHeight >= 1080) {
        // Standard resolution screens (1080p) - use medium font
        fontPath = "flat-earth/skin/fonts/arial_black_32.fnt";
      } else {
        // Low resolution screens (720p and below) - use smaller font
        fontPath = "flat-earth/skin/fonts/segoe_ui_18.fnt";
      }
      
      speakerNameFont = new BitmapFont(com.badlogic.gdx.Gdx.files.internal(fontPath));
      speakerNameFont.setColor(Color.WHITE);
    } catch (Exception e) {
      logger.warn("Failed to load speaker name font, using default font", e);
      // If loading fails, use default font
      speakerNameFont = SimpleUI.font();
      speakerNameFont.setColor(Color.WHITE);
    }
  }

  /**
   * Disposes of component resources including custom fonts.
   * Ensures dialogue is finished before disposing.
   */
  @Override
  public void dispose() {
    if (!finished) {
      finishDialogue();
    }
    // Release custom font resources
    if (dialogueFont != null && dialogueFont != SimpleUI.font()) {
      dialogueFont.dispose();
    }
    if (speakerNameFont != null && speakerNameFont != SimpleUI.font()) {
      speakerNameFont.dispose();
    }
    super.dispose();
  }

  /**
   * Record representing a single dialogue entry with text, portrait, audio, and styling options.
   * 
   * @param text Dialogue text to display
   * @param portraitPath Portrait image path (optional, null means no portrait)
   * @param soundPath Dialogue audio path (optional, null means no audio)
   * @param portraitSide Portrait position ("left" or "right", optional, defaults to "left")
   * @param dialogueBackgroundPath Dialogue box background image path (optional, null uses default background)
   * @param speakerName Speaker's name (optional, null means no name displayed)
   * @param fontScale Font scale ratio (optional, defaults to 0.8f)
   */
  public record DialogueEntry(String text, String portraitPath, String soundPath, String portraitSide, String dialogueBackgroundPath, String speakerName, Float fontScale) {
    /**
     * Creates a dialogue entry without audio (backward compatibility).
     * 
     * @param text Dialogue text
     * @param portraitPath Portrait image path
     */
    public DialogueEntry(String text, String portraitPath) {
      this(text, portraitPath, null, "left", null, null, 0.8f);
    }
    
    /**
     * Creates a dialogue entry with audio (backward compatibility).
     * 
     * @param text Dialogue text
     * @param portraitPath Portrait image path
     * @param soundPath Audio file path
     */
    public DialogueEntry(String text, String portraitPath, String soundPath) {
      this(text, portraitPath, soundPath, "left", null, null, 0.8f);
    }
    
    /**
     * Creates a dialogue entry with portrait position (backward compatibility).
     * 
     * @param text Dialogue text
     * @param portraitPath Portrait image path
     * @param soundPath Audio file path
     * @param portraitSide Portrait position ("left" or "right")
     */
    public DialogueEntry(String text, String portraitPath, String soundPath, String portraitSide) {
      this(text, portraitPath, soundPath, portraitSide, null, null, 0.8f);
    }
    
    /**
     * Creates a dialogue entry with custom background (backward compatibility).
     * 
     * @param text Dialogue text
     * @param portraitPath Portrait image path
     * @param soundPath Audio file path
     * @param portraitSide Portrait position
     * @param dialogueBackgroundPath Background image path
     */
    public DialogueEntry(String text, String portraitPath, String soundPath, String portraitSide, String dialogueBackgroundPath) {
      this(text, portraitPath, soundPath, portraitSide, dialogueBackgroundPath, null, 0.8f);
    }
    
    /**
     * Creates a dialogue entry with speaker name (backward compatibility).
     * 
     * @param text Dialogue text
     * @param portraitPath Portrait image path
     * @param soundPath Audio file path
     * @param portraitSide Portrait position
     * @param dialogueBackgroundPath Background image path
     * @param speakerName Speaker's name
     */
    public DialogueEntry(String text, String portraitPath, String soundPath, String portraitSide, String dialogueBackgroundPath, String speakerName) {
      this(text, portraitPath, soundPath, portraitSide, dialogueBackgroundPath, speakerName, 0.8f);
    }
  }

  /**
   * Updates dialogue box sizing based on background texture dimensions.
   * Can preserve aspect ratio or use default fixed dimensions.
   * 
   * @param backgroundTexture Background texture (null for default sizing)
   * @param preserveAspect Whether to preserve texture aspect ratio
   */
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

  /**
   * Checks if the background path refers to a "talker" background image.
   * 
   * @param path Background image path
   * @return true if path ends with "talker.png" or "talker2.png"
   */
  private boolean isTalkerBackground(String path) {
    if (path == null) {
      return false;
    }
    String normalised = path.replace("\\", "/").toLowerCase(Locale.ROOT);
    return normalised.endsWith("talker.png") || normalised.endsWith("talker2.png");
  }

  /**
   * Displays the "Ready for Fight!" message with animated word sequence.
   * Each word appears, scales in, pauses, then scales out before the next word appears.
   */
  private void showReadyForFightMessage() {
    // Create larger, clearer font
    BitmapFont largeFont = createLargeFont();
    
    // Split text into three words
    String[] words = {"Ready", "for", "Fight!"};
    float screenWidth = com.badlogic.gdx.Gdx.graphics.getWidth();
    float screenHeight = com.badlogic.gdx.Gdx.graphics.getHeight();
    
    // Create label for each word
    Label[] wordLabels = new Label[words.length];
    for (int i = 0; i < words.length; i++) {
      // Use specified color RGB(246, 255, 205) - light yellow-green
      Color customColor = new Color(246f/255f, 255f/255f, 205f/255f, 1f);
      Label.LabelStyle labelStyle = new Label.LabelStyle(largeFont, customColor);
      wordLabels[i] = new Label(words[i], labelStyle);
      wordLabels[i].setAlignment(Align.center);
      
      // Set initial position (screen center)
      wordLabels[i].setPosition(
          (screenWidth - wordLabels[i].getPrefWidth()) / 2f,
          (screenHeight - wordLabels[i].getPrefHeight()) / 2f
      );
      
      // Set initial scale (large)
      wordLabels[i].setScale(3.0f);
      
      // Initially hide all words
      wordLabels[i].setVisible(false);
      
      // Add to stage
      stage.addActor(wordLabels[i]);
    }
    
    // Add animation effects for each word
    animateWordsSequentially(wordLabels, largeFont);
  }
  
  /**
   * Animates words in sequence, starting with the first word.
   * 
   * @param wordLabels Array of word labels to animate
   * @param largeFont Font to dispose after all animations complete
   */
  private void animateWordsSequentially(Label[] wordLabels, BitmapFont largeFont) {
    animateSingleWord(wordLabels, 0, largeFont);
  }
  
  /**
   * Animates a single word at the specified index.
   * After completion, automatically starts animation for the next word.
   * 
   * @param wordLabels Array of all word labels
   * @param wordIndex Index of current word to animate
   * @param largeFont Font to dispose after all animations complete
   */
  private void animateSingleWord(Label[] wordLabels, int wordIndex, BitmapFont largeFont) {
    if (wordIndex >= wordLabels.length) {
      GameStateService gameState = ServiceLocator.getGameStateService();
      if (gameState != null) {
        gameState.markReadyPromptFinished();
      }
      // All word animations complete, clean up resources
      com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
        @Override
        public void run() {
          // Clean up all labels
          for (Label label : wordLabels) {
            if (label != null) {
              label.remove();
            }
          }
          // Clean up font resources
          if (largeFont != null && largeFont != SimpleUI.font()) {
            largeFont.dispose();
          }
        }
      }, 0.1f);
      return;
    }
    
    final Label currentLabel = wordLabels[wordIndex];
    final int nextWordIndex = wordIndex + 1;
    
    // Show current word
    currentLabel.setVisible(true);
    
    // Start scale animation: from 3.0x to 1.0x
    animateScale(currentLabel, 3.0f, 1.0f, 0.4f, () -> {
      // After scaling complete, wait 0.2s then start fade-out animation
      com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
        @Override
        public void run() {
          // Start fade-out animation: linear scale from 1.0x to 0.1x
          animateScaleLinear(currentLabel, 1.0f, 0.1f, 0.2f, () -> {
            // Hide word after fade-out complete
            currentLabel.setVisible(false);
            
            // Immediately start next word animation
            animateSingleWord(wordLabels, nextWordIndex, largeFont);
          });
        }
      }, 0.2f);
    });
  }
  
  /**
   * Performs a scale animation with easing.
   * 
   * @param label Label to animate
   * @param startScale Starting scale value
   * @param endScale Ending scale value
   * @param duration Animation duration in seconds
   * @param onComplete Callback to run when animation completes
   */
  private void animateScale(Label label, float startScale, float endScale, float duration, Runnable onComplete) {
    final float[] currentTime = {0f};
    final float totalDuration = duration;
    
    com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
      @Override
      public void run() {
        currentTime[0] += 0.016f;
        
        if (currentTime[0] >= totalDuration) {
          label.setScale(endScale);
          this.cancel();
          if (onComplete != null) {
            onComplete.run();
          }
          return;
        }
        
        // Use easing function for smooth animation
        //float progress = currentTime[0] / totalDuration;
        //float easedProgress = easeOutElastic(progress); // Use elastic easing for bounce effect
        
        //float currentScale = startScale + (endScale - startScale) * easedProgress;
        //label.setScale(currentScale);
      }
    }, 0f, 0.016f);
  }
  
  /**
   * Performs a linear scale animation.
   * 
   * @param label Label to animate
   * @param startScale Starting scale value
   * @param endScale Ending scale value
   * @param duration Animation duration in seconds
   * @param onComplete Callback to run when animation completes
   */
  private void animateScaleLinear(Label label, float startScale, float endScale, float duration, Runnable onComplete) {
    final float[] currentTime = {0f};
    final float totalDuration = duration;
    
    com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
      @Override
      public void run() {
        currentTime[0] += 0.016f;
        
        if (currentTime[0] >= totalDuration) {
          label.setScale(endScale);
          this.cancel();
          if (onComplete != null) {
            onComplete.run();
          }
          return;
        }
        
        // Use linear interpolation for constant speed animation
        float progress = currentTime[0] / totalDuration;
        float currentScale = startScale + (endScale - startScale) * progress;
        label.setScale(currentScale);
      }
    }, 0f, 0.016f);
  }
  
  /**
   * Creates a large font for display messages.
   * Attempts to load arial_black_32.fnt, falls back to default font if loading fails.
   * 
   * @return BitmapFont scaled appropriately for large text display
   */
  private BitmapFont createLargeFont() {
    try {
      // Try to load a larger font
      BitmapFont font = new BitmapFont(com.badlogic.gdx.Gdx.files.internal("flat-earth/skin/fonts/arial_black_32.fnt"));
      font.getData().setScale(2.0f);
      return font;
    } catch (Exception e) {
      // If loading fails, use default font and scale up
      BitmapFont defaultFont = SimpleUI.font();
      defaultFont.getData().setScale(3.0f);
      return defaultFont;
    }
  }
}
