package com.csse3200.game.components.hero.engineer;

import com.csse3200.game.components.Component;
import com.csse3200.game.components.currencysystem.CurrencyManagerComponent;
import com.csse3200.game.components.currencysystem.CurrencyComponent.CurrencyType;
import com.csse3200.game.components.hero.engineer.OwnerComponent; // ✅ 别忘了导入
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;

/**
 * 定时给 owner 玩家产出货币的组件
 */
public class CurrencyGeneratorComponent extends Component {
    private Entity owner;          // ❌ 去掉 final，让它能在 create() 时动态获取
    private final CurrencyType type;   // 产出货币类型
    private final int amount;          // 每次产出的数量
    private final float intervalSec;   // 间隔秒
    private float timer = 0f;

    public CurrencyGeneratorComponent(Entity owner, CurrencyType type, int amount, float intervalSec) {
        this.owner = owner;
        this.type = type;
        this.amount = amount;
        this.intervalSec = intervalSec;
    }

    @Override
    public void create() {
        super.create();
        // ✅ 如果没有传 owner，就尝试从 OwnerComponent 里获取
        if (owner == null) {
            OwnerComponent oc = this.entity.getComponent(OwnerComponent.class);
            if (oc != null) {
                this.owner = oc.getOwner();
            }
        }
    }

    @Override
    public void update() {
        float dt = ServiceLocator.getTimeSource().getDeltaTime();
        timer += dt;
        if (timer >= intervalSec) {
            timer -= intervalSec;

            if (owner == null) {
                System.out.println("[CurrencyGen] owner == null，产币被跳过");
                return;
            }

            CurrencyManagerComponent cm = owner.getComponent(CurrencyManagerComponent.class);
            if (cm == null) {
                System.out.println("[CurrencyGen] 玩家没挂 CurrencyManagerComponent，产币被跳过");
                return;
            }

            int before = cm.getCurrencyAmount(type);
            cm.addCurrency(type, amount); // 直接加钱，并自动更新UI
            int after = cm.getCurrencyAmount(type);
            System.out.println("[CurrencyGen] +" + amount + " " + type
                    + "  from " + before + " -> " + after);
        }
    }

}

