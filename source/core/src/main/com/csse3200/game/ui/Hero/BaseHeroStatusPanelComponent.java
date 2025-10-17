package com.csse3200.game.ui.Hero;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.SimpleUI;
import com.csse3200.game.ui.UltimateButtonComponent;

/**
 * 英雄状态栏通用基类（百分比定位/尺寸版）：
 * - 名字、HP、能量、伤害、等级、升级费用、ULT 按钮
 * - 子类可覆写 buildExtraSections()/bindExtraListeners() 增加自定义区块
 * - 面板自动放在：Hotbar 正下方 → 召唤工具条正下方
 */
public class BaseHeroStatusPanelComponent extends Component {
    protected final Entity hero;
    protected final String heroName;

    // 色彩/布局参数（子类在构造器里指定）
    protected final Color bgColor;
    protected final Color textColor;
    protected final Color accentColor;

    // ====== 右侧 UI 纵向栈的公共百分比参数（与你的 Hotbar/Toolbar 保持一致）======
    /** Hotbar：高度占屏幕比例（你的最终版是 0.28f） */
    protected static final float HOTBAR_HEIGHT_PCT = 0.28f;
    /** Hotbar 底缘：垂直居中 → 0.5 + 高度的一半 */
    protected static final float HOTBAR_BOTTOM_PCT = 0.5f + HOTBAR_HEIGHT_PCT * 0.5f;

    /** 召唤工具条高度（你的最终版是 0.06f） */
    protected static final float TOOLBAR_HEIGHT_PCT = 0.06f;
    /** 工具条与状态栏之间的间距 */
    protected static final float GAP_BELOW_TOOLBAR_PCT = 0.0f; // 想紧贴就设 0f

    /** 右侧这些面板的统一宽度（与你的 Hotbar 相同：0.195f） */
    protected static final float COMMON_PANEL_WIDTH_PCT = 0.195f;
    /** 状态栏自身高度（可调） */
    protected final float panelHeightPct; // 替代原 panelHeightScale（相对屏幕高的比例）

    /** 与屏幕右边的外边距（与你的 Hotbar 一致：0f 贴边） */
    protected static final float RIGHT_MARGIN_PCT = 0.0f;

    // 舞台与容器
    protected Stage stage;
    protected Table root;
    protected Table card;

    // 公共控件
    protected Label nameLabel, hpLabel, energyLabel, levelLabel, costLabel, damageLabel;
    protected TextButton ultBtn, upgradeBtn;

    public BaseHeroStatusPanelComponent(Entity hero,
                                        String heroName,
                                        Color bgColor,
                                        Color textColor,
                                        Color accentColor,
                                        float panelHeightPct /* 面板高度占屏幕的比例，例如 0.12f~0.26f */) {
        this.hero = hero;
        this.heroName = (heroName != null) ? heroName : "Hero";
        this.bgColor = (bgColor != null) ? bgColor : new Color(0.15f, 0.15f, 0.18f, 0.90f);
        this.textColor = (textColor != null) ? textColor : Color.WHITE;
        this.accentColor = (accentColor != null) ? accentColor : new Color(0.35f, 0.75f, 1.00f, 1f);
        this.panelHeightPct = (panelHeightPct > 0f) ? panelHeightPct : 0.24f;
    }

