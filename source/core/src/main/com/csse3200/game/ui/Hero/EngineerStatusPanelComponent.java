package com.csse3200.game.ui.Hero;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.csse3200.game.components.currencysystem.CurrencyComponent;
import com.csse3200.game.ui.Hero.BaseHeroStatusPanelComponent;
import com.csse3200.game.ui.SimpleUI;

public class EngineerStatusPanelComponent extends BaseHeroStatusPanelComponent {
    // Capacity
    private Label aliveLabel;
    private ProgressBar aliveBar;

    // Cooldown
    private Label cooldownLabel;
    private ProgressBar cooldownBar;

    // 升级花费（替换父类的 costLabel）
    private Table costRow;
    private Label costTitleLabel, costNumLabel;
    private Image costIcon;

    // 默认币种（避免空指针）
    private CurrencyComponent.CurrencyType defaultCurrency = CurrencyComponent.CurrencyType.METAL_SCRAP;

    public EngineerStatusPanelComponent(com.csse3200.game.entities.Entity hero, String heroName) {
        super(
                hero,
                heroName != null ? heroName : "Engineer",
                new Color(0.15f, 0.15f, 0.18f, 0.90f), // Background
                Color.WHITE,                           // Text
                new Color(0.98f, 0.80f, 0.10f, 1f),    // Accent: industrial yellow
                0.32f                                  // Panel height slightly taller
        );
    }

    @Override
    public void create() {
        super.create();

        if (costLabel != null) {
            costLabel.remove(); // 移除父类已添加到 card 的纯文本行
            costLabel = null;
        }

        // 构建：Upgrade cost: [number] [icon]
        Skin skin = new Skin();
        skin.add("default", new Label.LabelStyle(SimpleUI.font(), textColor));

        costTitleLabel = new Label("Upgrade cost: ", skin);
        costNumLabel   = new Label("—", skin);

        costIcon = new Image(currencyIconDrawable(defaultCurrency));
        costIcon.setScaling(Scaling.stretch);

        costRow = new Table();
        costRow.add(costTitleLabel).left();
        costRow.add(costNumLabel).left().padRight(6f);
        costRow.add(costIcon).left().size(22f, 22f); // 图标显示大小，可调 22/28/32

        // 由于父类已经把 upgradeBtn/ultBtn 加进 card，这里把它们取出来再按顺序加入，保证 costRow 在前
        if (upgradeBtn != null) upgradeBtn.remove();
        if (ultBtn != null) ((Actor) ultBtn).remove();

        card.add(costRow).left().row();
        // 恢复与父类一致的布局参数
        card.add(upgradeBtn)
                .left()
                .width(Value.percentWidth(0.45f, card))
                .padTop(Value.percentHeight(0.02f, card))
                .row();
        card.add((Actor) ultBtn).left().row();

        // 首次刷新（从组件读真实价格+币种）
        refreshUpgradeInfo();
    }

    @Override
    protected void refreshUpgradeInfo() {
        var engUp = hero.getComponent(
                com.csse3200.game.components.hero.engineer.EngineerUpgradeComponent.class);


        if (engUp == null) { // 兜底：非工程师，走父类逻辑
            super.refreshUpgradeInfo();
            return;
        }

        int lvl   = engUp.getLevel();
        int maxLv = engUp.getMaxLevel();
        int nextCost  = engUp.getNextCost(); // -1 表示无下一次（满级或异常）

        if (levelLabel != null) levelLabel.setText("Lv. " + lvl);

        // 满级：显示 MAX LEVEL，隐藏数字与图标
        if (lvl >= maxLv || nextCost < 0) {
            if (costTitleLabel != null) {
                costTitleLabel.setText("MAX LEVEL");
                costTitleLabel.setColor(accentColor.cpy().lerp(Color.GRAY, 0.4f));
            }
            if (costNumLabel != null) {
                costNumLabel.setText("");
            }
            if (costIcon != null) {
                costIcon.setVisible(false);
            }
            if (upgradeBtn != null) {
                upgradeBtn.setDisabled(true);
                upgradeBtn.setText("Maxed");
                var st = upgradeBtn.getStyle();
                st.fontColor = Color.GRAY;
                st.overFontColor = Color.GRAY;
                st.downFontColor = Color.GRAY;
            }
            return;
        }

        // 未满级：从组件读取真实的“下一次升级费用 + 币种”
        CurrencyComponent.CurrencyType nextType = defaultCurrency;
        try {
            // 如果你已在 EngineerUpgradeComponent 实现了 getNextCurrencyType()，这里会生效；
            // 否则保持默认币种。
            var method = engUp.getClass().getMethod("getNextCurrencyType");
            Object ret = method.invoke(engUp);
            if (ret instanceof CurrencyComponent.CurrencyType) {
                nextType = (CurrencyComponent.CurrencyType) ret;
            }
        } catch (Throwable ignored) {
            // 没有该方法就用默认币种
        }

        // 更新 UI
        if (costTitleLabel != null) {
            costTitleLabel.setText("Upgrade cost: ");
            costTitleLabel.setColor(textColor);
        }
        if (costNumLabel != null) {
            costNumLabel.setText(String.valueOf(nextCost));
            costNumLabel.setColor(textColor);
        }
        if (costIcon != null) {
            costIcon.setDrawable(currencyIconDrawable(nextType));
            costIcon.setVisible(nextCost > 0);
        }

        if (upgradeBtn != null) {
            upgradeBtn.setDisabled(false);
            upgradeBtn.setText("Upgrade");
            var st = upgradeBtn.getStyle();
            st.fontColor = Color.BLACK;
            st.overFontColor = Color.BLACK;
            st.downFontColor = Color.BLACK;
        }
    }

