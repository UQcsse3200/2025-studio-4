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
 * Hero 武器切换工具条（与工程师工具条一致的百分比布局）
 * - 固定在屏幕右侧，位于 Hero Hotbar 下方
 * - 点击 3 个图标触发 "ui:weapon:switch" 事件（参数：1/2/3）
 * - 第一次成功手动切换后，外层 HeroOneShotFormSwitchComponent 会锁定，
 *   本组件收到 "ui:weapon:locked" 后置灰禁用。
 */
public class HeroWeaponSwitcherToolbarComponent extends Component {
    private final Entity hero;
    private Stage stage;
    private Table root;

    // ====== 可按你的 Hotbar 尺寸微调 ======
    private static final float HOTBAR_HEIGHT_PCT = 0.28f;
    private static final float HOTBAR_BOTTOM_PCT = 0.5f + HOTBAR_HEIGHT_PCT * 0.5f;
    private static final float GAP_BELOW_HOTBAR_PCT = 0.00f;
    private static final float PANEL_WIDTH_PCT = 0.195f;
    private static final float BAR_HEIGHT_PCT = 0.06f;
    private static final float RIGHT_MARGIN_PCT = 0.0f;

    private final List<Texture> toDispose = new ArrayList<>();
    private ImageButton btnForm1, btnForm2, btnForm3;
    private boolean locked = false;

    // 允许把按钮图标作为构造参数传入；也可以直接用 hero 的三张皮肤作为按钮
    private final String icon1, icon2, icon3;

    public HeroWeaponSwitcherToolbarComponent(Entity hero, String icon1, String icon2, String icon3) {
        this.hero = hero;
        this.icon1 = icon1;
        this.icon2 = icon2;
        this.icon3 = icon3;
    }

    @Override
    public void create() {
        stage = ServiceLocator.getRenderService().getStage();

        root = new Table();
        root.setFillParent(true);
        root.top().right();
        root.padTop(Value.percentHeight(HOTBAR_BOTTOM_PCT + GAP_BELOW_HOTBAR_PCT, root))
                .padRight(Value.percentWidth(RIGHT_MARGIN_PCT, root));

        Table bar = new Table();
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

        root.add(bar)
                .width(Value.percentWidth(PANEL_WIDTH_PCT, root))
                .height(Value.percentHeight(BAR_HEIGHT_PCT, root));
        stage.addActor(root);

        // 监听锁定事件（由 HeroOneShotFormSwitchComponent 触发）
        hero.getEvents().addListener("ui:weapon:locked", this::onLocked);
    }

    private void requestSwitch(int form) {
        if (locked) return;
        hero.getEvents().trigger("ui:weapon:switch", form); // 只发事件，不做切换细节
    }

    private void onLocked() {
        locked = true;
        setButtonsEnabled(false);
    }

    private void setButtonsEnabled(boolean enabled) {
        btnForm1.setDisabled(!enabled);
        btnForm2.setDisabled(!enabled);
        btnForm3.setDisabled(!enabled);
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
        // 不再着色：down/disabled 都用同一张（或者重新 new 一份，不 tint）
        TextureRegionDrawable down = new TextureRegionDrawable(new TextureRegion(tex));
        TextureRegionDrawable disabled = new TextureRegionDrawable(new TextureRegion(tex));

        ImageButton.ImageButtonStyle st = new ImageButton.ImageButtonStyle();
        st.imageUp = up;
        st.imageDown = down;
        st.imageDisabled = disabled;

        ImageButton btn = new ImageButton(st);
        // 不再改颜色/透明度
        // btn.setColor(1,1,1,1); // 可留可删（默认就是白色）
        return btn;
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

    private static final class SimpleClick extends com.badlogic.gdx.scenes.scene2d.utils.ClickListener {
        private final Runnable run;
        private SimpleClick(Runnable r) { this.run = r; }
        @Override public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent e, float x, float y) { run.run(); }
        public static SimpleClick on(Runnable r) { return new SimpleClick(r); }
    }
}
