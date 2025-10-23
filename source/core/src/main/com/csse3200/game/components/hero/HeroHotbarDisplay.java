package com.csse3200.game.components.hero;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.csse3200.game.entities.configs.HeroSkinAtlas;
import com.csse3200.game.files.UserSettings;
import com.csse3200.game.services.GameStateService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;

/**
 * Hero hotbar in the middle-right of the screen (percentage-based layout).
 * - Uses Value.percentWidth/percentHeight for width/height/margins
 * - Always on the right side, vertically centered
 * - Shows only the currently selected hero's icon button; clicking calls HeroPlacementComponent to place/cancel
 */
public class HeroHotbarDisplay extends UIComponent {
    private Table rootTable;
    private Skin uiSkin;
    private HeroPlacementComponent placement;
    private boolean isVisible = true; // 默认可见

    // Background texture (manually created, must be disposed)
    private Texture bgTexture;

    // Icon textures (must be disposed)
    private Texture engTex;
    private Texture samTex;
    private Texture defTex;
    private ImageButton chosenBtn;
    private Table btnTable;
    private AutoCloseable unsubSkinChanged;
    private AutoCloseable unsubSelectedHeroChanged;


    @Override
    public void create() {
        super.create();
        placement = entity.getComponent(HeroPlacementComponent.class);
        uiSkin = skin;

        // ===== 1) Root table fills the stage for percentage-based positioning =====
        rootTable = new Table();
        rootTable.setFillParent(true);
        stage.addActor(rootTable);

        // ===== 2) Background container (semi-transparent dark) =====
        bgTexture = buildSolidTexture(new Color(0.15f, 0.15f, 0.18f, 0.9f));
        Drawable bgDrawable = new TextureRegionDrawable(new TextureRegion(bgTexture));

        Container<Table> container = new Container<>();
        container.setBackground(bgDrawable);
        // You can use percentage padding if needed; here we use the outer cell's padRight to control distance from the right edge
        // container.pad(Value.percentWidth(0.006f, rootTable));

        // ===== 3) Content: title + icon button (only the selected hero) =====
        Table content = new Table();

        Label title = new Label("HERO", uiSkin, "title");
        title.setAlignment(Align.center);
        content.add(title)
                .center()
                .padBottom(Value.percentHeight(0.02f, rootTable))
                .row();

        // Icon button area (only one button)
        Table btnTable = new Table();
        ImageButton chosenBtn = buildChosenHeroButton();
        // Button size also uses percentages, based on rootTable (stage) or content;
        // here we base it on rootTable for more stable behavior across resolutions:
        btnTable.add(chosenBtn)
                .width(Value.percentWidth(0.08f, rootTable))   // button width = 8% of screen width
                .height(Value.percentHeight(0.12f, rootTable)) // button height = 12% of screen height
                .center();

        ScrollPane sp = new ScrollPane(btnTable, uiSkin);
        sp.setScrollingDisabled(true, true);
        sp.setFadeScrollBars(false);

        content.add(sp).expand().fill();

        container.setActor(content);

        // ===== 4) Place the container at the middle-right (percentage width/height + right margin) =====
        rootTable.add(container)
                .width(Value.percentWidth(0.195f, rootTable))
                .height(Value.percentHeight(0.28f, rootTable))   // panel height = 28% of screen height
                .expand()                                        // occupy available space (enables alignment)
                .align(Align.right)                              // horizontally flush right, vertically centered by default
                .padRight(Value.percentWidth(0f, rootTable));

        // If you want to nudge it up/down vertically, add percentage padTop/padBottom here:
        // .padTop(Value.percentHeight(0.03f, rootTable))
        // .padBottom(Value.percentHeight(0.01f, rootTable))

        applyUiScale(); // Keep your existing UI scale logic
        GameStateService gs = ServiceLocator.getGameStateService();

        // Skin change: if the skin changed for the currently selected hero, refresh the hotbar icon
        unsubSkinChanged = gs.onSkinChanged((who, newSkin) -> {
            if (gs.getSelectedHero() == who) {
                refreshChosenHeroIcon(); // swap only the image, don't rebuild the button
            }
        });

        // Selected-hero change: rebuild the button (icon and click logic together)
        unsubSelectedHeroChanged = gs.onSelectedHeroChanged(nowSelected -> {
            rebuildChosenHeroButton();   // rebuild the button in the UI
        });
    }

