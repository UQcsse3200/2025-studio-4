package com.csse3200.game.components.mainmenu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
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
import com.csse3200.game.ui.UIStyleHelper;
import com.csse3200.game.utils.Difficulty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Map Selection UI using the universal orange button style and a neat centered layout.
 */
public class MapSelectionDisplay extends UIComponent {
    private static final String BG_TEX = "images/main_menu_notext.png";
    private static final String THUMB_TEX = "images/Main_Game_Button.png"; // fallback
    private String thumbTexMap1;
    private String thumbTexMap2;
    private Table root;
    private Table bottomBar;

    private final List<MapEntry> entries = new ArrayList<>();
    private int currentIndex = 0;

    private Image thumbImage;
    private Label mapNameLabel;
    private Label counterLabel;

    @Override
    public void create() {
        super.create();
        ensureThumbLoaded();
        buildEntries();
        addActors();
        refreshCard();
    }

    /* ------------------------------------------------------ */
    /*   Asset resolution                                     */
    /* ------------------------------------------------------ */

    private void ensureThumbLoaded() {
        ResourceService rs = ServiceLocator.getResourceService();

        String mmap1 = "images/mmap1.png";
        String mmapFallback = "images/mmap.png"; // present in assets
        String mmap2 = "images/mmap2.png";

        boolean hasMmap1 = Gdx.files.internal(mmap1).exists();
        boolean hasMmapFallback = Gdx.files.internal(mmapFallback).exists();
        boolean hasMmap2 = Gdx.files.internal(mmap2).exists();

        thumbTexMap1 = hasMmap1 ? mmap1 : (hasMmapFallback ? mmapFallback : THUMB_TEX);
        thumbTexMap2 = hasMmap2 ? mmap2 : THUMB_TEX;

        rs.loadTextures(new String[] { THUMB_TEX, thumbTexMap1, thumbTexMap2, BG_TEX });
        rs.loadAll();
    }

    private void buildEntries() {
        MapEntry defaultMap = new MapEntry();
        defaultMap.mapId = null;            // default game area
        defaultMap.displayName = "Icebox";
        defaultMap.thumbTex = thumbTexMap1;
        entries.add(defaultMap);

        MapEntry mapTwo = new MapEntry();
        mapTwo.mapId = "MapTwo";
        mapTwo.displayName = "Ascent";
        mapTwo.thumbTex = thumbTexMap2;
        entries.add(mapTwo);

        if (entries.isEmpty()) {
            MapEntry placeholder = new MapEntry();
            placeholder.mapId = null;
            placeholder.displayName = "Default";
            placeholder.thumbTex = THUMB_TEX;
            entries.add(placeholder);
        }
    }

    /* ------------------------------------------------------ */
    /*   UI                                                    */
    /* ------------------------------------------------------ */

