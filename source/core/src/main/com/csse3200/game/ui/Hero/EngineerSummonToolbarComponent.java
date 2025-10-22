package com.csse3200.game.ui.Hero;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;

import java.util.ArrayList;
import java.util.List;

/**
 * Engineer summon toolbar (safe version: zero tint / zero alpha changes / prevents batch color bleed)
 */
public class EngineerSummonToolbarComponent extends Component {
    private final Entity hero;
    private Stage stage;
    private Table root;

    private ImageButton btnMelee, btnTurret, btnCurrency;

    private float cdRemaining = 0f;
    private boolean canAnyPlace = true;

    private static final float HOTBAR_HEIGHT_PCT = 0.28f;
    private static final float HOTBAR_BOTTOM_PCT = 0.5f + HOTBAR_HEIGHT_PCT * 0.5f;
    private static final float GAP_BELOW_HOTBAR_PCT = 0.00f;

    private static final float PANEL_WIDTH_PCT = 0.195f;
    private static final float BAR_HEIGHT_PCT = 0.06f;
    private static final float RIGHT_MARGIN_PCT = 0.0f;

    private final List<Texture> toDispose = new ArrayList<>();

    public EngineerSummonToolbarComponent(Entity hero) { this.hero = hero; }

    @Override
    public void create() {
        stage = ServiceLocator.getRenderService().getStage();

        // Root container: flush right, placed under the Hotbar
        root = new Table();
        root.setFillParent(true);
        root.top().right();
        root.padTop(Value.percentHeight(HOTBAR_BOTTOM_PCT + GAP_BELOW_HOTBAR_PCT, root))
                .padRight(Value.percentWidth(RIGHT_MARGIN_PCT, root));
        root.setColor(Color.WHITE); // Prevent tint propagation from parent

        // Bar background
        Table bar = new Table();
        bar.setBackground(new TextureRegionDrawable(makeSolid(2, 2, new Color(0.15f, 0.15f, 0.18f, 0.85f))));
        bar.pad(Value.percentHeight(0.006f, root));
        bar.defaults().padRight(Value.percentWidth(0.008f, root));
        bar.setColor(Color.WHITE); // Prevent tint propagation from parent

        // Three buttons (safe buttons that auto-reset the batch color internally)
        btnMelee    = makeIconButton("images/engineer/Sentry.png");
        btnTurret   = makeIconButton("images/engineer/Turret.png");
        btnCurrency = makeIconButton("images/engineer/Currency_tower.png");

        btnMelee.addListener(SimpleClick.on(() -> requestSummon("melee")));
        btnTurret.addListener(SimpleClick.on(() -> requestSummon("turret")));
        btnCurrency.addListener(SimpleClick.on(() -> requestSummon("currencyBot")));

        bar.add(btnMelee).size(Value.percentHeight(0.9f, bar), Value.percentHeight(0.9f, bar)).left();
        bar.add(btnTurret).size(Value.percentHeight(0.9f, bar), Value.percentHeight(0.9f, bar)).left();
        bar.add(btnCurrency).size(Value.percentHeight(0.9f, bar), Value.percentHeight(0.9f, bar)).left();

        root.add(bar)
                .width(Value.percentWidth(PANEL_WIDTH_PCT, root))
                .height(Value.percentHeight(BAR_HEIGHT_PCT, root));

        stage.addActor(root);

        bindHeroEvents();
        refreshDisabledState();
    }

    private void bindHeroEvents() {
        hero.getEvents().addListener("summon:cooldown", (Float remaining, Float total) -> {
            cdRemaining = (remaining != null) ? Math.max(0f, remaining) : 0f;
            refreshDisabledState();
        });
        hero.getEvents().addListener("summonAliveChanged", (Integer alive, Integer max) -> {
            canAnyPlace = (max != null && alive != null) ? (alive < max) : true;
            refreshDisabledState();
        });
    }

