package com.csse3200.game.components.hero;


import com.csse3200.game.components.Component;
import com.csse3200.game.components.currencysystem.CurrencyComponent.CurrencyType;
import com.csse3200.game.components.currencysystem.CurrencyManagerComponent;
import com.badlogic.gdx.utils.TimeUtils;

/**
 * 英雄大招组件：
 * - 按按钮/事件触发
 * - 扣除玩家钱包里的货币
 * - 提升攻击伤害（倍率）
 * - 持续 5 秒后恢复
 */
public class HeroUltimateComponent extends Component{
    private static final int ULT_COST = 2;            // 一次大招消耗多少货币
    private static final long ULT_DURATION_MS = 5000;  // 持续 5 秒
    private static final float ULT_MULTIPLIER = 2.0f;  // 伤害倍率

    private boolean active = false;
    private long endAtMs = 0L;
    private int lastTenths = -1; // 上次发送的“十分之一秒”计数

    // 指向已有的升级组件（里面缓存了 player + wallet）
    private HeroUpgradeComponent upgrade;

    @Override
    public void create() {
        upgrade = entity.getComponent(HeroUpgradeComponent.class);

        // UI 或键盘触发 "ultimate.request" 事件时调用
        entity.getEvents().addListener("ultimate.request", () -> onRequest());
    }

    private void onRequest() {
        if (active) return;

        if (upgrade == null) {
            // 没有升级组件，直接开大招（方便测试）
            activateNow();
            return;
        }

        com.csse3200.game.components.currencysystem.CurrencyManagerComponent wallet =
                (upgrade != null) ? upgrade.getWallet() : null;


        if (wallet == null) {
            // 钱包还没准备好，提示失败
            entity.getEvents().trigger("ultimate.failed", "Wallet not ready");
            return;
        }

        // 尝试扣费
        boolean ok = wallet.trySpendCurrency(CurrencyType.METAL_SCRAP, ULT_COST);
        if (!ok) {
            entity.getEvents().trigger("ultimate.failed", "Not enough " + CurrencyType.METAL_SCRAP);
            return;
        }

        // 扣费成功 → 激活大招
        activateNow();
    }

    private void activateNow() {
        active = true;
        endAtMs = TimeUtils.millis() + ULT_DURATION_MS;
        lastTenths = -1; // 重置节流
        // 通知攻击组件提升伤害
        entity.getEvents().trigger("attack.multiplier", ULT_MULTIPLIER);
        entity.getEvents().trigger("ultimate.state", true);
    }

    public void update() {
            if (!active) return;

            long now = TimeUtils.millis();
            long remainMs = Math.max(0, endAtMs - now);

                    // 每 0.1 秒广播一次剩余时间（单位：秒，保留 1 位小数）
                            int tenths = (int) (remainMs / 100); // 5000ms -> 50, 490ms -> 4
            if (tenths != lastTenths) {
                  lastTenths = tenths;
                  float remainSec = tenths / 10f;
                  entity.getEvents().trigger("ultimate.remaining", remainSec);
                }

                    // 计时结束 → 复原
        if (remainMs == 0) {
                  active = false;
                  entity.getEvents().trigger("attack.multiplier", 1.0f);
                  entity.getEvents().trigger("ultimate.state", false);
                  entity.getEvents().trigger("ultimate.remaining", 0f); // 结束时发 0
                }
          }

    @Override
    public void dispose() {
        if (active) {
            entity.getEvents().trigger("attack.multiplier", 1.0f);
            entity.getEvents().trigger("ultimate.state", false);
            active = false;
        }
    }
}
