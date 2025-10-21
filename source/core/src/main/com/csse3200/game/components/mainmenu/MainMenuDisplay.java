package com.csse3200.game.components.mainmenu;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.csse3200.game.areas.ForestGameArea;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.csse3200.game.files.UserSettings;
import com.csse3200.game.services.GameStateService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import com.csse3200.game.ui.UIStyleHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A ui component for displaying the Main menu.
 */
public class MainMenuDisplay extends UIComponent {
  private static final Logger logger = LoggerFactory.getLogger(MainMenuDisplay.class);
  private static final float Z_INDEX = 2f;
  private Table table;

  @Override
  public void create() {
    super.create();
    addActors();
    ForestGameArea.NUM_ENEMIES_DEFEATED = 0;
    ForestGameArea.NUM_ENEMIES_TOTAL = 0;
  }

  private void addActors() {
    table = new Table();
    table.setFillParent(true);

    Image backgroundImage =
            new Image(
                    ServiceLocator.getResourceService()
                            .getAsset("images/main_menu_background.png", Texture.class));
    backgroundImage.setFillParent(true);
    stage.addActor(backgroundImage);

    TextButtonStyle customButtonStyle = UIStyleHelper.orangeButtonStyle();
    TextButton startBtn = new TextButton("New Game", customButtonStyle);
    TextButton loadBtn = new TextButton("Continue", customButtonStyle);
    TextButton exitBtn = new TextButton("Exit", customButtonStyle);

    ImageButton settingsBtn = createImageButton("images/settings.png", "Settings");
    ImageButton bookBtn = createImageButton("images/book.png", "Book");
    ImageButton storyBtn = createImageButton("images/story.png", "Story"); // Using same image for story
    ImageButton rankingBtn = createImageButton("images/rank.png", "Ranking");

    Image starImage = new Image(
            ServiceLocator.getResourceService().getAsset(
                    "images/star.png",
                    Texture.class
            )
    );
    Label starsLabel = new Label(
            Integer.toString(ServiceLocator.getGameStateService().getStars()),
            skin,
            "large"
    );

    float buttonWidth = 200f;
    float buttonHeight = 50f;
    float imageButtonSize = 64f;

    startBtn.getLabel().setColor(Color.WHITE);
    loadBtn.getLabel().setColor(Color.WHITE);
    exitBtn.getLabel().setColor(Color.WHITE);

    startBtn.addListener(
            new ChangeListener() {
              @Override
              public void changed(ChangeEvent changeEvent, Actor actor) {
                logger.debug("Start button clicked");
                ServiceLocator.registerGameStateService(new GameStateService());
                entity.getEvents().trigger("start");
              }
            });

    loadBtn.addListener(
            new ChangeListener() {
              @Override
              public void changed(ChangeEvent changeEvent, Actor actor) {
                logger.debug("Continue button clicked");
                entity.getEvents().trigger("continue");
              }
            });

    settingsBtn.addListener(
            new ChangeListener() {
              @Override
              public void changed(ChangeEvent changeEvent, Actor actor) {
                logger.debug("Settings button clicked");
                entity.getEvents().trigger("settings");
              }
            });

    rankingBtn.addListener(
            new ChangeListener() {
              @Override
              public void changed(ChangeEvent changeEvent, Actor actor) {
                logger.debug("Ranking button clicked");
                entity.getEvents().trigger("ranking");
              }
            });

    exitBtn.addListener(
            new ChangeListener() {
              @Override
              public void changed(ChangeEvent changeEvent, Actor actor) {
                logger.debug("Exit button clicked");
                entity.getEvents().trigger("exit");
              }
            });

    bookBtn.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent changeEvent, Actor actor) {
        logger.debug("Book button clicked");
        entity.getEvents().trigger("book");
      }
    });

    storyBtn.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent changeEvent, Actor actor) {
        logger.debug("Story button clicked");
        entity.getEvents().trigger("story");
      }
    });

    table.add().expandY().row();

    HorizontalGroup starGroup = new HorizontalGroup();
    starGroup.space(5);
    starGroup.addActor(starImage);
    starGroup.addActor(starsLabel);
    table.add(starGroup);
    table.row();

    table.add(startBtn).size(buttonWidth, buttonHeight).padTop(50f);
    table.row();
    table.add(loadBtn).size(buttonWidth, buttonHeight).padTop(20f);
    table.row();
    table.add(exitBtn).size(buttonWidth, buttonHeight).padTop(20f);
    table.row();

    HorizontalGroup imageButtonsGroup = new HorizontalGroup();
    imageButtonsGroup.space(20);
    Table settingsContainer = new Table();
    Table bookContainer = new Table();
    Table storyContainer = new Table();
    Table rankingContainer = new Table();

    settingsContainer.add(settingsBtn).size(imageButtonSize, imageButtonSize);
    bookContainer.add(bookBtn).size(imageButtonSize, imageButtonSize);
    storyContainer.add(storyBtn).size(imageButtonSize, imageButtonSize);
    rankingContainer.add(rankingBtn).size(imageButtonSize, imageButtonSize);

    imageButtonsGroup.addActor(settingsContainer);
    imageButtonsGroup.addActor(bookContainer);
    imageButtonsGroup.addActor(storyContainer);
    imageButtonsGroup.addActor(rankingContainer);

    table.add(imageButtonsGroup).padTop(20f);
    table.row();
    table.add().expandY();

    stage.addActor(table);
    applyUiScale();
  }

  /**
   * Creates an ImageButton with the specified image, with error handling
   *
   * @param imagePath Path to the button image
   * @param buttonName Name for the button (for logging)
   * @return The created Button
   */
  private ImageButton createImageButton(String imagePath, String buttonName) {
    try {
      logger.debug("Attempting to load texture from: {}", imagePath);

      Texture texture = ServiceLocator.getResourceService().getAsset(imagePath, Texture.class);
      logger.debug("Successfully loaded texture for {}", buttonName);

      TextureRegion region = new TextureRegion(texture);
      TextureRegionDrawable drawable = new TextureRegionDrawable(region);

      TextureRegionDrawable drawableDown = new TextureRegionDrawable(region);
      drawableDown.setMinSize(drawable.getMinWidth() * 0.9f, drawable.getMinHeight() * 0.9f);

      ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
      style.up = drawable;
      style.down = drawableDown;
      style.over = drawable;

      ImageButton button = new ImageButton(style);
      button.setTransform(true);
      button.setScale(0.8f);
      return button;
    } catch (Exception e) {
      logger.error("Failed to create {} button from {}: {}", buttonName, imagePath, e.getMessage());
      logger.error("Stack trace: ", e);
      try {
        logger.debug("Attempting fallback to star.png");
        Texture fallbackTexture = ServiceLocator.getResourceService().getAsset("images/star.png", Texture.class);
        TextureRegionDrawable fallbackDrawable = new TextureRegionDrawable(new TextureRegion(fallbackTexture));

        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        style.up = fallbackDrawable;
        style.down = fallbackDrawable;
        style.over = fallbackDrawable;

        ImageButton button = new ImageButton(style);
        button.setTransform(true);
        button.setScale(0.8f);

        return button;
      } catch (Exception ex) {
        logger.error("Failed to create fallback button: {}", ex.getMessage());
        return new ImageButton(new ImageButton.ImageButtonStyle());
      }
    }
  }

  private void applyUiScale() {
    UserSettings.Settings settings = UserSettings.get();
    if (table != null) {
      table.setTransform(true);
      table.validate();
      table.setOrigin(table.getWidth() / 2f, table.getHeight() / 2f);
      table.setScale(settings.uiScale);
    }
  }

  @Override
  public void draw(SpriteBatch batch) {
    // Empty implementation as required by interface
  }

  @Override
  public float getZIndex() {
    return Z_INDEX;
  }

  private TextButtonStyle createCustomButtonStyle() {
    TextButtonStyle style = new TextButtonStyle();

    // Use Segoe UI font
    style.font = skin.getFont("segoe_ui");

    // Load button background image
    Texture buttonTexture = ServiceLocator.getResourceService()
            .getAsset("images/Main_Menu_Button_Background.png", Texture.class);
    TextureRegion buttonRegion = new TextureRegion(buttonTexture);

    // Create NinePatch for scalable button background
    NinePatch buttonPatch = new NinePatch(buttonRegion, 10, 10, 10, 10);

    // Create pressed state NinePatch (slightly darker)
    NinePatch pressedPatch = new NinePatch(buttonRegion, 10, 10, 10, 10);
    pressedPatch.setColor(new Color(0.8f, 0.8f, 0.8f, 1f));

    // Create hover state NinePatch (slightly brighter)
    NinePatch hoverPatch = new NinePatch(buttonRegion, 10, 10, 10, 10);
    hoverPatch.setColor(new Color(1.1f, 1.1f, 1.1f, 1f));

    // Set button states
    style.up = new NinePatchDrawable(buttonPatch);
    style.down = new NinePatchDrawable(pressedPatch);
    style.over = new NinePatchDrawable(hoverPatch);

    // Set font colors
    style.fontColor = Color.WHITE;
    style.downFontColor = Color.LIGHT_GRAY;
    style.overFontColor = Color.WHITE;

    return style;
  }

  @Override
  public void dispose() {
    table.clear();
    super.dispose();
  }
}