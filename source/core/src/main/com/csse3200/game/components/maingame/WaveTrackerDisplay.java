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
    private Label totalLabel;
    private Table table;
    private Table backgroundPanel;
    private Table borderPanel;
    private ProgressBar progressBar;
    private int currentWave = 1;
    private final int totalWaves;
    private static final float FLOAT_DISTANCE = 5f;
    private static final float FLOAT_DURATION = 2f;

    public WaveTrackerDisplay(int totalWaves) {
        this.totalWaves = totalWaves;
    }

    @Override
    public void create() {
        super.create();

        // Create the wave number label
        waveNumberLabel = new Label(String.valueOf(currentWave), skin, "title");
        waveNumberLabel.setFontScale(2.5f);
        waveNumberLabel.setAlignment(Align.center);

        Label waveLabel = new Label("WAVE", skin, "large");
        waveLabel.setAlignment(Align.center);
        waveLabel.setColor(new Color(0.9f, 0.9f, 0.9f, 1f));

        totalLabel = new Label("/ " + totalWaves, skin, "large");
        totalLabel.setColor(new Color(0.7f, 0.7f, 0.7f, 1f));

        // Create progress bar for wave progression
        ProgressBar.ProgressBarStyle progressBarStyle = new ProgressBar.ProgressBarStyle();
        progressBarStyle.background = skin.newDrawable("white", new Color(0.2f, 0.2f, 0.2f, 0.8f));
        progressBarStyle.knobBefore = skin.newDrawable("white", new Color(0.3f, 1f, 0.5f, 0.9f));
        progressBarStyle.knob = skin.newDrawable("white", new Color(0, 0, 0, 0));

        progressBar = new ProgressBar(0, totalWaves, 1, false, progressBarStyle);
        progressBar.setValue(currentWave);
        progressBar.setAnimateDuration(0.5f);

        borderPanel = new Table();
        Drawable borderBackground = skin.newDrawable("white", new Color(0.3f, 1f, 0.5f, 0.4f));
        borderPanel.setBackground(borderBackground);
        borderPanel.pad(3f);

        // Create background panel
        backgroundPanel = new Table();
        Drawable background = skin.newDrawable("white", new Color(0.05f, 0.05f, 0.1f, 0.85f));
        backgroundPanel.setBackground(background);
        backgroundPanel.pad(20f, 30f, 20f, 30f);

        // Layout the wave information vertically
        Table contentTable = new Table();
        contentTable.add(waveLabel).padBottom(5f).row();
        contentTable.add(waveNumberLabel).padBottom(3f).row();
        contentTable.add(totalLabel).padBottom(10f).row();
        contentTable.add(progressBar).width(150f).height(8f);

        backgroundPanel.add(contentTable);
        borderPanel.add(backgroundPanel);

        // Main table for positioning
        table = new Table();
        table.top();
        table.setFillParent(true);
        table.add(borderPanel).expandX().center().padTop(10f).row();

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
                Actions.scaleTo(1f, 1f, 0.2f),
                Actions.run(this::startFloatingAnimation)
        ));

        // Update wave color
        updateWaveColor();

        // Listen for wave updates
        entity.getEvents().addListener("updateWave", this::updateWave);
    }

    /**
     * Start a continuous subtle floating animation
     */
    private void startFloatingAnimation() {
        borderPanel.addAction(Actions.forever(
                Actions.sequence(
                        Actions.moveBy(0f, FLOAT_DISTANCE, FLOAT_DURATION),
                        Actions.moveBy(0f, -FLOAT_DISTANCE, FLOAT_DURATION)
                )
        ));
    }

    /**
     * Update the current wave number
     */
    private void updateWave(int waveNumber) {
        this.currentWave = waveNumber;

        waveNumberLabel.setText(String.valueOf(currentWave));
        progressBar.setValue(currentWave);

        if (currentWave == totalWaves) {
            showBossWaveMessage();
        }

        borderPanel.clearActions();
        startFloatingAnimation();

        updateWaveColor();
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
     * Get the color based on current progression
     */
    private Color getProgressionColor() {
        float progression = (float) currentWave / totalWaves;

        if (progression < 0.33f) {
            return new Color(0.3f, 1f, 0.5f, 0.8f);
        } else if (progression < 0.66f) {
            return new Color(1f, 0.9f, 0.3f, 0.8f);
        } else {
            return new Color(1f, 0.4f, 0.2f, 0.8f);
        }
    }

    /**
     * Update the color of the wave number and progress bar based on progression
     */
    private void updateWaveColor() {
        float progression = (float) currentWave / totalWaves;

        Color numberColor;
        Color progressColor;

        if (currentWave == totalWaves) {
            numberColor = new Color(1f, 0.1f, 0.1f, 1f);
            progressColor = new Color(1f, 0.1f, 0.1f, 1f);
        } else if (progression < 0.33f) {
            numberColor = new Color(0.3f, 1f, 0.5f, 1f);
            progressColor = new Color(0.3f, 1f, 0.5f, 0.9f);
        } else if (progression < 0.66f) {
            numberColor = new Color(1f, 0.9f, 0.3f, 1f);
            progressColor = new Color(1f, 0.8f, 0.2f, 0.9f);
        } else {
            numberColor = new Color(1f, 0.4f, 0.2f, 1f);
            progressColor = new Color(1f, 0.3f, 0.1f, 0.9f);
        }

        waveNumberLabel.setColor(numberColor);

        ProgressBar.ProgressBarStyle style = progressBar.getStyle();
        style.knobBefore = skin.newDrawable("white", progressColor);
        progressBar.setStyle(style);
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