    private void addActors() {
        // Background
        Image bg = new Image(ServiceLocator.getResourceService().getAsset(BG_TEX, Texture.class));
        bg.setFillParent(true);
        stage.addActor(bg);

        // Title
        Label title = new Label("Select Map", skin);
        title.setFontScale(1.8f);
        title.setColor(new Color(1f, 0.9f, 0.6f, 1f));
        title.getStyle().fontColor = Color.valueOf("CFF2FF");
        title.setAlignment(Align.center);

        // Thumbnail card
        thumbImage = new Image(ServiceLocator.getResourceService().getAsset(THUMB_TEX, Texture.class));
        thumbImage.setScaling(Scaling.fit);

        mapNameLabel = new Label("", skin);
        mapNameLabel.setAlignment(Align.center);

        counterLabel = new Label("", skin);
        counterLabel.setAlignment(Align.center);

        // Difficulty selector

        Label difficultyLabel = new Label("Difficulty:", skin);
        difficultyLabel.getStyle().fontColor = Color.valueOf("B0E0E6");
        final SelectBox<String> difficultySelect = new SelectBox<>(skin);
        difficultySelect.setItems("Easy", "Normal", "Hard");

        Table difficultyRow = new Table();
        difficultyRow.add(difficultyLabel).right().padRight(10f);
        difficultyRow.add(difficultySelect).width(160f).left();

        // Assemble the center "card" (image + labels + difficulty)
        Table card = new Table();
        card.defaults().pad(6f);
        card.add(thumbImage).width(420f).height(420f).row();
        card.add(mapNameLabel).padTop(8f).row();
        card.add(counterLabel).padTop(2f).row();
        card.add(difficultyRow).padTop(10f).row();

        // Orange button style for all menu buttons
        TextButton.TextButtonStyle orange = UIStyleHelper.orangeButtonStyle();

        // Left/right arrows — small fixed-size buttons so they never stretch
        TextButton leftArrow = new TextButton("<", orange);
        TextButton rightArrow = new TextButton(">", orange);
        leftArrow.getLabel().setAlignment(Align.center);
        rightArrow.getLabel().setAlignment(Align.center);

        // Force compact arrow size
        final float ARROW_W = 64f, ARROW_H = 48f;

        leftArrow.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) { move(-1); }
        });
        rightArrow.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) { move(+1); }
        });

        // Place arrows close to the image using a 3-column row
        Table carouselRow = new Table();
        carouselRow.add(leftArrow).width(ARROW_W).height(ARROW_H).padRight(20f).bottom();
        carouselRow.add(card).center().pad(4f);
        carouselRow.add(rightArrow).width(ARROW_W).height(ARROW_H).padLeft(20f).bottom();

        // Bottom menu bar with Back / Unlocks / Play (orange)
        TextButton backBtn = new TextButton("Back", orange);
        TextButton unlocksBtn = new TextButton("Select hero", orange);
        TextButton playBtn = new TextButton("Play", orange);

        backBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                entity.getEvents().trigger("backToMainMenu");
            }
        });

        unlocksBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                entity.getEvents().trigger("toUpgradeMenu");
            }
        });

        playBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                MapEntry e = entries.get(currentIndex);
                Difficulty diff;
                String sel = difficultySelect.getSelected();
                if ("Hard".equals(sel)) diff = Difficulty.HARD;
                else if ("Normal".equals(sel)) diff = Difficulty.MEDIUM;
                else diff = Difficulty.EASY;
                entity.getEvents().trigger("mapSelected", e.mapId, diff);
            }
        });

        // Build the main root (title + carousel)
        root = new Table();
        root.setFillParent(true);
        root.top().center();
        root.add(title).expandX().padTop(28f).row();
        root.add(carouselRow).expand().center().padTop(14f).row();

        stage.addActor(root);

        // Separate bottom bar anchored to the bottom (prevents crowding)
        bottomBar = new Table();
        bottomBar.setFillParent(true);
        bottomBar.bottom().padBottom(28f);
        bottomBar.add(backBtn).width(170f).height(56f).padRight(60f).left().expandX();
        bottomBar.add(unlocksBtn).width(170f).height(56f).center();
        bottomBar.add(playBtn).width(170f).height(56f).padLeft(60f).right().expandX();
        stage.addActor(bottomBar);

        // Disable arrows if only one entry
        boolean single = entries.size() <= 1;
        leftArrow.setDisabled(single);
        rightArrow.setDisabled(single);

        applyUiScale();
    }

    private void applyUiScale() {
        UserSettings.Settings settings = UserSettings.get();
        if (root != null) {
            root.setTransform(true);
            root.validate();
            root.setOrigin(root.getWidth() / 2f, root.getHeight() / 2f);
            root.setScale(settings.uiScale);
        }
        if (bottomBar != null) {
            bottomBar.setTransform(true);
            bottomBar.validate();
            bottomBar.setOrigin(bottomBar.getWidth() / 2f, 0f);  // Bottom-center origin
            bottomBar.setScale(settings.uiScale);
        }
    }

    /* ------------------------------------------------------ */
    /*   Carousel helpers                                     */
    /* ------------------------------------------------------ */

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

    /* ------------------------------------------------------ */
    /*   Misc                                                 */
    /* ------------------------------------------------------ */

    @Override
    protected void draw(SpriteBatch batch) { /* stage draws */ }

    @Override
    public void dispose() {
        if (root != null) root.clear();
        super.dispose();
    }

    private static class MapEntry {
        String mapId;
        String displayName;
        String thumbTex;
    }
}
