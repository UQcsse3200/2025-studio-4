package com.csse3200.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;

/**
 * 左侧黄色栏里的“英雄状态卡片”。
 * - 显示：头像、名字、HP、能量/怒气、技能CD、等级等
 * - 通过事件刷新：hero.hp, hero.energy, ultimate.remaining, hero.level
 * - 也可以直接持有 hero 引用定时pull（看你项目习惯）
 */
public class HeroStatusPanelComponent extends Component {
    private final Entity hero; // 建议把英雄实体传进来；如果你们用 SelectedHeroService，也可以从那取
    private final String heroName; // 英雄名字
    private Stage stage;
    private Table root;         // 贴到 Stage 的根Table（只负责定位）
    private Table card;         // 实际的卡片内容

    // UI 控件引用，便于更新文字
    private Label nameLabel, hpLabel, energyLabel, levelLabel, costLabel; // 移除 ultCdLabel
    private TextButton ultBtn, upgradeBtn; // 新增 ULT 按钮和升级按钮引用

    public HeroStatusPanelComponent(Entity hero) {
        this.hero = hero;
        this.heroName = "Hero"; // 默认名字
    }

    public HeroStatusPanelComponent(Entity hero, String heroName) {
        this.hero = hero;
        this.heroName = heroName;
    }

    @Override
    public void create() {
        stage = ServiceLocator.getRenderService().getStage();

        // ==== 皮肤与字体（沿用你们的 SimpleUI）====
        Skin skin = new Skin();
        skin.add("default-font", SimpleUI.font(), com.badlogic.gdx.graphics.g2d.BitmapFont.class);
        // 生成基础 Label 样式
        Label.LabelStyle ls = new Label.LabelStyle(SimpleUI.font(), Color.BLACK);
        skin.add("default", ls);

        // ==== 背景：浅黄卡片 ====
        TextureRegionDrawable yellowBg = new TextureRegionDrawable(makeSolid(240, 200, new Color(0xFFF3ACFF))); // #FFF3AC

        // ==== 左侧根布局：固定在左边，宽度与你黄色栏一致 ====
        root = new Table();
        root.setFillParent(true);
        root.left().center().padLeft(16);    // 左上角
        root.defaults().left();

        // 如果黄色背景是全屏左栏，这里**不再**设置背景；若想自己画出一块卡片：
        card = new Table(skin);
        card.setBackground(yellowBg);
        card.pad(12);
        card.defaults().left().padBottom(6);


        // ==== 文本 ====
        nameLabel   = new Label(heroName, skin); // 使用传入的英雄名字
        hpLabel     = new Label("HP: 100/100", skin);
        energyLabel = new Label("Energy: 50/50", skin);
        levelLabel  = new Label("Lv. 1", skin);
        Label damageLabel = new Label("DMG: -", skin);
        costLabel   = new Label("Upgrade cost: 200", skin);

        // ==== ULT 按钮 ====
        ultBtn = UltimateButtonComponent.createUltimateButton(hero);

        // ==== 升级按钮 ====
        TextButton.TextButtonStyle upStyle = SimpleUI.primaryButton();
        upStyle.font = SimpleUI.font();
        upStyle.fontColor = Color.BLACK;
        upStyle.overFontColor = Color.BLACK;
        upStyle.downFontColor = Color.BLACK;
        upgradeBtn = new TextButton("Upgrade", upStyle);

        // 布局：头像+右侧信息
        Table row = new Table();
        Table info = new Table();
        info.add(nameLabel).left().row();
        info.add(levelLabel).left();
        row.add(info).left().expandX();

        card.add(row).left().row();
        card.add(hpLabel).left().row();
        card.add(damageLabel).left().row();
        card.add(energyLabel).left().row();
        card.add(costLabel).left().row();
        card.add(upgradeBtn).left().width(120).padTop(4).row();
        card.add(ultBtn).left().row(); // 用按钮替换原 ultCdLabel

        // 把卡片塞到左栏（按你黄色区域从上往下的顺序放）
        root.add(card).width(220).growX();

        stage.addActor(root);

        // ==== 事件对接：根据你现有事件名监听并更新 ====
        // 1) HP 变化
        hero.getEvents().addListener("hero.hp", (Integer cur, Integer max) -> {
            if (cur == null || max == null) return;
            hpLabel.setText("HP: " + cur + "/" + max);
        });

        // ★ 新增：伤害事件
        hero.getEvents().addListener("hero.damage", (Integer dmg) -> {
            if (dmg == null) return;
            damageLabel.setText("DMG: " + dmg);
        });

        // 2) 能量/怒气 变化
        hero.getEvents().addListener("hero.energy", (Integer cur, Integer max) -> {
            if (cur == null || max == null) return;
            energyLabel.setText("Energy: " + cur + "/" + max);
        });

        // 3) 等级变化
        hero.getEvents().addListener("hero.level", (Integer lv) -> {
            if (lv == null) return;
            levelLabel.setText("Lv. " + lv);
            refreshUpgradeInfo(); // 刷新升级信息
        });

        // 4) 升级按钮事件
        upgradeBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                hero.getEvents().trigger("requestUpgrade", findPlayer());
                refreshUpgradeInfo();
            }
        });

        // 5) 响应升级结果
        hero.getEvents().addListener("upgraded", (Integer level, Object currencyType, Integer cost) -> refreshUpgradeInfo());
        hero.getEvents().addListener("upgradeFailed", (String msg) -> refreshUpgradeInfo());

        // 初始化升级信息
        refreshUpgradeInfo();
    }

    @Override
    public void dispose() {
        if (root != null) root.remove(); // 移除UI
    }

    private Entity findPlayer() {
        for (Entity e : ServiceLocator.getEntityService().getEntities()) {
            com.csse3200.game.components.currencysystem.CurrencyManagerComponent wallet = e.getComponent(com.csse3200.game.components.currencysystem.CurrencyManagerComponent.class);
            if (wallet != null) return e;
        }
        return null;
    }

    private void refreshUpgradeInfo() {
        com.csse3200.game.components.hero.HeroUpgradeComponent up = hero.getComponent(com.csse3200.game.components.hero.HeroUpgradeComponent.class);
        int lvl = (up != null) ? up.getLevel() : 1;
        int next = lvl + 1;
        int cost = (next <= 3) ? next * 200 : 0;
        costLabel.setText(next <= 3 ? ("Upgrade cost: " + cost) : "Already reach the max Level");
        upgradeBtn.setDisabled(next > 3);
    }

    /** 生成纯色背景贴图（小工具） */
    private static TextureRegion makeSolid(int w, int h, Color c) {
        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pm.setColor(c);
        pm.fill();
        Texture tex = new Texture(pm);
        pm.dispose();
        return new TextureRegion(tex);
    }
}
