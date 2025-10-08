package com.csse3200.game.components.settingsmenu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Graphics.Monitor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
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

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;

import com.badlogic.gdx.scenes.scene2d.ui.Stack;

import com.csse3200.game.ui.UiStyles;

/**
 * Settings menu display and logic. If you bork the settings, they can be changed manually in
 * CSSE3200Game/settings.json under home directory (This is C:/users/[username] on Windows).
 */
public class SettingsMenuDisplay extends UIComponent {
    private static final Logger logger = LoggerFactory.getLogger(SettingsMenuDisplay.class);
    private final GdxGame game;

    private Table rootTable;
    private TextField fpsText;
    private CheckBox fullScreenCheck;
    private CheckBox vsyncCheck;
    private Slider uiScaleSlider;
    private Slider musicVolumeSlider;
    private Slider soundVolumeSlider;
    private SelectBox<StringDecorator<DisplayMode>> displayModeSelect;
    private SelectBox<String> difficultySelect;
    private SelectBox<String> heroWeaponSelect;
    private SelectBox<String> heroEffectSelect;
    private boolean overlayMode = false;

    private Image backgroundImage;
    private Image dimImage;
    private Texture dimTexHandle;
    private Texture panelTexHandle;
    private Stack overlayStack;

    public SettingsMenuDisplay(GdxGame game) {
        super();
        this.game = game;
        this.overlayMode = false;
    }

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
        if (!overlayMode) {
            backgroundImage = new Image(ServiceLocator.getResourceService()
                    .getAsset("images/main_menu_background.png", Texture.class));
            backgroundImage.setFillParent(true);
            stage.addActor(backgroundImage);
        }

        Label title = createTitleLabel();
        Table settingsTable = makeSettingsTable();
        Table menuBtns = makeMenuBtns();

        Table panel = new Table(skin);
        panel.defaults().pad(12f);
        if (skin.has("window", Drawable.class)) {
            panel.setBackground(skin.getDrawable("window"));
        } else if (skin.has("dialog", Drawable.class)) {
            panel.setBackground(skin.getDrawable("dialog"));
        } else {
            Pixmap pm = new Pixmap(8, 8, Format.RGBA8888);
            pm.setColor(new Color(0f, 0f, 0f, 0.35f));
            pm.fill();
            panelTexHandle = new Texture(pm);
            pm.dispose();
            panel.setBackground(new TextureRegionDrawable(new TextureRegion(panelTexHandle)));
        }
        panel.add(settingsTable).row();
        panel.add(menuBtns).fillX();

        rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.add(title).expandX().top().padTop(20f);
        rootTable.row().padTop(30f);
        rootTable.add(panel).center()
                .width(Math.min(Gdx.graphics.getWidth() * 0.55f, 720f));

        if (overlayMode) {
            Pixmap px = new Pixmap(1, 1, Format.RGBA8888);
            px.setColor(new Color(0f, 0f, 0f, 0.70f));
            dimTexHandle = new Texture(px);
            px.dispose();
            dimImage = new Image(dimTexHandle);
            dimImage.setFillParent(true);

            overlayStack = new Stack();
            overlayStack.setFillParent(true);
            overlayStack.add(dimImage);
            overlayStack.add(rootTable);
            stage.addActor(overlayStack);
            overlayStack.toFront();
            overlayStack.setVisible(false);

            entity.getEvents().addListener("showSettingsOverlay", () -> overlayStack.setVisible(true));
            entity.getEvents().addListener("hideSettingsOverlay", () -> overlayStack.setVisible(false));

            stage.addListener(new InputListener() {
                @Override
                public boolean keyDown(InputEvent event, int keycode) {
                    if (!overlayStack.isVisible()) return false;
                    if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.P) {
                        entity.getEvents().trigger("hideSettingsOverlay");
                        entity.getEvents().trigger("showPauseUI");
                        return true;
                    }
                    return false;
                }
            });

