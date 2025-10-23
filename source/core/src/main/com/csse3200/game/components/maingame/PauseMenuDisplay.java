package com.csse3200.game.components.maingame;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.GdxGame;
import com.csse3200.game.services.PlayerAvatarService;
import com.csse3200.game.services.PlayerNameService;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pause popup that ALWAYS renders above everything and toggles with ESC.
 * (No game pausing logic here; purely visual/input overlay.)
 */
public class PauseMenuDisplay extends UIComponent {
    private static final Logger logger = LoggerFactory.getLogger(PauseMenuDisplay.class);

    // Very high to sort above other UI layers if your UI system uses z-indices.
    private static final float Z_INDEX = 10_000f;

    private static final String PANEL_TEX = "images/settings_bg.png";
    private static final String BTN_TEX   = "images/settings_bg_button.png";

    private final GdxGame game;
    private Texture dimTexHandle;
    private Table overlayTable;
    private Image dimImage;
    private Image pauseIcon;

    public PauseMenuDisplay(GdxGame game) { this.game = game; }

    @Override
    public void create() {
        super.create();
        ensureAssetsLoaded();
        addActors();

        // ESC key toggles the pause overlay
        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    entity.getEvents().trigger("togglePause");
                    return true;
                }
                return false;
            }
        });

        entity.getEvents().addListener("showPauseUI", this::showOverlay);
        entity.getEvents().addListener("hidePauseUI", this::hideOverlay);
        entity.getEvents().addListener("togglePause", this::toggleOverlay);
    }

    private void ensureAssetsLoaded() {
        var rs = ServiceLocator.getResourceService();
        rs.loadTextures(new String[] { PANEL_TEX, BTN_TEX, "images/pause_button.png" });
        rs.loadAll();
    }

    private void addActors() {
        // Dimmer (full-screen). Touchable so it swallows clicks; remove if you only want visuals.
        Pixmap px = new Pixmap(1, 1, Format.RGBA8888);
        px.setColor(new Color(0f, 0f, 0f, 0.55f));
        px.fill();
        dimTexHandle = new Texture(px);
        px.dispose();

        dimImage = new Image(dimTexHandle);
        dimImage.setFillParent(true);
        dimImage.setVisible(false);
        dimImage.setTouchable(Touchable.enabled);
        dimImage.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                event.stop(); // eat input so nothing underneath is clickable
            }
        });
        stage.addActor(dimImage);

        // Pause window (compact, settings-themed)
        Texture panelTex = ServiceLocator.getResourceService().getAsset(PANEL_TEX, Texture.class);
        NinePatch panelPatch = new NinePatch(new TextureRegion(panelTex), 20, 20, 20, 20);

        overlayTable = new Table(skin);
        overlayTable.setFillParent(true);
        overlayTable.setVisible(false);
        overlayTable.setTouchable(Touchable.enabled);

        final float PANEL_W = 520f;
        final float PANEL_H = 560f;

        Table window = new Table(skin);
        window.setBackground(new NinePatchDrawable(panelPatch));
        window.pad(18f, 24f, 18f, 24f);
        window.defaults().pad(8f);

        Label title = new Label("Paused", skin, "title");
        title.setColor(Color.valueOf("CFF2FF"));

        Table playerInfo = createPlayerInfoSection();

        TextButtonStyle btnStyle = createSettingsButtonStyle();
        TextButton resume   = new TextButton("Resume", btnStyle);
        TextButton save     = new TextButton("Save", btnStyle);
        TextButton settings = new TextButton("Settings", btnStyle);
        TextButton ranking  = new TextButton("Ranking", btnStyle);
        TextButton quit     = new TextButton("Quit to Main Menu", btnStyle);

        resume.addListener(new ChangeListener(){ @Override public void changed(ChangeEvent e, Actor a){ toggleOverlay(); }});
        save.addListener(new ChangeListener(){ @Override public void changed(ChangeEvent e, Actor a){ entity.getEvents().trigger("hidePauseUI"); entity.getEvents().trigger("save"); }});
        settings.addListener(new ChangeListener(){ @Override public void changed(ChangeEvent e, Actor a){ entity.getEvents().trigger("showSettingsOverlay"); }});
        ranking.addListener(new ChangeListener(){ @Override public void changed(ChangeEvent e, Actor a){ entity.getEvents().trigger("showRanking"); }});
        quit.addListener(new ChangeListener(){ @Override public void changed(ChangeEvent e, Actor a){ entity.getEvents().trigger("quitToMenu"); }});

        Table buttons = new Table();
        buttons.defaults().width(240f).height(48f).padTop(10f);
        buttons.add(resume).row();
        buttons.add(save).row();
        buttons.add(settings).row();
        buttons.add(ranking).row();
        buttons.add(quit).row();

        window.add(title).expandX().padBottom(10f).row();
        window.add(playerInfo).padBottom(6f).row();
        window.add(buttons).center().row();

        overlayTable.add(window).size(PANEL_W, PANEL_H).center();
        stage.addActor(overlayTable);

        // Pause icon (opens/closes overlay)
        Texture pauseTex = ServiceLocator.getResourceService().getAsset("images/pause_button.png", Texture.class);
        pauseIcon = new Image(pauseTex);
        Table topRight = new Table();
        topRight.setFillParent(true);
        topRight.top().right();
        topRight.add(pauseIcon).size(48f, 48f).padTop(12f).padRight(150f);
        stage.addActor(topRight);

        pauseIcon.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                toggleOverlay();
            }
        });
    }

    private void bringOverlayToFront() {
        if (dimImage != null) dimImage.toFront();
        if (overlayTable != null) overlayTable.toFront();
        // Grab focus so keyboard/scroll never reaches anything below.
        stage.setKeyboardFocus(overlayTable);
        stage.setScrollFocus(overlayTable);
    }

    private void showOverlay() {
        dimImage.setVisible(true);
        overlayTable.setVisible(true);
        if (pauseIcon != null) pauseIcon.setVisible(false); // avoid overlap/icon clicks
        bringOverlayToFront();
    }

    private void hideOverlay() {
        overlayTable.setVisible(false);
        dimImage.setVisible(false);
        if (pauseIcon != null) pauseIcon.setVisible(true);
    }

    private void toggleOverlay() {
        if (overlayTable.isVisible()) hideOverlay();
        else showOverlay();
    }

    private TextButtonStyle createSettingsButtonStyle() {
        TextButtonStyle style = new TextButtonStyle();
        style.font = skin.getFont("segoe_ui");

        Texture tex = ServiceLocator.getResourceService().getAsset(BTN_TEX, Texture.class);
        TextureRegion tr = new TextureRegion(tex);
        TextureRegionDrawable up   = new TextureRegionDrawable(tr);
        TextureRegionDrawable down = new TextureRegionDrawable(tr);
        TextureRegionDrawable over = new TextureRegionDrawable(tr);

        down.tint(new Color(0.8f, 0.8f, 0.8f, 1f));
        over.tint(new Color(1.08f, 1.08f, 1.08f, 1f));

        style.up = up; style.down = down; style.over = over;
        style.fontColor = Color.WHITE;
        style.overFontColor = Color.WHITE;
        style.downFontColor = Color.LIGHT_GRAY;
        return style;
    }

    private Table createPlayerInfoSection() {
        Table t = new Table();
        PlayerNameService nameService = ServiceLocator.getPlayerNameService();
        PlayerAvatarService avatarService = ServiceLocator.getPlayerAvatarService();

        String name = nameService != null ? nameService.getPlayerName() : "Player";
        String avatarId = avatarService != null ? avatarService.getPlayerAvatar() : "avatar_1";

        Image avatar = createAvatarImage(avatarId, avatarService);
        Label nameLabel = new Label(name, skin);
        nameLabel.setColor(Color.CYAN);

        if (avatar != null) t.add(avatar).size(56, 56).padRight(12f);
        t.add(nameLabel);
        return t;
    }

    private Image createAvatarImage(String avatarId, PlayerAvatarService svc) {
        if (svc == null) return null;
        try {
            String path = svc.getAvatarImagePath(avatarId);
            Texture tex = ServiceLocator.getResourceService().getAsset(path, Texture.class);
            return new Image(new TextureRegionDrawable(new TextureRegion(tex)));
        } catch (Exception e) {
            return null;
        }
    }

    @Override protected void draw(SpriteBatch batch) { /* Stage draws */ }
    @Override public float getZIndex() { return Z_INDEX; }

    @Override
    public void dispose() {
        if (overlayTable != null) overlayTable.clear();
        if (dimTexHandle != null) dimTexHandle.dispose();
        super.dispose();
    }
}
