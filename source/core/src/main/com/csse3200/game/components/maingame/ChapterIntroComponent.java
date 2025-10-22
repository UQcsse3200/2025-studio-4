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
 * Component for displaying chapter introduction text in the center of the screen.
 * 
 * <p>This component creates a full-screen overlay that displays story text paragraphs
 * sequentially. It supports both automatic progression (3 seconds per text) and
 * manual advancement through user clicks. The first text is displayed as a title
 * with larger font, while subsequent texts use story formatting.</p>
 * 
 * <p>The component includes a semi-transparent black background overlay and
 * automatically manages font resources, ensuring proper cleanup when the
 * introduction sequence completes.</p>
 * 
 * @author Team1
 * @since sprint 4
 */
public class ChapterIntroComponent extends UIComponent {
    private static final Logger logger = LoggerFactory.getLogger(ChapterIntroComponent.class);
    
    /** Array of story texts to display sequentially */
    private final String[] storyTexts;
    /** Callback function to execute when the introduction sequence completes */
    private final Runnable onComplete;
    /** Main overlay table containing the text display */
    private Table overlayTable;
    /** Index of the currently displayed text */
    private int currentTextIndex = 0;
    /** Whether the introduction sequence has finished */
    private boolean finished = false;
    /** Font used for story text content */
    private BitmapFont storyFont;
    /** Font used for title text (first paragraph) */
    private BitmapFont titleFont;
    
    /**
     * Creates a chapter introduction component.
     * 
     * @param storyTexts array of text strings to display sequentially
     * @param onComplete callback function executed when introduction finishes
     */
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
    
    /**
     * Creates and configures fonts for title and story text display.
     * 
     * <p>Attempts to load custom fonts from the flat-earth skin directory.
     * If font loading fails, falls back to the default SimpleUI font
     * with appropriate scaling adjustments.</p>
     */
    private void createFonts() {
        try {
            // Create title font (larger)
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
            // Create story text font
            storyFont = new BitmapFont(com.badlogic.gdx.Gdx.files.internal("flat-earth/skin/fonts/pixel_32.fnt"));
            storyFont.getData().setScale(1.2f);
            storyFont.setColor(new Color(0.9f, 0.9f, 0.9f, 1f)); // Light gray
        } catch (Exception e) {
            logger.warn("Failed to load story font, using default font", e);
            storyFont = SimpleUI.font();
            storyFont.getData().setScale(1.5f);
            storyFont.setColor(new Color(0.9f, 0.9f, 0.9f, 1f));
        }
    }
    
    /**
     * Builds the full-screen overlay with semi-transparent background.
     * 
     * <p>Creates a touchable overlay that covers the entire screen with
     * a semi-transparent black background. Adds a click listener to allow
     * manual progression through the text sequence.</p>
     */
    private void buildOverlay() {
        float screenWidth = com.badlogic.gdx.Gdx.graphics.getWidth();
        float screenHeight = com.badlogic.gdx.Gdx.graphics.getHeight();
        
        // Create semi-transparent black background
        overlayTable = new Table();
        overlayTable.setFillParent(true);
        overlayTable.setBackground(SimpleUI.solid(new Color(0f, 0f, 0f, 0.8f)));
        overlayTable.setTouchable(Touchable.enabled);
        
        // Add click listener to advance text on any click
        overlayTable.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showNextText();
            }
        });
        
        stage.addActor(overlayTable);
    }
    
    /**
     * Displays the next text in the sequence or finishes the introduction.
     * 
     * <p>Shows the current text with appropriate formatting (title font for first
     * text, story font for others). Automatically schedules the next text to appear
     * after 3 seconds, or allows manual advancement through user clicks.</p>
     */
    private void showNextText() {
        if (finished || currentTextIndex >= storyTexts.length) {
            finishIntro();
            return;
        }
        
        // Clear previous content
        overlayTable.clearChildren();
        
        String currentText = storyTexts[currentTextIndex];
        
        // Create text label
        Label.LabelStyle labelStyle;
        if (currentTextIndex == 0) {
            // First paragraph is title, use title font
            labelStyle = new Label.LabelStyle(titleFont, Color.WHITE);
        } else {
            // Other paragraphs use story font
            labelStyle = new Label.LabelStyle(storyFont, new Color(0.9f, 0.9f, 0.9f, 1f));
        }
        
        Label textLabel = new Label(currentText, labelStyle);
        textLabel.setWrap(true);
        textLabel.setAlignment(Align.center);
        
        // Set text area size (70% of screen width, adaptive height)
        float textWidth = com.badlogic.gdx.Gdx.graphics.getWidth() * 0.7f;
        float textHeight = com.badlogic.gdx.Gdx.graphics.getHeight() * 0.6f;
        
        overlayTable.add(textLabel)
                .width(textWidth)
                .height(textHeight)
                .center();
        
        currentTextIndex++;
        
        // Auto-advance (after 3 seconds)
        com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
            @Override
            public void run() {
                showNextText();
            }
        }, 3.0f);
    }
    
    /**
     * Completes the introduction sequence and cleans up resources.
     * 
     * <p>Removes the overlay, disposes of font resources, executes the completion
     * callback, and disposes of the entity. This method is idempotent and
     * can be called multiple times safely.</p>
     */
    private void finishIntro() {
        if (finished) {
            return;
        }
        finished = true;
        
        // Remove overlay
        if (overlayTable != null) {
            overlayTable.remove();
            overlayTable = null;
        }
        
        // Dispose font resources
        if (storyFont != null && storyFont != SimpleUI.font()) {
            storyFont.dispose();
        }
        if (titleFont != null && titleFont != SimpleUI.font()) {
            titleFont.dispose();
        }
        
        // Execute completion callback
        if (onComplete != null) {
            try {
                onComplete.run();
            } catch (Exception e) {
                logger.error("Error running chapter intro completion callback", e);
            }
        }
        
        entity.dispose();
    }
    
    /**
     * Override of draw method - stage handles all drawing.
     * 
     * @param batch the sprite batch for rendering
     */
    @Override
    protected void draw(SpriteBatch batch) {
        // Stage handles drawing
    }
    
    /**
     * Ensures proper cleanup when the component is disposed.
     * 
     * <p>If the introduction hasn't finished naturally, this method
     * will complete it and clean up resources before disposing.</p>
     */
    @Override
    public void dispose() {
        if (!finished) {
            finishIntro();
        }
        super.dispose();
    }
}