    @Override
    public void create() {
        stage = ServiceLocator.getRenderService().getStage();

        // 皮肤与文字
        Skin skin = new Skin();
        Label.LabelStyle ls = new Label.LabelStyle(SimpleUI.font(), textColor);
        skin.add("default", ls);

        // 背景
        TextureRegionDrawable darkBg = new TextureRegionDrawable(makeSolid(4, 4, bgColor));

        // ===== 根容器：右上；纵向位置 = Hotbar 底 + 工具条高 + 间距 =====
        root = new Table();
        root.setFillParent(true);
        root.align(Align.topRight);
        root.padTop(Value.percentHeight(HOTBAR_BOTTOM_PCT + TOOLBAR_HEIGHT_PCT + GAP_BELOW_TOOLBAR_PCT, root));
        root.padRight(Value.percentWidth(RIGHT_MARGIN_PCT, root));

        // ===== 卡片 =====
        card = new Table(skin);
        card.setBackground(darkBg);
        // 内边距 & 行间距 使用相对卡片的百分比，更稳
        card.pad(Value.percentWidth(0.02f, card));             // 左右留白
        card.defaults().left().padBottom(Value.percentHeight(0.02f, card));

        // 文本与按钮
        nameLabel   = new Label(heroName, skin);
        levelLabel  = new Label("Lv. 1", skin);
        costLabel   = new Label("Upgrade cost: 400", skin);

        ultBtn = UltimateButtonComponent.createUltimateButton(hero);

        TextButton.TextButtonStyle upStyle = SimpleUI.primaryButton();
        upStyle.font = SimpleUI.font();
        upStyle.fontColor = Color.BLACK;
        upStyle.overFontColor = Color.BLACK;
        upStyle.downFontColor = Color.BLACK;
        upgradeBtn = new TextButton("Upgrade", upStyle);

        Table info = new Table();
        info.add(nameLabel).left().row();
        info.add(levelLabel).left();

        // 组装公共区
        card.add(info).left().row();
        card.add(energyLabel).left().row();

        // —— 子类扩展区（容量/冷却等） —— //
        buildExtraSections(card, skin,
                Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // 升级/ULT
        card.add(costLabel).left().row();
        card.add(upgradeBtn)
                .left()
                .width(Value.percentWidth(0.45f, card))              // 升级按钮宽 = 卡片宽 45%
                .padTop(Value.percentHeight(0.02f, card))
                .row();
        card.add(ultBtn).left().row();

        // 将卡片按 “等宽对齐 & 百分比高度” 放进右侧布局
        root.add(card)
                .width(Value.percentWidth(COMMON_PANEL_WIDTH_PCT, root))
                .height(Value.percentHeight(panelHeightPct, root));
        stage.addActor(root);

        // ===== 公共事件 =====

        hero.getEvents().addListener("hero.level", (Integer lv) -> {
            if (lv == null) return;
            levelLabel.setText("Lv. " + lv);
            refreshUpgradeInfo();
        });

        upgradeBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                hero.getEvents().trigger("requestUpgrade", findPlayer());
                refreshUpgradeInfo();
            }
        });

        hero.getEvents().addListener("upgraded", (Integer level,
                                                  com.csse3200.game.components.currencysystem.CurrencyComponent.CurrencyType currencyType,
                                                  Integer cost) -> refreshUpgradeInfo());

        hero.getEvents().addListener("upgradeFailed", (String msg) -> refreshUpgradeInfo());

        // 子类的额外事件（如容量/冷却）
        bindExtraListeners();

        refreshUpgradeInfo();
    }

    @Override
    public void dispose() {
        if (root != null) root.remove();
    }

    /** 子类可覆写：构建额外UI区域（容量/冷却等）。默认不添加。 */
    protected void buildExtraSections(Table card, Skin skin, float sw, float sh) {}

    /** 子类可覆写：绑定额外事件监听（容量/冷却等）。默认不绑定。 */
    protected void bindExtraListeners() {}

    /** 通用进度条样式（暗灰背景 + 强调色填充） */
    protected ProgressBar.ProgressBarStyle buildBarStyle() {
        ProgressBar.ProgressBarStyle s = new ProgressBar.ProgressBarStyle();
        s.background = new TextureRegionDrawable(makeSolid(8, 8, new Color(0.10f, 0.10f, 0.12f, 1f)));
        s.knobBefore = new TextureRegionDrawable(makeSolid(8, 8, accentColor));
        s.knob = new TextureRegionDrawable(makeSolid(1, 8, new Color(1f, 1f, 1f, 0f)));
        return s;
    }

    /** 根据当前等级刷新升级信息与按钮状态 */
    protected void refreshUpgradeInfo() {
        com.csse3200.game.components.hero.HeroUpgradeComponent up =
                hero.getComponent(com.csse3200.game.components.hero.HeroUpgradeComponent.class);

        int lvl = (up != null) ? up.getLevel() : 1;
        int next = lvl + 1;

        // ★ 英雄最高等级为 2 → 只升级一次
        if (lvl >= 2) {
            costLabel.setText("MAX LEVEL");
            costLabel.setColor(accentColor.cpy().lerp(Color.GRAY, 0.4f));

            upgradeBtn.setDisabled(true);
            upgradeBtn.setText("Maxed");
            upgradeBtn.getStyle().fontColor = Color.GRAY;
            upgradeBtn.getStyle().overFontColor = Color.GRAY;
            upgradeBtn.getStyle().downFontColor = Color.GRAY;
            return;
        }

    }


    protected Entity findPlayer() {
        for (Entity e : ServiceLocator.getEntityService().getEntities()) {
            com.csse3200.game.components.currencysystem.CurrencyManagerComponent wallet =
                    e.getComponent(com.csse3200.game.components.currencysystem.CurrencyManagerComponent.class);
            if (wallet != null) return e;
        }
        return null;
    }

    /** 生成纯色贴图（注意：此处未集中回收，若频繁创建可上资源管理器） */
    protected static TextureRegion makeSolid(int w, int h, Color c) {
        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pm.setColor(c);
        pm.fill();
        Texture tex = new Texture(pm);
        pm.dispose();
        return new TextureRegion(tex);
    }
}

