package com.csse3200.game.components.mainmenu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.csse3200.game.areas.ForestGameArea;
import com.csse3200.game.files.UserSettings;
import com.csse3200.game.services.GameStateService;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main menu styled to match the Settings screen (same panel + button look).
 * Minimal logic changes; only layout/visuals updated for consistency.
 */
public class MainMenuDisplay extends UIComponent {
  private static final Logger logger = LoggerFactory.getLogger(MainMenuDisplay.class);
  private static final float Z_INDEX = 2f;

  // Reuse the same art as Settings for perfect consistency
  private static final String BG_TEX    = "images/main_menu_background.png";
  private static final String PANEL_TEX = "images/settings_bg.png";
  private static final String BTN_TEX   = "images/settings_bg_button.png";

  private Table root; // holds the centered panel

  @Override
  public void create() {
    super.create();
    ensureAssetsLoaded();
    addActors();
    ForestGameArea.NUM_ENEMIES_DEFEATED = 0;
    ForestGameArea.NUM_ENEMIES_TOTAL = 0;
  }

  /** Make sure panel/button textures exist to avoid runtime failures. */
  private void ensureAssetsLoaded() {
    ResourceService rs = ServiceLocator.getResourceService();
    rs.loadTextures(new String[]{ BG_TEX, PANEL_TEX, BTN_TEX,
            "images/cogwheel2-modified.png", "images/book-modified.png",
            "images/story3-modified.png", "images/rank-modified.png", "images/star.png" });
    rs.loadAll();
  }

  private void addActors() {
    // Background
    Image backgroundImage = new Image(ServiceLocator.getResourceService().getAsset(BG_TEX, Texture.class));
    backgroundImage.setFillParent(true);
    stage.addActor(backgroundImage);

    // Title (match Settings cyan header)
    Label title = new Label("Main Menu", skin, "title");
    title.setAlignment(Align.center);
    title.setColor(Color.valueOf("CFF2FF"));

    // Buttons — same style as Settings screen
    TextButtonStyle buttonStyle = createSettingsButtonStyle();
    TextButton startBtn = new TextButton("New Game", buttonStyle);
    TextButton loadBtn  = new TextButton("Continue", buttonStyle);
    TextButton exitBtn  = new TextButton("Exit", buttonStyle);

    startBtn.getLabel().setColor(Color.WHITE);
    loadBtn.getLabel().setColor(Color.WHITE);
    exitBtn.getLabel().setColor(Color.WHITE);

    startBtn.addListener(new ChangeListener() {
      @Override public void changed(ChangeEvent changeEvent, Actor actor) {
        logger.debug("Start button clicked");
        ServiceLocator.registerGameStateService(new GameStateService());
        entity.getEvents().trigger("start");
      }
    });
    loadBtn.addListener(new ChangeListener() {
      @Override public void changed(ChangeEvent changeEvent, Actor actor) {
        logger.debug("Continue button clicked");
        entity.getEvents().trigger("continue");
      }
    });
    exitBtn.addListener(new ChangeListener() {
      @Override public void changed(ChangeEvent changeEvent, Actor actor) {
        logger.debug("Exit button clicked");
        entity.getEvents().trigger("exit");
      }
    });

    // Icon buttons (unchanged behavior, laid out inside the panel footer)
    ImageButton settingsBtn = createImageButton("images/cogwheel2-modified.png", "Settings");
    ImageButton bookBtn     = createImageButton("images/book-modified.png", "Book");
    ImageButton storyBtn    = createImageButton("images/story3-modified.png", "Story");
    ImageButton rankingBtn  = createImageButton("images/rank-modified.png", "Ranking");

    settingsBtn.addListener(new ChangeListener() {
      @Override public void changed(ChangeEvent changeEvent, Actor actor) {
        logger.debug("Settings button clicked");
        entity.getEvents().trigger("settings");
      }
    });
    bookBtn.addListener(new ChangeListener() {
      @Override public void changed(ChangeEvent changeEvent, Actor actor) {
        logger.debug("Book button clicked");
        entity.getEvents().trigger("book");
      }
    });
    storyBtn.addListener(new ChangeListener() {
      @Override public void changed(ChangeEvent changeEvent, Actor actor) {
        logger.debug("Story button clicked");
        entity.getEvents().trigger("story");
      }
    });
    rankingBtn.addListener(new ChangeListener() {
      @Override public void changed(ChangeEvent changeEvent, Actor actor) {
        logger.debug("Ranking button clicked");
        entity.getEvents().trigger("ranking");
      }
    });

    // Settings-style panel (same NinePatch as the Settings screen)
    Texture panelTex = ServiceLocator.getResourceService().getAsset(PANEL_TEX, Texture.class);
    NinePatch panelPatch = new NinePatch(new TextureRegion(panelTex), 20, 20, 20, 20);
    Table panel = new Table(skin);
    panel.setBackground(new NinePatchDrawable(panelPatch));
    panel.defaults().pad(12f);

    // Center column of big menu buttons
    Table mainButtons = new Table();
    mainButtons.add(startBtn).size(220f, 52f).padTop(8f).row();
    mainButtons.add(loadBtn).size(220f, 52f).padTop(12f).row();
    mainButtons.add(exitBtn).size(220f, 52f).padTop(12f).row();

    // Footer row of circular icon buttons
    float iconSize = 64f;
    Table footerIcons = new Table();
    footerIcons.padTop(16f);
    footerIcons.add(settingsBtn).size(iconSize, iconSize).padRight(18f);
    footerIcons.add(bookBtn).size(iconSize, iconSize).padRight(18f);
    footerIcons.add(storyBtn).size(iconSize, iconSize).padRight(18f);
    footerIcons.add(rankingBtn).size(iconSize, iconSize);

    // Compose panel content
    panel.add(title).expandX().top().padTop(10f).padBottom(10f).row();
    panel.add(mainButtons).center().row();
    panel.add(footerIcons).center().padBottom(6f).row();

    // Root centers the panel at Settings-like size
    root = new Table();
    root.setFillParent(true);
    root.add(panel).center()
            .size(Math.min(Gdx.graphics.getWidth() * 0.45f, 600f),
                    Math.min(Gdx.graphics.getHeight() * 0.75f, 650f));
    stage.addActor(root);

    applyUiScale();
  }