            if (backgroundImage != null) backgroundImage.setVisible(false);
        } else {
            stage.addActor(rootTable);
        }

        UserSettings.Settings currentSettings = UserSettings.get();
        if (rootTable != null) {
            rootTable.setTransform(true);
            rootTable.setScale(currentSettings.uiScale);
        }
    }

    private Table makeSettingsTable() {
        UserSettings.Settings settings = UserSettings.get();

        Label fpsLabel = new Label("FPS Cap:", skin);
        fpsText = new TextField(Integer.toString(settings.fps), skin);

        Label fullScreenLabel = new Label("Fullscreen:", skin);
        fullScreenCheck = new CheckBox("", skin);
        fullScreenCheck.setChecked(settings.fullscreen);

        Label vsyncLabel = new Label("VSync:", skin);
        vsyncCheck = new CheckBox("", skin);
        vsyncCheck.setChecked(settings.vsync);

        Label uiScaleLabel = new Label("UI Scale:", skin);
        uiScaleSlider = new Slider(0.2f, 2f, 0.1f, false, skin);
        uiScaleSlider.setValue(settings.uiScale);
        Label uiScaleValue = new Label(String.format("%.2fx", settings.uiScale), skin);

        Label musicVolumeLabel = new Label("Music Volume:", skin);
        musicVolumeSlider = new Slider(0f, 1f, 0.1f, false, skin);
        musicVolumeSlider.setValue(settings.musicVolume);
        Label musicVolumeValue = new Label(String.format("%.0f%%", settings.musicVolume * 100), skin);

        Label soundVolumeLabel = new Label("Sound Volume:", skin);
        soundVolumeSlider = new Slider(0f, 1f, 0.1f, false, skin);
        soundVolumeSlider.setValue(settings.soundVolume);
        Label soundVolumeValue = new Label(String.format("%.0f%%", settings.soundVolume * 100), skin);

        Label difficultyLabel = new Label("Difficulty:", skin);
        difficultySelect = new SelectBox<>(skin);
        difficultySelect.setItems("Easy", "Normal", "Hard");
        difficultySelect.setSelected(settings.difficulty);

        Label displayModeLabel = new Label("Resolution:", skin);
        displayModeSelect = new SelectBox<>(skin);
        Monitor selectedMonitor = Gdx.graphics.getMonitor();
        displayModeSelect.setItems(getDisplayModes(selectedMonitor));
        displayModeSelect.setSelected(getActiveMode(displayModeSelect.getItems()));

        Label heroWeaponLabel = new Label("Hero Weapon:", skin);
        heroWeaponSelect = new SelectBox<>(skin);
        heroWeaponSelect.setItems("Normal Sword", "Weapon 2", "Weapon 3");
        heroWeaponSelect.setSelected(settings.heroWeapon.equals("default") ? "Normal Sword" : settings.heroWeapon);

        Label heroEffectLabel = new Label("Weapon Sound:", skin);
        heroEffectSelect = new SelectBox<>(skin);
        heroEffectSelect.setItems("Sound 1", "Sound 2", "Sound 3");
        heroEffectSelect.setSelected(settings.heroEffect.equals("default") ? "Sound 1" : settings.heroEffect);

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
        Table musicVolumeTable = new Table();
        musicVolumeTable.add(musicVolumeSlider).width(100).left();
        musicVolumeTable.add(musicVolumeValue).left().padLeft(5f).expandX();

        table.add(musicVolumeLabel).right().padRight(15f);
        table.add(musicVolumeTable).left();

        table.row().padTop(10f);
        Table soundVolumeTable = new Table();
        soundVolumeTable.add(soundVolumeSlider).width(100).left();
        soundVolumeTable.add(soundVolumeValue).left().padLeft(5f).expandX();

        table.add(soundVolumeLabel).right().padRight(15f);
        table.add(soundVolumeTable).left();

        table.row().padTop(10f);
        table.add(difficultyLabel).right().padRight(15f);
        table.add(difficultySelect).left();

        table.row().padTop(10f);
        table.add(displayModeLabel).right().padRight(15f);
        table.add(displayModeSelect).left();

        table.row().padTop(20f);
        Label heroCustomizationTitle = new Label("Hero Customization", skin);
        heroCustomizationTitle.setColor(Color.YELLOW);
        table.add(heroCustomizationTitle).colspan(2).center().padBottom(10f);

        table.row().padTop(10f);
        table.add(heroWeaponLabel).right().padRight(15f);
        table.add(heroWeaponSelect).left();

        table.row().padTop(10f);
        table.add(heroEffectLabel).right().padRight(15f);
        table.add(heroEffectSelect).left();

        return table;
    }

    private StringDecorator<DisplayMode> getActiveMode(Array<StringDecorator<DisplayMode>> modes) {
        DisplayMode active = Gdx.graphics.getDisplayMode();
        for (StringDecorator<DisplayMode> stringMode : modes) {
            DisplayMode mode = stringMode.object;
            if (active.width == mode.width && active.height == mode.height && active.refreshRate == mode.refreshRate) {
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
        var style = UiStyles.orangeButton(skin);
        TextButton backBtn  = new TextButton("Back", style);
        TextButton applyBtn = new TextButton("Apply", style);

        backBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                if (overlayMode) {
                    entity.getEvents().trigger("hideSettingsOverlay");
                    entity.getEvents().trigger("showPauseUI");
                } else {
                    exitMenu();
                }
            }
        });

        applyBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                applyChanges();
            }
        });

        Table table = new Table();
        table.add(backBtn).size(150f, 50f).expandX().left().pad(0f, 15f, 15f, 0f);
        table.add(applyBtn).size(150f, 50f).expandX().right().pad(0f, 0f, 15f, 15f);
        return table;
    }

    private void applyChanges() {
        UserSettings.Settings settings = UserSettings.get();

        Integer fpsVal = parseOrNull(fpsText.getText());
        if (fpsVal != null) settings.fps = fpsVal;
        settings.fullscreen = fullScreenCheck.isChecked();
        settings.uiScale = uiScaleSlider.getValue();
        settings.displayMode = new DisplaySettings(displayModeSelect.getSelected().object);
        settings.vsync = vsyncCheck.isChecked();
        settings.musicVolume = musicVolumeSlider.getValue();
        settings.soundVolume = soundVolumeSlider.getValue();
        settings.difficulty = difficultySelect.getSelected();
        settings.heroWeapon = heroWeaponSelect.getSelected().toLowerCase();
        settings.heroEffect = heroEffectSelect.getSelected().toLowerCase();

        UserSettings.set(settings, true);

        DisplayMode selectedMode = displayModeSelect.getSelected().object;
        if (settings.fullscreen) {
            Gdx.graphics.setFullscreenMode(selectedMode);
        } else {
            Gdx.graphics.setWindowedMode(selectedMode.width, selectedMode.height);
        }

        Gdx.graphics.setVSync(settings.vsync);
        Gdx.graphics.setForegroundFPS(settings.fps);

        if (ServiceLocator.getAudioService() != null) {
            ServiceLocator.getAudioService().setMusicVolume(settings.musicVolume);
            ServiceLocator.getAudioService().setSoundVolume(settings.soundVolume);
            ServiceLocator.getAudioService().updateSettings();
        }

        applyUiScale(settings.uiScale);

        if (ServiceLocator.getEntityService() != null) {
            for (var entity : ServiceLocator.getEntityService().getEntities()) {
                entity.getEvents().trigger("settingsApplied", settings);
            }
        }

        logger.info("Settings applied (fullscreen={}, {}x{}, fps={}, scale={})",
                settings.fullscreen, selectedMode.width, selectedMode.height, settings.fps, settings.uiScale);
    }

    private void applyUiScale(float scale) {
        if (rootTable != null) {
            rootTable.setTransform(true);
            rootTable.validate();
            rootTable.setOrigin(rootTable.getWidth() / 2f, rootTable.getHeight() / 2f);
            rootTable.setScale(scale);
        }
    }

    private void exitMenu() {
        if (overlayMode) {
            entity.getEvents().trigger("hideSettingsOverlay");
            entity.getEvents().trigger("hidePauseUI");
            entity.getEvents().trigger("resume");
            return;
        }
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
    protected void draw(SpriteBatch batch) {}

    @Override
    public void update() {
        stage.act(ServiceLocator.getTimeSource().getDeltaTime());
    }

    private Label createTitleLabel() {
        BitmapFont arialBlackFont = skin.getFont("arial_black");
        Label titleLabel = new Label("Settings", new Label.LabelStyle(arialBlackFont, Color.WHITE));
        titleLabel.setFontScale(1.5f);
        return titleLabel;
    }

    private TextButtonStyle createCustomButtonStyle() {
        TextButtonStyle style = new TextButtonStyle();
        style.font = skin.getFont("segoe_ui");

        Texture buttonTexture = ServiceLocator.getResourceService()
                .getAsset("images/Main_Menu_Button_Background.png", Texture.class);
        TextureRegion buttonRegion = new TextureRegion(buttonTexture);
        NinePatch buttonPatch = new NinePatch(buttonRegion, 10, 10, 10, 10);
        NinePatch pressedPatch = new NinePatch(buttonRegion, 10, 10, 10, 10);
        pressedPatch.setColor(new Color(0.8f, 0.8f, 0.8f, 1f));
        NinePatch hoverPatch = new NinePatch(buttonRegion, 10, 10, 10, 10);
        hoverPatch.setColor(new Color(1.1f, 1.1f, 1.1f, 1f));

        style.up = new NinePatchDrawable(buttonPatch);
        style.down = new NinePatchDrawable(pressedPatch);
        style.over = new NinePatchDrawable(hoverPatch);
        style.fontColor = Color.WHITE;
        style.downFontColor = Color.LIGHT_GRAY;
        style.overFontColor = Color.WHITE;

        return style;
    }

    @Override
    public float getZIndex() {
        return 150f;
    }

    @Override
    public void dispose() {
        if (overlayStack != null) overlayStack.clear();
        if (dimTexHandle != null) dimTexHandle.dispose();
        if (panelTexHandle != null) panelTexHandle.dispose();
        super.dispose();
    }
}
