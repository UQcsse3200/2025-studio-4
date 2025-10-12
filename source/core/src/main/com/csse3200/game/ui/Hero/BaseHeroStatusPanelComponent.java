package com.csse3200.game.ui.Hero;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.SimpleUI;
import com.csse3200.game.ui.UltimateButtonComponent;

/**
 * 英雄状态栏通用基类：
 * - 名字、HP、能量、伤害、等级、升级费用、ULT 按钮
 * - 子类可覆写 buildExtraSections()/bindExtraListeners() 增加自定义区块（如工程师的容量/冷却）
 * - 子类可在构造器里设定颜色与高度
 */
public class BaseHeroStatusPanelComponent extends Component {
    protected final Entity hero;
    protected final String heroName;

    // 色彩/布局参数（子类在构造器里指定）
    protected final Color bgColor;
    protected final Color textColor;
    protected final Color accentColor;
    protected final float panelHeightScale; // 面板高度相对屏幕高的比例（如0.24f）

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
                                        float panelHeightScale) {
        this.hero = hero;
        this.heroName = (heroName != null) ? heroName : "Hero";
        this.bgColor = (bgColor != null) ? bgColor : new Color(0.15f, 0.15f, 0.18f, 0.90f);
        this.textColor = (textColor != null) ? textColor : Color.WHITE;
        this.accentColor = (accentColor != null) ? accentColor : new Color(0.35f, 0.75f, 1.00f, 1f);
        this.panelHeightScale = (panelHeightScale > 0f) ? panelHeightScale : 0.24f;
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

        // 布局常量
        float sw = Gdx.graphics.getWidth();
        float sh = Gdx.graphics.getHeight();
        float HOTBAR_W = sw * 0.20f;
        float HOTBAR_H = sh * 0.20f;
        float GAP      = sh * 0.012f;
        float PANEL_W  = HOTBAR_W;
        float PANEL_H  = sh * panelHeightScale;

        // 根容器：右上，位于 Hotbar 下方
        root = new Table();
        root.setFillParent(true);
        root.top().right();
        root.padTop(sh * 0.5f + HOTBAR_H * 0.5f + GAP).padRight(0f);

        // 卡片
        card = new Table(skin);
        card.setBackground(darkBg);
        card.pad(sw * 0.008f);
        card.defaults().left().padBottom(sh * 0.006f);

        // 文本与按钮
        nameLabel   = new Label(heroName, skin);
        hpLabel     = new Label("HP: 100/100", skin);
        energyLabel = new Label("Energy: 50/50", skin);
        levelLabel  = new Label("Lv. 1", skin);
        damageLabel = new Label("DMG: -", skin);
        costLabel   = new Label("Upgrade cost: 200", skin);

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
        card.add(hpLabel).left().row();
        card.add(damageLabel).left().row();
        card.add(energyLabel).left().row();

        // —— 子类扩展区（容量/冷却等） ——
        buildExtraSections(card, skin, sw, sh);

        // 升级/ULT
        card.add(costLabel).left().row();
        card.add(upgradeBtn).left().width(sw * 0.10f).padTop(sh * 0.004f).row();
        card.add(ultBtn).left().row();

        root.add(card).width(PANEL_W).height(PANEL_H);
        stage.addActor(root);

        // 公共事件
        hero.getEvents().addListener("hero.hp", (Integer cur, Integer max) -> {
            if (cur == null || max == null) return;
            hpLabel.setText("HP: " + cur + "/" + max);
        });

        hero.getEvents().addListener("hero.damage", (Integer dmg) -> {
            if (dmg == null) return;
            damageLabel.setText("DMG: " + dmg);
        });

        hero.getEvents().addListener("hero.energy", (Integer cur, Integer max) -> {
            if (cur == null || max == null) return;
            energyLabel.setText("Energy: " + cur + "/" + max);
        });

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

    protected void refreshUpgradeInfo() {
        com.csse3200.game.components.hero.HeroUpgradeComponent up =
                hero.getComponent(com.csse3200.game.components.hero.HeroUpgradeComponent.class);
        int lvl = (up != null) ? up.getLevel() : 1;
        int next = lvl + 1;
        int cost = (next <= 3) ? next * 200 : 0;
        costLabel.setText(next <= 3 ? ("Upgrade cost: " + cost) : "Already reach the max Level");
        upgradeBtn.setDisabled(next > 3);
    }

    protected Entity findPlayer() {
        for (Entity e : ServiceLocator.getEntityService().getEntities()) {
            com.csse3200.game.components.currencysystem.CurrencyManagerComponent wallet =
                    e.getComponent(com.csse3200.game.components.currencysystem.CurrencyManagerComponent.class);
            if (wallet != null) return e;
        }
        return null;
    }

    /** 生成纯色贴图 */
    protected static TextureRegion makeSolid(int w, int h, Color c) {
        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pm.setColor(c);
        pm.fill();
        Texture tex = new Texture(pm);
        pm.dispose();
        return new TextureRegion(tex);
    }
}
