package com.csse3200.game.components.hero.engineer;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.services.ServiceLocator;

/**
 * 让同实体上的 TurretAttackComponent 以 上→右→下→左 的顺序依次射击。
 * 不旋转贴图，只改变子弹方向。
 */
public class FourWayCycleComponent extends Component {
    private final float switchIntervalSec;
    private float timer = 0f;

    private static final Vector2[] DIRS = {
            new Vector2(0, 1),   // 上
            new Vector2(1, 0),   // 右
            new Vector2(0,-1),   // 下
            new Vector2(-1,0)    // 左
    };
    private int idx; // 当前方向序号

    /** 从给定初始方向开始（找最接近的四向），并按固定间隔轮换 */
    public FourWayCycleComponent(float switchIntervalSec, Vector2 initialDir) {
        this.switchIntervalSec = switchIntervalSec;
        this.idx = pickNearestIndex(initialDir);
    }

    @Override
    public void create() {
        var atk = entity.getComponent(TurretAttackComponent.class);
        if (atk != null) atk.setDirection(DIRS[idx]);
    }

    @Override
    public void update() {
        float dt = ServiceLocator.getTimeSource().getDeltaTime();
        timer += dt;
        if (timer < switchIntervalSec) return;

        timer = 0f;
        idx = (idx + 1) % DIRS.length;

        var atk = entity.getComponent(TurretAttackComponent.class);
        if (atk != null) atk.setDirection(DIRS[idx]);
    }

    private static int pickNearestIndex(Vector2 v) {
        if (v == null) return 0;
        Vector2 n = new Vector2(v).nor();
        int best = 0; float bestDot = -Float.MAX_VALUE;
        for (int i = 0; i < DIRS.length; i++) {
            float d = n.dot(DIRS[i]);
            if (d > bestDot) { bestDot = d; best = i; }
        }
        return best; // 选与初始传入方向最接近的四向作为起点
    }
}

