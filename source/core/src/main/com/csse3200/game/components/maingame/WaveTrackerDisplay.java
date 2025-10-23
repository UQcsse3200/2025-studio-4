package com.csse3200.game.components.maingame;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.csse3200.game.ui.UIComponent;

/**
 * Displays the current wave number to the player
 */
public class WaveTrackerDisplay extends UIComponent {
    private Label waveNumberLabel;
    private Label waveLabel;
    private Label totalLabel;
    private Table table;
    private Table backgroundPanel;
    private Table borderPanel;
    private ProgressBar progressBar;
    private int currentWave = 0;
    private final int totalWaves;

    // Base scale factors for responsive scaling
    private static final float BASE_WIDTH = 1920f;
    private static final float BASE_HEIGHT = 1080f;
    private float currentScale = 1f;

    public WaveTrackerDisplay(int totalWaves) {
        this.totalWaves = totalWaves;
    }

    @Override
    public void create() {
        super.create();

        // Calculate initial scale based on window size
        updateScale();

        // Create custom label styles with white font color
        Label.LabelStyle waveLabelStyle = new Label.LabelStyle(skin.get("large", Label.LabelStyle.class));
        waveLabelStyle.fontColor = Color.WHITE;

        Label.LabelStyle numberLabelStyle = new Label.LabelStyle(skin.get("title", Label.LabelStyle.class));
        numberLabelStyle.fontColor = Color.WHITE;

        Label.LabelStyle totalLabelStyle = new Label.LabelStyle(skin.get("large", Label.LabelStyle.class));
        totalLabelStyle.fontColor = Color.WHITE;

        // Create labels with custom styles
        waveLabel = new Label("WAVE", waveLabelStyle);
        waveLabel.setAlignment(Align.center);
        waveLabel.setFontScale(0.8f * currentScale);

        waveNumberLabel = new Label(String.valueOf(currentWave), numberLabelStyle);
        waveNumberLabel.setFontScale(1.8f * currentScale);
        waveNumberLabel.setAlignment(Align.center);

        totalLabel = new Label("/ " + totalWaves, totalLabelStyle);
        totalLabel.setFontScale(0.9f * currentScale);

        // Create progress bar
        ProgressBar.ProgressBarStyle progressBarStyle = new ProgressBar.ProgressBarStyle();
        progressBarStyle.background = skin.newDrawable("white", new Color(0.25f, 0.25f, 0.25f, 0.9f)); // Dark grey
        progressBarStyle.knobBefore = skin.newDrawable("white", new Color(0.9f, 0.9f, 0.9f, 1f)); // White progress
        progressBarStyle.knob = skin.newDrawable("white", new Color(0, 0, 0, 0));

        progressBar = new ProgressBar(0, totalWaves, 1, false, progressBarStyle);
        progressBar.setValue(currentWave);
        progressBar.setAnimateDuration(0.5f);

        borderPanel = new Table();
        Drawable borderBackground = skin.newDrawable("white", new Color(0.5f, 0.5f, 0.5f, 0.5f)); // Grey border
        borderPanel.setBackground(borderBackground);
        borderPanel.pad(2f * currentScale);

        // Create background panel
        backgroundPanel = new Table();
        Drawable background = skin.newDrawable("white", new Color(0.15f, 0.15f, 0.15f, 0.9f)); // Dark grey
        backgroundPanel.setBackground(background);
        backgroundPanel.pad(12f * currentScale, 20f * currentScale, 12f * currentScale, 20f * currentScale);

        // Layout the wave information
        Table contentTable = new Table();
        contentTable.add(waveLabel).padRight(8f * currentScale);
        contentTable.add(waveNumberLabel).padRight(3f * currentScale);
        contentTable.add(totalLabel).padRight(15f * currentScale);
        contentTable.add(progressBar).width(120f * currentScale).height(18f * currentScale);

        backgroundPanel.add(contentTable);
        borderPanel.add(backgroundPanel);

        // Main table for positioning
        table = new Table();
        table.top();
        table.setFillParent(true);
        table.add(borderPanel).expandX().center().padTop(10f * currentScale).row();

        stage.addActor(table);

        borderPanel.setTransform(true);
        borderPanel.setOrigin(Align.center);
        borderPanel.setScale(0.3f);
        borderPanel.setRotation(15f);
        borderPanel.getColor().a = 0f;
        borderPanel.addAction(Actions.sequence(
                Actions.parallel(
                        Actions.fadeIn(0.4f),
                        Actions.scaleTo(1.15f, 1.15f, 0.4f),
                        Actions.rotateTo(0f, 0.4f)
                ),
                Actions.scaleTo(1f, 1f, 0.2f)
                // No floating animation - stays still
        ));

        // Update wave color
        updateWaveColor();

        // Listen for wave updates
        entity.getEvents().addListener("updateWave", this::updateWave);
    }

    /**
     * Calculate scale factor based on current window size
     */
    private float calculateScale() {
        float widthScale = stage.getWidth() / BASE_WIDTH;
        float heightScale = stage.getHeight() / BASE_HEIGHT;
        float scale = Math.min(widthScale, heightScale);
        // Clamp scale to reasonable bounds
        return Math.max(0.5f, Math.min(scale, 2.0f));
    }

