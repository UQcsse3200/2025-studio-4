package com.csse3200.game.components.player;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.csse3200.game.components.PlayerCombatStatsComponent;
import com.csse3200.game.components.currencysystem.CurrencyComponent.CurrencyType;
import com.csse3200.game.components.currencysystem.CurrencyManagerComponent;
import com.csse3200.game.services.PlayerNameService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;

import java.util.EnumMap;
import java.util.Map;

/**
 * A ui component for displaying player stats, e.g. health.
 */
public class PlayerStatsDisplay extends UIComponent {
  Table table;
  private Image heartImage;
  private Label healthLabel;
<<<<<<< HEAD
  private Label playerNameLabel;

=======
>>>>>>> origin/main
  private final Map<CurrencyType, Image> currencyImages = new EnumMap<>(CurrencyType.class);
  private final Map<CurrencyType, Label> currencyLabels = new EnumMap<>(CurrencyType.class);
  private Image scoreImage;
  private Label scoreLabel;

  /**
   * Creates reusable ui styles and adds actors to the stage.
   */
  @Override
  public void create() {
    super.create();
    addActors();

    entity.getEvents().addListener("updateHealth", this::updatePlayerHealthUI);
    entity.getEvents().addListener("updateCurrencyUI", this::updatePlayerCurrencyAmountUI);
<<<<<<< HEAD
    entity.getEvents().addListener("updatePlayerName", this::updatePlayerNameUI);
=======
    entity.getEvents().addListener("updateScore", this::updatePlayerScoreUI);
>>>>>>> origin/main
  }

  /**
   * Creates actors and positions them on the stage using a table.
   * @see Table for positioning options
   */
  private void addActors() {
    table = new Table();
    table.top().left();
    table.setFillParent(true);
    table.padTop(45f).padLeft(5f);

    // Player name
    String playerName = getPlayerName();
    playerNameLabel = new Label("Player: " + playerName, skin, "large");
    table.add(playerNameLabel).colspan(2).left().pad(5);
    table.row();

    // Heart image
    float heartSideLength = 60f;
    heartImage = new Image(ServiceLocator.getResourceService().getAsset("images/heart.png", Texture.class));

    // Health text
    int health = entity.getComponent(PlayerCombatStatsComponent.class).getHealth();
    CharSequence healthText = String.format("Health: %d", health);
    healthLabel = new Label(healthText, skin, "large");

    // Score image (trophy)
    float scoreSideLength = 64f;
    scoreImage = new Image(ServiceLocator.getResourceService().getAsset("images/score_trophy.png", Texture.class));

    // Score text
    int score = 0; //entity.getComponent(ScrapStatsComponent.class).getScrap();
    CharSequence scoreText = String.format("Score: %d", score);
    scoreLabel = new Label(scoreText, skin, "large");

    table.add(heartImage).size(heartSideLength).pad(5);
    table.add(healthLabel);
    table.row();

    // Dynamically render currencies
    for (CurrencyType currencyType : CurrencyType.values()) {
      Image currencyImage = new Image(
              ServiceLocator.getResourceService().getAsset(
                      currencyType.getTexturePath(),
                      Texture.class
              )
      );
      int currencyAmount = entity.getComponent(CurrencyManagerComponent.class).getCurrencyAmount(currencyType);
      Label currencyLabel = new Label(
              String.format("%s%n%d", currencyType.getDisplayName(), currencyAmount),
              skin,
              "large"
      );

      currencyImages.put(currencyType, currencyImage);
      currencyLabels.put(currencyType, currencyLabel);

      float sideLength = 64f;
      table.add(currencyImage).size(sideLength).pad(5);
      table.add(currencyLabel).left();
      table.row();
    }

    // Score text position
    table.row();
    table.add(scoreImage).size(scoreSideLength).pad(5);
    table.add(scoreLabel).left().padTop(5f);

    stage.addActor(table);
  }

  @Override
  public void draw(SpriteBatch batch)  {
    // draw is handled by the stage
  }

  /**
   * Updates the player's health on the ui.
   * @param health player health
   */
  public void updatePlayerHealthUI(int health) {
    CharSequence text = String.format("Health: %d", health);
    healthLabel.setText(text);
  }

  /**
   * Updates the player's currency amount for certain type on the UI.
   * @param type currency type to render
   * @param amount player currency amount for certain type
   */
  public void updatePlayerCurrencyAmountUI(CurrencyType type, int amount) {
    Label label = currencyLabels.get(type);
    if (label != null) {
      label.setText(String.format("%s%n%d", type.getDisplayName(), amount));
    }
  }

  /**
<<<<<<< HEAD
   * Gets the current player's name from the PlayerNameService.
   * @return the player's name or "Player" if service is not available
   */
  private String getPlayerName() {
    PlayerNameService playerNameService = ServiceLocator.getPlayerNameService();
    if (playerNameService != null) {
      return playerNameService.getPlayerName();
    }
    return "Player";
  }

  /**
   * Updates the player's name on the UI.
   */
  public void updatePlayerNameUI() {
    if (playerNameLabel != null) {
      String playerName = getPlayerName();
      playerNameLabel.setText("Player: " + playerName);
    }
  }
=======
   * Updates the player's total score on the ui.
   * @param totalScore player total score
   */
  private void updatePlayerScoreUI(int totalScore) {
    CharSequence text = String.format("Score: %d", totalScore);
    scoreLabel.setText(text);
  }

>>>>>>> origin/main

  @Override
  public void dispose() {
    super.dispose();
    heartImage.remove();
    healthLabel.remove();
    if (playerNameLabel != null) {
      playerNameLabel.remove();
    }

    for (Image image : currencyImages.values()) {
      image.remove();
    }
    for (Label label : currencyLabels.values()) {
      label.remove();
    }
    currencyImages.clear();
    currencyLabels.clear();
    scoreImage.remove();
    scoreLabel.remove();
  }
}
