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

public class WeatherStatusDisplay extends UIComponent {
  private static final float DISPLAY_DURATION = 4f;
  private static final float FADE_DURATION = 0.35f;
  private static final Color BRILLIANT_WHITE = new Color(0.96f, 0.98f, 1f, 1f);

  private final PlasmaWeatherController controller;
  private Table root;
  private Label statusLabel;
  private String lastRenderedLabel = "";
  private boolean initialToastPending = true;
  private String pendingLabel = "";

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
    pendingLabel =
        controller != null
            ? "Weather: " + controller.getCurrentWeatherLabel()
            : "Weather: Unknown";
    lastRenderedLabel = pendingLabel;
    statusLabel.setText(pendingLabel);
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
    if (initialToastPending) {
      lastRenderedLabel = content;
      pendingLabel = content;
      statusLabel.setText(content);
      return;
    }
    lastRenderedLabel = content;
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
