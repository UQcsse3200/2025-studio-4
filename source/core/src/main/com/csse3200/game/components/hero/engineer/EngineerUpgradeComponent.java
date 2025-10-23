package com.csse3200.game.components.hero.engineer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.currencysystem.CurrencyComponent.CurrencyType;
import com.csse3200.game.components.currencysystem.CurrencyManagerComponent;
import com.csse3200.game.components.hero.HeroTurretAttackComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;

import java.util.Arrays;
import java.util.Map;

/**
 * Engineer-only upgrade component:
 * - Supports its own price curve, currency type, and max level (defaults to pricier settings)
 * - Triggers via: Enter/Numpad Enter or external event ("requestUpgrade", playerEntity)
 * - On success broadcasts ("upgraded", level, currencyType, cost) and UI refresh events
 */
public class EngineerUpgradeComponent extends Component {
    /** Current level (starts at 1) */
    private int level = 1;

    /** Upgrade price curve: array length = number of possible upgrades. e.g., {300, 500} => 1->2=300, 2->3=500, max level=3 */
    private int[] upgradeCosts = new int[]{1000}; // Default: can only upgrade to level 2, price 1000 (engineer is pricier)

    /** Currency type (engineer uses METAL_SCRAP by default) */
    private CurrencyType currencyType = CurrencyType.METAL_SCRAP;

    /** SFX settings (optional) */
    private String upgradeSfxKey = "sounds/hero_upgrade.ogg";
    private float upgradeSfxVolume = 1.0f;
    private String shootSfxLevel2 = "sounds/hero_lv2_shot.ogg";
    private float shootSfxVolume = 1.0f;

    /** Cached player and wallet */
    private Entity player;
    private CurrencyManagerComponent wallet;

    /** —— Optional injection: price curve and currency —— */
    public EngineerUpgradeComponent setUpgradeCosts(CurrencyType currency, int... costs) {
        if (currency != null) this.currencyType = currency;
        if (costs != null && costs.length > 0) {
            this.upgradeCosts = Arrays.stream(costs).map(c -> Math.max(0, c)).toArray();
        }
        return this;
    }

    /** Optional injection: player reference (recommended to attach at spawn to reduce global lookups) */
    public EngineerUpgradeComponent attachPlayer(Entity player) {
        this.player = player;
        this.wallet = (player != null) ? player.getComponent(CurrencyManagerComponent.class) : null;
        return this;
    }

    public EngineerUpgradeComponent setUpgradeSfxVolume(float vol) {
        this.upgradeSfxVolume = clamp01(vol);
        return this;
    }
    public EngineerUpgradeComponent setLv2ShootSfx(String key) {
        if (key != null && !key.isBlank()) this.shootSfxLevel2 = key;
        return this;
    }
    public EngineerUpgradeComponent setShootSfxVolume(float vol) {
        this.shootSfxVolume = clamp01(vol);
        return this;
    }

    private static float clamp01(float v) { return Math.max(0f, Math.min(1f, v)); }

    /** Get max level = number of upgrades + 1 */
    public int getMaxLevel() {
        return (upgradeCosts == null || upgradeCosts.length == 0) ? 1 : (upgradeCosts.length + 1);
    }
    public int getNextCost() {
        int next = level + 1;
        int cost = getCostForLevel(next);
        return (level >= getMaxLevel() || cost == Integer.MAX_VALUE) ? -1 : cost;
    }

    public CurrencyType getCurrencyType() {
        return currencyType;
    }

    /** Get cost for the next level (nextLevel starts from 2); if out of bounds, return Integer.MAX_VALUE to avoid accidental charges */
    private int getCostForLevel(int nextLevel) {
        if (upgradeCosts == null) return Integer.MAX_VALUE;
        int idx = nextLevel - 2;
        if (idx < 0 || idx >= upgradeCosts.length) return Integer.MAX_VALUE;
        return upgradeCosts[idx];
    }

    @Override
    public void create() {
        // Allow external events to trigger upgrades (UI buttons, etc.)
        entity.getEvents().addListener("requestUpgrade", (Entity p) -> {
            if (p != null && p != this.player) attachPlayer(p);
            tryUpgrade();
        });
        broadcastSnapshot();
    }

    private void broadcastSnapshot() {
        entity.getEvents().trigger("hero.level", level);
        CombatStatsComponent stats = entity.getComponent(CombatStatsComponent.class);
        if (stats != null) {
            entity.getEvents().trigger("hero.damage", stats.getBaseAttack());
        }
    }

