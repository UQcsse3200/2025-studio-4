package com.csse3200.game.components.maingame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
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

public class PauseMenuDisplay extends UIComponent {
    private static final Logger logger = LoggerFactory.getLogger(PauseMenuDisplay.class);
    private static final float Z_INDEX = 100f;

    private static final String PANEL_TEX = "images/settings_bg.png";
    private static final String BTN_TEX   = "images/settings_bg_button.png";

    private final GdxGame game;
    private Texture dimTexHandle;
    private Table overlayTable;
    private Image dimImage;
    private Image pauseIcon;
    private boolean shown = false;

    public PauseMenuDisplay(GdxGame game) {
        this.game = game;
    }

    @Override
    public void create() {
        super.create();
        ensureAssetsLoaded();
        addActors();
        entity.getEvents().addListener("showPauseUI", this::showOverlay);
        entity.getEvents().addListener("hidePauseUI", this::hideOverlay);
    }

    private void ensureAssetsLoaded() {
        ResourceService rs = ServiceLocator.getResourceService();
        rs.loadTextures(new String[] { PANEL_TEX, BTN_TEX });
        rs.loadAll();
    }

    private void addActors() {
        // Dim layer
        Pixmap px = new Pixmap(1, 1, Format.RGBA8888);
        px.setColor(new Color(0f, 0f, 0f, 0.55f));
        px.fill();
        dimTexHandle = new Texture(px);
        px.dispose();

        dimImage = new Image(dimTexHandle);
        dimImage.setFillParent(true);
        dimImage.setVisible(false);
        stage.addActor(dimImage);

        // Settings-style panel
        Texture panelTex = ServiceLocator.getResourceService().getAsset(PANEL_TEX, Texture.class);
        NinePatch panelPatch = new NinePatch(new TextureRegion(panelTex), 20, 20, 20, 20);
        overlayTable = new Table(skin);
        overlayTable.setFillParent(true);
        overlayTable.setVisible(false);

        Table window = new Table(skin);
        window.defaults().pad(10f);
        window.setBackground(new NinePatchDrawable(panelPatch));

        Label title = new Label("Paused", skin, "title");
        title.setColor(Color.valueOf("CFF2FF"));

        // Player info
        Table playerInfo = createPlayerInfoSection();

        // Buttons — use EXACT same style as Settings
        TextButtonStyle btnStyle = createSettingsButtonStyle();
        TextButton resume   = new TextButton("Resume", btnStyle);
        TextButton save     = new TextButton("Save", btnStyle);
        TextButton settings = new TextButton("Settings", btnStyle);
        TextButton ranking  = new TextButton("Ranking", btnStyle);
        TextButton quit     = new TextButton("Quit to Main Menu", btnStyle);

        resume.addListener(new ChangeListener() { @Override public void changed(ChangeEvent e, Actor a) { entity.getEvents().trigger("resume"); }});
        save.addListener(new ChangeListener()   { @Override public void changed(ChangeEvent e, Actor a) { entity.getEvents().trigger("hidePauseUI"); entity.getEvents().trigger("save"); }});
        settings.addListener(new ChangeListener(){ @Override public void changed(ChangeEvent e, Actor a) { entity.getEvents().trigger("hidePauseUI"); entity.getEvents().trigger("showSettingsOverlay"); }});
        ranking.addListener(new ChangeListener() { @Override public void changed(ChangeEvent e, Actor a) { entity.getEvents().trigger("hidePauseUI"); entity.getEvents().trigger("showRanking"); }});
        quit.addListener(new ChangeListener()    { @Override public void changed(ChangeEvent e, Actor a) { entity.getEvents().trigger("quitToMenu"); }});

        window.add(title).padBottom(12f).row();
        window.add(playerInfo).padBottom(15f).row();
        window.add(resume).size(280f, 50f).row();
        window.add(save).size(280f, 50f).row();
        window.add(settings).size(280f, 50f).row();
        window.add(ranking).size(280f, 50f).row();
        window.add(quit).size(280f, 50f).padTop(10f).row();

        overlayTable.add(window).center()
                .size(Math.min(Gdx.graphics.getWidth() * 0.45f, 600f),
                        Math.min(Gdx.graphics.getHeight() * 0.75f, 650f));
        stage.addActor(overlayTable);

        // Pause icon (unchanged)
        Texture pauseTex = ServiceLocator.getResourceService().getAsset("images/pause_button.png", Texture.class);
        pauseIcon = new Image(pauseTex);
        Table topRight = new Table();
        topRight.setFillParent(true);
        topRight.top().right();
        topRight.add(pauseIcon).size(48f, 48f).padTop(12f).padRight(150f);
        stage.addActor(topRight);

        pauseIcon.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                logger.debug("Pause icon clicked");
                entity.getEvents().trigger("togglePause");
            }
        });
    }

    private TextButtonStyle createSettingsButtonStyle() {
        TextButtonStyle style = new TextButtonStyle();
        style.font = skin.getFont("segoe_ui");

        Texture tex = ServiceLocator.getResourceService().getAsset(BTN_TEX, Texture.class);
        TextureRegion tr = new TextureRegion(tex);
        TextureRegionDrawable up = new TextureRegionDrawable(tr);
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

    private void showOverlay() { shown = true; dimImage.setVisible(true); overlayTable.setVisible(true); }
    private void hideOverlay() { shown = false; dimImage.setVisible(false); overlayTable.setVisible(false); }

    @Override protected void draw(SpriteBatch batch) {}

    @Override public float getZIndex() { return Z_INDEX; }

    private Table createPlayerInfoSection() {
        Table t = new Table();
        PlayerNameService nameService = ServiceLocator.getPlayerNameService();
        PlayerAvatarService avatarService = ServiceLocator.getPlayerAvatarService();

        String name = nameService != null ? nameService.getPlayerName() : "Player";
        String avatarId = avatarService != null ? avatarService.getPlayerAvatar() : "avatar_1";

        Image avatar = createAvatarImage(avatarId, avatarService);
        Label nameLabel = new Label(name, skin);
        nameLabel.setColor(Color.CYAN);

        if (avatar != null) t.add(avatar).size(60, 60).padRight(15f);
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
            logger.warn("Failed to load avatar {}", avatarId);
            return null;
        }
    }

    @Override
    public void dispose() {
        if (overlayTable != null) overlayTable.clear();
        if (dimTexHandle != null) dimTexHandle.dispose();
        super.dispose();
    }
}