    /**
     * Update scale based on current window size
     */
    private void updateScale() {
        currentScale = calculateScale();
    }

    /**
     * Check if window has been resized and update layout if needed
     */
    private void checkAndHandleResize() {
        float newScale = calculateScale();

        if (Math.abs(newScale - currentScale) > 0.01f) {
            currentScale = newScale;

            // Update font scales
            waveLabel.setFontScale(0.8f * currentScale);
            waveNumberLabel.setFontScale(1.8f * currentScale);
            totalLabel.setFontScale(0.9f * currentScale);

            // Recreate the layout with new scale
            table.clear();

            // Update padding
            borderPanel.pad(2f * currentScale);
            backgroundPanel.pad(12f * currentScale, 20f * currentScale, 12f * currentScale, 20f * currentScale);

            // Update progress bar size and rebuild content
            Table contentTable = new Table();
            contentTable.add(waveLabel).padRight(8f * currentScale);
            contentTable.add(waveNumberLabel).padRight(3f * currentScale);
            contentTable.add(totalLabel).padRight(15f * currentScale);
            contentTable.add(progressBar).width(120f * currentScale).height(18f * currentScale);

            backgroundPanel.clear();
            backgroundPanel.add(contentTable);

            table.add(borderPanel).expandX().center().padTop(10f * currentScale).row();
        }
    }

    @Override
    public void update() {
        super.update();
        checkAndHandleResize();
    }

    /**
     * Update the current wave number
     */
    private void updateWave(int waveNumber) {
        this.currentWave = waveNumber;

        waveNumberLabel.setText(String.valueOf(currentWave));
        progressBar.setValue(currentWave);

        // Clear any existing actions
        borderPanel.clearActions();

        updateWaveColor();
    }

    /**
     * Trigger boss wave message when wave actually starts
     */
    public void triggerBossWaveMessage() {
        if (currentWave == totalWaves) {
            showBossWaveMessage();
        }
    }

    /**
     * Show boss wave message
     */
    private void showBossWaveMessage() {
        // Create label
        Label bossLabel = new Label("BOSS WAVE", skin, "title");
        bossLabel.setFontScale(4f);
        bossLabel.setAlignment(Align.center);
        bossLabel.setColor(new Color(1f, 0.1f, 0.1f, 1f));

        // Create background panel
        Table bossMessagePanel = new Table();
        Drawable bossBackground = skin.newDrawable("white", new Color(0f, 0f, 0f, 0.8f));
        bossMessagePanel.setBackground(bossBackground);
        bossMessagePanel.pad(30f, 60f, 30f, 60f);
        bossMessagePanel.add(bossLabel);

        Table bossBorderPanel = new Table();
        Drawable bossBorder = skin.newDrawable("white", new Color(1f, 0.1f, 0.1f, 0.6f));
        bossBorderPanel.setBackground(bossBorder);
        bossBorderPanel.pad(5f);
        bossBorderPanel.add(bossMessagePanel);

        Table containerTable = new Table();
        containerTable.setFillParent(true);
        containerTable.center();
        containerTable.add(bossBorderPanel);

        stage.addActor(containerTable);

        bossBorderPanel.setTransform(true);
        bossBorderPanel.setOrigin(Align.center);

        bossBorderPanel.getColor().a = 0f;
        bossBorderPanel.addAction(Actions.sequence(
                Actions.fadeIn(0.4f),
                Actions.delay(2.0f),
                Actions.fadeOut(0.4f),
                Actions.run(containerTable::remove)
        ));
    }

    /**
     * Update the color of the wave number and progress bar based on progression
     * WAVE and / text stay white, wave number is light blue (or red for boss wave)
     */
    private void updateWaveColor() {
        Color waveNumberColor;
        Color progressColor;
        Color borderColor;

        if (currentWave == totalWaves) {
            // Boss wave - Red theme for wave number
            waveNumberColor = new Color(1f, 0.2f, 0.2f, 1f);
            progressColor = new Color(0.95f, 0.2f, 0.2f, 1f);
            borderColor = new Color(0.8f, 0.1f, 0.1f, 0.6f);
        } else {
            // Normal waves - Light blue for wave number
            waveNumberColor = new Color(0.5f, 0.7f, 1f, 1f); // Light blue
            progressColor = new Color(0.9f, 0.9f, 0.9f, 1f);
            borderColor = new Color(0.5f, 0.5f, 0.5f, 0.5f);
        }

        // WAVE and / text always stay white
        waveLabel.getStyle().fontColor = Color.WHITE;
        totalLabel.getStyle().fontColor = Color.WHITE;

        // Wave number changes color based on wave
        waveNumberLabel.getStyle().fontColor = waveNumberColor;

        // Update progress bar color
        ProgressBar.ProgressBarStyle style = progressBar.getStyle();
        style.knobBefore = skin.newDrawable("white", progressColor);
        progressBar.setStyle(style);

        // Update border color
        Drawable newBorder = skin.newDrawable("white", borderColor);
        borderPanel.setBackground(newBorder);
    }

    @Override
    protected void draw(SpriteBatch batch) {
    }

    @Override
    public void dispose() {
        super.dispose();
        table.remove();
    }
}