    @Override
    public void update() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) ||
                Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_ENTER)) {
            tryUpgrade();
        }
    }

    private void tryUpgrade() {
        if (level >= getMaxLevel()) {
            Gdx.app.log("EngineerUpgrade", "failed: max level");
            entity.getEvents().trigger("upgradeFailed", "Already at max level");
            return;
        }

        if (player == null || wallet == null) {
            if (player == null) player = findPlayerEntity();
            wallet = (player != null) ? player.getComponent(CurrencyManagerComponent.class) : null;
            if (player == null || wallet == null) {
                Gdx.app.log("EngineerUpgrade", "failed: player or wallet not ready");
                entity.getEvents().trigger("upgradeFailed", "Player or wallet not ready");
                return;
            }
        }

        int nextLevel = level + 1;
        int cost = getCostForLevel(nextLevel);

        if (!wallet.canAffordAndSpendCurrency(Map.of(currencyType, cost))) {
            Gdx.app.log("EngineerUpgrade", "failed: not enough " + currencyType + ", need=" + cost);
            entity.getEvents().trigger("upgradeFailed", "Not enough " + currencyType);
            return;
        }

        level = nextLevel;
        applyStatGrowth(level);
        Gdx.app.log("EngineerUpgrade", "success: level=" + level + ", cost=" + cost);
        entity.getEvents().trigger("upgraded", level, currencyType, cost);

        playUpgradeSfx();
        if (level >= 2) {
            applyLv2ShootSfx();
        }

        // Broadcast new stats to the UI
        entity.getEvents().trigger("hero.level", level);
        CombatStatsComponent stats = entity.getComponent(CombatStatsComponent.class);
        if (stats != null) {
            entity.getEvents().trigger("hero.damage", stats.getBaseAttack());
        }
    }

    /** Engineer growth: you can also customize this differently from generic heroes */
    private void applyStatGrowth(int newLevel) {
        // Example: Engineer focuses on summoning rather than damage; here we keep base attack at 10
        // and improve attack speed from 1.1s -> 0.9s
        CombatStatsComponent stats = entity.getComponent(CombatStatsComponent.class);
        if (stats != null) {
            stats.setBaseAttack(50);
        }
        HeroTurretAttackComponent atk = entity.getComponent(HeroTurretAttackComponent.class);
        if (atk != null) {
            float cd = (newLevel >= 2) ? 5.0f : 1.1f;
            atk.setCooldown(cd);
        }
    }

    /** Fallback strategy to find a player (with a wallet) */
    private Entity findPlayerEntity() {
        for (Entity e : ServiceLocator.getEntityService().getEntities()) {
            if (e.getComponent(com.csse3200.game.components.player.PlayerActions.class) != null &&
                    e.getComponent(CurrencyManagerComponent.class) != null) {
                return e;
            }
        }
        return null;
    }

    private void playUpgradeSfx() {
        if (upgradeSfxKey == null || upgradeSfxKey.isBlank()) return;
        float vol = clamp01(upgradeSfxVolume);
        try {
            var rs = ServiceLocator.getResourceService();
            if (rs != null) {
                Sound s = null;
                try { s = rs.getAsset(upgradeSfxKey, Sound.class); } catch (Throwable ignored) {}
                if (s != null) {
                    s.play(vol);
                    return;
                }
            }
            if (!Gdx.files.internal(upgradeSfxKey).exists() || Gdx.audio == null) return;
            Sound s2 = Gdx.audio.newSound(Gdx.files.internal(upgradeSfxKey));
            s2.play(vol);
        } catch (Throwable ignored) {}
    }

    private void applyLv2ShootSfx() {
        if (shootSfxLevel2 == null || shootSfxLevel2.isBlank()) return;
        HeroTurretAttackComponent atk = entity.getComponent(HeroTurretAttackComponent.class);
        if (atk != null) {
            atk.setShootSfxKey(shootSfxLevel2).setShootSfxVolume(shootSfxVolume);
            Gdx.app.log("EngineerUpgrade", "Lv2 shoot sfx -> " + shootSfxLevel2);
        }
    }

    // Public read-only accessors (optional)
    public int getLevel() { return level; }
    public CurrencyManagerComponent getWallet() { return wallet; }
    public Entity getPlayer() { return player; }
}
