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
 * 工程师召唤工具条（安全版：零 tint / 零透明度改动 / 防批处理串色）
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

        // 根容器：贴右，放在 Hotbar 下
        root = new Table();
        root.setFillParent(true);
        root.top().right();
        root.padTop(Value.percentHeight(HOTBAR_BOTTOM_PCT + GAP_BELOW_HOTBAR_PCT, root))
                .padRight(Value.percentWidth(RIGHT_MARGIN_PCT, root));
        root.setColor(Color.WHITE); // 防父节点 tint 传染

        // 条形背景
        Table bar = new Table();
        bar.setBackground(new TextureRegionDrawable(makeSolid(2, 2, new Color(0.15f, 0.15f, 0.18f, 0.85f))));
        bar.pad(Value.percentHeight(0.006f, root));
        bar.defaults().padRight(Value.percentWidth(0.008f, root));
        bar.setColor(Color.WHITE); // 防父节点 tint 传染

        // 三个按钮（安全按钮，内部自复位 batch 颜色）
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

        // 只禁用，不改透明度/不做 tint
        btnMelee.setDisabled(disabled);
        btnTurret.setDisabled(disabled);
        btnCurrency.setDisabled(disabled);

        // 明确设为白色，避免继承父节点颜色
        btnMelee.setColor(1,1,1,1);
        btnTurret.setColor(1,1,1,1);
        btnCurrency.setColor(1,1,1,1);
        // 同时把内部 image 也设白，双保险
        getImage(btnMelee).setColor(1,1,1,1);
        getImage(btnTurret).setColor(1,1,1,1);
        getImage(btnCurrency).setColor(1,1,1,1);
    }

    private Image getImage(ImageButton b) {
        // ImageButton 内部的 image 是第一个 child
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

    // ===== 工具 =====

    /** 防串色的 ImageButton：绘制前后把 Batch 颜色强制设回白色 */
    private static class SafeImageButton extends ImageButton {
        public SafeImageButton(ImageButtonStyle style) { super(style); }
        @Override
        public void draw(Batch batch, float parentAlpha) {
            Color old = batch.getColor();
            batch.setColor(1f,1f,1f,1f); // 防外部乘色影响到本控件
            super.draw(batch, parentAlpha);
            batch.setColor(old);         // 恢复外部颜色，防本控件影响别人
        }
    }

    /** 生成图标按钮（不使用任何 tint/背景；使用 SafeImageButton） */
    private ImageButton makeIconButton(String texPath) {
        Texture tex = new Texture(Gdx.files.internal(texPath));
        toDispose.add(tex);
        TextureRegionDrawable up = new TextureRegionDrawable(new TextureRegion(tex));
        TextureRegionDrawable down = new TextureRegionDrawable(new TextureRegion(tex));
        TextureRegionDrawable disabled = new TextureRegionDrawable(new TextureRegion(tex));

        ImageButton.ImageButtonStyle st = new ImageButton.ImageButtonStyle();
        // 不使用皮肤背景，避免奇怪的默认着色
        st.up = st.down = st.over = st.checked = st.disabled = null;
        st.imageUp = up;
        st.imageDown = down;
        st.imageDisabled = disabled;

        ImageButton btn = new SafeImageButton(st);
        btn.setColor(1,1,1,1);                 // 明确设白
        Image img = getImage(btn);
        if (img != null) img.setColor(1,1,1,1);// 明确设白（子节点）
        return btn;
    }

    /** 生成纯色纹理区域，用于背景色块（半透明） */
    private TextureRegion makeSolid(int w, int h, Color c) {
        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pm.setColor(c);
        pm.fill();
        Texture tex = new Texture(pm);
        pm.dispose();
        toDispose.add(tex);
        return new TextureRegion(tex);
    }

    /** 无参数点击监听器 */
    private static final class SimpleClick extends com.badlogic.gdx.scenes.scene2d.utils.ClickListener {
        private final Runnable run;
        private SimpleClick(Runnable r) { this.run = r; }
        @Override public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent e, float x, float y) { run.run(); }
        public static SimpleClick on(Runnable r) { return new SimpleClick(r); }
    }
}


