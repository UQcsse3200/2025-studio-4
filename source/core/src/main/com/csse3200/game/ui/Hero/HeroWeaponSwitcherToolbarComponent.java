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
    private Table bar; // ✅ 保存工具条容器，方便动态修改
    private ImageButton btnForm1, btnForm2, btnForm3;
    private boolean locked = false;

    // 初始 L1 三图
    private final String icon1, icon2, icon3;
    // L2 只有一张，用来折叠后的单按钮
    private final String l2SingleIcon;

    // 折叠状态标记：升级后从三按钮 -> 单按钮
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

        // 如果你的系统仍会发这个事件，就保持兼容
        hero.getEvents().addListener("ui:weapon:locked", this::onLocked);

        // 监听升级：到 L2 就折叠为单按钮
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

        // ★ 把整条工具条变高：从 6% 提到 9~10%（自己微调）
        float NEW_BAR_HEIGHT_PCT = 0.10f; // 0.10 = 占屏高的 10%
        if (barCell != null) {
            barCell.height(Value.percentHeight(NEW_BAR_HEIGHT_PCT, root));
        }

        // 清空并重加单按钮
        bar.clearChildren();
        bar.defaults().padRight(0f);
        bar.pad(Value.percentHeight(0.004f, root)); // 轻微内边距，避免紧贴

        // 让内部 Image 等比缩放到按钮框内（像素风建议在创建/更新贴图时用 Nearest 过滤）
        btnForm1.getImage().setScaling(Scaling.fit);

        // ★ 单按钮占满条高（略留 2% 余量防止背景描边被顶满）
        float pct = 0.98f; // 0.98 更紧凑；想更大就 1.0f
        bar.add(btnForm1)
                .size(Value.percentHeight(pct, bar), Value.percentHeight(pct, bar))
                .center();

        // 升级后单按钮仅展示（如需可点就注掉）
        btnForm1.setDisabled(true);

        root.invalidateHierarchy();
        Gdx.app.log("HeroWeaponUI", "Collapsed to single button (L2), bar height raised.");
    }

    private void requestSwitch(int form) {
        if (locked) return;
        // 升级后折叠为单按钮：默认不再允许切换（只展示）
        if (collapsedToSingle) {
            return; // 什么都不做；若想仍可切换，请见 onEnterLevel2 的注释
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
