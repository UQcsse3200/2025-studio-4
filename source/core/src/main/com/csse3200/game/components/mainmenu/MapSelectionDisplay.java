package com.csse3200.game.components.mainmenu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Scaling;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/** Map selection UI shown after pressing "New Game". */
public class MapSelectionDisplay extends UIComponent {
    private static final String BG_TEX = "images/main_menu_background.png";
    private static final String THUMB_TEX = "images/Main_Game_Button.png"; // default fallback
    
    // Resolved thumbnail textures for the two built-in maps
    private String thumbTexMap1; // prefers images/mmap1.png, falls back to images/mmap.png or THUMB_TEX
    private String thumbTexMap2; // prefers images/mmap2.png, falls back to THUMB_TEX

    private Table root;

    // Carousel state
    private final List<MapEntry> entries = new ArrayList<>();
    private int currentIndex = 0;

    // UI needing refresh
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

    private void ensureThumbLoaded() {
        ResourceService rs = ServiceLocator.getResourceService();
        
        // Resolve thumbnails with graceful fallbacks
        String mmap1 = "images/mmap1.png";
        String mmapFallback = "images/mmap.png"; // present in assets
        String mmap2 = "images/mmap2.png";
        
        boolean hasMmap1 = Gdx.files.internal(mmap1).exists();
        boolean hasMmapFallback = Gdx.files.internal(mmapFallback).exists();
        boolean hasMmap2 = Gdx.files.internal(mmap2).exists();
        
        thumbTexMap1 = hasMmap1 ? mmap1 : (hasMmapFallback ? mmapFallback : THUMB_TEX);
        thumbTexMap2 = hasMmap2 ? mmap2 : THUMB_TEX;
        
        rs.loadTextures(new String[]{THUMB_TEX, thumbTexMap1, thumbTexMap2});
        rs.loadAll();
    }

    private void buildEntries() {
        // Add built-in maps first
        addBuiltInMaps();
        
//        // Then add TMX files from maps directory
//        List<FileHandle> maps = findMaps();
//        for (FileHandle fh : maps) {
//            MapEntry e = new MapEntry();
//            e.mapId = fh.nameWithoutExtension();
//            e.displayName = e.mapId;
//            e.thumbTex = THUMB_TEX;                   // reuse placeholder thumbnail
//            entries.add(e);
//        }
        
        // If no maps at all, add default
        if (entries.isEmpty()) {
            MapEntry placeholder = new MapEntry();
            placeholder.mapId = null;                 // will pass null -> game uses default
            placeholder.displayName = "Default";
            placeholder.thumbTex = THUMB_TEX;
            entries.add(placeholder);
        }
    }

    /**
     * Add built-in maps that don't rely on TMX files.
     */
    private void addBuiltInMaps() {
        // Default Forest map
        MapEntry defaultMap = new MapEntry();
        defaultMap.mapId = null;                     // null means default ForestGameArea
        defaultMap.displayName = "Icebox";
        defaultMap.thumbTex = thumbTexMap1;
        entries.add(defaultMap);
        
        // Ascent
        MapEntry ascent = new MapEntry();
        ascent.mapId = "Ascent";                     // This will trigger ForestGameArea2
        ascent.displayName = "Ascent";
        ascent.thumbTex = thumbTexMap2;
        entries.add(ascent);
    }

    private void addActors() {
        // Background (same as other menus)
        Texture bg = ServiceLocator.getResourceService().getAsset(BG_TEX, Texture.class);
        Image bgImage = new Image(bg);
        bgImage.setFillParent(true);
        stage.addActor(bgImage);

        // Title
        Label title = new Label("Select Map", skin, "title");

        // Thumbnail + labels (no dialog/window background)
        thumbImage = new Image(ServiceLocator.getResourceService().getAsset(THUMB_TEX, Texture.class));
        thumbImage.setScaling(Scaling.fit);

        mapNameLabel = new Label("", skin);
        counterLabel = new Label("", skin);

        Table card = new Table();
        card.defaults().pad(6f);
        card.add(thumbImage).width(420f).height(420f).row();
        card.add(mapNameLabel).padTop(8f).row();
        card.add(counterLabel).padTop(2f);

        // Left/Right arrows
        TextButton leftArrow = new TextButton("<", skin);
        TextButton rightArrow = new TextButton(">", skin);
        leftArrow.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                move(-1);
            }
        });
        rightArrow.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                move(+1);
            }
        });

        // Play + Back (to corners)
        TextButton playBtn = new TextButton("Play", skin);
        playBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                MapEntry e = entries.get(currentIndex);
                entity.getEvents().trigger("mapSelected", e.mapId); // may be null (default)
            }
        });

        TextButton backBtn = new TextButton("Back", skin);
        backBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                entity.getEvents().trigger("backToMainMenu");
            }
        });

        // Root layout (title + carousel only)
        root = new Table();
        root.setFillParent(true);
        root.top();

        root.add(title).expandX().top().padTop(24f).row();

        Table carouselRow = new Table();
        carouselRow.add(leftArrow).padRight(24f);
        carouselRow.add(card).pad(4f);
        carouselRow.add(rightArrow).padLeft(24f);
        root.add(carouselRow).expand().center();

        stage.addActor(root);

        // Bottom corners overlay
        Table corners = new Table();
        corners.setFillParent(true);
        corners.bottom();

        corners.add(backBtn).left().pad(20f).expandX();
        corners.add(playBtn).right().pad(20f);

        stage.addActor(corners);

        // Disable arrows if only 1 entry (visible but inert)
        boolean single = entries.size() <= 1;
        leftArrow.setDisabled(single);
        rightArrow.setDisabled(single);
    }

    private void move(int delta) {
        if (entries.isEmpty()) return;
        int n = entries.size();
        currentIndex = (currentIndex + delta) % n;
        if (currentIndex < 0) currentIndex += n;
        refreshCard();
    }

    private void refreshCard() {
        if (entries.isEmpty()) return;
        MapEntry e = entries.get(currentIndex);

        // Update thumbnail
        Texture tex = ServiceLocator.getResourceService().getAsset(e.thumbTex, Texture.class);
        thumbImage.setDrawable(new Image(tex).getDrawable());

        // Update labels with requested format: "Map N (Name)"
        String name = (e.displayName != null && !e.displayName.isEmpty()) ? e.displayName : "Default";
        mapNameLabel.setText("Map " + (currentIndex + 1) + " (" + name + ")");
        counterLabel.setText((currentIndex + 1) + " / " + entries.size());
    }

    /**
     * Find .tmx maps under assets/maps/ (internal).
     */
//    private List<FileHandle> findMaps() {
//        List<FileHandle> out = new ArrayList<>();
//        FileHandle dir = Gdx.files.internal("maps");
//        if (dir.exists() && dir.isDirectory()) {
//            Arrays.stream(dir.list())
//                    .filter(f -> f.extension().equalsIgnoreCase("tmx"))
//                    .sorted(Comparator.comparing(FileHandle::name))
//                    .forEach(out::add);
//        }
//        return out;
//    }

    @Override
    protected void draw(SpriteBatch batch) { /* stage draws */ }

    @Override
    public void dispose() {
        if (root != null) root.clear();
        super.dispose();
    }

    private static class MapEntry {
        String mapId;        // null for default
        String displayName;  // shown under thumbnail
        String thumbTex;     // texture path, final texture
    }
}
