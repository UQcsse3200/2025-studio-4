package com.csse3200.game.ui.Hero;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.SimpleUI;
import com.csse3200.game.ui.UltimateButtonComponent;

/**
 * 左侧黄色栏里的“英雄状态卡片”：
 * - 显示：名字、HP、能量、伤害、等级、升级费用、ULT按钮
 * - 召唤系统UI：召唤容量（已放置/最大）+ 进度条；冷却倒计时 + 进度条
 *
 * 监听的事件：
 * - hero.hp(Integer cur, Integer max)
 * - hero.energy(Integer cur, Integer max)
 * - hero.damage(Integer dmg)
 * - hero.level(Integer lv)
 * - summonAliveChanged(Integer alive, Integer max)     // 来自 EngineerSummonComponent
 * - summon:cooldown(Float remaining, Float total)      // 来自 EngineerSummonComponent
 * - upgraded(...) / upgradeFailed(...)
 */
public class HeroStatusPanelComponent extends Component {
    private final Entity hero;
    private final String heroName;

    private Stage stage;
    private Table root;
    private Table card;

    // 基本属性
    private Label nameLabel, hpLabel, energyLabel, levelLabel, costLabel, damageLabel;
    private TextButton ultBtn, upgradeBtn;

    // 召唤容量
    private Label aliveLabel;
    private ProgressBar aliveBar;
    private int lastAlive = 0;
    private int lastMax = 0;

    // 冷却
    private Label cooldownLabel;
    private ProgressBar cooldownBar;

    public HeroStatusPanelComponent(Entity hero) {
        this(hero, "Hero");
    }

    public HeroStatusPanelComponent(Entity hero, String heroName) {
        this.hero = hero;
        this.heroName = heroName != null ? heroName : "Hero";
    }

