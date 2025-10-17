package com.csse3200.game.ui.Hero;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.csse3200.game.entities.Entity;
import com.badlogic.gdx.utils.Timer;

/**
 * 普通英雄状态栏（用于 Hero1/2/3 等）
 * - 在基础信息下方新增：Weapon Switch Cooldown 条
 * - 会在英雄升级锁定后，自动变为灰色“Locked”状态
 */
public class DefaultHeroStatusPanelComponent extends BaseHeroStatusPanelComponent {

    private Label weaponCooldownLabel;
    private ProgressBar weaponCooldownBar;

    /** 是否已被升级锁定（来自 HeroOneShotFormSwitchComponent 的事件） */
    private boolean locked = false;

    /** 用于逐步更新冷却进度的计时器任务 */
    private Timer.Task cooldownTask;

    public DefaultHeroStatusPanelComponent(Entity hero, String heroName) {
        super(
                hero,
                heroName != null ? heroName : "Hero",
                new Color(0.15f, 0.15f, 0.18f, 0.90f),
                Color.WHITE,
                new Color(0.35f, 0.75f, 1.00f, 1f),
                0.28f
        );
    }

    @Override
    protected void buildExtraSections(Table card, Skin skin, float sw, float sh) {
        // === Weapon Switch Cooldown ===
        weaponCooldownBar = new ProgressBar(0f, 1f, 0.01f, false, buildBarStyle());
        weaponCooldownBar.setAnimateDuration(0.1f);
        weaponCooldownBar.setValue(1f);

        weaponCooldownLabel = new Label("Switch ready", skin);

        card.add(new Label("Weapon Cooldown", skin))
                .left()
                .padTop(sh * 0.004f)
                .row();
        card.add(weaponCooldownBar)
                .left()
                .width(sw * 0.10f)
                .padTop(sh * 0.004f)
                .row();
        card.add(weaponCooldownLabel).left().row();
    }

    @Override
    protected void bindExtraListeners() {
        // === 启动冷却 ===
        hero.getEvents().addListener("ui:weapon:cooldown:start", (Long totalMs) -> {
            if (locked || totalMs == null || totalMs <= 0) return;

            cancelCooldown();
            weaponCooldownBar.setValue(0f);
            weaponCooldownLabel.setText("Switching...");
            weaponCooldownLabel.setColor(textColor);

            // Timer定时更新（比Thread安全）
            float totalSec = totalMs / 1000f;
            cooldownTask = Timer.schedule(new Timer.Task() {
                float elapsed = 0f;
                @Override
                public void run() {
                    if (locked) { cancel(); return; }
                    elapsed += 0.05f;
                    float progress = Math.min(elapsed / totalSec, 1f);
                    weaponCooldownBar.setValue(progress);
                    if (progress >= 1f) {
                        weaponCooldownLabel.setText("Switch ready");
                        weaponCooldownLabel.setColor(accentColor.cpy().lerp(textColor, 0.4f));
                        cancel();
                    }
                }
            }, 0f, 0.05f);
        });

        // === 冷却中提示 ===
        hero.getEvents().addListener("ui:weapon:cooldown", (Long remainMs) -> {
            if (locked || remainMs == null) return;
            float remainSec = remainMs / 1000f;
            weaponCooldownLabel.setText(String.format(java.util.Locale.US, "Ready in %.1fs", remainSec));
            weaponCooldownLabel.setColor(textColor);
        });

        // === 被锁定后（来自 HeroOneShotFormSwitchComponent） ===
        hero.getEvents().addListener("ui:weapon:locked", () -> {
            locked = true;
            cancelCooldown();
            weaponCooldownLabel.setText("Locked after upgrade");
            weaponCooldownLabel.setColor(Color.GRAY);
            weaponCooldownBar.setValue(0f);
        });

        // === 若有解锁逻辑（未来版本可扩展） ===
        hero.getEvents().addListener("ui:weapon:unlocked", () -> {
            locked = false;
            weaponCooldownLabel.setText("Switch ready");
            weaponCooldownLabel.setColor(accentColor.cpy().lerp(textColor, 0.4f));
            weaponCooldownBar.setValue(1f);
        });
    }

    /** 取消当前冷却任务 */
    private void cancelCooldown() {
        if (cooldownTask != null) {
            cooldownTask.cancel();
            cooldownTask = null;
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        cancelCooldown();
    }
}


