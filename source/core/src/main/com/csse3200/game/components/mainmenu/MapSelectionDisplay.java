package com.csse3200.game.components.mainmenu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.csse3200.game.files.UserSettings;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import com.csse3200.game.utils.Difficulty;

import java.util.ArrayList;
import java.util.List;

/**
 * Map Selection aligned to the Settings panel look & feel.
 */
public class MapSelectionDisplay extends UIComponent {
    private static final String BG_TEX    = "images/main_menu_notext.png";
    private static final String PANEL_TEX = "images/settings_bg.png";
    private static final String BTN_TEX   = "images/settings_bg_button.png";

    private static final String FALLBACK_THUMB = "images/Main_Game_Button.png";
    private String thumbTexMap1, thumbTexMap2;

    private Table root;

    private final List<MapEntry> entries = new ArrayList<>();
    private int currentIndex = 0;

    private Image thumbImage;
    private Label mapNameLabel;
    private Label counterLabel;

    @Override
    public void create() {
        super.create();
        ensureAssetsLoaded();
        buildEntries();
        addActors();
        refreshCard();
    }

    private void ensureAssetsLoaded() {
        ResourceService rs = ServiceLocator.getResourceService();
        String mmap1 = "images/mmap1.png";
        String mmap2 = "images/mmap2.png";
        String fallback = "images/mmap.png";

        thumbTexMap1 = Gdx.files.internal(mmap1).exists() ? mmap1 :
                (Gdx.files.internal(fallback).exists() ? fallback : FALLBACK_THUMB);
        thumbTexMap2 = Gdx.files.internal(mmap2).exists() ? mmap2 : FALLBACK_THUMB;

        rs.loadTextures(new String[]{ BG_TEX, PANEL_TEX, BTN_TEX, thumbTexMap1, thumbTexMap2, FALLBACK_THUMB });
        rs.loadAll();
    }

    private void buildEntries() {
        entries.add(new MapEntry("Icebox", thumbTexMap1, null));
        entries.add(new MapEntry("Ascent", thumbTexMap2, "MapTwo"));
        if (entries.isEmpty()) entries.add(new MapEntry("Default", FALLBACK_THUMB, null));
    }

    private void addActors() {
        // Background
        Image bg = new Image(ServiceLocator.getResourceService().getAsset(BG_TEX, Texture.class));
        bg.setFillParent(true);
        stage.addActor(bg);

        // Title
        Label title = new Label("Select Map", skin, "title");
        title.setColor(Color.valueOf("CFF2FF"));
        title.setAlignment(Align.center);

        // Preview content
        thumbImage = new Image(ServiceLocator.getResourceService().getAsset(FALLBACK_THUMB, Texture.class));
        thumbImage.setScaling(Scaling.fit);

        mapNameLabel = new Label("", skin);
        mapNameLabel.setAlignment(Align.center);
        mapNameLabel.setColor(Color.valueOf("AEE7F2"));

        counterLabel = new Label("", skin);
        counterLabel.setAlignment(Align.center);
        counterLabel.setColor(Color.LIGHT_GRAY);

        // Difficulty row
        Label diffLabel = new Label("Difficulty:", skin);
        diffLabel.setColor(Color.valueOf("F9B44C")); // warm gold-orange like the buttons
        diffLabel.setAlignment(Align.right);
        final SelectBox<String> diffSelect = new SelectBox<>(skin);
        diffSelect.setItems("Easy", "Normal", "Hard");
        Table diffRow = new Table();
        diffRow.add(diffLabel).right().padRight(10f);
        diffRow.add(diffSelect).width(170f).left();

        // Card (image + labels) – give vertical breathing room
        Table card = new Table();
        card.defaults().pad(6f);
        card.add(thumbImage).width(420f).height(300f).padBottom(8f).row();
        card.add(mapNameLabel).padTop(4f).row();
        card.add(counterLabel).padTop(2f).row();

        // Button style
        TextButtonStyle buttonStyle = createSettingsButtonStyle();

        // Carousel – arrows vertically centered with thumbnail
        TextButton leftArrow  = new TextButton("<", buttonStyle);
        TextButton rightArrow = new TextButton(">", buttonStyle);
        leftArrow.addListener(new ChangeListener(){ @Override public void changed(ChangeEvent e, Actor a){ move(-1); }});
        rightArrow.addListener(new ChangeListener(){ @Override public void changed(ChangeEvent e, Actor a){ move(+1); }});

        Table carousel = new Table();
        carousel.defaults().pad(0f);
        carousel.add(leftArrow).width(56f).height(44f).padRight(16f).center();
        carousel.add(card).expandX().center();
        carousel.add(rightArrow).width(56f).height(44f).padLeft(16f).center();

        // Action buttons INSIDE panel bottom (like Settings)
        TextButton back = new TextButton("Back", buttonStyle);
        TextButton hero = new TextButton("Select Hero", buttonStyle);
        TextButton play = new TextButton("Play", buttonStyle);
        back.addListener(new ChangeListener(){ @Override public void changed(ChangeEvent e, Actor a){ entity.getEvents().trigger("backToMainMenu"); }});
        hero.addListener(new ChangeListener(){ @Override public void changed(ChangeEvent e, Actor a){ entity.getEvents().trigger("toUpgradeMenu"); }});
        play.addListener(new ChangeListener(){ @Override public void changed(ChangeEvent e, Actor a){
            MapEntry entry = entries.get(currentIndex);
            String sel = diffSelect.getSelected();
            Difficulty diff = "Hard".equals(sel) ? Difficulty.HARD : "Normal".equals(sel) ? Difficulty.MEDIUM : Difficulty.EASY;
            entity.getEvents().trigger("mapSelected", entry.mapId, diff);
        }});

        Table bottomBtns = new Table();
        bottomBtns.defaults().width(170f).height(52f).pad(0f, 10f, 0f, 10f);
        bottomBtns.add(back);
        bottomBtns.add(hero);
        bottomBtns.add(play);

        // Settings-style panel wrapper (same look; slightly wider for the image)
        Texture panelTex = ServiceLocator.getResourceService().getAsset(PANEL_TEX, Texture.class);
        NinePatch panelPatch = new NinePatch(new TextureRegion(panelTex), 20, 20, 20, 20);
        final float PANEL_W = 640f;  // slight bump so content breathes
        final float PANEL_H = 680f;

        Table panel = new Table(skin);
        panel.setBackground(new NinePatchDrawable(panelPatch));
        panel.pad(20f, 24f, 20f, 24f);
        panel.defaults().pad(8f);

        panel.add(title).expandX().padTop(2f).padBottom(14f).row();
        panel.add(carousel).growX().padBottom(8f).row();
        panel.add(diffRow).padTop(6f).row();
        panel.add(bottomBtns).padTop(14f).row();

        root = new Table();
        root.setFillParent(true);
        root.add(panel).size(PANEL_W, PANEL_H).center();
        stage.addActor(root);
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

    @Override protected void draw(SpriteBatch batch) { }

    @Override
    public void dispose() {
        if (root != null) root.clear();
        super.dispose();
    }

    private static class MapEntry {
        String displayName, thumbTex, mapId;
        MapEntry(String displayName, String thumbTex, String mapId) {
            this.displayName = displayName; this.thumbTex = thumbTex; this.mapId = mapId;
        }
    }
}