    @Override
    public void create() {
        stage = ServiceLocator.getRenderService().getStage();

        // 统一皮肤/样式
        Skin skin = new Skin();
        Label.LabelStyle ls = new Label.LabelStyle(SimpleUI.font(), Color.WHITE);
        skin.add("default", ls);

        // 暗色卡片背景
        TextureRegionDrawable darkBg = new TextureRegionDrawable(
                makeSolid(4, 4, new Color(0.15f, 0.15f, 0.18f, 0.9f))
        );

        float sw = Gdx.graphics.getWidth();
        float sh = Gdx.graphics.getHeight();

        float HOTBAR_W = sw * 0.20f;
        float HOTBAR_H = sh * 0.20f;
        float GAP      = sh * 0.012f;

        float PANEL_W  = HOTBAR_W;
        float PANEL_H  = sh * 0.26f; // 稍微加高一点给容量+冷却两块区域

        root = new Table();
        root.setFillParent(true);
        root.top().right();
        // 放在 Hotbar 正下方
        root.padTop(sh * 0.5f + HOTBAR_H * 0.5f + GAP).padRight(0f);

        card = new Table(skin);
        card.setBackground(darkBg);
        card.pad(sw * 0.008f);
        card.defaults().left().padBottom(sh * 0.006f);

        // ===== 基本文本与按钮 =====
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

        // ===== 通用进度条样式（暗灰背景 + 主题黄前景） =====
        ProgressBar.ProgressBarStyle barStyle = new ProgressBar.ProgressBarStyle();
        barStyle.background = new TextureRegionDrawable(makeSolid(8, 8, new Color(0.10f, 0.10f, 0.12f, 1f)));
        TextureRegionDrawable yellowFill = new TextureRegionDrawable(makeSolid(8, 8, new Color(0.98f, 0.80f, 0.10f, 1f)));
        barStyle.knobBefore = yellowFill;
        barStyle.knob = new TextureRegionDrawable(makeSolid(1, 8, new Color(1f, 1f, 1f, 0f)));

        // ===== 召唤容量（已放置 / 最大） =====
        aliveBar = new ProgressBar(0f, 1f, 0.01f, false, barStyle);
        aliveBar.setAnimateDuration(0.08f);
        aliveBar.setValue(0f);
        aliveLabel = new Label("Summons: 0 / -", skin); // 等待事件填充最大值

        // ===== 冷却倒计时 =====
        cooldownBar = new ProgressBar(0f, 1f, 0.01f, false, barStyle);
        cooldownBar.setAnimateDuration(0.08f);
        cooldownBar.setValue(1f); // 初始视为就绪
        cooldownLabel = new Label("Summon ready", skin);

        // ===== 组装布局 =====
        card.add(info).left().row();
        card.add(hpLabel).left().row();
        card.add(damageLabel).left().row();
        card.add(energyLabel).left().row();

        // —— 容量块 ——
        card.add(new Label("Summon Capacity", skin)).left().row();
        card.add(aliveBar).left().width(sw * 0.10f).padTop(sh * 0.004f).row();
        card.add(aliveLabel).left().row();

        // —— 冷却块 ——
        card.add(new Label("Summon Cooldown", skin)).left().row();
        card.add(cooldownBar).left().width(sw * 0.10f).padTop(sh * 0.004f).row();
        card.add(cooldownLabel).left().row();

        // —— 升级/ULT ——
        card.add(costLabel).left().row();
        card.add(upgradeBtn).left().width(sw * 0.10f).padTop(sh * 0.004f).row();
        card.add(ultBtn).left().row();

        root.add(card).width(PANEL_W).height(PANEL_H);
        stage.addActor(root);

        // ===== 事件绑定 =====
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

        // —— 容量监听：alive/max ——
        hero.getEvents().addListener("summonAliveChanged", (Integer alive, Integer max) -> {
            if (alive == null || max == null) return;
            lastAlive = Math.max(0, alive);
            lastMax   = Math.max(0, max);

            float progress = (lastMax > 0) ? (Math.min(lastAlive, lastMax) / (float) lastMax) : 0f;
            aliveBar.setValue(progress);

            // 文案 & 满载高亮
            if (lastMax > 0) {
                boolean full = lastAlive >= lastMax;
                String text = "Summons: " + lastAlive + " / " + lastMax + (full ? " (FULL)" : "");
                aliveLabel.setText(text);
                aliveLabel.setColor(full ? new Color(0.98f, 0.80f, 0.10f, 1f) : Color.WHITE);
            } else {
                aliveLabel.setText("Summons: " + lastAlive + " / -");
                aliveLabel.setColor(Color.WHITE);
            }
        });

        // —— 冷却监听：remaining/total ——
        hero.getEvents().addListener("summon:cooldown", (Float remaining, Float total) -> {
            if (remaining == null || total == null) return;
            float rem = Math.max(0f, remaining);
            float tot = Math.max(0.0001f, total); // 防止除0
            float progress = (tot - rem) / tot;   // 0~1（0:刚开始冷却; 1:已就绪）
            cooldownBar.setValue(progress);

            if (rem > 0f) {
                String txt = String.format(java.util.Locale.US, "Next summon in %.1fs", rem);
                cooldownLabel.setText(txt);
                cooldownLabel.setColor(Color.WHITE);
            } else {
                cooldownLabel.setText("Summon ready");
                cooldownLabel.setColor(new Color(0.80f, 1f, 0.80f, 1f)); // 就绪时给个淡绿提示
            }
        });

        // —— 升级按钮 ——
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

        // 初始化升级显示
        refreshUpgradeInfo();
    }

    @Override
    public void dispose() {
        if (root != null) root.remove();
    }

    private Entity findPlayer() {
        for (Entity e : ServiceLocator.getEntityService().getEntities()) {
            com.csse3200.game.components.currencysystem.CurrencyManagerComponent wallet =
                    e.getComponent(com.csse3200.game.components.currencysystem.CurrencyManagerComponent.class);
            if (wallet != null) return e;
        }
        return null;
    }

    private void refreshUpgradeInfo() {
        com.csse3200.game.components.hero.HeroUpgradeComponent up =
                hero.getComponent(com.csse3200.game.components.hero.HeroUpgradeComponent.class);
        int lvl = (up != null) ? up.getLevel() : 1;
        int next = lvl + 1;
        int cost = (next <= 3) ? next * 200 : 0;
        costLabel.setText(next <= 3 ? ("Upgrade cost: " + cost) : "Already reach the max Level");
        upgradeBtn.setDisabled(next > 3);
    }

    /** 生成纯色贴图 */
    private static TextureRegion makeSolid(int w, int h, Color c) {
        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pm.setColor(c);
        pm.fill();
        Texture tex = new Texture(pm);
        pm.dispose();
        return new TextureRegion(tex);
    }
}


