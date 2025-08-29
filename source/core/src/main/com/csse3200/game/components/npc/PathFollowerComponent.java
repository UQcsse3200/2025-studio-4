package com.csse3200.game.components.npc;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.physics.components.PhysicsMovementComponent;

import java.util.List;

/**
 * 路径跟随组件 - 使敌人沿着预定义的路径移动
 */
public class PathFollowerComponent extends Component {
    private List<Vector2> pathPoints;
    private int currentTargetIndex = 0;
    private float speed = 1.0f;
    private float reachThreshold = 0.5f; // 到达目标点的距离阈值
    private boolean pathCompleted = false;

    /**
     * 创建路径跟随组件
     * @param pathPoints 路径点列表（世界坐标）
     * @param speed 移动速度
     */
    public PathFollowerComponent(List<Vector2> pathPoints, float speed) {
        this.pathPoints = pathPoints;
        this.speed = speed;
    }

    @Override
    public void update() {
        if (pathPoints == null || pathPoints.isEmpty() || pathCompleted) {
            return;
        }

        if (currentTargetIndex >= pathPoints.size()) {
            pathCompleted = true;
            System.out.println("👻 敌人完成了路径行走");
            return;
        }

        Vector2 currentPos = entity.getPosition();
        Vector2 targetPos = pathPoints.get(currentTargetIndex);

        // 计算到目标点的距离
        float distance = currentPos.dst(targetPos);

        if (distance <= reachThreshold) {
            // 到达当前目标点，移动到下一个目标
            currentTargetIndex++;
            System.out.println("🎯 敌人到达路径点 " + currentTargetIndex + "/" + pathPoints.size());
            return;
        }

        // 计算移动方向
        Vector2 direction = new Vector2(targetPos).sub(currentPos).nor();
        Vector2 velocity = direction.scl(speed);

        // 使用物理移动组件进行移动
        PhysicsMovementComponent movementComponent = entity.getComponent(PhysicsMovementComponent.class);
        if (movementComponent != null) {
            movementComponent.setTarget(velocity);
        }
    }

    /**
     * 重置路径跟随状态
     */
    public void resetPath() {
        currentTargetIndex = 0;
        pathCompleted = false;
    }

    /**
     * 检查路径是否完成
     */
    public boolean isPathCompleted() {
        return pathCompleted;
    }

    /**
     * 获取当前目标点索引
     */
    public int getCurrentTargetIndex() {
        return currentTargetIndex;
    }

    /**
     * 设置移动速度
     */
    public void setSpeed(float speed) {
        this.speed = speed;
    }

    /**
     * 获取移动速度
     */
    public float getSpeed() {
        return speed;
    }
}
