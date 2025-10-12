package com.csse3200.game.components.hero.samurai;

import com.csse3200.game.components.Component;
import com.csse3200.game.services.ServiceLocator;

import java.util.HashMap;
import java.util.Map;

/**
 * 多技能独立冷却组件：
 * - setTotal(skill, total) 配置每个技能的总CD
 * - trigger(skill)         让该技能进入冷却（立即广播一次）
 * - isReady(skill)         是否就绪
 * - update()               每帧递减并按步长节流广播
 *
 * 事件（由本组件触发）：
 *  - "skill:cooldown"  (SkillCooldownInfo payload)
 *  - "skill:ready"     (String skill)
 */
public class SkillCooldowns extends Component {
    /** 量化步长（秒）：与 Engineer 的 0.05s 节流一致 */
    private static final float QUANT_STEP = 0.05f;

    /** 单技能冷却数据 */
    private static class CD {
        float remain;      // 当前剩余秒
        float total;       // 总时长
        float lastEmitQ;   // 上次广播时的量化 remain（用于节流）
    }

    /** 事件负载：单参数传递，避免多参数 trigger 重载不匹配 */
    public static class SkillCooldownInfo {
        public final String skill;
        public final float remain;
        public final float total;
        public final float progress01; // 0~1，1 表示就绪

        public SkillCooldownInfo(String skill, float remain, float total, float progress01) {
            this.skill = skill;
            this.remain = remain;
            this.total = total;
            this.progress01 = progress01;
        }
    }

    private final Map<String, CD> cds = new HashMap<>();

    /** 配置技能总冷却（不会立刻触发冷却） */
    public SkillCooldowns setTotal(String skill, float totalSec) {
        CD c = cds.computeIfAbsent(skill, k -> new CD());
        c.total = Math.max(0f, totalSec);
        return this;
    }

    /** 技能是否就绪（冷却完成） */
    public boolean isReady(String skill) {
        CD c = cds.get(skill);
        return c == null || c.remain <= 0f;
    }

    /** 进入冷却（从 total 开始倒计时），并立即广播一次 */
    public void trigger(String skill) {
        CD c = cds.get(skill);
        if (c == null) {
            // 允许动态触发：未配置则 total=0 => 立即就绪（但仍创建条目）
            c = new CD();
            cds.put(skill, c);
        }
        c.remain = c.total;
        emit(skill, c); // 立刻发一次，和 Engineer 一致
    }

    /** 可选：缩短冷却（如击杀回能） */
    public void reduce(String skill, float deltaSec) {
        CD c = cds.get(skill);
        if (c == null || deltaSec <= 0f) return;
        c.remain = Math.max(0f, c.remain - deltaSec);
        emit(skill, c);                    // 立即广播更新
        if (c.remain == 0f) {
            entity.getEvents().trigger("skill:ready", skill);
        }
    }

    /** 可选：重置（立即就绪） */
    public void reset(String skill) {
        CD c = cds.get(skill);
        if (c == null) return;
        c.remain = 0f;
        emit(skill, c);
        entity.getEvents().trigger("skill:ready", skill);
    }

    /** 查询剩余秒数 */
    public float getRemain(String skill) {
        CD c = cds.get(skill);
        return c == null ? 0f : Math.max(0f, c.remain);
    }

    /** 查询 0~1 进度（1 表示就绪） */
    public float getProgress01(String skill) {
        CD c = cds.get(skill);
        if (c == null || c.total <= 0f) return 1f;
        return 1f - (Math.max(0f, c.remain) / c.total);
    }

    @Override
    public void update() {
        float dt = ServiceLocator.getTimeSource().getDeltaTime();

        for (Map.Entry<String, CD> e : cds.entrySet()) {
            String skill = e.getKey();
            CD c = e.getValue();
            if (c.remain > 0f) {
                float prev = c.remain;
                c.remain = Math.max(0f, c.remain - dt);

                // 量化步长节流广播；或从 >0 变为 0 时强制广播一次
                float quantPrev = (float) Math.floor(prev / QUANT_STEP) * QUANT_STEP;
                float quantNow  = (float) Math.floor(c.remain / QUANT_STEP) * QUANT_STEP;

                if (quantNow != c.lastEmitQ || (prev > 0f && c.remain == 0f)) {
                    c.lastEmitQ = quantNow;
                    emit(skill, c);
                }

                if (c.remain == 0f) {
                    entity.getEvents().trigger("skill:ready", skill);
                }
            }
        }
    }

    /** 广播一次 "skill:cooldown"（remain/total/progress01） */
    private void emit(String skill, CD c) {
        float total = c.total;
        float remain = Math.max(0f, c.remain);
        float progress = (total <= 0f) ? 1f : (1f - remain / total);

        entity.getEvents().trigger(
                "skill:cooldown",
                new SkillCooldownInfo(skill, remain, total, progress)
        );
    }
}



