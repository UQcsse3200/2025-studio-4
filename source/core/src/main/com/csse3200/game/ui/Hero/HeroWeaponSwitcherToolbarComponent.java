package com.csse3200.game.ui.Hero;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;

import java.util.ArrayList;
import java.util.List;

public class HeroWeaponSwitcherToolbarComponent extends Component {
    private final Entity hero;
    private Stage stage;
    private Table root;
    private Cell<Table> barCell;

    private static final float HOTBAR_HEIGHT_PCT = 0.28f;
    private static final float HOTBAR_BOTTOM_PCT = 0.5f + HOTBAR_HEIGHT_PCT * 0.5f;
    private static final float GAP_BELOW_HOTBAR_PCT = 0.00f;
    private static final float PANEL_WIDTH_PCT = 0.195f;
    private static final float BAR_HEIGHT_PCT = 0.06f;
    private static final float RIGHT_MARGIN_PCT = 0.0f;

    private final List<Texture> toDispose = new ArrayList<>();
    private Table bar; // Save toolbar container for dynamic modifications
    private ImageButton btnForm1, btnForm2, btnForm3;
    private boolean locked = false;

    // Initial L1 three icons
    private final String icon1, icon2, icon3;
    // L2 has only one image, used for the collapsed single button
    private final String l2SingleIcon;

    // Collapse flag: after upgrade, switch from three buttons -> single button
    private boolean collapsedToSingle = false;

    public HeroWeaponSwitcherToolbarComponent(Entity hero, String icon1, String icon2, String icon3, String l2SingleIcon) {
        this.hero = hero;
        this.icon1 = icon1;
        this.icon2 = icon2;
        this.icon3 = icon3;
        this.l2SingleIcon = l2SingleIcon;
    }

    @Override
    public void create() {
        stage = ServiceLocator.getRenderService().getStage();

        root = new Table();
        root.setFillParent(true);
        root.top().right();
        root.padTop(Value.percentHeight(HOTBAR_BOTTOM_PCT + GAP_BELOW_HOTBAR_PCT, root))
                .padRight(Value.percentWidth(RIGHT_MARGIN_PCT, root));

        bar = new Table();
        bar.setBackground(new TextureRegionDrawable(makeSolid(2, 2, new Color(0.15f, 0.15f, 0.18f, 0.85f))));
        bar.pad(Value.percentHeight(0.006f, root));
        bar.defaults().padRight(Value.percentWidth(0.008f, root));

        btnForm1 = makeIconButton(icon1);
        btnForm2 = makeIconButton(icon2);
        btnForm3 = makeIconButton(icon3);

        btnForm1.addListener(SimpleClick.on(() -> requestSwitch(1)));
        btnForm2.addListener(SimpleClick.on(() -> requestSwitch(2)));
        btnForm3.addListener(SimpleClick.on(() -> requestSwitch(3)));

        bar.add(btnForm1).size(Value.percentHeight(0.9f, bar), Value.percentHeight(0.9f, bar)).left();
        bar.add(btnForm2).size(Value.percentHeight(0.9f, bar), Value.percentHeight(0.9f, bar)).left();
        bar.add(btnForm3).size(Value.percentHeight(0.9f, bar), Value.percentHeight(0.9f, bar)).left();

        barCell = root.add(bar)
                .width(Value.percentWidth(PANEL_WIDTH_PCT, root))
                .height(Value.percentHeight(BAR_HEIGHT_PCT, root));
        stage.addActor(root);

        // Keep compatibility if your system still emits this event
        hero.getEvents().addListener("ui:weapon:locked", this::onLocked);

        // Listen for upgrades: collapse to single button at L2
        hero.getEvents().addListener("hero.level", (Integer level) -> {
            if (level != null && level >= 2) onEnterLevel2();
        });
        hero.getEvents().addListener("upgraded", (Integer level, Object _t, Integer _c) -> {
            if (level != null && level >= 2) onEnterLevel2();
        });
    }

