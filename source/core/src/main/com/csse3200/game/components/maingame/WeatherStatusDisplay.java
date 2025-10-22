package com.csse3200.game.components.maingame;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.csse3200.game.components.effects.PlasmaWeatherController;
import com.csse3200.game.ui.UIComponent;

/**
 * Displays the current weather status in the game UI.
 * 
 * <p>This component creates a UI overlay that shows the current weather condition
 * (Sunny or Plasma Storm) in the top-right corner of the screen. It continuously
 * updates to reflect changes in the weather state from the PlasmaWeatherController.</p>
 * 
 * <p>The display uses a white label with scaled font to ensure visibility
 * against various background elements. The component automatically updates
 * every frame to stay synchronized with the weather controller.</p>
 * 
 * @author Team1
 * @since sprint 4
 */
public class WeatherStatusDisplay extends UIComponent {
  /** The weather controller to monitor for status changes */
  private final PlasmaWeatherController controller;
  /** Root table container for the UI elements */
  private Table root;
  /** Label displaying the current weather status */
  private Label statusLabel;

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

  /**
   * Updates the weather status label with current information from the controller.
   * Handles null controller gracefully by displaying "Unknown" status.
   */
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
