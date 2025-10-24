package com.csse3200.game.components.mainmenu;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.csse3200.game.areas.ForestGameArea;
import com.csse3200.game.files.UserSettings;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main menu themed like Settings, with a smaller panel placed lower
 * so the big game title remains visible above it.
 */
public class MainMenuDisplay extends UIComponent {
  private static final Logger logger = LoggerFactory.getLogger(MainMenuDisplay.class);
  private static final float Z_INDEX = 2f;

  private static final String BG_TEX    = "images/main_menu_background.png";
  private static final String PANEL_TEX = "images/settings_bg.png";
  private static final String BTN_TEX   = "images/settings_bg_button.png";

  private Table root;

  @Override
  public void create() {
    super.create();
    ensureAssetsLoaded();
    addActors();
    ForestGameArea.NUM_ENEMIES_DEFEATED = 0;
    ForestGameArea.NUM_ENEMIES_TOTAL = 0;
  }

  private void ensureAssetsLoaded() {
    ResourceService rs = ServiceLocator.getResourceService();
    rs.loadTextures(new String[]{
        BG_TEX, PANEL_TEX, BTN_TEX,
        "images/cogwheel2-modified.png", "images/book-modified.png",
        "images/story3-modified.png", "images/rank-modified.png", "images/star.png"
    });
    rs.loadAll();
  }

  private void addActors() {
    // Background
    Image backgroundImage =
        new Image(ServiceLocator.getResourceService().getAsset(BG_TEX, Texture.class));
    backgroundImage.setFillParent(true);
    stage.addActor(backgroundImage);

    // Button style (same family as Settings)
    TextButtonStyle buttonStyle = createSettingsButtonStyle();
    TextButton startBtn = new TextButton("New Game", buttonStyle);
    TextButton loadBtn  = new TextButton("Continue", buttonStyle);
    TextButton exitBtn  = new TextButton("Exit", buttonStyle);

    startBtn.getLabel().setColor(Color.WHITE);
    loadBtn.getLabel().setColor(Color.WHITE);
    exitBtn.getLabel().setColor(Color.WHITE);

    // Icon buttons
    ImageButton settingsBtn = createImageButton("images/cogwheel2-modified.png", "settings");
    ImageButton bookBtn     = createImageButton("images/book-modified.png", "book");
    ImageButton storyBtn    = createImageButton("images/story3-modified.png", "story");
    ImageButton rankingBtn  = createImageButton("images/rank-modified.png", "ranking");

    // === Listeners ===
    startBtn.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent changeEvent, Actor actor) {
        logger.debug("Start button clicked");
        entity.getEvents().trigger("start");
      }
    });

    loadBtn.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent changeEvent, Actor actor) {
        logger.debug("Continue button clicked");
        entity.getEvents().trigger("continue");
      }
    });

    exitBtn.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent changeEvent, Actor actor) {
        logger.debug("Exit button clicked");
        entity.getEvents().trigger("exit");
      }
    });

    settingsBtn.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent changeEvent, Actor actor) {
        logger.debug("Settings button clicked");
        entity.getEvents().trigger("settings");
      }
    });

    rankingBtn.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent changeEvent, Actor actor) {
        logger.debug("Ranking button clicked");
        entity.getEvents().trigger("ranking");
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

    // === Panel (smaller + tighter) ===
    Texture panelTex = ServiceLocator.getResourceService().getAsset(PANEL_TEX, Texture.class);
    NinePatch panelPatch = new NinePatch(new TextureRegion(panelTex), 20, 20, 20, 20);

    final float PANEL_W = 410f;        // smaller frame
    final float PANEL_H = 380f;        // shorter height (reveals more of game title)

    Table panel = new Table(skin);
    panel.setBackground(new NinePatchDrawable(panelPatch));
    panel.pad(18f, 22f, 18f, 22f);
    panel.defaults().pad(6f);

    // Title (tucked up into the top band)
    Label title = new Label("Main Menu", skin, "title");
    title.setColor(Color.valueOf("CFF2FF"));
    title.setAlignment(Align.center);
    panel.add(title).expandX().padTop(-6f).padBottom(8f).row();

    // Center column of menu buttons
    Table mainButtons = new Table();
    mainButtons.defaults().width(240f).height(46f).padTop(10f);
    mainButtons.add(startBtn).row();
    mainButtons.add(loadBtn).row();
    mainButtons.add(exitBtn).row();

    // Footer icons
    float iconSize = 56f;
    Table footerIcons = new Table();
    footerIcons.defaults().size(iconSize, iconSize).padRight(16f);
    footerIcons.add(settingsBtn);
    footerIcons.add(bookBtn);
    footerIcons.add(storyBtn);
    footerIcons.add(rankingBtn);

    panel.add(mainButtons).center().row();
    panel.add(footerIcons).center().padTop(12f).row();

    // === Root positioning ===
    // Place the panel lower so the large title above is fully visible.
    root = new Table();
    root.setFillParent(true);
    root.top();                    // anchor from top
    root.add().height(170f).row(); // push panel down ~170px (tweak if needed)
    root.add(panel).size(PANEL_W, PANEL_H).center();
    root.row();
    root.add().expandY();          // fill the rest
    stage.addActor(root);

    applyUiScale();
  }

  private ImageButton createImageButton(String imagePath, String eventName) {
    Texture texture = ServiceLocator.getResourceService().getAsset(imagePath, Texture.class);
    TextureRegionDrawable drawable = new TextureRegionDrawable(new TextureRegion(texture));
    ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
    style.up = drawable;
    style.down = drawable;
    style.over = drawable;

    ImageButton button = new ImageButton(style);
    button.setTransform(true);
    button.setScale(0.85f);
    button.addListener(new ChangeListener() {
      @Override public void changed(ChangeEvent event, Actor actor) {
        entity.getEvents().trigger(eventName);
      }
    });
    return button;
  }

  /** Orange slab buttons like the Settings menu */
  private TextButtonStyle createSettingsButtonStyle() {
    TextButtonStyle style = new TextButtonStyle();
    style.font = skin.getFont("segoe_ui");

    Texture tex = ServiceLocator.getResourceService().getAsset(BTN_TEX, Texture.class);
    TextureRegion tr = new TextureRegion(tex);

    TextureRegionDrawable up   = new TextureRegionDrawable(tr);
    TextureRegionDrawable down = new TextureRegionDrawable(tr);
    TextureRegionDrawable over = new TextureRegionDrawable(tr);

    down.tint(new Color(0.8f, 0.8f, 0.8f, 1f));
    over.tint(new Color(1.1f, 1.1f, 1.1f, 1f));

    style.up = up; style.down = down; style.over = over;
    style.fontColor = Color.WHITE;
    style.overFontColor = Color.WHITE;
    style.downFontColor = Color.LIGHT_GRAY;
    return style;
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
