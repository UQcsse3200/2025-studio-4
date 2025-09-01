package com.csse3200.game.components.settingsmenu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Graphics.Monitor;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.GdxGame;
import com.csse3200.game.GdxGame.ScreenType;
import com.csse3200.game.files.UserSettings;
import com.csse3200.game.files.UserSettings.DisplaySettings;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import com.csse3200.game.utils.StringDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//  Added imports for overlay dim generation
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Color;

/**
 * Settings menu display and logic. If you bork the settings, they can be changed manually in
 * CSSE3200Game/settings.json under  home directory (This is C:/users/[username] on Windows).
 */
public class SettingsMenuDisplay extends UIComponent {
  private static final Logger logger = LoggerFactory.getLogger(SettingsMenuDisplay.class);
  private final GdxGame game;

  private Table rootTable;
  private TextField fpsText;
  private CheckBox fullScreenCheck;
  private CheckBox vsyncCheck;
  private Slider uiScaleSlider;
  private SelectBox<StringDecorator<DisplayMode>> displayModeSelect;
  private boolean overlayMode = false;

  //  Added: references used in overlay mode
  private Image backgroundImage;    // main-menu background (hidden in overlay mode)
  private Image dimImage;           // semi-transparent dim layer for overlay
  private Texture dimTexHandle;     // generated 1x1 texture; disposed in dispose()

  public SettingsMenuDisplay(GdxGame game) {
    super();
    this.game = game;
    this.overlayMode = overlayMode; // (benign no-op; kept as-is)
  }

  //  Added: overlay-mode constructor
  public SettingsMenuDisplay(GdxGame game, boolean overlayMode) {
    this(game);
    this.overlayMode = overlayMode;
  }

  @Override
  public void create() {
    super.create();
    addActors();
  }

  private void addActors() {
    // 背景图片 (kept  code; ONLY change is assigning to field, not a local var)
    if (!overlayMode) {
      backgroundImage =
              new Image(
                      ServiceLocator.getResourceService()
                              .getAsset("images/main_menu_background.png", Texture.class));
      backgroundImage.setFillParent(true);
      stage.addActor(backgroundImage);
    }

    Label title = new Label("Settings", skin, "title");
    Table settingsTable = makeSettingsTable();
    Table menuBtns = makeMenuBtns();

    rootTable = new Table();
    rootTable.setFillParent(true);

    rootTable.add(title).expandX().top().padTop(20f);

    rootTable.row().padTop(30f);
    rootTable.add(settingsTable).expandX().expandY();

    rootTable.row();
    rootTable.add(menuBtns).fillX();

    stage.addActor(rootTable);

    //  Added: Overlay wiring (only active when overlayMode=true)
    if (overlayMode) {
      // Hide the main-menu background while used as an overlay
      if (backgroundImage != null) {
        backgroundImage.setVisible(false);
      }

      // Create a semi-transparent dim layer without needing an asset
      Pixmap px = new Pixmap(1, 1, Format.RGBA8888);
      px.setColor(new Color(0f, 0f, 0f, 0.55f)); // ~55% black
      dimTexHandle = new Texture(px);
      px.dispose();

      dimImage = new Image(dimTexHandle);
      dimImage.setFillParent(true);
      stage.addActor(dimImage);

      // Keep settings UI above the dim layer
      dimImage.toBack();
      rootTable.toFront();

      // Start hidden; Pause overlay will toggle us
      dimImage.setVisible(false);
      rootTable.setVisible(false);

      // Events used by Pause overlay to toggle Settings overlay
      entity.getEvents().addListener("showSettingsOverlay", () -> {
        if (dimImage != null) dimImage.setVisible(true);
        if (rootTable != null) rootTable.setVisible(true);
      });
      entity.getEvents().addListener("hideSettingsOverlay", () -> {
        if (rootTable != null) rootTable.setVisible(false);
        if (dimImage != null) dimImage.setVisible(false);
      });
    }
  }

  private Table makeSettingsTable() {
    // Get current values
    UserSettings.Settings settings = UserSettings.get();

    // Create components
    Label fpsLabel = new Label("FPS Cap:", skin);
    fpsText = new TextField(Integer.toString(settings.fps), skin);

    Label fullScreenLabel = new Label("Fullscreen:", skin);
    fullScreenCheck = new CheckBox("", skin);
    fullScreenCheck.setChecked(settings.fullscreen);

    Label vsyncLabel = new Label("VSync:", skin);
    vsyncCheck = new CheckBox("", skin);
    vsyncCheck.setChecked(settings.vsync);

    Label uiScaleLabel = new Label("ui Scale (Unused):", skin);
    uiScaleSlider = new Slider(0.2f, 2f, 0.1f, false, skin);
    uiScaleSlider.setValue(settings.uiScale);
    Label uiScaleValue = new Label(String.format("%.2fx", settings.uiScale), skin);

    Label displayModeLabel = new Label("Resolution:", skin);
    displayModeSelect = new SelectBox<>(skin);
    Monitor selectedMonitor = Gdx.graphics.getMonitor();
    displayModeSelect.setItems(getDisplayModes(selectedMonitor));
    displayModeSelect.setSelected(getActiveMode(displayModeSelect.getItems()));

    // Position Components on table
    Table table = new Table();

    table.add(fpsLabel).right().padRight(15f);
    table.add(fpsText).width(100).left();

    table.row().padTop(10f);
    table.add(fullScreenLabel).right().padRight(15f);
    table.add(fullScreenCheck).left();

    table.row().padTop(10f);
    table.add(vsyncLabel).right().padRight(15f);
    table.add(vsyncCheck).left();

    table.row().padTop(10f);
    Table uiScaleTable = new Table();
    uiScaleTable.add(uiScaleSlider).width(100).left();
    uiScaleTable.add(uiScaleValue).left().padLeft(5f).expandX();

    table.add(uiScaleLabel).right().padRight(15f);
    table.add(uiScaleTable).left();

    table.row().padTop(10f);
    table.add(displayModeLabel).right().padRight(15f);
    table.add(displayModeSelect).left();

    // Events on inputs
    uiScaleSlider.addListener(
            (Event event) -> {
              float value = uiScaleSlider.getValue();
              uiScaleValue.setText(String.format("%.2fx", value));
              return true;
            });

    return table;
  }

