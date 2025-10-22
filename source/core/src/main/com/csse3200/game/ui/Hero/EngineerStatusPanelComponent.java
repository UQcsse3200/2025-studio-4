package com.csse3200.game.ui.Hero;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.csse3200.game.ui.Hero.BaseHeroStatusPanelComponent;

/**
 * Engineer status panel:
 * - Adds two sections on top of the base class: Summon Capacity (placed/max) + Summon Cooldown (countdown)
 * - Listens to events:
 *   - summonAliveChanged(alive, max)
 *   - summon:cooldown(remaining, total)
 */
public class EngineerStatusPanelComponent extends BaseHeroStatusPanelComponent {
    // Capacity
    private Label aliveLabel;
    private ProgressBar aliveBar;

    // Cooldown
    private Label cooldownLabel;
    private ProgressBar cooldownBar;

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
        // Replace the hardcoded “200” from the base class with a placeholder
        if (costLabel != null) {
            costLabel.setText("Upgrade cost: —");
        }
        // Immediately refresh once using the engineer component’s actual price
        refreshUpgradeInfo();
    }
    @Override
    protected void refreshUpgradeInfo() {
        var engUp = hero.getComponent(
                com.csse3200.game.components.hero.engineer.EngineerUpgradeComponent.class);

        if (engUp == null) { // Fallback: if not an engineer, use base-class logic
            super.refreshUpgradeInfo();
            return;
        }

        int lvl   = engUp.getLevel();
        int maxLv = engUp.getMaxLevel();
        int cost  = engUp.getNextCost(); // -1 means no next upgrade (max level or error)

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

        // Show only the numeric price
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
}
