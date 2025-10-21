package com.csse3200.game.components.hero.samurai.attacks;

import com.badlogic.gdx.math.Vector2;

public interface ISamuraiAttack {
    boolean isActive();
    boolean canTrigger();                  // 包含CD、锁、内部防抖判定
    void trigger(Vector2 targetOrNull);    // jab/sweep 用 target, spin 允许 null
    void cancel();                         // 被打断/换动作
    void update(float dt);                 // 每帧推进
    float getFacingDeg();                  // 输出最新朝向（用于角色朝向对齐）
}
