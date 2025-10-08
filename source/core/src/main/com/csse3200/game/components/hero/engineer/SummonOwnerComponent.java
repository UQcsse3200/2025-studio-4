package com.csse3200.game.components.hero.engineer;

import com.csse3200.game.components.Component;

/**
 * 挂在每个召唤物实体上：生成成功 → 通知 spawned；死亡/移除 → 通知 died。
 */
public class SummonOwnerComponent extends Component {
    private final EngineerSummonComponent owner;
    private final String type;
    private boolean counted = false;

    public SummonOwnerComponent(EngineerSummonComponent owner, String type) {
        this.owner = owner;
        this.type = type;
    }

    @Override
    public void create() {
        // 生成成功 → +1（只执行一次）
        if (!counted) {
            owner.getEntity().getEvents().trigger("summon:spawned", entity, type);
            counted = true;
        }

        // 监听各种“消亡”路径
        entity.getEvents().addListener("death", this::onGone);
        entity.getEvents().addListener("entityDeath", this::onGone);
        entity.getEvents().addListener("setDead", (Boolean d) -> { if (d) onGone(); });
        entity.getEvents().addListener("despawn", this::onGone); // 手动拆除等
    }

    private void onGone() {
        if (counted) {
            owner.getEntity().getEvents().trigger("summon:died", entity, type);
            counted = false;
        }
    }
}


