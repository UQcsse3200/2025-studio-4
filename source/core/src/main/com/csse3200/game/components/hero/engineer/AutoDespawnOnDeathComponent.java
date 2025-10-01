package com.csse3200.game.components.hero.engineer;

import com.badlogic.gdx.Gdx;
import com.csse3200.game.components.Component;

import java.util.concurrent.atomic.AtomicBoolean;

/** 给召唤物/炮台用：HP=0 自动、安全移除（一次性）。 */
public class AutoDespawnOnDeathComponent extends Component {
    private final AtomicBoolean scheduled = new AtomicBoolean(false);

    @Override
    public void create() {
        // 兼容你们 CombatStatsComponent 里触发的三种事件
        entity.getEvents().addListener("death", this::onDeath);
        entity.getEvents().addListener("entityDeath", this::onDeath);
        entity.getEvents().addListener("setDead", (Boolean dead) -> { if (dead) onDeath(); });
    }

    private void onDeath() {
        // 一次性保险：只调度一次
        if (!scheduled.compareAndSet(false, true)) return;

        // 在下一帧安全移除（避免在回调/物理步进中直接 dispose）
        Gdx.app.postRunnable(() -> {
            if (entity != null) entity.dispose();
        });
    }
}

