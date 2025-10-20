package com.csse3200.game.ui.Hero;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;

import java.util.ArrayList;
import java.util.List;

/**
 * 武士攻击工具条（百分比定位版）
 * - 固定在屏幕右侧，位于 Hotbar 正下
 * - 三个按钮：居合斩(1) / 横扫斩(2) / 旋风斩(3)
 * - 点击触发 hero 事件 "ui:samurai:attack" -> "jab"/"sweep"/"spin"
 */
public class SamuraiAttackToolbarComponent extends Component {
    private final Entity hero;
    private Stage stage;
    private Table root;

    private ImageButton btnJab, btnSweep, btnSpin;
    private Label lbl1, lbl2, lbl3;

    // 如果后面你想做冷却 / 禁用，可通过这些状态刷新
    private float cdRemaining = 0f;
    private boolean canAttack = true;

    // ===== 与 Engineer 工具条一致的百分比参数（可按需调整）=====
    private static final float HOTBAR_HEIGHT_PCT = 0.28f;
    private static final float HOTBAR_BOTTOM_PCT = 0.5f + HOTBAR_HEIGHT_PCT * 0.5f;
    private static final float GAP_BELOW_HOTBAR_PCT = 0.00f;

    private static final float PANEL_WIDTH_PCT = 0.195f;
    // 稍高一些给数字标签留空间
    private static final float BAR_HEIGHT_PCT = 0.06f;
    private static final float RIGHT_MARGIN_PCT = 0.0f;

    private final List<Texture> toDispose = new ArrayList<>();
    private BitmapFont font;

    public SamuraiAttackToolbarComponent(Entity hero) {
        this.hero = hero;
    }

    @Override
    public void create() {
        stage = ServiceLocator.getRenderService().getStage();
        font = new BitmapFont(); // 默认字体即可
        font.getData().setScale(0.9f); // 适当缩小

        root = new Table();
        root.setFillParent(true);
        root.top().right();
        root.padTop(Value.percentHeight(HOTBAR_BOTTOM_PCT + GAP_BELOW_HOTBAR_PCT, root))
                .padRight(Value.percentWidth(RIGHT_MARGIN_PCT, root));

        Table bar = new Table();
        bar.setBackground(new TextureRegionDrawable(makeSolid(2, 2, new Color(0.15f, 0.15f, 0.18f, 0.85f))));
        bar.pad(Value.percentHeight(0.006f, root));
        bar.defaults().padRight(Value.percentWidth(0.010f, root)); // 按钮间距

        // 三个“垂直单元”：图标按钮 + 下方数字标签
        btnJab   = makeIconButton("images/samurai/Stab.png");
        btnSweep = makeIconButton("images/samurai/Slash.png");
        btnSpin  = makeIconButton("images/samurai/Spin.png");

        lbl1 = makeKeyLabel("1");
        lbl2 = makeKeyLabel("2");
        lbl3 = makeKeyLabel("3");

        // 点击事件 -> 通知武士攻击组件
        btnJab.addListener(SimpleClick.on(() -> triggerAttack("jab")));
        btnSweep.addListener(SimpleClick.on(() -> triggerAttack("sweep")));
        btnSpin.addListener(SimpleClick.on(() -> triggerAttack("spin")));

        // 每个“单元”用一个小 Table 装：上图标/下数字
        bar.add(makeIconWithLabel(btnJab,  lbl1, bar)).left();
        bar.add(makeIconWithLabel(btnSweep,lbl2, bar)).left();
        bar.add(makeIconWithLabel(btnSpin, lbl3, bar)).left();

        root.add(bar)
                .width(Value.percentWidth(PANEL_WIDTH_PCT, root))
                .height(Value.percentHeight(BAR_HEIGHT_PCT, root));

        stage.addActor(root);

        bindHeroEvents();       // 预留：如你后续发冷却/能否攻击事件，可自动禁用
        refreshDisabledState(); // 初始状态
    }

    private void bindHeroEvents() {
        // 如果你在 Samurai 组件里触发这些事件，UI 就会联动
        // 冷却：hero.getEvents().trigger("samurai:cooldown", remaining)
        hero.getEvents().addListener("samurai:cooldown", (Float remaining) -> {
            cdRemaining = remaining != null ? Math.max(0f, remaining) : 0f;
            refreshDisabledState();
        });
        // 能否攻击：hero.getEvents().addListener("samurai:canAttack", (Boolean ok)->{...})
        hero.getEvents().addListener("samurai:canAttack", (Boolean ok) -> {
            canAttack = ok == null || ok;
            refreshDisabledState();
        });
    }

    private void refreshDisabledState() {
        boolean disabled = (cdRemaining > 0f) || !canAttack;
        setButtonEnabled(btnJab,   !disabled);
        setButtonEnabled(btnSweep, !disabled);
        setButtonEnabled(btnSpin,  !disabled);
    }

    private void setButtonEnabled(ImageButton btn, boolean enabled) {
        btn.setDisabled(!enabled);
        float a = enabled ? 1f : 0.4f;
        btn.getColor().a = a;
    }

    private void triggerAttack(String type) {
        // 这里可以做一次“虚拟询问”，如果你想：
        // boolean[] allow = new boolean[]{true};
        // hero.getEvents().trigger("samurai:canAttack?", allow);
        // if (!allow[0]) { hero.getEvents().trigger("ui:toast", "On cooldown"); return; }

        if (cdRemaining > 0f) {
            hero.getEvents().trigger("ui:toast", "On cooldown");
            return;
        }
        hero.getEvents().trigger("ui:samurai:attack", type);
    }

    private Table makeIconWithLabel(ImageButton btn, Label label, Table bar) {
        Table cell = new Table();
        float iconSize = 0.58f; // 相对 bar 高度（给标签留空间）
        cell.add(btn)
                .size(Value.percentHeight(iconSize, bar), Value.percentHeight(iconSize, bar))
                .row();
        cell.add(label).padTop(Value.percentHeight(0.004f, bar));
        return cell;
    }

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

    private Label makeKeyLabel(String text) {
        Label.LabelStyle ls = new Label.LabelStyle(font, Color.valueOf("C0F2FF")); // 赛博浅青
        Label lb = new Label(text, ls);
        lb.setAlignment(1); // center
        return lb;
    }

    private TextureRegion makeSolid(int w, int h, Color c) {
        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pm.setColor(c);
        pm.fill();
        Texture tex = new Texture(pm);
        pm.dispose();
        toDispose.add(tex);
        return new TextureRegion(tex);
    }

    @Override
    public void dispose() {
        if (root != null) root.remove();
        if (font != null) { font.dispose(); font = null; }
        for (Texture t : toDispose) if (t != null) t.dispose();
        toDispose.clear();
    }

    /** 小工具：无参数点击监听器 */
    private static final class SimpleClick extends com.badlogic.gdx.scenes.scene2d.utils.ClickListener {
        private final Runnable run;
        private SimpleClick(Runnable r) { this.run = r; }
        @Override public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent e, float x, float y) { run.run(); }
        public static SimpleClick on(Runnable r) { return new SimpleClick(r); }
    }
}
