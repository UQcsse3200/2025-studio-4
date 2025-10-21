package com.csse3200.game.components.maingame;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.csse3200.game.components.effects.PlasmaWeatherController;
import com.csse3200.game.ui.UIComponent;

public class WeatherStatusDisplay extends UIComponent {
  private final PlasmaWeatherController controller;
  private Table root;
  private Label statusLabel;

  public WeatherStatusDisplay(PlasmaWeatherController controller) {
    this.controller = controller;
  }

  @Override
  public void create() {
    super.create();
    root = new Table();
    root.setFillParent(true);
    root.top().right();
    root.padTop(15f);
    root.padRight(400f);


    statusLabel = new Label("", skin, "title");
    statusLabel.setColor(Color.WHITE);
    statusLabel.setFontScale(0.6f);

    root.add(statusLabel);
    stage.addActor(root);
    updateLabel();
  }

  @Override
  public void update() {
    updateLabel();
  }

  private void updateLabel() {
    if (controller == null) {
      statusLabel.setText("Weather: Unknown");
      return;
    }
    statusLabel.setText("Weather: " + controller.getCurrentWeatherLabel());
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
}