  private StringDecorator<DisplayMode> getActiveMode(Array<StringDecorator<DisplayMode>> modes) {
    DisplayMode active = Gdx.graphics.getDisplayMode();

    for (StringDecorator<DisplayMode> stringMode : modes) {
      DisplayMode mode = stringMode.object;
      if (active.width == mode.width
              && active.height == mode.height
              && active.refreshRate == mode.refreshRate) {
        return stringMode;
      }
    }
    return null;
  }

  private Array<StringDecorator<DisplayMode>> getDisplayModes(Monitor monitor) {
    DisplayMode[] displayModes = Gdx.graphics.getDisplayModes(monitor);
    Array<StringDecorator<DisplayMode>> arr = new Array<>();

    for (DisplayMode displayMode : displayModes) {
      arr.add(new StringDecorator<>(displayMode, this::prettyPrint));
    }

    return arr;
  }

  private String prettyPrint(DisplayMode displayMode) {
    return displayMode.width + "x" + displayMode.height + ", " + displayMode.refreshRate + "hz";
  }

  private Table makeMenuBtns() {
    //  Added Back button; kept  Exit/Apply
    TextButton backBtn = new TextButton("Back", skin);
    TextButton exitBtn = new TextButton("Exit", skin);
    TextButton applyBtn = new TextButton("Apply", skin);

    backBtn.addListener(
            new ChangeListener() {
              @Override
              public void changed(ChangeEvent changeEvent, Actor actor) {
                logger.debug("Back button clicked");
                if (overlayMode) {
                  // Return to Pause overlay (stay in-game)
                  entity.getEvents().trigger("hideSettingsOverlay");
                  entity.getEvents().trigger("showPauseUI");
                } else {
                  // From main menu settings, Back behaves like Exit
                  exitMenu();
                }
              }
            });

    exitBtn.addListener(
            new ChangeListener() {
              @Override
              public void changed(ChangeEvent changeEvent, Actor actor) {
                logger.debug("Exit button clicked");
                exitMenu();
              }
            });

    applyBtn.addListener(
            new ChangeListener() {
              @Override
              public void changed(ChangeEvent changeEvent, Actor actor) {
                logger.debug("Apply button clicked");
                applyChanges();
              }
            });

    Table table = new Table();
    table.add(backBtn).expandX().left().pad(0f, 15f, 15f, 0f);
    table.add(exitBtn).pad(0f, 10f, 15f, 10f);
    table.add(applyBtn).expandX().right().pad(0f, 0f, 15f, 15f);
    return table;
  }

  private void applyChanges() {
    UserSettings.Settings settings = UserSettings.get();

    Integer fpsVal = parseOrNull(fpsText.getText());
    if (fpsVal != null) {
      settings.fps = fpsVal;
    }
    settings.fullscreen = fullScreenCheck.isChecked();
    settings.uiScale = uiScaleSlider.getValue();
    settings.displayMode = new DisplaySettings(displayModeSelect.getSelected().object);
    settings.vsync = vsyncCheck.isChecked();

    UserSettings.set(settings, true);
  }

  private void exitMenu() {
    if (overlayMode) {
      // In overlay mode, do not leave the game; go back to Pause overlay
      entity.getEvents().trigger("hideSettingsOverlay");
      entity.getEvents().trigger("showPauseUI");
      return;
    }
    // Original behaviour (main menu)
    game.setScreen(ScreenType.MAIN_MENU);
  }

  private Integer parseOrNull(String num) {
    try {
      return Integer.parseInt(num, 10);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  @Override
  protected void draw(SpriteBatch batch) {
    // draw is handled by the stage
  }

  @Override
  public void update() {
    stage.act(ServiceLocator.getTimeSource().getDeltaTime());
  }

  @Override
  public void dispose() {
    if (rootTable != null) rootTable.clear();
    if (dimTexHandle != null) {
      dimTexHandle.dispose();
      dimTexHandle = null;
    }
    super.dispose();
  }
}
