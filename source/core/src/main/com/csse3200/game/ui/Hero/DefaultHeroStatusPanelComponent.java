package com.csse3200.game.ui.Hero;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.csse3200.game.entities.Entity;
import com.badlogic.gdx.utils.Timer;

/**
 * Default hero status panel (for Hero1/2/3, etc.)
 * - Adds a “Weapon Switch Cooldown” bar below the basic info
 * - Automatically turns into gray “Locked” state after the hero is upgrade-locked
 */
public class DefaultHeroStatusPanelComponent extends BaseHeroStatusPanelComponent {

    private Label weaponCooldownLabel;
    private ProgressBar weaponCooldownBar;

    /** Whether switching has been locked due to upgrade (event from HeroOneShotFormSwitchComponent) */
    private boolean locked = false;

    /** Timer task used to incrementally update cooldown progress */
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
        // === Start cooldown ===
        hero.getEvents().addListener("ui:weapon:cooldown:start", (Long totalMs) -> {
            if (locked || totalMs == null || totalMs <= 0) return;

            cancelCooldown();
            weaponCooldownBar.setValue(0f);
            weaponCooldownLabel.setText("Switching...");
            weaponCooldownLabel.setColor(textColor);

            // Timer-based updates (safer than using a Thread)
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

        // === During cooldown hint ===
        hero.getEvents().addListener("ui:weapon:cooldown", (Long remainMs) -> {
            if (locked || remainMs == null) return;
            float remainSec = remainMs / 1000f;
            weaponCooldownLabel.setText(String.format(java.util.Locale.US, "Ready in %.1fs", remainSec));
            weaponCooldownLabel.setColor(textColor);
        });

        // === After being locked (from HeroOneShotFormSwitchComponent) ===
        hero.getEvents().addListener("ui:weapon:locked", () -> {
            locked = true;
            cancelCooldown();
            weaponCooldownLabel.setText("Locked after upgrade");
            weaponCooldownLabel.setColor(Color.GRAY);
            weaponCooldownBar.setValue(0f);
        });

        // === If there is unlock logic (for future expansion) ===
        hero.getEvents().addListener("ui:weapon:unlocked", () -> {
            locked = false;
            weaponCooldownLabel.setText("Switch ready");
            weaponCooldownLabel.setColor(accentColor.cpy().lerp(textColor, 0.4f));
            weaponCooldownBar.setValue(1f);
        });
    }

    /** Cancel the current cooldown task */
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
