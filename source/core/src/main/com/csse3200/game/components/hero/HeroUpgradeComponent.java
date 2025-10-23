package com.csse3200.game.components.hero;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.currencysystem.CurrencyManagerComponent;
import com.csse3200.game.components.currencysystem.CurrencyComponent.CurrencyType;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;
import com.badlogic.gdx.audio.Sound;

import java.util.Map;

/**
 * Hero upgrade component.
 * <p>
 * Features:
 * - Can be triggered via keyboard (Enter / Numpad Enter) or the "requestUpgrade" event.
 * - Deducts cost from the player's wallet.
 * - Applies stat growth to the hero on upgrade, with a level cap.
 * - Emits events so UI/other systems can react.
 * <p>
 * Events emitted:
 * - "upgradeFailed" (String reason)
 * - "upgraded"      (Integer newLevel, CurrencyType currencyType, Integer cost)
 * - "hero.level"    (Integer level)
 * - "hero.damage"   (Integer baseAttack or equivalent)
 */
public class HeroUpgradeComponent extends Component {
    /**
     * Current level and maximum level cap.
     */
    private int level = 1;
    private final int maxLevel = 2;

    /**
     * SFX keys/volumes for upgrade and level-2 shooting.
     */
    private String upgradeSfxKey = "sounds/hero_upgrade.ogg";
    private float upgradeSfxVolume = 1.0f;
    private String shootSfxLevel2 = "sounds/hero_lv2_shot.ogg";
    private float shootSfxVolume = 1.0f;

    /**
     * Currency config and cost formula.
     */
    private final CurrencyType costType = CurrencyType.METAL_SCRAP;

    /**
     * Cached refs to the player and their wallet.
     */
    private Entity player;
    private CurrencyManagerComponent wallet;

    /**
     * Inject the player entity (recommended during hero spawn).
     * Avoids repeated lookups and ambiguity.
     */
    public HeroUpgradeComponent attachPlayer(Entity player) {
        this.player = player;
        this.wallet = (player != null) ? player.getComponent(CurrencyManagerComponent.class) : null;
        return this;
    }

    public HeroUpgradeComponent setUpgradeSfxVolume(float vol) {
        this.upgradeSfxVolume = Math.max(0f, Math.min(1f, vol));
        return this;
    }

    public HeroUpgradeComponent setLv2ShootSfx(String key) {
        if (key != null && !key.isBlank()) this.shootSfxLevel2 = key;
        return this;
    }

    public HeroUpgradeComponent setShootSfxVolume(float vol) {
        this.shootSfxVolume = Math.max(0f, Math.min(1f, vol));
        return this;
    }

    /**
     * Upgrade cost formula (customize as needed).
     */
    private int getCostForLevel(int nextLevel) {
        return nextLevel * 200;
    }

    @Override
    public void create() {
        // Allow external systems (e.g., UI) to request an upgrade.
        entity.getEvents().addListener("requestUpgrade", (Entity p) -> {
            if (p != null && p != this.player) attachPlayer(p);
            tryUpgrade();
        });

        // Send initial snapshot to sync UI.
        broadcastSnapshot();
    }

    /**
     * Emit a one-off snapshot of current level/damage for UI binding.
     */
    private void broadcastSnapshot() {
        CombatStatsComponent stats = entity.getComponent(CombatStatsComponent.class);
        if (stats != null) {
            entity.getEvents().trigger("hero.level", level);
            entity.getEvents().trigger("hero.damage", stats.getBaseAttack());
        }
        // Example: you could also broadcast energy/rage here if applicable.
    }