    /**
     * Create the icon button for the hero currently selected in GameStateService, and attach its click logic.
     */
    private ImageButton buildChosenHeroButton() {
        GameStateService gs = ServiceLocator.getGameStateService();
        GameStateService.HeroType chosen =
                (gs != null) ? gs.getSelectedHero() : GameStateService.HeroType.HERO;

        String skinKey = (gs != null) ? gs.getSelectedSkin(chosen) : "default";
        String iconPath = HeroSkinAtlas.body(chosen, skinKey);

        // Prefer ResourceService to avoid repeatedly new'ing Textures
        var rs = ServiceLocator.getResourceService();
        Texture tex = rs.getAsset(iconPath, Texture.class);
        if (tex == null) {
            rs.loadTextures(new String[]{iconPath});
            while (!rs.loadForMillis(10)) {}
            tex = rs.getAsset(iconPath, Texture.class);
        }

        ImageButton btn = new ImageButton(new TextureRegionDrawable(new TextureRegion(tex)));

        // Click callback (keep your original logic)
        switch (chosen) {
            case ENGINEER -> addHeroClick(btn, "engineer");
            case SAMURAI  -> addHeroClick(btn, "samurai");
            default       -> addHeroClick(btn, "default");
        }
        return btn;
    }


    /**
     * Click callback: request/cancel placement for the corresponding hero.
     */
    private void addHeroClick(ImageButton btn, String heroType) {
        btn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (placement != null) {
                    // Clicking again on the same type toggles cancel inside HeroPlacementComponent
                    placement.requestPlacement(heroType);
                } else {
                    // Fallback event (if the component isn't attached)
                    entity.getEvents().trigger("heroPlacement:request", heroType);
                }
            }
        });
    }

    /**
     * Apply UI scale from user settings.
     */
    private void applyUiScale() {
        UserSettings.Settings st = UserSettings.get();
        if (rootTable != null) {
            rootTable.setTransform(true);
            rootTable.validate();
            rootTable.setOrigin(0f, 0f);
            rootTable.setScale(st.uiScale);
        }
    }

    /**
     * Utility: create a solid-color texture (used for semi-transparent background).
     */
    private Texture buildSolidTexture(Color color) {
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(color);
        pm.fill();
        Texture tex = new Texture(pm);
        pm.dispose();
        return tex;
    }

    /**
     * Utility: safely load a texture (returns a 1x1 transparent texture on failure to avoid NPE).
     */
    private Texture safeLoad(String path) {
        try {
            return new Texture(Gdx.files.internal(path));
        } catch (Exception e) {
            Gdx.app.error("HeroHotbarDisplay", "Failed to load texture: " + path, e);
            // fallback: transparent pixel
            Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pm.setColor(0, 0, 0, 0);
            pm.fill();
            Texture tex = new Texture(pm);
            pm.dispose();
            return tex;
        }
    }

    private void refreshChosenHeroIcon() {
        if (chosenBtn == null) return;
        GameStateService gs = ServiceLocator.getGameStateService();
        if (gs == null) return;

        GameStateService.HeroType chosen = gs.getSelectedHero();
        String skinKey = gs.getSelectedSkin(chosen);
        String iconPath = HeroSkinAtlas.body(chosen, skinKey);

        Texture tex = safeLoad(iconPath); // or use the ResourceService path
        chosenBtn.getStyle().imageUp = new TextureRegionDrawable(new TextureRegion(tex));
        chosenBtn.invalidateHierarchy();
    }

    private void rebuildChosenHeroButton() {
        if (chosenBtn == null) return;
        Actor parent = chosenBtn.getParent();
        if (!(parent instanceof Table btnTable)) return;

        ImageButton newBtn = buildChosenHeroButton();
        btnTable.clearChildren();
        btnTable.add(newBtn)
                .width(Value.percentWidth(0.08f, rootTable))
                .height(Value.percentHeight(0.12f, rootTable))
                .center();
        btnTable.invalidateHierarchy();
        chosenBtn = newBtn;
    }

    /**
     * 设置英雄UI的可见性
     * @param visible true显示，false隐藏
     */
    public void setVisible(boolean visible) {
        this.isVisible = visible;
        if (rootTable != null) {
            rootTable.setVisible(visible);
        }
    }

    @Override
    public void draw(SpriteBatch batch) {
        // UI is rendered by Stage; nothing extra to draw here
    }

    @Override
    public void dispose() {
        if (rootTable != null) rootTable.clear();
        try { if (unsubSkinChanged != null) unsubSkinChanged.close(); } catch (Exception ignore) {}
        try { if (unsubSelectedHeroChanged != null) unsubSelectedHeroChanged.close(); } catch (Exception ignore) {}

        // Dispose of manually created/loaded textures
        if (bgTexture != null) { bgTexture.dispose(); bgTexture = null; }
        if (engTex != null) { engTex.dispose(); engTex = null; }
        if (samTex != null) { samTex.dispose(); samTex = null; }
        if (defTex != null) { defTex.dispose(); defTex = null; }
        super.dispose();
    }
}
