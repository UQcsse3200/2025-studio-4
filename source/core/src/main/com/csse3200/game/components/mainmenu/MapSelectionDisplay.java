package com.csse3200.game.components.mainmenu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.csse3200.game.files.UserSettings;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import com.csse3200.game.ui.SettingsStyleHelper;
import com.csse3200.game.utils.Difficulty;

import java.util.ArrayList;
import java.util.List;

/** Map Selection styled to match the Settings screen (same panel + buttons). */
public class MapSelectionDisplay extends UIComponent {
    private static final String BG_TEX = "images/main_menu_notext.png";
    private static final String THUMB_TEX_FALLBACK = "images/Main_Game_Button.png";
    private String thumbTexMap1, thumbTexMap2;

    private Table root, bottomBar;
    private final List<MapEntry> entries = new ArrayList<>();
    private int currentIndex = 0;
    private Image thumbImage;
    private Label mapNameLabel;
    private Label counterLabel;

    @Override
    public void create() {
        super.create();
        preloadAssets();
        buildEntries();
        addActors();
        refreshCard();
    }

    /** Make sure all textures we use are loaded (avoids runtime crashes). */
    private void preloadAssets() {
        ResourceService rs = ServiceLocator.getResourceService();

        // choose thumbnails that exist
        String mmap1 = "images/mmap1.png";
        String mmap2 = "images/mmap2.png";
        String mmapFallback = "images/mmap.png";

        thumbTexMap1 = Gdx.files.internal(mmap1).exists()
                ? mmap1
                : (Gdx.files.internal(mmapFallback).exists() ? mmapFallback : THUMB_TEX_FALLBACK);
        thumbTexMap2 = Gdx.files.internal(mmap2).exists() ? mmap2 : THUMB_TEX_FALLBACK;

        rs.loadTextures(new String[] {
                BG_TEX,
                "images/settings_bg.png",
                "images/settings_bg_button.png",
                thumbTexMap1, thumbTexMap2, THUMB_TEX_FALLBACK
        });
        rs.loadAll();
    }

    private void buildEntries() {
        entries.add(new MapEntry("Icebox", thumbTexMap1, null));     // default map
        entries.add(new MapEntry("Ascent",  thumbTexMap2, "MapTwo"));
        if (entries.isEmpty()) entries.add(new MapEntry("Default", THUMB_TEX_FALLBACK, null));
    }