    @Override
    public void update() {
        // Keyboard trigger (Enter / Numpad Enter).
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) ||
                Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_ENTER)) {
            tryUpgrade();
        }
    }

    /**
     * Attempt to upgrade the hero:
     * - Check cap, player/wallet availability, and cost.
     * - Spend currency and apply stat growth.
     * - Emit events/SFX.
     */
    private void tryUpgrade() {
        if (level >= maxLevel) {
            Gdx.app.log("HeroUpgrade", "failed: max level");
            entity.getEvents().trigger("upgradeFailed", "Already at max level");
            return;
        }

        if (player == null || wallet == null) {
            // Fallback lookup if not injected.
            if (player == null) player = findPlayerEntity();
            wallet = (player != null) ? player.getComponent(CurrencyManagerComponent.class) : null;

            if (player == null || wallet == null) {
                Gdx.app.log("HeroUpgrade", "failed: player or wallet not ready");
                entity.getEvents().trigger("upgradeFailed", "Player or wallet not ready");
                return;
            }
        }

        int nextLevel = level + 1;
        int cost = getCostForLevel(nextLevel);

        if (!wallet.canAffordAndSpendCurrency(Map.of(costType, cost))) {
            Gdx.app.log("HeroUpgrade", "failed: not enough " + costType + ", need=" + cost);
            entity.getEvents().trigger("upgradeFailed", "Not enough " + costType);
            return;
        }

        level = nextLevel;
        applyStatGrowth(level);
        Gdx.app.log("HeroUpgrade", "success: level=" + level + ", cost=" + cost);
        entity.getEvents().trigger("upgraded", level, costType, cost);
        playUpgradeSfx();

        if (level >= 2) {
            applyLv2ShootSfx();
        }

        // Broadcast new numbers for UI refresh.
        entity.getEvents().trigger("hero.level", level);
        CombatStatsComponent stats = entity.getComponent(CombatStatsComponent.class);
        if (stats != null) {
            entity.getEvents().trigger("hero.damage", stats.getBaseAttack());
        }
    }

    /**
     * Apply stat growth when leveling up (customize as needed).
     */
    private void applyStatGrowth(int newLevel) {
        // (1) Fix base attack to 12 regardless of previous value.
        CombatStatsComponent stats = entity.getComponent(CombatStatsComponent.class);
        if (stats != null) {
            stats.setBaseAttack(12);
            // If you'd like to also increase health, do it here.
            // stats.setHealth(stats.getHealth() + 20);
        }

        // (2) Fix attack rate to 1 shot/second (cooldown = 1.0s).
        //     Assumes your shooting logic lives in HeroTurretAttackComponent.
        HeroTurretAttackComponent atk = entity.getComponent(HeroTurretAttackComponent.class);
        if (atk != null) {
            atk.setCooldown(1.0f); // seconds per shot
        }
    }

    /**
     * Fallback player lookup:
     * Finds the first entity with PlayerActions and a CurrencyManagerComponent.
     */
    private Entity findPlayerEntity() {
        for (Entity e : ServiceLocator.getEntityService().getEntities()) {
            if (e.getComponent(com.csse3200.game.components.player.PlayerActions.class) != null &&
                    e.getComponent(CurrencyManagerComponent.class) != null) {
                return e;
            }
        }
        return null;
    }

    public int getLevel() {
        return level;
    }
    
    /**
     * Sets the hero level directly (used when restoring from save).
     * Also applies stat growth and triggers UI updates.
     * @param newLevel the level to set
     */
    public void setLevel(int newLevel) {
        if (newLevel < 1) newLevel = 1;
        if (newLevel > maxLevel) newLevel = maxLevel;
        this.level = newLevel;
        applyStatGrowth(newLevel);
        broadcastSnapshot();
        Gdx.app.log("HeroUpgrade", "Hero level set to " + newLevel);
    }

    /**
     * Wallet accessor (e.g., for an ultimate that needs currency).
     */
    public CurrencyManagerComponent getWallet() {
        return wallet;
    }

    /**
     * Optional: player accessor if needed elsewhere.
     */
    public Entity getPlayer() {
        return player;
    }

    /**
     * Play the upgrade SFX with ResourceService first, then with a file fallback.
     */
    private void playUpgradeSfx() {
        if (upgradeSfxKey == null || upgradeSfxKey.isBlank()) return;
        float vol = Math.max(0f, Math.min(1f, upgradeSfxVolume));
        try {
            var rs = ServiceLocator.getResourceService();
            if (rs != null) {
                Sound s = null;
                try {
                    s = rs.getAsset(upgradeSfxKey, Sound.class);
                } catch (Throwable ignored) {
                }
                if (s != null) {
                    long id = s.play(vol);
                    Gdx.app.log("HeroUpgradeSFX", "Played via ResourceService: " + upgradeSfxKey + " id=" + id);
                    return;
                } else {
                    Gdx.app.error("HeroUpgradeSFX", "Not in ResourceService (or null): " + upgradeSfxKey);
                }
            } else {
                Gdx.app.error("HeroUpgradeSFX", "ResourceService is null");
            }

            // Fallback playback using Gdx.files/Gdx.audio.
            if (!Gdx.files.internal(upgradeSfxKey).exists()) {
                Gdx.app.error("HeroUpgradeSFX", "File NOT FOUND: " + upgradeSfxKey);
                return;
            }
            if (Gdx.audio == null) {
                Gdx.app.error("HeroUpgradeSFX", "Gdx.audio is NULL");
                return;
            }
            Sound s2 = Gdx.audio.newSound(Gdx.files.internal(upgradeSfxKey));
            long id2 = s2.play(vol);
            Gdx.app.log("HeroUpgradeSFX", "Played via fallback newSound: " + upgradeSfxKey + " id=" + id2);
        } catch (Throwable t) {
            Gdx.app.error("HeroUpgradeSFX", "Play failed: " + upgradeSfxKey, t);
        }
    }

    /**
     * On level 2, set a unified shooting SFX on the attack component.
     */
    private void applyLv2ShootSfx() {
        if (shootSfxLevel2 == null || shootSfxLevel2.isBlank()) return;
        HeroTurretAttackComponent atk = entity.getComponent(HeroTurretAttackComponent.class);
        if (atk != null) {
            atk.setShootSfxKey(shootSfxLevel2).setShootSfxVolume(shootSfxVolume);
            Gdx.app.log("HeroUpgrade", "Lv2 shoot sfx -> " + shootSfxLevel2);
        }
    }
}

