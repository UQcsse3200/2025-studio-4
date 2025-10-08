package com.csse3200.game.components.hero.engineer;

import com.badlogic.gdx.Gdx;
import com.csse3200.game.components.Component;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 血量<=0 时自动安全移除（仅一次）。在下一帧执行 dispose，避免回调/物理步内销毁。
 * 额外：在销毁前触发 "despawn" 事件，方便统一计数处理。
 */
public class AutoDespawnOnDeathComponent extends Component {
    private final AtomicBoolean scheduled = new AtomicBoolean(false);

    @Override
    public void create() {
        entity.getEvents().addListener("death", this::onDeath);
        entity.getEvents().addListener("entityDeath", this::onDeath);
        entity.getEvents().addListener("setDead", (Boolean dead) -> {
            if (dead) onDeath();
        });
    }

    private void onDeath() {
        if (!scheduled.compareAndSet(false, true)) return;

        // 先广播 despawn（用于 SummonOwnerComponent 统一 -1）
        if (entity != null) {
            entity.getEvents().trigger("despawn");
        }

        // 下一帧安全移除
        Gdx.app.postRunnable(() -> {
            if (entity != null) entity.dispose();
        });
    }
}