    private void addActors() {
        // background image (same as settings screen uses on non-overlay screens)
        Image bg = new Image(ServiceLocator.getResourceService().getAsset(BG_TEX, Texture.class));
        bg.setFillParent(true);
        stage.addActor(bg);

        // Title
        Label title = new Label("Select Map", skin);
        title.setAlignment(Align.center);
        title.setColor(Color.valueOf("CFF2FF"));
        title.setFontScale(1.8f);

        // Main thumbnail card
        thumbImage = new Image(ServiceLocator.getResourceService().getAsset(THUMB_TEX_FALLBACK, Texture.class));
        thumbImage.setScaling(Scaling.fit);

        mapNameLabel = new Label("", skin);
        mapNameLabel.setAlignment(Align.center);
        mapNameLabel.setColor(Color.valueOf("AEE7F2"));

        counterLabel = new Label("", skin);
        counterLabel.setAlignment(Align.center);
        counterLabel.setColor(Color.LIGHT_GRAY);

        Label diffLabel = new Label("Difficulty:", skin);
        final SelectBox<String> diffSelect = new SelectBox<>(skin);
        diffSelect.setItems("Easy", "Normal", "Hard");

        Table diffRow = new Table();
        diffRow.add(diffLabel).padRight(10f);
        diffRow.add(diffSelect).width(160f);

        Table card = new Table();
        card.defaults().pad(8f);
        card.add(thumbImage).width(420).height(420).row();
        card.add(mapNameLabel).padTop(10).row();
        card.add(counterLabel).padTop(4).row();
        card.add(diffRow).padTop(10).row();

        // Settings-like panel frame around the central content
        Table panel = new Table(skin);
        panel.setBackground(SettingsStyleHelper.createSettingsPanelDrawable());
        panel.defaults().pad(12f);
        panel.add(card).center();

        // Buttons: same style as Settings
        TextButton.TextButtonStyle orange = SettingsStyleHelper.createSettingsButtonStyle(skin);
        TextButton leftArrow  = new TextButton("<", orange);
        TextButton rightArrow = new TextButton(">", orange);
        TextButton backBtn    = new TextButton("Back", orange);
        TextButton heroBtn    = new TextButton("Select Hero", orange);
        TextButton playBtn    = new TextButton("Play", orange);

        leftArrow.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) { move(-1); }
        });
        rightArrow.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) { move(+1); }
        });

        backBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                entity.getEvents().trigger("backToMainMenu");
            }
        });
        heroBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                entity.getEvents().trigger("toUpgradeMenu");
            }
        });
        playBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                MapEntry e = entries.get(currentIndex);
                Difficulty diff = switch (diffSelect.getSelected()) {
                    case "Hard" -> Difficulty.HARD;
                    case "Normal" -> Difficulty.MEDIUM;
                    default -> Difficulty.EASY;
                };
                entity.getEvents().trigger("mapSelected", e.mapId, diff);
            }
        });

        // Center row (arrows + panel)
        Table centerRow = new Table();
        centerRow.add(leftArrow).width(64).height(48).padRight(20);
        centerRow.add(panel).center()
                .size(Math.min(Gdx.graphics.getWidth() * 0.45f, 600f),
                        Math.min(Gdx.graphics.getHeight() * 0.65f, 520f));
        centerRow.add(rightArrow).width(64).height(48).padLeft(20);

        root = new Table();
        root.setFillParent(true);
        root.top().center();
        root.add(title).expandX().padTop(28f).row();
        root.add(centerRow).expand().center().padTop(14f).row();
        stage.addActor(root);

        bottomBar = new Table();
        bottomBar.setFillParent(true);
        bottomBar.bottom().padBottom(28f);
        bottomBar.add(backBtn).width(170).height(56).padRight(60f);
        bottomBar.add(heroBtn).width(170).height(56);
        bottomBar.add(playBtn).width(170).height(56).padLeft(60f);
        stage.addActor(bottomBar);

        boolean single = entries.size() <= 1;
        leftArrow.setDisabled(single);
        rightArrow.setDisabled(single);

        applyUiScale();
    }

    private void applyUiScale() {
        UserSettings.Settings s = UserSettings.get();
        if (root != null) {
            root.setTransform(true);
            root.validate();
            root.setOrigin(root.getWidth() / 2f, root.getHeight() / 2f);
            root.setScale(s.uiScale);
        }
        if (bottomBar != null) {
            bottomBar.setTransform(true);
            bottomBar.validate();
            bottomBar.setOrigin(bottomBar.getWidth() / 2f, 0f);
            bottomBar.setScale(s.uiScale);
        }
    }

    private void move(int delta) {
        if (entries.isEmpty()) return;
        int n = entries.size();
        currentIndex = (currentIndex + delta + n) % n;
        refreshCard();
    }

    private void refreshCard() {
        if (entries.isEmpty()) return;
        MapEntry e = entries.get(currentIndex);
        Texture tex = ServiceLocator.getResourceService().getAsset(e.thumbTex, Texture.class);
        thumbImage.setDrawable(new Image(tex).getDrawable());
        mapNameLabel.setText("Map " + (currentIndex + 1) + " (" + e.displayName + ")");
        counterLabel.setText((currentIndex + 1) + " / " + entries.size());
    }

    @Override protected void draw(SpriteBatch batch) {}
    @Override public void dispose() { if (root != null) root.clear(); super.dispose(); }

    private static class MapEntry {
        String displayName, thumbTex, mapId;
        MapEntry(String name, String tex, String id) { displayName = name; thumbTex = tex; mapId = id; }
    }
}
