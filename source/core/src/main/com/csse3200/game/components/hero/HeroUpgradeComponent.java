package com.csse3200.game.components.hero;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.currencysystem.CurrencyManagerComponent;
import com.csse3200.game.components.currencysystem.CurrencyComponent.CurrencyType;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;

import java.util.Map;

/**
 * Hero upgrade component:
 * - Triggered by pressing Enter / Numpad Enter, or via an event.
 * - Deducts cost from the player's wallet.
 * - Increases the hero's stats when upgraded.
 * - Notifies UI and other systems via events.
 */
public class HeroUpgradeComponent extends Component {
    /** Current level and maximum level cap */
    private int level = 1;
    private final int maxLevel = 2;

    /** Currency type and cost formula */
    private final CurrencyType costType = CurrencyType.METAL_SCRAP;

    /** Cached references to player entity and wallet to avoid repeated global searches */
    private Entity player;
    private CurrencyManagerComponent wallet;

    /**
     * Optionally inject player entity (recommended during spawnHeroAt).
     * Reduces lookup overhead and avoids ambiguity.
     */
    public HeroUpgradeComponent attachPlayer(Entity player) {
        this.player = player;
        this.wallet = (player != null) ? player.getComponent(CurrencyManagerComponent.class) : null;
        return this;
    }

    /** Upgrade cost formula (can be customized) */
    private int getCostForLevel(int nextLevel) {
        return nextLevel * 200;
    }

    @Override
    public void create() {
        // Allow upgrades via external event (e.g., UI button or script)
        entity.getEvents().addListener("requestUpgrade", (Entity p) -> {
            if (p != null && p != this.player) attachPlayer(p);
            tryUpgrade();
        });

        broadcastSnapshot();
    }

    private void broadcastSnapshot() {
        CombatStatsComponent stats = entity.getComponent(CombatStatsComponent.class);
        if (stats != null) {
            // grade
            entity.getEvents().trigger("hero.level", level);
            // Damage (change according to your field: baseAttack / totalAttack)
            entity.getEvents().trigger("hero.damage", stats.getBaseAttack());
        }
        //
        //Energy/rage can also be broadcast once if there is a corresponding component (for example)
        // entity.getEvents().trigger("hero.energy", curEnergy, maxEnergy);
    }

    @Override
    public void update() {
        // Trigger upgrade when Enter or Numpad Enter is pressed
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) ||
                Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_ENTER)) {
            tryUpgrade();
        }
    }

    /**
     * Attempts to upgrade hero:
     * - Verifies level cap.
     * - Validates player and wallet references.
     * - Deducts upgrade cost from wallet.
     * - Applies stat growth and triggers events.
     */
    private void tryUpgrade() {
        if (level >= maxLevel) {
            Gdx.app.log("HeroUpgrade", "failed: max level");
            entity.getEvents().trigger("upgradeFailed", "Already at max level");
            return;
        }

        if (player == null || wallet == null) {
            // Fallback: try to find player again
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


        // ✅ 广播新数值，供UI刷新
        entity.getEvents().trigger("hero.level", level);
        CombatStatsComponent stats = entity.getComponent(CombatStatsComponent.class);
        if (stats != null) {
            entity.getEvents().trigger("hero.damage", stats.getBaseAttack());
        }
    }

    /** Applies stat growth when hero levels up (customizable). */
    /** Applies stat growth when hero levels up (customizable). */
    private void applyStatGrowth(int newLevel) {
        // ① 固定基础攻击为 12（无论之前是多少）
        CombatStatsComponent stats = entity.getComponent(CombatStatsComponent.class);
        if (stats != null) {
            stats.setBaseAttack(12);
            // 如果不想改生命，这里就不再 +20 了；若仍想顺带加血，可自行加：
            // stats.setHealth(stats.getHealth() + 20);
        }

        // ② 固定攻速为 1 秒 1 发（即冷却 = 1.0f 秒）
        //    这里假设你的射击逻辑在 HeroTurretAttackComponent 里
        HeroTurretAttackComponent atk = entity.getComponent(HeroTurretAttackComponent.class);
        if (atk != null) {
            atk.setCooldown(1.0f); // 冷却秒数：每 1.0s 可射一发
        }
    }


    /**
     * Fallback player lookup:
     * Finds the first entity with both PlayerActions and a CurrencyManagerComponent.
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

    /** Returns wallet component (used by other systems such as ultimate). */
    public CurrencyManagerComponent getWallet() {
        return wallet;
    }

    /** Returns player entity reference (optional, if needed). */
    public Entity getPlayer() {
        return player;
    }
}
