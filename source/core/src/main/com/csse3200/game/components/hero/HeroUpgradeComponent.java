package com.csse3200.game.components.hero;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.currencysystem.CurrencyManagerComponent;
import com.csse3200.game.components.currencysystem.CurrencyComponent.CurrencyType;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;

/**
 * 英雄升级组件：按 Enter / Numpad Enter 触发升级；
 * 从玩家钱包扣费；给英雄（this.entity）加属性；通过事件通知 UI。
 */
public class HeroUpgradeComponent extends Component {
    /** 当前等级与上限 */
    private int level = 1;
    private final int maxLevel = 3;

    /** 货币类型与单次费用公式 */
    private final CurrencyType costType = CurrencyType.METAL_SCRAP;

    /** 缓存：玩家实体与钱包，避免每帧全局扫描 */
    private Entity player;
    private CurrencyManagerComponent wallet;

    /** 外部可选择注入玩家（推荐在 spawnHeroAt 时调用），减少查找与歧义 */
    public HeroUpgradeComponent attachPlayer(Entity player) {
        this.player = player;
        this.wallet = (player != null) ? player.getComponent(CurrencyManagerComponent.class) : null;
        return this;
    }

    /** 升级费用：可按需改公式 */
    private int getCostForLevel(int nextLevel) {
        return nextLevel * 2;
    }

    @Override
    public void create() {
        // 允许通过事件触发（例如 UI 按钮或脚本）
        entity.getEvents().addListener("requestUpgrade", (Entity p) -> {
            if (p != null && p != this.player) attachPlayer(p);
            tryUpgrade();
        });
    }

    @Override
    public void update() {
        // 按 Enter 或小键盘 Enter 升级
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) ||
                Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_ENTER)) {
            tryUpgrade();
        }
    }

    /** 仅升级英雄本体；玩家只作为付款方 */
    private void tryUpgrade() {
        if (level >= maxLevel) {
            Gdx.app.log("HeroUpgrade", "failed: max level");
            entity.getEvents().trigger("upgradeFailed", "Already at max level");
            return;
        }

        if (player == null || wallet == null) {
            // 兜底：再尝试找一次
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

        if (!wallet.trySpendCurrency(costType, cost)) {
            Gdx.app.log("HeroUpgrade", "failed: not enough " + costType + ", need=" + cost);
            entity.getEvents().trigger("upgradeFailed", "Not enough " + costType);
            return;
        }

        level = nextLevel;
        applyStatGrowth(level);
        Gdx.app.log("HeroUpgrade", "success: level=" + level + ", cost=" + cost);
        entity.getEvents().trigger("upgraded", level, costType, cost);
    }


    /** 属性成长：按需自定义 */
    private void applyStatGrowth(int newLevel) {
        CombatStatsComponent stats = entity.getComponent(CombatStatsComponent.class);
        if (stats != null) {
            stats.setBaseAttack(stats.getBaseAttack() + 10);
            stats.setHealth(stats.getHealth() + 20);
        }
    }

    /** 兜底：按“玩家识别 + 钱包存在”寻找玩家 */
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
}


