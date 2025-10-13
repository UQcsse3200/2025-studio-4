package com.csse3200.game.ui.Hero;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
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
 * 工程师召唤工具条（百分比定位版）
 * - 固定在屏幕右侧，位于工程师 Hotbar 正下方一点点
 * - 尽量占据更少空间（细长横条）
 * - 尺寸、边距全部用百分比，可随分辨率自适应
 */
public class EngineerSummonToolbarComponent extends Component {
    private final Entity hero; // 监听 hero 事件（冷却、容量）
    private Stage stage;
    private Table root;

    private ImageButton btnMelee, btnTurret, btnCurrency;

    private float cdRemaining = 0f;     // 冷却剩余
    private boolean canAnyPlace = true; // 容量/类型判定结果

    // ========= 百分比参数（可按你项目实际 Hotbar 尺寸调整） =========
    /** Hotbar 高度占屏幕的比例（若你上面的热键栏不是 28%，就改这里） */
    private static final float HOTBAR_HEIGHT_PCT = 0.28f;
    /** Hotbar 垂直居中 → Hotbar 底缘大约在 50% + HOTBAR_HEIGHT_PCT/2 */
    private static final float HOTBAR_BOTTOM_PCT = 0.5f + HOTBAR_HEIGHT_PCT * 0.5f;
    /** 工具条与 Hotbar 的垂直间距（百分比） */
    private static final float GAP_BELOW_HOTBAR_PCT = 0.00f; // 2%

    /** 工具条整体宽度（和 Hotbar 差不多窄一点） */
    private static final float PANEL_WIDTH_PCT = 0.195f;
    /** 工具条整体高度（更薄，不占空间） */
    private static final float BAR_HEIGHT_PCT = 0.06f;    // 6% 屏幕高
    /** 距离屏幕右侧边距（越小越贴边） */
    private static final float RIGHT_MARGIN_PCT = 0.0f;  // 2%

    // 纹理资源需要统一释放
    private final List<Texture> toDispose = new ArrayList<>();

    public EngineerSummonToolbarComponent(Entity hero) {
        this.hero = hero;
    }

    @Override
    public void create() {
        stage = ServiceLocator.getRenderService().getStage();

        // 根容器：贴右、从顶部向下偏移（刚好在 Hotbar 下方）
        root = new Table();
        root.setFillParent(true);
        root.top().right();
        root.padTop(Value.percentHeight(HOTBAR_BOTTOM_PCT + GAP_BELOW_HOTBAR_PCT, root))
                .padRight(Value.percentWidth(RIGHT_MARGIN_PCT, root));

        // 条形背景容器（你可以换成带描边/圆角的 NinePatch，这里先用内边距撑开）
        Table bar = new Table();
        bar.setBackground(new TextureRegionDrawable(makeSolid(2, 2, new Color(0.15f, 0.15f, 0.18f, 0.85f))));
        bar.pad(Value.percentHeight(0.006f, root)); // 内边距：高度 0.6%
        bar.defaults().padRight(Value.percentWidth(0.008f, root)); // 图标之间水平间距 0.8% 宽

        // 三个按钮（使用百分比尺寸，跟随条高）
        btnMelee    = makeIconButton("images/engineer/Sentry.png");
        btnTurret   = makeIconButton("images/engineer/Turret.png");
        btnCurrency = makeIconButton("images/engineer/Currency_tower.png");

        // 点击 -> 发给 EngineerSummonComponent 统一处理（更利于规则集中）
        btnMelee.addListener(SimpleClick.on(() -> requestSummon("melee")));
        btnTurret.addListener(SimpleClick.on(() -> requestSummon("turret")));
        btnCurrency.addListener(SimpleClick.on(() -> requestSummon("currencyBot")));

        // 按钮大小 = 工具条高度的 90%（始终是正方形）
        bar.add(btnMelee)
                .size(Value.percentHeight(0.9f, bar), Value.percentHeight(0.9f, bar))
                .left();
        bar.add(btnTurret)
                .size(Value.percentHeight(0.9f, bar), Value.percentHeight(0.9f, bar))
                .left();
        bar.add(btnCurrency)
                .size(Value.percentHeight(0.9f, bar), Value.percentHeight(0.9f, bar))
                .left();

        // 把工具条加到根表，并用百分比宽高
        root.add(bar)
                .width(Value.percentWidth(PANEL_WIDTH_PCT, root))
                .height(Value.percentHeight(BAR_HEIGHT_PCT, root));

        stage.addActor(root);

        bindHeroEvents();
        refreshDisabledState(); // 初始化按钮状态
    }

    private void bindHeroEvents() {
        // 冷却事件：summon:cooldown(remaining,total)
        hero.getEvents().addListener("summon:cooldown", (Float remaining, Float total) -> {
            cdRemaining = (remaining != null) ? Math.max(0f, remaining) : 0f;
            refreshDisabledState();
        });

        // 容量变化：影响按钮是否可点（但不同类型上限不同，点击时仍会再次询问）
        hero.getEvents().addListener("summonAliveChanged", (Integer alive, Integer max) -> {
            canAnyPlace = (max != null && alive != null) ? (alive < max) : true;
            refreshDisabledState();
        });
    }

    private void refreshDisabledState() {
        boolean onCooldown = cdRemaining > 0f;
        boolean disabled = onCooldown || !canAnyPlace;
        btnMelee.setDisabled(disabled);
        btnTurret.setDisabled(disabled);
        btnCurrency.setDisabled(disabled);

        float alpha = disabled ? 0.4f : 1f;
        btnMelee.getColor().a = alpha;
        btnTurret.getColor().a = alpha;
        btnCurrency.getColor().a = alpha;
    }

    private void requestSummon(String type) {
        // 点击时再次“虚拟询问”一次容量/类型限制（和 PlacementController 同一条通路）
        boolean[] allow = new boolean[]{true};
        hero.getEvents().trigger("summon:canSpawn?", type, allow);
        if (!allow[0]) {
            hero.getEvents().trigger("ui:toast", "Summon limit reached");
            return;
        }
        if (cdRemaining > 0f) {
            hero.getEvents().trigger("ui:toast", "On cooldown");
            return;
        }
        // 交给 EngineerSummonComponent 统一映射纹理&进入摆放模式
        hero.getEvents().trigger("ui:summon:request", type);
    }

    @Override
    public void dispose() {
        if (root != null) root.remove();
        // 释放按钮纹理
        for (Texture t : toDispose) {
            if (t != null) t.dispose();
        }
        toDispose.clear();
    }

    // ========= 工具 =========

    /** 生成图标按钮（带按下和禁用态的色调变化） */
    private ImageButton makeIconButton(String texPath) {
        Texture tex = new Texture(Gdx.files.internal(texPath));
        toDispose.add(tex);
        TextureRegionDrawable dr = new TextureRegionDrawable(new TextureRegion(tex));

        ImageButton.ImageButtonStyle st = new ImageButton.ImageButtonStyle();
        st.imageUp = dr;
        st.imageDown = dr.tint(new Color(0.7f, 0.7f, 0.7f, 1f));
        st.imageDisabled = dr.tint(new Color(1f, 1f, 1f, 0.35f));

        return new ImageButton(st);
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

    /** 小工具：无参数点击监听器 */
    private static final class SimpleClick extends com.badlogic.gdx.scenes.scene2d.utils.ClickListener {
        private final Runnable run;
        private SimpleClick(Runnable r) { this.run = r; }
        @Override public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent e, float x, float y) { run.run(); }
        public static SimpleClick on(Runnable r) { return new SimpleClick(r); }
    }
}

