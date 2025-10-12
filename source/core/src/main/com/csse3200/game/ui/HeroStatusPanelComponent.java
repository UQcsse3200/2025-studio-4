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

        // === 与 HeroHotbarDisplay 保持一致的深色主题 ===
        Skin skin = new Skin();
        // 白字更适合深色背景
        Label.LabelStyle ls = new Label.LabelStyle(SimpleUI.font(), Color.WHITE);
        skin.add("default", ls);

        // —— 与 Hotbar 一致的暗色背景（0.15,0.15,0.18,0.9）——
        TextureRegionDrawable darkBg = new TextureRegionDrawable(makeSolid(4, 4, new Color(0.15f, 0.15f, 0.18f, 0.9f)));

        // === 屏幕与右侧栏的布局常量（与 HeroHotbarDisplay 对齐） ===
        float sw = Gdx.graphics.getWidth();
        float sh = Gdx.graphics.getHeight();

        // Hotbar 的宽高（你的 Hotbar 代码里用的是 width=0.20f, height=0.20f）
        float HOTBAR_W = sw * 0.20f;
        float HOTBAR_H = sh * 0.20f;
        float SIDE_PAD  = sw * 0.012f;  // 右侧外边距
        float GAP       = sh * 0.012f;  // Hotbar 与状态卡片之间的垂直间距

        // 本面板尺寸尽量与 Hotbar 同宽，稍高一些
        float PANEL_W   = HOTBAR_W;
        float PANEL_H   = sh * 0.24f;

        // === 根 Table：固定在“右上”，并向下偏移 Hotbar 的高度 → 恰好在 Hotbar 正下方 ===
        root = new Table();
        root.setFillParent(true);
        // 改成：
        root.top().right();
// Hotbar靠右居中 => Hotbar 顶边 = sh * 0.5f + HOTBAR_H * 0.5f
        root.padTop(sh * 0.5f + HOTBAR_H * 0.5f + GAP).padRight(0f);

        // === 卡片内容 ===
        card = new Table(skin);
        card.setBackground(darkBg);
        card.pad(sw * 0.008f);
        card.defaults().left().padBottom(sh * 0.006f);

        // 文本与按钮（白字）
        nameLabel   = new Label(heroName, skin);
        hpLabel     = new Label("HP: 100/100", skin);
        energyLabel = new Label("Energy: 50/50", skin);
        levelLabel  = new Label("Lv. 1", skin);
        Label damageLabel = new Label("DMG: -", skin);
        costLabel   = new Label("Upgrade cost: 200", skin);

        // ULT 按钮（可复用你们的样式，但把字色设为黑或白都可）
        ultBtn = UltimateButtonComponent.createUltimateButton(hero);

        // 升级按钮（用你们 SimpleUI 的主按钮样式，适配深色背景）
        TextButton.TextButtonStyle upStyle = SimpleUI.primaryButton();
        upStyle.font = SimpleUI.font();
        upStyle.fontColor = Color.BLACK;     // 按钮内用浅底深字
        upStyle.overFontColor = Color.BLACK;
        upStyle.downFontColor = Color.BLACK;
        upgradeBtn = new TextButton("Upgrade", upStyle);

        // 头像/信息行（如需头像，这里可加 Image）
        Table info = new Table();
        info.add(nameLabel).left().row();
        info.add(levelLabel).left();

        card.add(info).left().row();
        card.add(hpLabel).left().row();
        card.add(damageLabel).left().row();
        card.add(energyLabel).left().row();
        card.add(costLabel).left().row();
        card.add(upgradeBtn).left().width(sw * 0.10f).padTop(sh * 0.004f).row();
        card.add(ultBtn).left().row();

        // 把卡片放进右侧根容器，并固定宽高与 Hotbar 对齐
        root.add(card).width(PANEL_W).height(PANEL_H);
        stage.addActor(root);

        // ==== 事件绑定 ====
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

        hero.getEvents().addListener("upgraded", (Integer level, Object currencyType, Integer cost) -> refreshUpgradeInfo());
        hero.getEvents().addListener("upgradeFailed", (String msg) -> refreshUpgradeInfo());

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
