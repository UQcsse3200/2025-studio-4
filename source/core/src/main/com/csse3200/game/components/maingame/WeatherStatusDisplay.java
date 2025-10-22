package com.csse3200.game.components.maingame;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.Align;
import com.csse3200.game.components.effects.PlasmaWeatherController;
import com.csse3200.game.services.GameStateService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;

/**
 * Displays the current weather status in the game UI.
 *
 * <p>This component creates a centred UI toast that shows the current weather condition
 * (Sunny or Plasma Storm). It continuously updates to reflect changes in the weather
 * state from the {@link PlasmaWeatherController}.</p>
 *
 * <p>The display uses a brilliant white label with scaled font to ensure visibility
 * against various background elements. The toast animates in, stays visible for a short
 * duration, and fades out automatically.</p>
 * 
 * @author Team1
 * @since sprint 4
 */
public class WeatherStatusDisplay extends UIComponent {
  private static final float DISPLAY_DURATION = 4f;
  private static final float FADE_DURATION = 0.35f;
  private static final Color BRILLIANT_WHITE = new Color(0.96f, 0.98f, 1f, 1f);
  /** The weather controller to monitor for status changes */
  private final PlasmaWeatherController controller;
  /** Root table container for the UI elements */
  private Table root;
  /** Label displaying the current weather status */
  private Label statusLabel;
  private String lastRenderedLabel = "";
  private boolean initialToastPending = true;

  /**
   * Creates a weather status display component.
   * 
   * @param controller the plasma weather controller to monitor
   */
  public WeatherStatusDisplay(PlasmaWeatherController controller) {
    this.controller = controller;
  }

  @Override
  public void create() {
    super.create();
    root = new Table();
    root.setFillParent(true);
    root.center();
    root.padTop(0f);
    root.padRight(0f);
    root.setVisible(false);

    statusLabel = new Label("", skin, "title");
    statusLabel.setAlignment(Align.center);
    statusLabel.setFontScale(0.8f);
    statusLabel.setColor(BRILLIANT_WHITE);

    root.add(statusLabel).center();
    stage.addActor(root);
    lastRenderedLabel =
        controller != null
            ? "Weather: " + controller.getCurrentWeatherLabel()
            : "Weather: Unknown";
    statusLabel.setText(lastRenderedLabel);
    statusLabel.getColor().a = 0f;
  }

  @Override
  public void update() {
    if (initialToastPending) {
      GameStateService gameState = ServiceLocator.getGameStateService();
      boolean readyReached = gameState == null || gameState.isReadyPromptFinished();
      if (readyReached) {
        initialToastPending = false;
        triggerToast(lastRenderedLabel);
      }
    }
    updateLabel();
  }

  /**
   * Updates the weather status label with current information from the controller.
   * Handles null controller gracefully by displaying "Unknown" status.
   */
  private void updateLabel() {
    if (controller == null) {
      handleLabelChange("Weather: Unknown");
      return;
    }
    handleLabelChange("Weather: " + controller.getCurrentWeatherLabel());
  }

  private void handleLabelChange(String content) {
    if (statusLabel == null) {
      return;
    }
    if (content.equals(lastRenderedLabel)) {
      return;
    }
    lastRenderedLabel = content;
    if (initialToastPending) {
      statusLabel.setText(content);
      return;
    }
    triggerToast(content);
  }

  @Override
  protected void draw(SpriteBatch batch) {
  }

  @Override
  public void dispose() {
    super.dispose();
    if (root != null) {
      root.remove();
      root = null;
    }
  }

  private void triggerToast(String content) {
    statusLabel.setText(content);
    statusLabel.invalidateHierarchy();
    float originX = statusLabel.getPrefWidth() / 2f;
    float originY = statusLabel.getPrefHeight() / 2f;
    statusLabel.setOrigin(originX, originY);
    statusLabel.clearActions();
    if (root != null) {
      root.setVisible(true);
    }

    statusLabel.setColor(BRILLIANT_WHITE);
    statusLabel.getColor().a = 0f;
    statusLabel.setScale(1.25f);

    statusLabel.addAction(
        Actions.sequence(
            Actions.parallel(
                Actions.fadeIn(0.2f),
                Actions.scaleTo(1.05f, 1.05f, 0.45f, Interpolation.swingOut)),
            Actions.delay(DISPLAY_DURATION),
            Actions.parallel(
                Actions.fadeOut(FADE_DURATION),
                Actions.scaleTo(0.82f, 0.82f, FADE_DURATION, Interpolation.sineIn)),
                Actions.run(
                () -> {
                  statusLabel.setScale(1f);
                  statusLabel.setColor(BRILLIANT_WHITE);
                  statusLabel.getColor().a = 1f;
                  if (root != null) {
                    root.setVisible(false);
                  }
                })));
  }
}