  /** Same button look as Settings: orange rounded rectangles with hover/press tints. */
  private TextButtonStyle createSettingsButtonStyle() {
    TextButtonStyle style = new TextButtonStyle();
    style.font = skin.getFont("segoe_ui");

    Texture tex = ServiceLocator.getResourceService().getAsset(BTN_TEX, Texture.class);
    TextureRegion tr = new TextureRegion(tex);

    TextureRegionDrawable up   = new TextureRegionDrawable(tr);
    TextureRegionDrawable down = new TextureRegionDrawable(tr);
    TextureRegionDrawable over = new TextureRegionDrawable(tr);

    // Slight tints to mimic Settings behavior
    down.tint(new Color(0.8f, 0.8f, 0.8f, 1f));
    over.tint(new Color(1.1f, 1.1f, 1.1f, 1f));

    style.up = up; style.down = down; style.over = over;
    style.fontColor = Color.WHITE;
    style.overFontColor = Color.WHITE;
    style.downFontColor = Color.LIGHT_GRAY;
    return style;
  }

  /**
   * Creates an ImageButton with the specified image, with error handling.
   */
  private ImageButton createImageButton(String imagePath, String buttonName) {
    try {
      Texture texture = ServiceLocator.getResourceService().getAsset(imagePath, Texture.class);
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
      try {
        Texture fallbackTexture = ServiceLocator.getResourceService().getAsset("images/star.png", Texture.class);
        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        TextureRegionDrawable dr = new TextureRegionDrawable(new TextureRegion(fallbackTexture));
        style.up = dr; style.down = dr; style.over = dr;
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
    if (root != null) {
      root.setTransform(true);
      root.validate();
      root.setOrigin(root.getWidth() / 2f, root.getHeight() / 2f);
      root.setScale(settings.uiScale);
    }
  }

  @Override public void draw(SpriteBatch batch) { /* stage draws */ }
  @Override public float getZIndex() { return Z_INDEX; }

  @Override
  public void dispose() {
    if (root != null) root.clear();
    super.dispose();
  }
}
