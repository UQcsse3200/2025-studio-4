package com.csse3200.game.ui.Hero;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.csse3200.game.ui.Hero.BaseHeroStatusPanelComponent;

/**
 * 工程师状态栏：
 * - 在基类基础上增加两块：Summon Capacity（已放置/最大）+ Summon Cooldown（倒计时）
 * - 监听事件：
 *   - summonAliveChanged(alive, max)
 *   - summon:cooldown(remaining, total)
 */
public class EngineerStatusPanelComponent extends BaseHeroStatusPanelComponent {
    // 容量
    private Label aliveLabel;
    private ProgressBar aliveBar;

    // 冷却
    private Label cooldownLabel;
    private ProgressBar cooldownBar;

    public EngineerStatusPanelComponent(com.csse3200.game.entities.Entity hero, String heroName) {
        super(
                hero,
                heroName != null ? heroName : "Engineer",
                new Color(0.15f, 0.15f, 0.18f, 0.90f), // 背景
                Color.WHITE,                           // 文本
                new Color(0.98f, 0.80f, 0.10f, 1f),    // 强调：工业黄
                0.32f                                  // 面板高度略高一些
        );
    }
    @Override
    public void create() {
        super.create();
        // 把基类里初始化写死的“200”替换成占位符
        if (costLabel != null) {
            costLabel.setText("Upgrade cost: —");
        }
        // 立刻按工程师组件的真实价格刷新一次
        refreshUpgradeInfo();
    }
    @Override
    protected void refreshUpgradeInfo() {
        var engUp = hero.getComponent(
                com.csse3200.game.components.hero.engineer.EngineerUpgradeComponent.class);

        if (engUp == null) { // 兜底：不是工程师就走基类逻辑
            super.refreshUpgradeInfo();
            return;
        }

        int lvl   = engUp.getLevel();
        int maxLv = engUp.getMaxLevel();
        int cost  = engUp.getNextCost(); // -1 表示无下一次（满级或异常）

        if (levelLabel != null) levelLabel.setText("Lv. " + lvl);

        if (lvl >= maxLv || cost < 0) {
            if (costLabel != null) {
                costLabel.setText("MAX LEVEL");
                costLabel.setColor(accentColor.cpy().lerp(Color.GRAY, 0.4f));
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

        // 只显示价格数字
        if (costLabel != null) {
            costLabel.setText("Upgrade cost: " + cost);
            costLabel.setColor(textColor);
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
        // 容量：alive/max
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

        // 冷却：remaining/total
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
}
