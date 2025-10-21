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
 * 专属工程师的升级组件：
 * - 支持独立价格曲线、货币类型、等级上限（默认更贵）
 * - 触发：Enter/小键盘Enter 或 外部事件("requestUpgrade", playerEntity)
 * - 成功会广播("upgraded", level, currencyType, cost) 和 UI 刷新事件
 */
public class EngineerUpgradeComponent extends Component {
    /** 当前等级（从1开始） */
    private int level = 1;

    /** 升级价格曲线：数组长度 = 可升级次数。例：{300, 500} => 1->2=300, 2->3=500，最大等级=3 */
    private int[] upgradeCosts = new int[]{1000}; // 默认：只升到2级，价格300（工程师贵点）

    /** 货币类型（工程师默认用 METAL_SCRAP） */
    private CurrencyType currencyType = CurrencyType.METAL_SCRAP;

    /** 音效设置（可选） */
    private String upgradeSfxKey = "sounds/hero_upgrade.ogg";
    private float upgradeSfxVolume = 1.0f;
    private String shootSfxLevel2 = "sounds/hero_lv2_shot.ogg";
    private float shootSfxVolume = 1.0f;

    /** 缓存玩家与钱包 */
    private Entity player;
    private CurrencyManagerComponent wallet;

    /** —— 可选注入：价格曲线与货币 —— */
    public EngineerUpgradeComponent setUpgradeCosts(CurrencyType currency, int... costs) {
        if (currency != null) this.currencyType = currency;
        if (costs != null && costs.length > 0) {
            this.upgradeCosts = Arrays.stream(costs).map(c -> Math.max(0, c)).toArray();
        }
        return this;
    }

    /** 可选注入：玩家引用（推荐在 spawn 时 attach，减少全局查找） */
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

    /** 获取最大等级 = 可升级次数 + 1 */
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

    /** 获取下一级价格（nextLevel 从2开始）；若越界返回 Integer.MAX_VALUE 以防误扣 */
    private int getCostForLevel(int nextLevel) {
        if (upgradeCosts == null) return Integer.MAX_VALUE;
        int idx = nextLevel - 2;
        if (idx < 0 || idx >= upgradeCosts.length) return Integer.MAX_VALUE;
        return upgradeCosts[idx];
    }

    @Override
    public void create() {
        // 允许外部事件触发升级（UI按钮等）
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

        // 广播新数值给 UI
        entity.getEvents().trigger("hero.level", level);
        CombatStatsComponent stats = entity.getComponent(CombatStatsComponent.class);
        if (stats != null) {
            entity.getEvents().trigger("hero.damage", stats.getBaseAttack());
        }
    }

    /** 工程师的成长：你也可以定制成跟通用英雄不同 */
    private void applyStatGrowth(int newLevel) {
        // 示例：工程师主打召唤，不强调伤害；这里维持基础攻击在 10，并把攻速提到 1.1s -> 0.9s
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

    /** 找玩家（带钱包）的回退策略 */
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

    // 对外只读（可选）
    public int getLevel() { return level; }
    public CurrencyManagerComponent getWallet() { return wallet; }
    public Entity getPlayer() { return player; }
}