    private void onEnterLevel2() {
        if (collapsedToSingle) return;
        collapsedToSingle = true;

        if (l2SingleIcon != null && !l2SingleIcon.isBlank()) {
            updateButtonIcon(btnForm1, l2SingleIcon);
        }

        // Increase the toolbar height: from 6% to ~9–10% (tune as needed)
        float NEW_BAR_HEIGHT_PCT = 0.10f; // 0.10 = 10% of screen height
        if (barCell != null) {
            barCell.height(Value.percentHeight(NEW_BAR_HEIGHT_PCT, root));
        }

        // Clear and add the single button
        bar.clearChildren();
        bar.defaults().padRight(0f);
        bar.pad(Value.percentHeight(0.004f, root)); // Slight inner padding to avoid hugging edges

        // Let the inner Image scale proportionally to fit the button (for pixel art, prefer Nearest filtering when creating/updating textures)
        btnForm1.getImage().setScaling(Scaling.fit);

        // Single button fills the bar height (leave ~2% margin to avoid clipping the background stroke)
        float pct = 0.98f; // 0.98 is tighter; use 1.0f for larger
        bar.add(btnForm1)
                .size(Value.percentHeight(pct, bar), Value.percentHeight(pct, bar))
                .center();

        // After upgrade the single button is display-only (comment out if you want it clickable)
        btnForm1.setDisabled(true);

        root.invalidateHierarchy();
        Gdx.app.log("HeroWeaponUI", "Collapsed to single button (L2), bar height raised.");
    }

    private void requestSwitch(int form) {
        if (locked) return;
        // After collapsing to single button post-upgrade: by default no longer allows switching (display only)
        if (collapsedToSingle) {
            return; // Do nothing; if you want switching, see the note in onEnterLevel2
        }
        hero.getEvents().trigger("ui:weapon:switch", form);
    }

    private void onLocked() {
        locked = true;
        setButtonsEnabled(false);
    }

    private void setButtonsEnabled(boolean enabled) {
        if (btnForm1 != null) btnForm1.setDisabled(!enabled);
        if (btnForm2 != null) btnForm2.setDisabled(!enabled);
        if (btnForm3 != null) btnForm3.setDisabled(!enabled);
    }

    @Override
    public void dispose() {
        if (root != null) root.remove();
        for (Texture t : toDispose) if (t != null) t.dispose();
        toDispose.clear();
    }

    private ImageButton makeIconButton(String texPath) {
        Texture tex = new Texture(Gdx.files.internal(texPath));
        toDispose.add(tex);
        TextureRegionDrawable up = new TextureRegionDrawable(new TextureRegion(tex));
        TextureRegionDrawable down = new TextureRegionDrawable(new TextureRegion(tex));
        TextureRegionDrawable disabled = new TextureRegionDrawable(new TextureRegion(tex));
        ImageButton.ImageButtonStyle st = new ImageButton.ImageButtonStyle();
        st.imageUp = up; st.imageDown = down; st.imageDisabled = disabled;
        return new ImageButton(st);
    }

    private void updateButtonIcon(ImageButton btn, String newTexPath) {
        Texture newTex = new Texture(Gdx.files.internal(newTexPath));
        toDispose.add(newTex);
        TextureRegionDrawable up = new TextureRegionDrawable(new TextureRegion(newTex));
        TextureRegionDrawable down = new TextureRegionDrawable(new TextureRegion(newTex));
        TextureRegionDrawable disabled = new TextureRegionDrawable(new TextureRegion(newTex));
        ImageButton.ImageButtonStyle newStyle = new ImageButton.ImageButtonStyle();
        newStyle.imageUp = up; newStyle.imageDown = down; newStyle.imageDisabled = disabled;
        btn.setStyle(newStyle);
        btn.invalidateHierarchy();
    }

    private TextureRegion makeSolid(int w, int h, Color c) {
        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pm.setColor(c); pm.fill();
        Texture tex = new Texture(pm);
        pm.dispose();
        toDispose.add(tex);
        return new TextureRegion(tex);
    }

    private static final class SimpleClick extends com.badlogic.gdx.scenes.scene2d.utils.ClickListener {
        private final Runnable run;
        private SimpleClick(Runnable r) { this.run = r; }
        @Override public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent e, float x, float y) { run.run(); }
        public static SimpleClick on(Runnable r) { return new SimpleClick(r); }
    }
}