    @Override
    protected void buildExtraSections(Table card, Skin skin, float sw, float sh) {
        // Summon Capacity
        aliveBar = new ProgressBar(0f, 1f, 0.01f, false, buildBarStyle());
        aliveBar.setAnimateDuration(0.08f);
        aliveBar.setValue(0f);
        aliveLabel = new Label("Summons: 0 / -", skin);

        card.add(new Label("Summon Capacity", skin)).left().row();
        card.add(aliveBar).left().width(sw * 0.10f).padTop(sh * 0.004f).row();
        card.add(aliveLabel).left().row();

        // Summon Cooldown
        cooldownBar = new ProgressBar(0f, 1f, 0.01f, false, buildBarStyle());
        cooldownBar.setAnimateDuration(0.08f);
        cooldownBar.setValue(1f);
        cooldownLabel = new Label("Summon ready", skin);

        card.add(new Label("Summon Cooldown", skin)).left().row();
        card.add(cooldownBar).left().width(sw * 0.10f).padTop(sh * 0.004f).row();
        card.add(cooldownLabel).left().row();
    }

    @Override
    protected void bindExtraListeners() {
        // Capacity: alive/max
        hero.getEvents().addListener("summonAliveChanged", (Integer alive, Integer max) -> {
            if (alive == null || max == null || aliveBar == null || aliveLabel == null) return;
            int a = Math.max(0, alive);
            int m = Math.max(0, max);
            float progress = (m > 0) ? (Math.min(a, m) / (float) m) : 0f;
            aliveBar.setValue(progress);

            boolean full = (m > 0 && a >= m);
            aliveLabel.setText(m > 0 ? ("Summons: " + a + " / " + m + (full ? " (FULL)" : "")) : ("Summons: " + a + " / -"));
            aliveLabel.setColor(full ? accentColor : textColor);
        });

        // Cooldown: remaining/total
        hero.getEvents().addListener("summon:cooldown", (Float remaining, Float total) -> {
            if (remaining == null || total == null || cooldownBar == null || cooldownLabel == null) return;
            float rem = Math.max(0f, remaining);
            float tot = Math.max(0.0001f, total);
            float progress = (tot - rem) / tot; // 0~1
            cooldownBar.setValue(progress);

            if (rem > 0f) {
                cooldownLabel.setText(String.format(java.util.Locale.US, "Next summon in %.1fs", rem));
                cooldownLabel.setColor(textColor);
            } else {
                cooldownLabel.setText("Summon ready");
                cooldownLabel.setColor(accentColor.cpy().lerp(textColor, 0.5f));
            }
        });
    }

    /** 根据币种返回图标 Drawable（assets/images/currency/*.png） */
    private Drawable currencyIconDrawable(CurrencyComponent.CurrencyType t) {
        String path;
        switch (t) {
            case METAL_SCRAP:   path = "images/currency/metal_scrap.png"; break;
            case TITANIUM_CORE: path = "images/currency/titanium_core.png"; break;
            case NEUROCHIP:     path = "images/currency/neurochip.png"; break;
            default:            path = "images/currency/currency_unknown.png"; break;
        }
        Texture tex;
        try {
            tex = new Texture(Gdx.files.internal(path));
            // 像素风清晰
            tex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        } catch (Exception e) {
            // 兜底：占位色块，避免 NPE
            Pixmap pm = new Pixmap(16, 16, Pixmap.Format.RGBA8888);
            pm.setColor(Color.GRAY);
            pm.fill();
            tex = new Texture(pm);
            pm.dispose();
        }
        return new TextureRegionDrawable(new com.badlogic.gdx.graphics.g2d.TextureRegion(tex));
    }
}
