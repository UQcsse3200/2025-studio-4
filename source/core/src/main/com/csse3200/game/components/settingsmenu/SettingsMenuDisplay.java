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

//  Added imports for overlay dim generation + panel background
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

//  Added imports for ESC handling in overlay mode
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;

//  Added import for overlay stacking
import com.badlogic.gdx.scenes.scene2d.ui.Stack;

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

    //  Added: for fallback boxed panel background
    private Texture panelTexHandle;

    //  Added: single container to keep dim behind panel but above other UI
    private Stack overlayStack;

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
        // 背景图片 (only create when not overlaying in-game)
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

        // Boxed panel with settings + buttons
        Table panel = new Table(skin);
        panel.defaults().pad(12f);
        if (skin.has("window", Drawable.class)) {
            panel.setBackground(skin.getDrawable("window"));
        } else if (skin.has("dialog", Drawable.class)) {
            panel.setBackground(skin.getDrawable("dialog"));
        } else {
            Pixmap pm = new Pixmap(8, 8, Format.RGBA8888);
            pm.setColor(new Color(0f, 0f, 0f, 0.35f)); // translucent box
            pm.fill();
            panelTexHandle = new Texture(pm);
            pm.dispose();
            panel.setBackground(new TextureRegionDrawable(new TextureRegion(panelTexHandle)));
        }
        panel.add(settingsTable).row();
        panel.add(menuBtns).fillX();

        // Root table holds title + panel
        rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.add(title).expandX().top().padTop(20f);
        rootTable.row().padTop(30f);
        rootTable.add(panel).center()
                .width(Math.min(Gdx.graphics.getWidth() * 0.55f, 720f));

        if (overlayMode) {
            // Stronger dim so it’s clearly visible behind the panel
            Pixmap px = new Pixmap(1, 1, Format.RGBA8888);
            px.setColor(new Color(0f, 0f, 0f, 0.70f)); // 70% black
            dimTexHandle = new Texture(px);
            px.dispose();
            dimImage = new Image(dimTexHandle);
            dimImage.setFillParent(true);

            // Stack keeps dim under the panel but together as one overlay
            overlayStack = new Stack();
            overlayStack.setFillParent(true);
            overlayStack.add(dimImage);   // bottom
            overlayStack.add(rootTable);  // top

            // Add overlay last and bring to front so it covers other UI
            stage.addActor(overlayStack);
            overlayStack.toFront();

            // Start hidden; Pause overlay will toggle us
            overlayStack.setVisible(false);

            // Events used by Pause overlay to toggle Settings overlay
            entity.getEvents().addListener("showSettingsOverlay", () -> {
                if (overlayStack != null) overlayStack.setVisible(true);
            });
            entity.getEvents().addListener("hideSettingsOverlay", () -> {
                if (overlayStack != null) overlayStack.setVisible(false);
            });

            // Handle ESC/P while settings overlay is visible: behave like "Back"
            stage.addListener(new InputListener() {
                @Override
                public boolean keyDown(InputEvent event, int keycode) {
                    if (overlayStack == null || !overlayStack.isVisible()) return false; // only if overlay is up
                    if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.P) {
                        entity.getEvents().trigger("hideSettingsOverlay");
                        entity.getEvents().trigger("showPauseUI");
                        return true; // consume so PauseInput doesn't also toggle
                    }
                    return false;
                }
            });

            // Hide main-menu background if it exists (overlay uses dim instead)
            if (backgroundImage != null) {
                backgroundImage.setVisible(false);
            }
        } else {
            // Non-overlay (main menu): show settings without dim
            stage.addActor(rootTable);
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
        // Back + Apply only (Exit removed)
        TextButton backBtn = new TextButton("Back", skin);
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
            // Close settings overlay and fully resume gameplay
            entity.getEvents().trigger("hideSettingsOverlay"); // hide our panel + dim
            entity.getEvents().trigger("hidePauseUI");         // ensure pause overlay is hidden
            entity.getEvents().trigger("resume");              // timeScale = 1f (game continues)
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

    // Keep this overlay above HUD/pause layers
    @Override
    public float getZIndex() {
        return 150f;
    }

    @Override
    public void dispose() {
        if (overlayStack != null) overlayStack.clear();
        if (dimTexHandle != null) {
            dimTexHandle.dispose();
            dimTexHandle = null;
        }
        if (panelTexHandle != null) {
            panelTexHandle.dispose();
            panelTexHandle = null;
        }
        super.dispose();
    }
}