    private void refreshDisabledState() {
        boolean onCooldown = cdRemaining > 0f;
        boolean disabled = onCooldown || !canAnyPlace;

        // Only disable; do not change alpha or apply tint
        btnMelee.setDisabled(disabled);
        btnTurret.setDisabled(disabled);
        btnCurrency.setDisabled(disabled);

        // Explicitly set to white to avoid inheriting parent color
        btnMelee.setColor(1,1,1,1);
        btnTurret.setColor(1,1,1,1);
        btnCurrency.setColor(1,1,1,1);
        // Also set the internal image to white as double insurance
        getImage(btnMelee).setColor(1,1,1,1);
        getImage(btnTurret).setColor(1,1,1,1);
        getImage(btnCurrency).setColor(1,1,1,1);
    }

    private Image getImage(ImageButton b) {
        // The internal image inside ImageButton is the first child
        for (Actor c : b.getChildren()) if (c instanceof Image i) return i;
        return null;
    }

    private void requestSummon(String type) {
        boolean[] allow = new boolean[]{true};
        hero.getEvents().trigger("summon:canSpawn?", type, allow);
        if (!allow[0]) { hero.getEvents().trigger("ui:toast", "Summon limit reached"); return; }
        if (cdRemaining > 0f) { hero.getEvents().trigger("ui:toast", "On cooldown"); return; }
        hero.getEvents().trigger("ui:summon:request", type);
    }

    @Override
    public void dispose() {
        if (root != null) root.remove();
        for (Texture t : toDispose) if (t != null) t.dispose();
        toDispose.clear();
    }

    // ===== Utilities =====

    /** ImageButton that prevents color bleeding: force the Batch color to white before/after drawing */
    private static class SafeImageButton extends ImageButton {
        public SafeImageButton(ImageButtonStyle style) { super(style); }
        @Override
        public void draw(Batch batch, float parentAlpha) {
            Color old = batch.getColor();
            batch.setColor(1f,1f,1f,1f); // Prevent external multiplicative color from affecting this widget
            super.draw(batch, parentAlpha);
            batch.setColor(old);         // Restore external color to avoid this widget affecting others
        }
    }

    /** Create an icon button (no tint/background; uses SafeImageButton) */
    private ImageButton makeIconButton(String texPath) {
        Texture tex = new Texture(Gdx.files.internal(texPath));
        toDispose.add(tex);
        TextureRegionDrawable up = new TextureRegionDrawable(new TextureRegion(tex));
        TextureRegionDrawable down = new TextureRegionDrawable(new TextureRegion(tex));
        TextureRegionDrawable disabled = new TextureRegionDrawable(new TextureRegion(tex));

        ImageButton.ImageButtonStyle st = new ImageButton.ImageButtonStyle();
        // Do not use skin backgrounds to avoid odd default tinting
        st.up = st.down = st.over = st.checked = st.disabled = null;
        st.imageUp = up;
        st.imageDown = down;
        st.imageDisabled = disabled;

        ImageButton btn = new SafeImageButton(st);
        btn.setColor(1,1,1,1);                 // Explicitly set to white
        Image img = getImage(btn);
        if (img != null) img.setColor(1,1,1,1);// Explicitly set to white (child)
        return btn;
    }

    /** Generate a solid texture region for background color blocks (semi-transparent) */
    private TextureRegion makeSolid(int w, int h, Color c) {
        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pm.setColor(c);
        pm.fill();
        Texture tex = new Texture(pm);
        pm.dispose();
        toDispose.add(tex);
        return new TextureRegion(tex);
    }

    /** Click listener with no parameters */
    private static final class SimpleClick extends com.badlogic.gdx.scenes.scene2d.utils.ClickListener {
        private final Runnable run;
        private SimpleClick(Runnable r) { this.run = r; }
        @Override public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent e, float x, float y) { run.run(); }
        public static SimpleClick on(Runnable r) { return new SimpleClick(r); }
    }
}
